package chat.cabal.protocol

object Constants {
    // Response types
    const val HASH_RESPONSE = 0
    const val POST_RESPONSE = 1
    const val CHANNEL_LIST_RESPONSE = 7

    // Request types
    const val POST_REQUEST = 2
    const val CANCEL_REQUEST = 3
    const val TIME_RANGE_REQUEST = 4
    const val CHANNEL_STATE_REQUEST = 5
    const val CHANNEL_LIST_REQUEST = 6
    const val MODERATION_STATE_REQUEST = 8

    // Post types
    const val TEXT_POST = 0
    const val DELETE_POST = 1
    const val INFO_POST = 2
    const val TOPIC_POST = 3
    const val JOIN_POST = 4
    const val LEAVE_POST = 5
    const val ROLE_POST = 6
    const val MODERATION_POST = 7
    const val BLOCK_POST = 8
    const val UNBLOCK_POST = 9

    // Moderation action types
    const val ACTION_HIDE_USER = 0
    const val ACTION_UNHIDE_USER = 1
    const val ACTION_HIDE_POST = 2
    const val ACTION_UNHIDE_POST = 3
    const val ACTION_DROP_POST = 4
    const val ACTION_UNDROP_POST = 5
    const val ACTION_DROP_CHANNEL = 6
    const val ACTION_UNDROP_CHANNEL = 7

    // Flags
    const val USER_FLAG = 2
    const val MOD_FLAG = 1
    const val ADMIN_FLAG = 0

    const val CABAL_CONTEXT = "\u0000"

    // Sizes
    const val MAX_VARINT_SIZE = 10
    const val REQID_SIZE = 4
    const val CIRCUITID_SIZE = 4
    const val HASH_SIZE = 32
    const val PUBLICKEY_SIZE = 32
    const val SECRETKEY_SIZE = 64
    const val SIGNATURE_SIZE = 64

    // Max sizes
    const val USER_NAME_MIN_CODEPOINTS = 1
    const val USER_NAME_MAX_CODEPOINTS = 32
    const val CHANNEL_NAME_MIN_CODEPOINTS = 1
    const val CHANNEL_NAME_MAX_CODEPOINTS = 64
    const val POST_TEXT_MAX_BYTES = 4096
    const val INFO_KEY_MIN_CODEPOINTS = 1
    const val INFO_KEY_MAX_CODEPOINTS = 128
    const val INFO_VALUE_MAX_BYTES = 4096
    const val TOPIC_MIN_CODEPOINTS = 0
    const val TOPIC_MAX_CODEPOINTS = 512
    const val REASON_MIN_CODEPOINTS = 0
    const val REASON_MAX_CODEPOINTS = 128
}
