package com.sillypantscoder.pixeldungeon4.level;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.registries.TileType;
import com.sillypantscoder.utils.LinePoints;
import com.sillypantscoder.utils.Random;

public class Level {
	public Tile[][] tiles;
	public ArrayList<Entity> entities;
	public Level(int width, int height) {
		tiles = new Tile[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y] = new Tile("none");
			}
		}
		this.entities = new ArrayList<Entity>();
	}
	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= tiles.length || y >= tiles[0].length) return null;
		return tiles[x][y];
	}
	public int[] getSpawnPoint() {
		ArrayList<int[]> possibleSpawns = new ArrayList<int[]>();
		for (int x = 0; x < this.tiles.length; x++) {
			for (int y = 0; y < this.tiles[0].length; y++) {
				if (TileType.allTileTypes.get(tiles[x][y].state).collisionType == TileType.CollisionType.NORMAL) {
					possibleSpawns.add(new int[] { x, y });
				}
			}
		}
		return Random.choice(possibleSpawns);
	}
	public int getNewEntityTime() {
		int minEntityTime = -1;
		for (Entity entity : entities) {
			if (minEntityTime == -1 || entity.time < minEntityTime) {
				minEntityTime = entity.time;
			}
		}
		return minEntityTime + 1;
	}
	public boolean doEntityTurn() {
		Entity minimumTimeEntity = null;
		int minimumTime = 0;
		for (Entity entity : entities) {
			if (minimumTimeEntity == null || entity.time < minimumTime) {
				minimumTimeEntity = entity;
				minimumTime = entity.time;
			}
		}
		if (minimumTimeEntity != null) {
			return minimumTimeEntity.takeTurn();
		} else return false;
	}
	public boolean isLocVisible(int x1, int y1, int x2, int y2) {
		int[][] points = LinePoints.get_line(new int[] { x1, y1 }, new int[] { x2, y2 });
		for (int i = 0; i < points.length - 1; i++) {
			String stateString = this.tiles[points[i][0]][points[i][1]].state;
			if (! TileType.allTileTypes.get(stateString).canSeeThrough) return false;
		}
		return true;
	}
}
