package com.sillypantscoder.pixeldungeonlevelgen;

import com.sillypantscoder.utils.Random;

public enum Direction {
	UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
	public int x;
	public int y;
	private Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Direction opposite() {
		if (this == UP) return DOWN;
		if (this == DOWN) return UP;
		if (this == LEFT) return RIGHT;
		if (this == RIGHT) return LEFT;
		throw new IllegalArgumentException("Invalid direction");
	}
	public int[][] threeOpposites() {
		if (this == UP) return new int[][] { new int[] { -1, -1 }, new int[] { 0, -1 }, new int[] { 1, -1 } };
		if (this == DOWN) return new int[][] { new int[] { -1, 1 }, new int[] { 0, 1 }, new int[] { 1, 1 } };
		if (this == LEFT) return new int[][] { new int[] { -1, -1 }, new int[] { -1, 0 }, new int[] { -1, 1 } };
		if (this == RIGHT) return new int[][] { new int[] { 1, -1 }, new int[] { 1, 0 }, new int[] { 1, 1 } };
		throw new IllegalArgumentException("Invalid direction");
	}
	public Direction randomButNotThisDirection() {
		Direction newDirection = Random.choice(values());
		while (newDirection == this) {
			newDirection = Random.choice(values());
		}
		return newDirection;
	}
	public Direction random90degTurn() {
		if (this == UP) return Random.choice(new Direction[] { LEFT, RIGHT });
		if (this == DOWN) return Random.choice(new Direction[] { LEFT, RIGHT });
		if (this == LEFT) return Random.choice(new Direction[] { UP, DOWN });
		if (this == RIGHT) return Random.choice(new Direction[] { UP, DOWN });
		throw new IllegalArgumentException("Invalid direction");
	}
}
