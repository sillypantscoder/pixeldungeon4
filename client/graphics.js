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
		this.fill(color)
	}
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
