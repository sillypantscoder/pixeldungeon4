package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.Rect;

public class Room {
	public Rect rect;
	public ArrayList<int[]> doors;
	public Room(Rect rect) {
		this.rect = rect;
		this.doors = new ArrayList<int[]>();
	}
	public boolean equals(Object other) {
		if (other instanceof Room otherRoom) {
			return this.rect.equals(otherRoom.rect);
		}
		return false;
	}
	public boolean hasDoor(int x, int y) {
		return this.doors.stream().anyMatch((p) -> p[0] == x && p[1] == y);
	}
	public void addDoors(int[][] possiblePositions) {
		for (int[] point : possiblePositions) {
			if (! this.rect.isPointOnBorder(point[0], point[1])) continue;
			if (this.hasDoor(point[0], point[1])) continue;
			this.doors.add(point);
		}
	}
	public void draw(Level level) {
		for (int x = this.rect.left(); x <= this.rect.right(); x++) {
			for (int y = this.rect.top(); y <= this.rect.bottom(); y++) {
				if (this.hasDoor(x, y)) level.tiles[x][y].state = "door_closed";
				else if (x == this.rect.left() || x == this.rect.right() || y == this.rect.top() || y == this.rect.bottom()) level.tiles[x][y].state = "wall";
				else level.tiles[x][y].state = "normal";
			}
		}
	}
}
