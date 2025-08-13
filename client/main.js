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
}

class Main {
	static async main() {
		var clientID = await Utils.post("/login", "");
		var main = new Main(clientID);
		main.getMessageFromServer()
	}
	/**
	 * @param {string} clientID
	 */
	constructor(clientID) {
		this.clientID = clientID;
	}
	async getMessageFromServer() {
		var message = await Utils.get("/get_message/" + this.clientID);
		// No message
		if (message.length == 0) {
			setTimeout(this.getMessageFromServer.bind(this), 1000);
			return;
		}
		// Handle message
		this.handleMessage(message.split("\n")).then(() => {
			this.getMessageFromServer.bind(this);
		})
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
