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
class AssetManager {
	constructor() {
		/** @type {{ definitions: { tile: Object<string, { collisionType: "none" | "normal" | "wall", canSeeThrough: boolean }> }, textures: Object<string, Object<string, Surface>> }} */
		this.assets = {
			"definitions": {
				"tile": {}
			},
			"textures": {
				"tile": {}
			}
		}
	}
	/**
	 * @param {Object<string, ArrayBuffer>} files
	 * @returns {Promise<void[]>}
	 */
	importAssets(files) {
		/** @type {Promise<void>[]} */
		var promises = []
		for (var _filename of Object.keys(files)) {
			let fileData = files[_filename]
			let filename = _filename.split(".")[0].split("/")
			// Different handling for textures
			if (filename[0] == "textures") {
				let registry = this.assets.textures
				promises.push(Surface.fromArrayBufferPNG(fileData).then((surface) => {
					registry[filename[1]][filename[2]] = surface
				}))
			} else if (filename[0] == "definitions") {
				if (filename[1] == "tile") {
					const decoder = new TextDecoder('utf-8')
					this.assets.definitions.tile[filename[2]] = JSON.parse(decoder.decode(fileData))
				} else {
					console.error("Unknown definition type for filename:", _filename, fileData)
				}
			} else {
				console.error("Unknown asset type for filename:", _filename, fileData)
			}
		}
		return Promise.all(promises)
	}
	/**
	 * @param {string} type
	 * @param {string} id
	 */
	getTexture(type, id) {
		var asset = this.assets.textures[type][id]
		if (asset == undefined) throw new Error(`Missing asset: Texture for ${type}/${id}`)
		return asset
	}
}

/** @typedef {{ state: string, visibility: 0 | 1 | 2 }} TileState */
class Game {
	/** @param {Main} main */
	constructor(main) {
		this.main = main
		/** @type {TileState[][]} */
		this.level = [[{ state: "none", visibility: 0 }]]
		this.assets = new AssetManager()
	}
	/**
	 * @param {number} xSize
	 * @param {number} ySize
	 */
	setLevelSize(xSize, ySize) {
		this.level = []
		for (var y = 0; y < ySize; y++) {
			/** @type {TileState[]} */
			var column = []
			this.level.push(column)
			for (var x = 0; x < xSize; x++) {
				/** @type {TileState} */
				var cell = { state: "none", visibility: 0 }
				column.push(cell)
			}
		}
	}
	/**
	 * @param {string[]} tilesData
	 */
	showTiles(tilesData) {
		for (var td of tilesData) {
			var x = Number(td.split(" ")[0])
			var y = Number(td.split(" ")[1])
			var state = td.split(" ")[2]
			this.level[x][y] = { state, visibility: 2 }
		}
	}
}
class Rendering {
	static TILE_SIZE = 16;
	static LEVEL_CANVAS = [...document.getElementsByTagName("canvas")].filter((v) => v.id == "level")[0]
	static setupCanvas() {
		this.LEVEL_CANVAS.width = window.innerWidth
		this.LEVEL_CANVAS.height = window.innerHeight
	}
	/**
	 * @param {TileState[][]} level
	 * @param {AssetManager} assetManager
	 */
	static renderTiles(level, assetManager) {
		var s = new Surface(level[0].length * this.TILE_SIZE, level.length * this.TILE_SIZE, "black");
		for (var x = 0; x < level[0].length; x++) {
			for (var y = 0; y < level.length; y++) {
				var tile = level[x][y]
				var tileImage = assetManager.getTexture("tile", tile.state)
				s.blit(tileImage, x * this.TILE_SIZE, y * this.TILE_SIZE)
			}
		}
		return s
	}
	/** @param {Game} game */
	static renderWholeScreen(game) {
		var s = this.renderTiles(game.level, game.assets);
		// Draw to canvas!
		s.drawToCanvas(this.LEVEL_CANVAS)
	}
}

class Main {
	static async main() {
		Rendering.setupCanvas()
		var clientID = await Utils.post("/login", "");
		var main = new Main(clientID);
		main.getAssetDataFromServer().then(() => {
			main.getMessagesFromServer()
			main.messageHandleLoop()
			main.renderLoop()
		})
		// @ts-ignore
		window.main = main
	}
	/** @param {string} clientID */
	constructor(clientID) {
		this.clientID = clientID;
		/** @type {string[][]} */
		this.messageQueue = [];
		// create game
		this.game = new Game(this)
	}
	async getAssetDataFromServer() {
		var blob = await Utils.getBinary("/data.zip");
		var assets = await Utils.unzipZipFile(blob);
		await this.game.assets.importAssets(assets)
	}
	async getMessagesFromServer() {
		var messages = JSON.parse(await Utils.get("/get_messages/" + this.clientID));
		// No message
		if (messages.length == 0) {
			setTimeout(this.getMessagesFromServer.bind(this), 3000);
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
		} else if (message[0] == "level_size") {
			var xSize = Number(message[1])
			var ySize = Number(message[2])
			this.game.setLevelSize(xSize, ySize)
		} else if (message[0] == "show_tiles") {
			this.game.showTiles(message.slice(1))
		} else {
			console.log("Unknown message!", message)
		}
	}
	async renderLoop() {
		while (true) {
			await new Promise((resolve) => requestAnimationFrame(resolve));
			this.renderFrame()
		}
	}
	renderFrame() {
		Rendering.renderWholeScreen(this.game)
	}
}
Main.main();
