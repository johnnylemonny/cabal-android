const net = require('net')
const dgram = require('dgram')
const path = require('path')
const os = require('os')
const Cabal = require('cabal-core')
const varint = require('varint')
const {
  CONSTANTS,
  generateKeyPair,
  encodeTextPost,
  decodeTextPost,
  encodePostResponse,
  decodeMessage,
  hashPost
} = require('./cable')

// Default public cabal.chat room: cabal://324eee92611cd877841c4de9fd5253e9dba6033329a837ee5f01beb005dffb2f
const DEFAULT_CABAL_KEY = '324eee92611cd877841c4de9fd5253e9dba6033329a837ee5f01beb005dffb2f'
const CABAL_KEY = process.env.CABAL_KEY || DEFAULT_CABAL_KEY
const DEFAULT_CHANNEL = process.env.DEFAULT_CHANNEL || 'default'
const TCP_PORT = parseInt(process.env.CABLE_TCP_PORT || '13330', 10)
const UDP_PORT = parseInt(process.env.CABLE_UDP_PORT || '13334', 10)
const STORAGE_PATH = process.env.STORAGE_PATH || path.join(os.tmpdir(), 'cabal-chat-bridge-db')

console.log('====================================================')
console.log('        CABAL <-> CABLE PROTOCOL BRIDGE            ')
console.log('====================================================')
console.log(`[Bridge] Cabal Room Key: ${CABAL_KEY}`)
console.log(`[Bridge] Default Channel: #${DEFAULT_CHANNEL}`)
console.log(`[Bridge] TCP Listen Port: ${TCP_PORT}`)
console.log(`[Bridge] UDP Discovery Port: ${UDP_PORT}`)
console.log(`[Bridge] Storage Directory: ${STORAGE_PATH}`)

// Generate local Ed25519 identity for Cable posts signed by this bridge
const bridgeIdentity = generateKeyPair()
console.log(`[Bridge] Bridge Identity Key: ${bridgeIdentity.publicKey.toString('hex').slice(0, 16)}...`)

// Store recent Cable posts for fast replication
const MAX_HISTORY = 300
const recentPosts = [] // array of { hash, serialized, channel, timestamp }
const knownPostHashes = new Set()

function cacheCablePost(serialized, channel, timestamp) {
  const hash = hashPost(serialized)
  const hashHex = hash.toString('hex')
  if (knownPostHashes.has(hashHex)) return null
  knownPostHashes.add(hashHex)

  const item = { hash, serialized, channel, timestamp }
  recentPosts.push(item)
  if (recentPosts.length > MAX_HISTORY) {
    const dropped = recentPosts.shift()
    knownPostHashes.delete(dropped.hash.toString('hex'))
  }
  return item
}

// Active TCP connections from Android clients
const clients = new Set()

function broadcastToClients(data, excludeSocket = null) {
  const lenBuf = Buffer.from(varint.encode(data.length))
  const packet = Buffer.concat([lenBuf, data])

  for (const client of clients) {
    if (client !== excludeSocket && !client.destroyed) {
      try {
        client.write(packet)
      } catch (err) {
        console.error('[Bridge] Failed to write to client:', err.message)
      }
    }
  }
}

// 1. Initialize Cabal Core (Cabal v1 / Hypercore)
console.log('[Bridge] Initializing Cabal Core...')
const cabal = Cabal(STORAGE_PATH, CABAL_KEY)

cabal.ready(function (err) {
  if (err) {
    console.error('[Bridge] Failed to initialize Cabal Core:', err)
    return
  }

  console.log('[Bridge] Cabal Core ready. Starting P2P swarm...')
  cabal.swarm(function (swarmErr, swarm) {
    if (swarmErr) {
      console.error('[Bridge] Swarm error:', swarmErr)
      return
    }
    console.log('[Bridge] Connected to Cabal P2P Hyperswarm network.')
  })

  // Set local nickname on Cabal swarm
  cabal.publishNick('CabalBridge', (nickErr) => {
    if (!nickErr) console.log('[Bridge] Nickname "CabalBridge" registered in Cabal swarm.')
  })

  // Listen for incoming messages from Cabal network (e.g. cabal.chat browser users)
  cabal.messages.events.on('message', function (msg) {
    if (!msg || !msg.value || !msg.value.content) return
    const channel = msg.value.content.channel || DEFAULT_CHANNEL
    const text = msg.value.content.text
    if (!text) return

    // Avoid echoing messages originally sent by the bridge
    if (msg.value.content.source === 'cable-android') return

    const ts = Math.floor((msg.value.timestamp || Date.now()) / 1000)
    console.log(`[Cabal -> Cable] [#${channel}] <${msg.key ? msg.key.slice(0, 8) : 'anon'}>: ${text}`)

    // Create a Cable TextPost
    const serializedPost = encodeTextPost({
      publicKey: bridgeIdentity.publicKey,
      secretKey: bridgeIdentity.secretKey,
      channel,
      text: `[cabal.chat] ${text}`,
      timestamp: ts
    })

    cacheCablePost(serializedPost, channel, ts)
    broadcastToClients(serializedPost)
  })
})

// 2. TCP Transport for Cable Protocol Clients (Android app)
const tcpServer = net.createServer((socket) => {
  const remoteAddr = `${socket.remoteAddress}:${socket.remotePort}`
  console.log(`[Bridge] New Cable peer connected: ${remoteAddr}`)
  clients.add(socket)

  let incomingBuffer = Buffer.alloc(0)

  socket.on('data', (chunk) => {
    incomingBuffer = Buffer.concat([incomingBuffer, chunk])

    while (incomingBuffer.length > 0) {
      let packetLength
      let varintBytes = 0
      try {
        packetLength = varint.decode(incomingBuffer)
        varintBytes = varint.decode.bytes
      } catch {
        break // Need more bytes to decode varint length
      }

      if (incomingBuffer.length < varintBytes + packetLength) {
        break // Packet incomplete, wait for next data chunk
      }

      const packet = incomingBuffer.subarray(varintBytes, varintBytes + packetLength)
      incomingBuffer = incomingBuffer.subarray(varintBytes + packetLength)

      handleIncomingCablePacket(socket, packet)
    }
  })

  socket.on('close', () => {
    console.log(`[Bridge] Cable peer disconnected: ${remoteAddr}`)
    clients.delete(socket)
  })

  socket.on('error', (err) => {
    console.log(`[Bridge] Socket error with ${remoteAddr}:`, err.message)
    clients.delete(socket)
  })
})

function handleIncomingCablePacket(socket, packet) {
  // Check if it's a Cable Message (Request / Response) or a raw Cable Post
  let isMessage = false
  try {
    const type = varint.decode(packet)
    const varintLen = varint.decode.bytes
    if (type >= 0 && type <= 8 && packet.length >= varintLen + 8) {
      // Check circuit ID (4 zeros)
      const c1 = packet[varintLen]
      const c2 = packet[varintLen + 1]
      const c3 = packet[varintLen + 2]
      const c4 = packet[varintLen + 3]
      if (c1 === 0 && c2 === 0 && c3 === 0 && c4 === 0) {
        isMessage = true
      }
    }
  } catch {}

  if (isMessage) {
    try {
      const msg = decodeMessage(packet)
      if (msg.type === CONSTANTS.TIME_RANGE_REQUEST) {
        console.log(`[Bridge] Received TimeRangeRequest for channel #${msg.channel}, limit: ${msg.limit}`)
        const filteredPosts = recentPosts
          .filter(p => !msg.channel || p.channel === msg.channel)
          .slice(-Math.min(msg.limit || 50, 100))
          .map(p => p.serialized)

        if (filteredPosts.length > 0) {
          const response = encodePostResponse(msg.reqId, filteredPosts)
          const lenBuf = Buffer.from(varint.encode(response.length))
          socket.write(Buffer.concat([lenBuf, response]))
          console.log(`[Bridge] Sent PostResponse with ${filteredPosts.length} posts to peer`)
        }
      } else if (msg.type === CONSTANTS.POST_REQUEST) {
        console.log(`[Bridge] Received PostRequest with ${msg.hashes ? msg.hashes.length : 0} hashes`)
        const matched = []
        if (msg.hashes && msg.hashes.length > 0) {
          const requestedSet = new Set(msg.hashes.map(h => Buffer.from(h).toString('hex')))
          for (const item of recentPosts) {
            if (requestedSet.has(item.hash.toString('hex'))) {
              matched.push(item.serialized)
            }
          }
        }
        if (matched.length > 0) {
          const response = encodePostResponse(msg.reqId, matched)
          const lenBuf = Buffer.from(varint.encode(response.length))
          socket.write(Buffer.concat([lenBuf, response]))
        }
      }
    } catch (err) {
      console.error('[Bridge] Failed to decode Cable Message:', err.message)
    }
    return
  }

  // Otherwise, handle as raw Cable Post
  try {
    const post = decodeTextPost(packet)
    if (post && post.type === CONSTANTS.TEXT_POST) {
      const authorHex = post.publicKey.toString('hex').slice(0, 8)
      console.log(`[Cable -> Cabal] [#${post.channel}] <${authorHex}>: ${post.text}`)

      cacheCablePost(packet, post.channel, post.timestamp)
      broadcastToClients(packet, socket) // Relay to other connected Cable peers

      // Forward to Cabal network
      if (cabal && cabal.publish) {
        cabal.publish({
          type: 'chat/text',
          content: {
            channel: post.channel || DEFAULT_CHANNEL,
            text: post.text,
            source: 'cable-android',
            author: authorHex
          }
        }, (pubErr) => {
          if (pubErr) console.error('[Bridge] Failed to publish to Cabal swarm:', pubErr)
          else console.log('[Bridge] Published message to cabal.chat successfully.')
        })
      }
    }
  } catch (err) {
    // Non-text post or unparseable, ignore safely
  }
}

tcpServer.listen(TCP_PORT, '0.0.0.0', () => {
  console.log(`[Bridge] Cable TCP Server listening on 0.0.0.0:${TCP_PORT}`)
})

// 3. UDP Peer Discovery Broadcast
const udpSocket = dgram.createSocket({ type: 'udp4', reuseAddr: true })

udpSocket.bind(UDP_PORT, () => {
  udpSocket.setBroadcast(true)
  console.log(`[Bridge] UDP Discovery broadcaster online on port ${UDP_PORT}`)

  // Broadcast announcements periodically
  setInterval(() => {
    // Format: CABAL|<key>|<port>
    const announceMsgDefault = Buffer.from(`CABAL|default|${TCP_PORT}`)
    const announceMsgKey = Buffer.from(`CABAL|${CABAL_KEY}|${TCP_PORT}`)

    const targets = ['255.255.255.255', '10.0.2.2']
    for (const target of targets) {
      udpSocket.send(announceMsgDefault, 0, announceMsgDefault.length, UDP_PORT, target, () => {})
      udpSocket.send(announceMsgKey, 0, announceMsgKey.length, UDP_PORT, target, () => {})
    }
  }, 5000)
})

// Graceful exit
function shutdown() {
  console.log('\n[Bridge] Shutting down bridge gracefully...')
  try { tcpServer.close() } catch {}
  try { udpSocket.close() } catch {}
  for (const client of clients) {
    try { client.destroy() } catch {}
  }
  process.exit(0)
}

process.on('SIGINT', shutdown)
process.on('SIGTERM', shutdown)
