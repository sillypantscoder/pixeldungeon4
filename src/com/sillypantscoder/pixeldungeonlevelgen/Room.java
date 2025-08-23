package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Rect;

public class Room {
	public Rect rect;
	public ArrayList<Door> doors;
	public RoomType type;
	public Room(Rect rect, RoomType type) {
		this.rect = rect;
		this.doors = new ArrayList<Door>();
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
		return new Room(rect, RoomType.NORMAL);
	}
	public boolean equals(Object other) {
		if (other instanceof Room otherRoom) {
			return this.rect.equals(otherRoom.rect);
		}
		return false;
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
	public void addDoor(int[] position, Direction direction) {
		if (this.hasAnyDoor(position[0], position[1])) return;
		this.doors.add(new Door(position[0], position[1], direction));
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
	public void closeDoor(int x, int y) {
		for (int i = 0; i < this.doors.size(); i++) {
			Door door = this.doors.get(i);
			if (door.x == x && door.y == y) {
				door.freeDirection = Optional.empty();
			}
		}
	}
	public void draw(Level level) {
		for (int x = this.rect.left(); x <= this.rect.right(); x++) {
			for (int y = this.rect.top(); y <= this.rect.bottom(); y++) {
				// Draw doors
				if (this.hasClosedDoor(x, y)) level.tiles[x][y].state = "door_closed";
				// Don't overwrite other rooms
				else if (! level.tiles[x][y].state.equals("none")) continue;
				// Draw walls
				else if (x == this.rect.left() || x == this.rect.right() || y == this.rect.top() || y == this.rect.bottom()) level.tiles[x][y].state = "wall";
				// Draw floor
				else level.tiles[x][y].state = "normal";
			}
		}
	}
	public static enum RoomType {
		NORMAL, START, END
	}
}
