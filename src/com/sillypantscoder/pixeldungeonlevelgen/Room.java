package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Rect;

public class Room extends AbstractRoom {
	public Rect rect;
	public RoomType type;
	public Room(Rect rect) {
		super();
		this.rect = rect;
		this.type = RoomType.NORMAL;
	}
	public static Room generateOnDoor(Door d, int width, int height) {
		Direction direction = d.freeDirection.orElseThrow();
		// Generate rectangle
		Rect rect;
		if (direction == Direction.UP) {
			int offset = Random.randint(1 - width, -1);
			rect = new Rect(d.x + offset, d.y - height, width, height);
		} else if (direction == Direction.DOWN) {
			int offset = Random.randint(1 - width, -1);
			rect = new Rect(d.x + offset, d.y, width, height);
		} else if (direction == Direction.LEFT) {
			int offset = Random.randint(1 - height, -1);
			rect = new Rect(d.x - width, d.y + offset, width, height);
		} else if (direction == Direction.RIGHT) {
			int offset = Random.randint(1 - height, -1);
			rect = new Rect(d.x, d.y + offset, width, height);
		} else {
			throw new IllegalArgumentException("Invalid direction");
		}
		// Create room
		return new Room(rect);
	}
	public void addDoor() {
		// Add a door at a random position on the border
		ArrayList<Door> possibleDoors = new ArrayList<Door>();
		// Top and bottom borders
		for (int x = this.rect.left() + 1; x < this.rect.right(); x++) {
			if (!this.hasAnyDoorNearby(x, this.rect.top())) possibleDoors.add(new Door(x, this.rect.top(), Direction.UP)); // door does not generate
			if (!this.hasAnyDoorNearby(x, this.rect.bottom())) possibleDoors.add(new Door(x, this.rect.bottom(), Direction.DOWN));
		}
		// Left and right borders
		for (int y = this.rect.top() + 1; y < this.rect.bottom(); y++) {
			if (!this.hasAnyDoorNearby(this.rect.left(), y)) possibleDoors.add(new Door(this.rect.left(), y, Direction.LEFT)); // door does not generate
			if (!this.hasAnyDoorNearby(this.rect.right(), y)) possibleDoors.add(new Door(this.rect.right(), y, Direction.RIGHT));
		}
		// Return a random door
		if (possibleDoors.size() == 0) return;
		Door door = possibleDoors.get((int)(Math.random() * possibleDoors.size()));
		this.doors.add(door);
		// System.out.println("added door with direction: " + door.freeDirection.orElse(null));
	}
	public void addDoors(int amount) {
		for (int i = 0; i < amount; i++) {
			this.addDoor();
		}
	}
	public void drawDecorations(Level level) {
		this.type.drawDecoration(level, this.rect.x + (this.rect.w / 2), this.rect.y + (this.rect.h / 2));
	}
	public void move(int dx, int dy) {
		super.move(dx, dy);
		this.rect.x += dx;
		this.rect.y += dy;
	}
	public int[][] getCoveredPositions() {
		ArrayList<int[]> positions = new ArrayList<int[]>();
		for (int x = this.rect.left() + 1; x < this.rect.right(); x++) { // " + 1" and "<" are because we don't want to include
			for (int y = this.rect.top() + 1; y < this.rect.bottom(); y++) { // wall positions in the resulting array
				positions.add(new int[] { x, y });
			}
		}
		int[][] out = new int[positions.size()][2];
		for (int i = 0; i < positions.size(); i++) {
			out[i][0] = positions.get(i)[0];
			out[i][1] = positions.get(i)[1];
		}
		return out;
	}
	public boolean isValid(boolean strict) { return true; }
}
