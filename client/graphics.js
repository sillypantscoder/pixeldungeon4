class Surface {
	/**
	 * @param {number} width
	 * @param {number} height
	 * @param {string} color
	 */
	constructor(width, height, color) {
		this.canvas = new OffscreenCanvas(width, height)
		/** @type {OffscreenCanvasRenderingContext2D} */
		this.context = this.canvas.getContext('2d') ?? (() => { throw new Error() })()
		if (color != "transparent") this.fill(color)
	}
	get_width() { return this.canvas.width }
	get_height() { return this.canvas.height }
	/**
	 * @param {string} color
	 */
	fill(color) {
		this.context.fillStyle = color
		this.context.fillRect(0, 0, this.canvas.width, this.canvas.height)
	}
	/**
	 * @param {Surface | HTMLImageElement} other
	 * @param {number} x
	 * @param {number} y
	 */
	blit(other, x, y) {
		this.context.drawImage(other instanceof Surface ? other.canvas : other, x, y)
	}
	copy() {
		var o = new Surface(this.canvas.width, this.canvas.height, "transparent")
		o.blit(this, 0, 0)
		return o
	}
	darken() {
		this.context.fillStyle = "rgba(0, 0, 0, 0.4)";
		this.context.fillRect(0, 0, this.canvas.width, this.canvas.height);
		return this;
	}
	/**
	 * @param {HTMLCanvasElement} canvas
	 */
	drawToCanvas(canvas) {
		var ctx = canvas.getContext('2d')
		if (ctx == null) throw new Error("wrong type of canvas")
		ctx.clearRect(0, 0, canvas.width, canvas.height)
		ctx.drawImage(this.canvas, 0, 0)
	}
	/**
	 * @param {number} x
	 * @param {number} y
	 * @param {number} width
	 * @param {number} height
	 */
	crop(x, y, width, height) {
		var cropped = new Surface(width, height, "transparent")
		cropped.context.drawImage(this.canvas, x, y, width, height, 0, 0, width, height)
		return cropped
	}
	flipHorizontally() {
		var flipped = new Surface(this.canvas.width, this.canvas.height, "transparent")
		flipped.context.translate(this.canvas.width, 0)
		flipped.context.scale(-1, 1)
		flipped.context.drawImage(this.canvas, 0, 0)
		flipped.context.setTransform(1, 0, 0, 1, 0, 0) // Reset transformation matrix
		return flipped
	}
	/**
	 * @param {ArrayBuffer} arrayBuffer
	 * @returns {Promise<Surface>}
	 */
	static fromArrayBufferPNG(arrayBuffer) {
		var blob = new Blob([arrayBuffer], { type: 'image/png' })
		var img = new Image()
		img.src = URL.createObjectURL(blob)
		return new Promise((resolve) => {
			img.addEventListener("load", () => {
				var surface = new Surface(img.width, img.height, 'transparent')
				surface.context.drawImage(img, 0, 0)
				resolve(surface)
			})
		})
	}
}
