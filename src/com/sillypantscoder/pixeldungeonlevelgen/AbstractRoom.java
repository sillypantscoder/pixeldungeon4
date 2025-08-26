package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.Random;

public abstract class AbstractRoom {
	public ArrayList<Door> doors;
	public AbstractRoom() {
		this.doors = new ArrayList<Door>();
	}
	public boolean hasAnyDoor(int x, int y) {
		return this.hasFreeDoor(x, y) || this.hasClosedDoor(x, y);
	}
	public boolean hasAnyDoorNearby(int x, int y) {
		return this.hasAnyDoor(x, y) ||
				this.hasAnyDoor(x+1, y) ||
				this.hasAnyDoor(x-1, y) ||
				this.hasAnyDoor(x, y+1) ||
				this.hasAnyDoor(x, y-1);
	}
	public boolean hasFreeDoor(int x, int y) {
		return this.doors.stream().anyMatch((p) -> p.freeDirection.isPresent() && p.x == x && p.y == y);
	}
	public boolean hasClosedDoor(int x, int y) {
		return this.doors.stream().anyMatch((p) -> p.freeDirection.isEmpty() && p.x == x && p.y == y);
	}
	public void closeDoor(int x, int y) {
		for (int i = 0; i < this.doors.size(); i++) {
			Door door = this.doors.get(i);
			if (door.x == x && door.y == y) {
				door.freeDirection = Optional.empty();
			}
		}
	}
	public void move(int dx, int dy) {
		for (Door d : this.doors) {
			d.x += dx;
			d.y += dy;
		}
	}
	public void drawWalls(Level level) {
		int[][] positions = this.getCoveredPositions();
		for (int[] pos : positions) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					level.tiles[pos[0]+dx][pos[1]+dy].state = "wall";
				}
			}
		}
	}
	public void drawGround(Level level) {
		int[][] positions = this.getCoveredPositions();
		for (int[] pos : positions) {
			level.tiles[pos[0]][pos[1]].state = "normal";
		}
	}
	public void drawDoors(Level level) {
		for (Door d : this.doors) {
			level.tiles[d.x][d.y].state = "door_closed";
		}
	}
	public abstract boolean isValid(boolean strict);
	public abstract int[][] getCoveredPositions();
	public boolean intersectsWith(AbstractRoom other) {
		int[][] thisPositions = this.getCoveredPositions();
		int[][] otherPositions = other.getCoveredPositions();
		for (int i = 0; i < thisPositions.length; i++) {
			for (int j = 0; j < otherPositions.length; j++) {
				if (Math.abs(thisPositions[i][0] - otherPositions[j][0]) <= 1 && Math.abs(thisPositions[i][1] - otherPositions[j][1]) <= 1) return true;
			}
		}
		return false;
	}
	public static AbstractRoom generateOnDoor(Door door) {
		if (Math.random() < 0.4 && door.canConnectToCorridor) return Corridor.generateOnDoor(door);
		return Room.generateOnDoor(door, (int)(Random.triangular(3, 8, 9)), (int)(Random.triangular(3, 8, 9)));
	}
}
