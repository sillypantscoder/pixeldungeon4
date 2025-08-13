package com.sillypantscoder.pixeldungeon4.level;

public class Level {
	public Tile[][] tiles;
	public Level(int width, int height) {
		tiles = new Tile[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y] = new Tile("none");
			}
		}
	}
	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) return null;
		return tiles[x][y];
	}
}
