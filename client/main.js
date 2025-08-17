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
	/**
	 * Get all the points that a line passes through.
	 * Used for lighting.
	 * @param {number} x1
	 * @param {number} y1
	 * @param {number} x2
	 * @param {number} y2
	 * @returns {{ x: number, y: number }[]}
	 */
	static get_line(x1, y1, x2, y2) {
		/*
			Bresenham's Line Algorithm
			Produces a list of coordinate points from start and end
		*/
		// Setup initial conditions
		var dx = x2 - x1;
		var dy = y2 - y1;

		// Determine how steep the line is
		var is_steep = Math.abs(dy) > Math.abs(dx);

		// Rotate line
		if (is_steep) {
			var _x1 = x1;
			x1 = y1;
			y1 = _x1;
			var _x2 = x2;
			x2 = y2;
			y2 = _x2;
		}

		// Swap start and end points if necessary and store swap state
		var swapped = false;
		if (x1 > x2) {
			var _x1 = x1;
			x1 = x2;
			x2 = _x1;
			var _y1 = y1;
			y1 = y2;
			y2 = _y1;
			swapped = true;
		}

		// Recalculate differentials
		dx = x2 - x1;
		dy = y2 - y1;

		// Calculate error
		var error = dx / 2.0;
		var ystep = -1;
		if (y1 < y2) ystep = 1;

		// Iterate over bounding box generating points between start and end
		var y = y1;
		/** @type {{ x: number, y: number }[]} */
		var points = [];
		for (var x = x1; x < x2 + 1; x++) {
			var coord = { x, y };
			if (is_steep) coord = { y, x };
			points.push(coord);
			error -= Math.abs(dy);
			if (error < 0) {
				y += ystep;
				error += dx;
			}
		}

		// Reverse the list if the coordinates were swapped
		/** @type {{ x: number, y: number }[]} */
		var ret = [];
		for (var i = 0; i < points.length; i++) {
			ret.push({ x: 0, y: 0 });
		}
		if (swapped) {
			for (var i = 0; i < points.length; i++) {
				ret[points.length - (i + 1)].x = points[i].x;
				ret[points.length - (i + 1)].y = points[i].y;
			}
		} else {
			for (var i = 0; i < points.length; i++) {
				ret[i].x = points[i].x;
				ret[i].y = points[i].y;
			}
		}
		return ret;
	}
}
class AssetManager {
	constructor() {
		/**
		 * @typedef {{ collisionType: "none" | "normal" | "wall", canSeeThrough: boolean }} TileDefinition
		 * @typedef {{ tileSize: number, initialAnimation: string, animations: Object<string, { frames: { x: number, y: number }[], next: string }> }} EntitySpritesheetDefinition
		 * @type {{ definitions: { tile: Object<string, TileDefinition>, entity_spritesheets: Object<string, EntitySpritesheetDefinition> }, textures: Object<string, Object<string, { normal: Surface, dark: Surface }>> }}
		 */
		this.assets = {
			"definitions": {
				"entity_spritesheets": {},
				"tile": {}
			},
			"textures": {
				"entity": {},
				"special": {},
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
					registry[filename[1]][filename[2]] = {
						normal: surface,
						dark: surface.copy().darken()
					}
				}))
			} else if (filename[0] == "definitions") {
				if (filename[1] == "entity_spritesheets") {
					const decoder = new TextDecoder('utf-8')
					this.assets.definitions.entity_spritesheets[filename[2]] = JSON.parse(decoder.decode(fileData))
				} else if (filename[1] == "tile") {
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
	 * @param {boolean | undefined} [dark]
	 */
	getTexture(type, id, dark) {
		var asset = this.assets.textures[type][id]
		if (asset == undefined) throw new Error(`Missing asset: Texture for ${type}/${id}`)
		if (dark == true) return asset.dark
		return asset.normal
	}
	/**
	 * @param {string} id
	 */
	getTileDefinition(id) {
		return this.assets.definitions.tile[id]
	}
	/**
	 * @param {{ type: string} & Object<string, any>} entity_data
	 * @returns {Entity}
	 */
	deserializeEntity(entity_data) {
		if (entity_data.type == "player") {
			var entity = new Player(entity_data.id, entity_data.x, entity_data.y, entity_data.health, entity_data.maxHealth);
			return entity
		}
		throw new Error("Entity type not found: " + JSON.stringify(entity_data))
	}
}
class SpritesheetDisplay {
	/**
	 * @param {Surface} surface
	 * @param {number} tileSize
	 * @param {Object<string, { frames: { x: number, y: number }[], next: string }>} animations
	 * @param {string} initialAnimation
	 */
	constructor(surface, tileSize, animations, initialAnimation) {
		/** @type {Object<string, Surface>} */
		this.surfaces = {}
		for (var x = 0; x < surface.get_width(); x += tileSize) {
			for (var y = 0; y < surface.get_height(); y += tileSize) {
				this.surfaces[`${x/tileSize},${y/tileSize}`] = surface.crop(x, y, tileSize, tileSize)
			}
		}
		this.animations = animations
		this.currentAnimation = initialAnimation
		this.currentAnimationPos = 0
		this.frameTime = 0
	}
	nextFrame() {
		this.frameTime += 1
		if (this.frameTime == 8) {
			this.frameTime = 0
			this.nextAnimationFrame()
		}
	}
	nextAnimationFrame() {
		this.currentAnimationPos += 1
		if (this.currentAnimationPos >= this.animations[this.currentAnimation].frames.length) {
			// Next animation!
			var next = this.animations[this.currentAnimation].next
			if (! Object.keys(this.animations).includes(next)) {
				// There is no next animation... repeat this frame forever...?
				this.currentAnimationPos -= 1;
				return;
			}
			// Set current animation
			this.currentAnimation = next
			this.currentAnimationPos = 0
		}
	}
	getFrame() {
		var image_pos = this.animations[this.currentAnimation].frames[this.currentAnimationPos]
		return this.surfaces[`${image_pos.x},${image_pos.y}`]
	}
}

class Actor {
	static ACTOR_MOVE_SPEED = 0.125;
	/**
	 * @param {SpritesheetDisplay} sprites
	 * @param {number} x
	 * @param {number} y
	 */
	constructor(sprites, x, y) {
		this.sprites = sprites
		this.x = x
		this.y = y
	}
	/**
	 * @param {number} targetX
	 * @param {number} targetY
	 */
	moveFrame(targetX, targetY) {
		if (this.x < targetX) this.x += Actor.ACTOR_MOVE_SPEED;
		if (this.x > targetX) this.x -= Actor.ACTOR_MOVE_SPEED;
		if (this.y < targetY) this.y += Actor.ACTOR_MOVE_SPEED;
		if (this.y > targetY) this.y -= Actor.ACTOR_MOVE_SPEED;
	}
	/**
	 * @param {string} entityID
	 * @param {AssetManager} assets
	 * @param {number} x
	 * @param {number} y
	 */
	static createForEntityID(entityID, assets, x, y) {
		var spritesImage = assets.getTexture("entity", entityID)
		var spritesData = assets.assets.definitions.entity_spritesheets[entityID]
		return new Actor(new SpritesheetDisplay(spritesImage, spritesData.tileSize, spritesData.animations, spritesData.initialAnimation), x, y)
	}
}
class Entity {
	/**
	 * @param {number} id
	 * @param {number} x
	 * @param {number} y
	 */
	constructor(id, x, y) {
		this.id = id
		this.x = x
		this.y = y
		/** @type {Actor | null} */
		this.actor = null
	}
	/**
	 * @abstract
	 * @returns {string}
	 */
	getEntityID() { throw new Error("`Entity` is an abstract class, `getID` must be overridden"); }
	/**
	 * @param {AssetManager} assets
	 */
	getNextFrame(assets) {
		if (this.actor == null) {
			this.actor = Actor.createForEntityID(this.getEntityID(), assets, this.x, this.y)
		} else {
			this.actor.sprites.nextFrame()
		}
		this.actor.moveFrame(this.x, this.y)
		return this.actor.sprites.getFrame()
	}
	/**
	 * @param {Game} game
	 */
	verifyVisible(game) {
		var tile = game.level[this.x][this.y]
		if (tile.visibility == 0) {
			game.removeEntity(this)
		}
	}
}
class LivingEntity extends Entity {
	/**
	 * @param {number} id
	 * @param {number} x
	 * @param {number} y
	 * @param {number} health
	 * @param {number} maxHealth
	 */
	constructor(id, x, y, health, maxHealth) {
		super(id, x, y)
		this.health = health
		this.maxHealth = maxHealth
	}
}
class Player extends LivingEntity {
	/**
	 * @param {number} id
	 * @param {number} x
	 * @param {number} y
	 * @param {number} health
	 * @param {number} maxHealth
	 */
	constructor(id, x, y, health, maxHealth) {
		super(id, x, y, health, maxHealth)
		/** @type {null | Entity | { x: number, y: number }} */
		this.target = null
	}
	getEntityID() { return "player" }
	/**
	 * @param {Game} game
	 */
	verifyVisible(game) {
		super.verifyVisible(game);
		// Update level light
		for (var x = 0; x < game.level.length; x++) {
			for (var y = 0; y < game.level[0].length; y++) {
				var tile = game.level[x][y]
				if (tile.visibility != 2) continue;
				if (! game.isLocVisible(this.x, this.y, x, y)) tile.visibility = 1
			}
		}
		// Update all entities
		for (var entity of game.entities) {
			if (entity == this) continue;
			entity.verifyVisible(game);
		}
	}
}

/** @typedef {{ state: string, visibility: 0 | 1 | 2 }} TileState */
class Game {
	/** @param {Main} main */
	constructor(main) {
		this.main = main
		/** @type {TileState[][]} */
		this.level = [[{ state: "none", visibility: 0 }]]
		/** @type {Entity[]} */
		this.entities = []
		this.me = new Player(0, 0, 0, 0, 0);
		this.assets = new AssetManager()
	}
	/**
	 * @param {number} id
	 */
	getEntityByID(id) {
		for (var entity of this.entities) {
			if (entity.id == id) return entity;
		}
		return null
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
	 * @param {number} x1
	 * @param {number} y1
	 * @param {number} x2
	 * @param {number} y2
	 */
	isLocVisible(x1, y1, x2, y2) {
		if (((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) > 64) return false;
		var points = Utils.get_line(x1, y1, x2, y2);
		for (var i = 1; i < points.length - 1; i++) {
			var stateString = this.level[points[i].x][points[i].y].state;
			if (! this.assets.getTileDefinition(stateString).canSeeThrough) return false;
		}
		return true;
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
	/**
	 * @param {Entity} entity
	 */
	removeEntity(entity) {
		this.entities.splice(this.entities.indexOf(entity), 1)
	}
}
class Rendering {
	static TILE_SIZE = 16;
	static LEVEL_CANVAS = [...document.getElementsByTagName("canvas")].filter((v) => v.id == "level")[0]
	/**
	 * @param {Game} game
	 */
	static setupCanvas(game) {
		this.LEVEL_CANVAS.width = window.innerWidth
		this.LEVEL_CANVAS.height = window.innerHeight
		// Clickable
		this.LEVEL_CANVAS.addEventListener("click", (e) => {
			var x = e.clientX
			var y = e.clientY
			Rendering.click(game, Math.floor(x / this.TILE_SIZE), Math.floor(y / this.TILE_SIZE));
		})
	}
	/**
	 * @param {TileState[][]} level
	 * @param {AssetManager} assetManager
	 */
	static renderTiles(level, assetManager) {
		var s = new Surface(level.length * this.TILE_SIZE, level[0].length * this.TILE_SIZE, "black");
		for (var x = 0; x < level.length; x++) {
			for (var y = 0; y < level[0].length; y++) {
				var tile = level[x][y]
				var tileImage = assetManager.getTexture("tile", tile.state, tile.visibility == 1)
				s.blit(tileImage, x * this.TILE_SIZE, y * this.TILE_SIZE)
			}
		}
		return s
	}
	/**
	 * @param {TileState[][]} level
	 * @param {Entity[]} entities
	 * @param {AssetManager} assetManager
	 */
	static renderEntities(level, entities, assetManager) {
		var s = new Surface(level.length * this.TILE_SIZE, level[0].length * this.TILE_SIZE, "transparent");
		for (var entity of entities) {
			var entityImage = entity.getNextFrame(assetManager)
			var actorPos = entity.actor ?? { x: -1, y: -1 }
			s.blit(entityImage, actorPos.x * this.TILE_SIZE, actorPos.y * this.TILE_SIZE)
		}
		return s
	}
	/** @param {Game} game */
	static renderWholeScreen(game) {
		var s = this.renderTiles(game.level, game.assets);
		s.blit(this.renderEntities(game.level, game.entities, game.assets), 0, 0);
		// Render target
		var target = game.me.target
		if (target != null) {
			var texture = game.assets.getTexture("special", "target_" + (target instanceof Entity ? "entity" : "pos"));
			s.blit(texture, target.x * this.TILE_SIZE, target.y * this.TILE_SIZE);
		}
		// Draw to canvas!
		s.drawToCanvas(this.LEVEL_CANVAS);
	}
	/**
	 * @param {Game} game
	 * @param {number} x
	 * @param {number} y
	 */
	static click(game, x, y) {
		if (x < 0 || y < 0 || x >= game.level.length || y >= game.level[0].length) return;
		var tile = game.level[x][y];
		if (tile.visibility == 0) return;
		// Set player target
		game.me.target = null
		for (var entity of game.entities) {
			if (entity.x == x && entity.y == y) {
				game.me.target = entity
			}
		}
		if (game.me.target == null) game.me.target = { x, y }
		// Post
		Utils.post("/click", game.main.clientID + "\n" + x + "\n" + y);
	}
}

class Main {
	static async main() {
		var clientID = await Utils.post("/login", "");
		var main = new Main(clientID);
		Rendering.setupCanvas(main.game);
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
			setTimeout(this.getMessagesFromServer.bind(this), 2000);
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
			await this.handleMessage(this.messageQueue[0])
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
		} else if (message[0] == "create_entity") {
			// check if entity already exists
			var entity = this.game.getEntityByID(JSON.parse(message[1]).id)
			if (entity != null) return;
			entity = this.game.assets.deserializeEntity(JSON.parse(message[1]))
			this.game.entities.push(entity)
		} else if (message[0] == "set_me") {
			var entity = this.game.getEntityByID(JSON.parse(message[1]))
			if (entity == null) throw new Error("can't set `me` to a nonexistent entity")
			if (! (entity instanceof Player)) throw new Error("can't set `me` to a non-player entity")
			this.game.me = entity
		} else if (message[0] == "move_entity") {
			var entity = this.game.getEntityByID(Number(message[1]))
			if (entity == null) throw new Error("Can't set position of nonexistent entity")
			// Wait for animation to finish
			while (entity.x != entity.actor?.x || entity.y != entity.actor?.y) await new Promise((resolve) => requestAnimationFrame(resolve))
			// Set position
			entity.x = Number(message[2])
			entity.y = Number(message[3])
			entity.verifyVisible(this.game)
		} else if (message[0] == "clear_target") {
			this.game.me.target = null
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
