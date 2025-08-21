package com.sillypantscoder.pixeldungeon4.level;

import com.sillypantscoder.pixeldungeon4.registries.TileType;

public class Tile {
	public String state;
	public Tile(String state) {
		this.state = state;
	}
	public TileType getData() {
		return TileType.allTileTypes.get(this.state);
	}
}
