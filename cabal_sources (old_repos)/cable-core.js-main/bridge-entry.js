const { CableCore } = require("./index.js");
const b4a = require("b4a");

// This will be called from Kotlin to initialize the engine
globalThis.initCable = function(publicKey, secretKey) {
  const opts = {
    keypair: {
      publicKey: b4a.from(publicKey, "hex"),
      secretKey: b4a.from(secretKey, "hex")
    },
    // We'll provide a storage shim that calls back to Kotlin
    storage: {
      // Mocking necessary storage methods if needed,
      // but ideally we map CableStore's level operations.
    }
  };

  // Simplified storage for now, will refine as we implement the bridge
  const level = {
    sublevel: () => level,
    open: (cb) => cb && cb(),
    get: (key, cb) => {
      const val = globalThis.kotlinStorage.get(b4a.toString(key, "hex"));
      if (val) cb(null, b4a.from(val, "hex"));
      else cb(new Error("NotFound"));
    },
    put: (key, val, cb) => {
      globalThis.kotlinStorage.put(b4a.toString(key, "hex"), b4a.toString(val, "hex"));
      if (cb) cb(null);
    },
    batch: (ops, cb) => {
      ops.forEach(op => {
        if (op.type === "put") {
          globalThis.kotlinStorage.put(b4a.toString(op.key, "hex"), b4a.toString(op.value, "hex"));
        }
      });
      if (cb) cb(null);
    }
  };

  globalThis.core = new CableCore(level, opts);

  globalThis.core.on("response", (buf) => {
    globalThis.kotlinNetwork.broadcast(b4a.toString(buf, "hex"));
  });

  globalThis.core.on("request", (buf) => {
    globalThis.kotlinNetwork.broadcast(b4a.toString(buf, "hex"));
  });

  // Emit chat messages to UI
  globalThis.core.on("chat/add", (data) => {
    globalThis.kotlinUI.onChatMessage(JSON.stringify(data));
  });
};

globalThis.handleIncomingData = function(dataHex) {
  if (globalThis.core) {
    globalThis.core._handleIncomingMessage(b4a.from(dataHex, "hex"));
  }
};

globalThis.postText = function(channel, text) {
  if (globalThis.core) {
    globalThis.core.postText(channel, text);
  }
};
