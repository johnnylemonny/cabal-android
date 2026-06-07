package chat.cabal.mobile.core

fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
