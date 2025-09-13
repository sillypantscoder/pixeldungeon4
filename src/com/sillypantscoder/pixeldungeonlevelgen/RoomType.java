package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.registries.TileType;
import com.sillypantscoder.utils.Random;

public enum RoomType {
	NORMAL(new ArrayList<String>()),
	ENTRY(TileType.findTilesWithCollisionType(TileType.CollisionType.LEVEL_ENTRY)),
	EXIT(TileType.findTilesWithCollisionType(TileType.CollisionType.LEVEL_EXIT));
	public ArrayList<String> decorationTiles;
	private RoomType(ArrayList<String> decorationTiles) {
		this.decorationTiles = decorationTiles;
	}
	public void drawDecoration(Level level, int x, int y) {
		if (decorationTiles.size() == 0) return;
		String selectedTile = Random.choice(decorationTiles);
		level.tiles[x][y].state = selectedTile;
	}
}
