const varint = require('varint')
const { blake2b } = require('@noble/hashes/blake2b')
const { sha512 } = require('@noble/hashes/sha512')
const ed = require('@noble/ed25519')
const crypto = require('crypto')

if (ed.etc) {
  ed.etc.sha512Sync = (...m) => sha512(ed.etc.concatBytes(...m))
}

// Cable Protocol Constants (draft-8)
const CONSTANTS = {
  // Response types
  HASH_RESPONSE: 0,
  POST_RESPONSE: 1,
  CHANNEL_LIST_RESPONSE: 7,

  // Request types
  POST_REQUEST: 2,
  CANCEL_REQUEST: 3,
  TIME_RANGE_REQUEST: 4,
  CHANNEL_STATE_REQUEST: 5,
  CHANNEL_LIST_REQUEST: 6,
  MODERATION_STATE_REQUEST: 8,

  // Post types
  TEXT_POST: 0,
  DELETE_POST: 1,
  INFO_POST: 2,
  TOPIC_POST: 3,
  JOIN_POST: 4,
  LEAVE_POST: 5,

  CIRCUITID_SIZE: 4,
  REQID_SIZE: 4,
  HASH_SIZE: 32,
  PUBLICKEY_SIZE: 32,
  SIGNATURE_SIZE: 64
}

function hashPost(serializedPost) {
  return Buffer.from(blake2b(serializedPost, { dkLen: 32 }))
}

function encodeVarint(num) {
  const bytes = varint.encode(num)
  return Buffer.from(bytes)
}

function readVarint(buf, offset = 0) {
  const val = varint.decode(buf, offset)
  const bytes = varint.decode.bytes
  return { val, bytes }
}

function generateKeyPair() {
  const privKey = crypto.randomBytes(32)
  const getPubFn = ed.getPublicKeySync || ed.getPublicKey
  const pubKey = getPubFn(privKey)
  return {
    secretKey: privKey,
    publicKey: Buffer.from(pubKey)
  }
}

function encodeTextPost({ publicKey, secretKey, channel, text, timestamp = Math.floor(Date.now() / 1000), links = [] }) {
  const channelBuf = Buffer.from(channel, 'utf8')
  const textBuf = Buffer.from(text, 'utf8')

  const linksCountBuf = encodeVarint(links.length)
  const postTypeBuf = encodeVarint(CONSTANTS.TEXT_POST)
  const timestampBuf = encodeVarint(timestamp)
  const channelLenBuf = encodeVarint(channelBuf.length)
  const textLenBuf = encodeVarint(textBuf.length)

  const payloadChunks = [
    linksCountBuf,
    ...links.map(l => Buffer.from(l)),
    postTypeBuf,
    timestampBuf,
    channelLenBuf,
    channelBuf,
    textLenBuf,
    textBuf
  ]
  const payload = Buffer.concat(payloadChunks)

  let signature = Buffer.alloc(CONSTANTS.SIGNATURE_SIZE)
  if (secretKey) {
    const signFn = ed.signSync || ed.sign
    const sig = signFn(payload, secretKey)
    signature = Buffer.from(sig)
  }

  const pub = Buffer.isBuffer(publicKey) ? publicKey : Buffer.from(publicKey)
  return Buffer.concat([pub, signature, payload])
}

function decodeTextPost(buf) {
  if (buf.length < CONSTANTS.PUBLICKEY_SIZE + CONSTANTS.SIGNATURE_SIZE) {
    throw new Error('Buffer too small for Cable post')
  }

  const publicKey = buf.subarray(0, CONSTANTS.PUBLICKEY_SIZE)
  const signature = buf.subarray(CONSTANTS.PUBLICKEY_SIZE, CONSTANTS.PUBLICKEY_SIZE + CONSTANTS.SIGNATURE_SIZE)
  let offset = CONSTANTS.PUBLICKEY_SIZE + CONSTANTS.SIGNATURE_SIZE

  // Links count
  const linksCountDec = readVarint(buf, offset)
  const linksCount = linksCountDec.val
  offset += linksCountDec.bytes

  const links = []
  for (let i = 0; i < linksCount; i++) {
    links.push(buf.subarray(offset, offset + CONSTANTS.HASH_SIZE))
    offset += CONSTANTS.HASH_SIZE
  }

  // Post type
  const postTypeDec = readVarint(buf, offset)
  const postType = postTypeDec.val
  offset += postTypeDec.bytes

  if (postType !== CONSTANTS.TEXT_POST) {
    return { type: postType, publicKey, signature, links }
  }

  // Timestamp
  const tsDec = readVarint(buf, offset)
  const timestamp = tsDec.val
  offset += tsDec.bytes

  // Channel
  const chLenDec = readVarint(buf, offset)
  offset += chLenDec.bytes
  const channel = buf.subarray(offset, offset + chLenDec.val).toString('utf8')
  offset += chLenDec.val

  // Text
  const textLenDec = readVarint(buf, offset)
  offset += textLenDec.bytes
  const text = buf.subarray(offset, offset + textLenDec.val).toString('utf8')

  return {
    type: postType,
    publicKey,
    signature,
    links,
    channel,
    timestamp,
    text
  }
}

function encodePostResponse(reqId, posts) {
  const typeBuf = encodeVarint(CONSTANTS.POST_RESPONSE)
  const circuitBuf = Buffer.alloc(CONSTANTS.CIRCUITID_SIZE, 0)
  const reqIdBuf = Buffer.isBuffer(reqId) ? reqId : Buffer.from(reqId)

  const chunks = [typeBuf, circuitBuf, reqIdBuf]
  for (const post of posts) {
    chunks.push(encodeVarint(post.length))
    chunks.push(post)
  }
  chunks.push(encodeVarint(0)) // Terminal 0 length

  return Buffer.concat(chunks)
}

function decodeMessage(buf) {
  let offset = 0
  const typeDec = readVarint(buf, offset)
  const type = typeDec.val
  offset += typeDec.bytes

  if (buf.length < offset + CONSTANTS.CIRCUITID_SIZE + CONSTANTS.REQID_SIZE) {
    throw new Error('Buffer too small for Cable message header')
  }

  const circuitId = buf.subarray(offset, offset + CONSTANTS.CIRCUITID_SIZE)
  offset += CONSTANTS.CIRCUITID_SIZE

  const reqId = buf.subarray(offset, offset + CONSTANTS.REQID_SIZE)
  offset += CONSTANTS.REQID_SIZE

  if (type === CONSTANTS.TIME_RANGE_REQUEST) {
    const ttlDec = readVarint(buf, offset)
    offset += ttlDec.bytes

    const chLenDec = readVarint(buf, offset)
    offset += chLenDec.bytes
    const channel = buf.subarray(offset, offset + chLenDec.val).toString('utf8')
    offset += chLenDec.val

    const timeStartDec = readVarint(buf, offset)
    offset += timeStartDec.bytes

    const timeEndDec = readVarint(buf, offset)
    offset += timeEndDec.bytes

    const limitDec = readVarint(buf, offset)

    return {
      type,
      circuitId,
      reqId,
      ttl: ttlDec.val,
      channel,
      timeStart: timeStartDec.val,
      timeEnd: timeEndDec.val,
      limit: limitDec.val
    }
  }

  if (type === CONSTANTS.POST_REQUEST) {
    const ttlDec = readVarint(buf, offset)
    offset += ttlDec.bytes

    const hashCountDec = readVarint(buf, offset)
    offset += hashCountDec.bytes

    const hashes = []
    for (let i = 0; i < hashCountDec.val; i++) {
      hashes.push(buf.subarray(offset, offset + CONSTANTS.HASH_SIZE))
      offset += CONSTANTS.HASH_SIZE
    }

    return {
      type,
      circuitId,
      reqId,
      ttl: ttlDec.val,
      hashes
    }
  }

  return { type, circuitId, reqId }
}

module.exports = {
  CONSTANTS,
  hashPost,
  generateKeyPair,
  encodeVarint,
  readVarint,
  encodeTextPost,
  decodeTextPost,
  encodePostResponse,
  decodeMessage
}
