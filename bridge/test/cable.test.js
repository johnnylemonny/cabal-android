const test = require('node:test')
const assert = require('node:assert')
const crypto = require('crypto')
const ed = require('@noble/ed25519')
const {
  CONSTANTS,
  generateKeyPair,
  encodeTextPost,
  decodeTextPost,
  encodePostResponse,
  decodeMessage,
  hashPost
} = require('../src/cable')

test('Cable Protocol - encode and decode TextPost', () => {
  const { secretKey, publicKey } = generateKeyPair()

  const original = {
    publicKey,
    secretKey,
    channel: 'default',
    text: 'Hello from Cabal online bridge!',
    timestamp: 1726000000,
    links: [crypto.randomBytes(32)]
  }

  const encoded = encodeTextPost(original)
  assert.ok(encoded.length > 96)

  const decoded = decodeTextPost(encoded)
  assert.strictEqual(decoded.type, CONSTANTS.TEXT_POST)
  assert.strictEqual(decoded.channel, 'default')
  assert.strictEqual(decoded.text, 'Hello from Cabal online bridge!')
  assert.strictEqual(decoded.timestamp, 1726000000)
  assert.strictEqual(decoded.links.length, 1)
  assert.deepStrictEqual(decoded.links[0], original.links[0])
  assert.deepStrictEqual(decoded.publicKey, original.publicKey)

  const hash = hashPost(encoded)
  assert.strictEqual(hash.length, 32)
})

test('Cable Protocol - encode and decode PostResponse', () => {
  const reqId = Buffer.from([1, 2, 3, 4])
  const dummyPost1 = Buffer.from('dummy-post-1')
  const dummyPost2 = Buffer.from('dummy-post-2')

  const encoded = encodePostResponse(reqId, [dummyPost1, dummyPost2])
  assert.ok(encoded.length > 10)

  const decodedHeader = decodeMessage(encoded)
  assert.strictEqual(decodedHeader.type, CONSTANTS.POST_RESPONSE)
  assert.deepStrictEqual(decodedHeader.reqId, reqId)
})
