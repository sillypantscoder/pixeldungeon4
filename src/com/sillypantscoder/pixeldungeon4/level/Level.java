package com.sillypantscoder.pixeldungeon4.level;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.registries.TileType;
import com.sillypantscoder.utils.LinePoints;
import com.sillypantscoder.utils.Pathfinding;
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
				if (tiles[x][y].getData().collisionType == TileType.CollisionType.NORMAL) {
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
	/**
	 * The next entity takes a turn.
	 * @return True if we can continue taking turns, false if no more turns are possible for some reason.
	 */
	public boolean doEntityTurn(Game game) {
		Entity minimumTimeEntity = null;
		int minimumTime = 0;
		for (Entity entity : entities) {
			if (minimumTimeEntity == null || entity.time < minimumTime) {
				minimumTimeEntity = entity;
				minimumTime = entity.time;
			}
		}
		if (minimumTimeEntity != null) {
			Entity targetEntity = minimumTimeEntity;
			// Find and do action
			// System.out.print(targetEntity.toString() + " - time: " + targetEntity.time);
			Optional<Action<?>> action = targetEntity.getAction(game.level);
			action.ifPresent((a) -> a.execute(game));
			// System.out.println(action.map((v) -> " - action: " + v.toString() + " - new time: " + targetEntity.time).orElse(" - no action"));
			return action.isPresent();
		} else return false;
	}
	public boolean isLocVisible(int x1, int y1, int x2, int y2) {
		if (((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) > 64) return false;
		int[][] points = LinePoints.get_line(new int[] { x1, y1 }, new int[] { x2, y2 });
		for (int i = 1; i < points.length - 1; i++) {
			Tile state = this.tiles[points[i][0]][points[i][1]];
			if (! state.getData().canSeeThrough) return false;
		}
		return true;
	}
	public int[][] findPath(int x1, int y1, int x2, int y2) {
		// Create the board
		int[][] board = new int[this.tiles.length][this.tiles[0].length];
		for (int x = 0; x < this.tiles.length; x++) {
			for (int y = 0; y < this.tiles[0].length; y++) {
				Tile state = this.tiles[x][y];
				boolean canWalkOn = state.getData().collisionType == TileType.CollisionType.NORMAL;
				board[x][y] = canWalkOn ? 1 : 0;
			}
		}
		// Find path
		return Pathfinding.findPath(board, new int[] { x1, y1 }, new int[] { x2, y2 }, true);
	}
}
