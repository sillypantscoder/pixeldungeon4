package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.Optional;

public class Door {
	public int x;
	public int y;
	public Optional<Direction> freeDirection;
	public int attempts = 10;
	public Door(int x, int y, Direction freeDirection) {
		this.x = x;
		this.y = y;
		this.freeDirection = Optional.ofNullable(freeDirection);
	}
}
