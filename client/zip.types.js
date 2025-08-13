class _BlobReader {
	/** @param {Blob} data */
	constructor(data) {}
}
class _TextWriter {
	constructor() {}
}

/**
 * @typedef {{ directory: false, filename: string, arrayBuffer: () => Promise<ArrayBuffer>, getData: (destination: _TextWriter) => Promise<string> }} FileEntry
 * @typedef {{ directory: true, filename: string }} DirectoryEntry
 */
class _ZipReader {
	/** @param {_BlobReader} reader */
	constructor(reader) {}
	/** @returns {Promise<(FileEntry | DirectoryEntry)[]>} */
	getEntries() { throw new Error(); }
	/** @returns {Promise<void>} */
	async close() {}
}

class zip {
	/** @typedef {_BlobReader} zip.BlobReader */
	static BlobReader = _BlobReader
	/** @typedef {_TextWriter} zip.TextWriter */
	static TextWriter = _TextWriter
	/** @typedef {_ZipReader} zip.ZipReader */
	static ZipReader = _ZipReader
}

