package com.sillypantscoder.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Main A* pathfinding class.
 * https://github.com/Qualia91/AStarAlg
 *
 */
public class PathfindingNew {
	public static void main(String[] args) {
		int[][] board = new int[][] {
			new int[] { 1, 1, 5, 0 },
			new int[] { 1, 1, 5, 5 },
			new int[] { 1, 1, 5, 5 },
			new int[] { 0, 1, 1, 1 }
		};
		int[][] route = PathfindingNew.findPath(board, new int[] { 0, 0 }, new int[] { 3, 3 }, false);
		// Print paths
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < board[0].length; y++) {
					boolean alreadyHasPos = false;
					for (int[] pos : route) {
						if (pos[0] == x && pos[1] == y) {
							System.out.print("..");
							alreadyHasPos = true;
						}
					}
					if (alreadyHasPos) continue;
					System.out.print("#123456789ABCDEFGHIJK".charAt(Math.round(board[x][y])));
					System.out.print("#123456789ABCDEFGHIJK".charAt(Math.round(board[x][y])));
				}
				System.out.println();
			}
			System.out.println("\n");
		for (int[] pos : route) {
			System.out.println(pos[0] + ", " + pos[1]);
		}
		System.out.println(route);
	}
	public static class Tile {
		public int x;
		public int y;
		public int tileWeight;
		public int score;
		public Tile parent;
		public boolean open;
		public Tile(int tileWeight) {
			this.x = -1;
			this.y = -1;
			this.tileWeight = tileWeight;
			this.score = 0;
			this.parent = null;
			this.open = true;
		}
	}

	private Tile[][] tiles;
	private Tile currentTile;
	private int[] endPosition;
	private int maxWidth;
	private int maxHeight;

	public static int[][] findPath(int[][] board, int[] startPosition, int[] endPosition, boolean allowDiagonals) {
		PathfindingNew p = new PathfindingNew();
		return p.getRoute(board, startPosition, endPosition, allowDiagonals);
	}
	public int[][] getRoute(int[][] board, int[] startPosition, int[] endPosition, boolean allowDiagonals) {
		Tile[][] tiles = new Tile[board.length][board[0].length];
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board.length; y++) {
				tiles[x][y] = new Tile(board[x][y]);
			}
		}
		List<Tile> path = getRoute(tiles, startPosition, endPosition, allowDiagonals);
		int[][] coordinates = new int[path.size()][2];
		for (int i = 0; i < path.size(); i++) {
			if (i % 2 == 0) {
				coordinates[path.size() - (i + 1)][0] = path.get(i).x;
				coordinates[path.size() - (i + 1)][1] = path.get(i).y;
			} else {
				coordinates[path.size() - (i + 1)][0] = path.get(i).y;
				coordinates[path.size() - (i + 1)][1] = path.get(i).x;
			}
		}
		return coordinates;
	}

	public List<Tile> getRoute(Tile[][] tiles, int[] startPosition, int[] endPosition, boolean allowDiagonals) {
		this.tiles = tiles;
		this.maxWidth = tiles.length;
		this.maxHeight = tiles[0].length;
		this.endPosition = endPosition;

		resetAllTiles();

		PriorityQueue<Tile> queue = new PriorityQueue<>(new Comparator<Tile>() {
			public int compare(Tile o1, Tile o2) {
				return o1.score - o2.score;
			}
		});
		queue.add(tiles[startPosition[0]][startPosition[1]]);

		boolean routeAvailable = false;

		while (!queue.isEmpty()) {

			do {
				if (queue.isEmpty()) break;
				currentTile = queue.remove();
			} while (!currentTile.open);

			currentTile.open = false;

			int currentX = currentTile.x;
			int currentY = currentTile.y;
			int currentScore = currentTile.score;

			if (currentTile.x == endPosition[0] && currentTile.y == endPosition[1]) {
				// at the end, return path
				routeAvailable = true;
				break;
			}

			// loop through neighbours and get scores. add these onto temp open list
			int smallestScore = Integer.MAX_VALUE;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					if (dx == 0 && dy == 0) continue; // skip self
					// Only allow diagonals if allowDiagonals is true
					if (Math.abs(dx) + Math.abs(dy) >= 2 && !allowDiagonals) continue;
					int nextX = currentX + dx;
					int nextY = currentY + dy;
					if (validTile(nextX, nextY)) {
						int score = getScoreOfTile(tiles[nextX][nextY], currentScore);
						if (score < smallestScore) {
							smallestScore = score;
						}
						Tile thisTile = tiles[nextX][nextY];
						thisTile.score = score;
						queue.add(thisTile);
						thisTile.parent = currentTile;
					}
				}
			}


		}

		// get List of tiles using current tile
		// returns reverse list btw
		if (routeAvailable) return getPath(currentTile);
		return new ArrayList<>();
	}

	private void resetAllTiles() {
		for (int row = 0; row < tiles.length; row++) {
			Tile[] tileRow = tiles[row];
			for (int col = 0; col < tiles[0].length; col++) {
				tileRow[col].x = col;
				tileRow[col].y = row;
				tileRow[col].open = true;
				tileRow[col].parent = null;
				tileRow[col].score = 0;
			}
		}
	}

	private List<Tile> getPath(Tile currentTile) {
		List<Tile> path = new ArrayList<>();
		while (currentTile != null) {
			path.add(currentTile);
			currentTile = currentTile.parent;
		}
		return path;
	}

	private int distanceScoreAway(Tile currentTile) {
		return Math.abs(endPosition[0] - currentTile.x) + Math.abs(endPosition[1] - currentTile.y);
	}

	private int getScoreOfTile(Tile tile, int currentScore) {
		int guessScoreLeft = distanceScoreAway(tile);
		int extraMovementCost = tile.tileWeight;
		int movementScore = currentScore + 1;
		return guessScoreLeft + movementScore + extraMovementCost;
	}

	private boolean validTile(int nextX, int nextY) {
		if (nextX >= 0 && nextX < maxWidth) {
			if (nextY >= 0 && nextY < maxHeight) {
				return tiles[nextX][nextY].open && tiles[nextX][nextY].tileWeight > 0;
			}
		}
		return false;
	}
}
