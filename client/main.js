class Utils {
	/**
	 * @param {string} path
	 * @returns {Promise<string>}
	 */
	static get(path) {
		return new Promise((resolve, reject) => {
			var x = new XMLHttpRequest()
			x.open("GET", path)
			x.addEventListener("loadend", () => {
				if (x.status == 200) resolve(x.responseText)
				else reject(x.status)
			})
			x.send()
		})
	}
	/**
	 * @param {string} path
	 * @param {string} body
	 * @returns {Promise<string>}
	 */
	static post(path, body) {
		return new Promise((resolve, reject) => {
			var x = new XMLHttpRequest()
			x.open("POST", path)
			x.addEventListener("loadend", () => {
				if (x.status == 200) resolve(x.responseText)
				else reject(x.status)
			})
			x.send(body)
		})
	}
	/**
	 * @param {string} path
	 * @returns {Promise<Blob>}
	 */
	static getBinary(path) {
		return new Promise((resolve) => {
			var x = new XMLHttpRequest()
			x.open("GET", path)
			x.responseType = "blob"
			x.addEventListener("loadend", () => {
				resolve(x.response)
			})
			x.send()
		})
	}
	/**
	 * @param {Blob} blob
	 */
	static async unzipZipFile(blob) {
		var zipFileReader = new zip.ZipReader(new zip.BlobReader(blob));
		/** @type {Object<string, ArrayBuffer>} */
		var extracted_files = {}
		// read entries
		var entries = await zipFileReader.getEntries()
		for (var entry of entries) {
			if (entry.directory) continue;
			else {
				extracted_files[entry.filename.split("/").slice(1).join("/")] = await entry.arrayBuffer()
			}
		}
		await zipFileReader.close();
		return extracted_files;
	}
}

class Main {
	static async main() {
		var clientID = await Utils.post("/login", "");
		var main = new Main(clientID);
		main.getAssetDataFromServer().then((v) => {
			main.getMessagesFromServer()
			main.messageHandleLoop()
		})
		// @ts-ignore
		window.main = main
	}
	/**
	 * @param {string} clientID
	 */
	constructor(clientID) {
		this.clientID = clientID;
		/** @type {string[][]} */
		this.messageQueue = [];
		/** @type {Object<string, ArrayBuffer>} */
		this.assets = {};
	}
	async getAssetDataFromServer() {
		var blob = await Utils.getBinary("/data.zip");
		var assets = await Utils.unzipZipFile(blob);
		this.assets = assets;
	}
	async getMessagesFromServer() {
		var messages = JSON.parse(await Utils.get("/get_messages/" + this.clientID));
		// No message
		if (messages.length == 0) {
			setTimeout(this.getMessagesFromServer.bind(this), 1000);
			return;
		}
		// Handle message
		for (var msg of messages) {
			this.messageQueue.push(msg)
		}
		setTimeout(this.getMessagesFromServer.bind(this), 500);
	}
	async messageHandleLoop() {
		while (true) {
			while (this.messageQueue.length == 0) await new Promise((resolve) => setTimeout(resolve, 100));
			this.handleMessage(this.messageQueue[0])
			this.messageQueue.shift()
		}
	}
	/**
	 * @param {string[]} message
	 */
	async handleMessage(message) {
		if (message[0] == "log") {
			for (var m of message.slice(1)) console.log(m)
		}
	}
}
Main.main();
