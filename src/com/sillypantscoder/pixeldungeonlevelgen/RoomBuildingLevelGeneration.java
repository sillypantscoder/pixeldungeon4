package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.level.Tile;
import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Rect;

/**
 * Generate a level by starting with a large square and subdividing it.
 */
public class RoomBuildingLevelGeneration {
	public static void main(String[] args) {
		Tile[][] level = generateLevel(110).tiles;
		// Print level as ASCII art
		for (int y = 0; y < level[0].length; y++) {
			for (int x = 0; x < level.length; x++) {
				if (level[x][y].state.equals("wall")) System.out.print("##");
				else if (level[x][y].state.equals("normal")) System.out.print("__");
				else if (level[x][y].state.equals("door_closed")) System.out.print("][");
				else System.out.print("..");
			}
			System.out.println();
		}
	}
	public static class Pair<A, B> {
		public A a;
		public B b;
		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}
	public static Level generateLevel(int worldSize) {
		// Stop generating doors after we've generated a lot of rooms
		AtomicReference<Double> doorsLeft = new AtomicReference<Double>(worldSize / 2d);
		Supplier<Integer> getNumberOfDoors = () -> {
			if (doorsLeft.get() <= 0) return 0;
			int amount = (int)(Math.round(Random.randfloat(0, Math.sqrt(doorsLeft.get())*3d )));
			if (amount > 4) amount = 4;
			// System.out.print("D -> " + doorsLeft.get() + " -> " + amount + " -> ");
			doorsLeft.set(doorsLeft.get() - amount);
			// System.out.println(doorsLeft.get());
			return amount;
		};
		// Start with a single starting room and a few doors
		ArrayList<Room> rooms = new ArrayList<Room>();
		rooms.add(new Room(new Rect(0, 0, (int)(Random.triangular(3, 7.5, 9)), (int)(Random.triangular(3, 7.5, 9))), Room.RoomType.START));
		rooms.get(0).addDoors(getNumberOfDoors.get());
		// Generation Loop
		while (true) {
			// Choose a random free door
			ArrayList<Door> freeDoors = new ArrayList<Door>();
			for (Room r : rooms) {
				for (Door door : r.doors) {
					if (door.freeDirection.isPresent()) freeDoors.add(door);
				}
			}
			if (freeDoors.size() == 0) break;
			Door selectedDoor = Random.choice(freeDoors);
			// Generate a room here
			Room r = Room.generateOnDoor(selectedDoor, (int)(Random.triangular(3, 7.5, 9)), (int)(Random.triangular(3, 7.5, 9)));
			// Ensure this room does not intersect any other rooms
			boolean validRoom = true;
			for (Room r2 : rooms) {
				if (r.rect.collideRect(r2.rect)) validRoom = false;
			}
			if (! validRoom) {
				selectedDoor.attempts -= 1;
				if (selectedDoor.attempts <= 0) {
					// Remove this door
					for (Room sourceRoom : rooms) {
						sourceRoom.doors.remove(selectedDoor);
					}
					doorsLeft.set(doorsLeft.get() + 0.5d);
				}
				continue;
			}
			// Save room and close attached door
			rooms.add(r);
			selectedDoor.freeDirection = Optional.empty();
			// Add the selected door to the new room as closed
			Door copyDoor = new Door(selectedDoor.x, selectedDoor.y, null);
			r.doors.add(copyDoor);
			// Add some doors to the new room
			r.addDoors(getNumberOfDoors.get());
		}
		// Move rooms so no coordinates are negative
		{
			int minX = 0;
			int minY = 0;
			for (Room r : rooms) {
				if (r.rect.x < minX) minX = r.rect.x;
				if (r.rect.y < minY) minY = r.rect.y;
			}
			for (Room r : rooms) {
				r.rect.x -= minX;
				r.rect.y -= minY;
				for (Door d : r.doors) {
					d.x -= minX;
					d.y -= minY;
				}
			}
		}
		// Initialize level
		int maxX = 0;
		int maxY = 0;
		for (Room r : rooms) {
			if (r.rect.x + r.rect.w > maxX) maxX = r.rect.x + r.rect.w;
			if (r.rect.y + r.rect.h > maxY) maxY = r.rect.y + r.rect.h;
		}
		Level level = new Level(maxX + 1, maxY + 1);
		// Draw the rooms
		for (Room r : rooms) {
			r.draw(level);
		}
		// // Draw the border
		// for (int i = 0; i < level.tiles.length; i++) {
		// 	level.tiles[i][0].state = "wall";
		// 	level.tiles[i][level.tiles[i].length - 1].state = "wall";
		// 	level.tiles[0][i].state = "wall";
		// 	level.tiles[level.tiles.length - 1][i].state = "wall";
		// }
		// Finish
		return level;
	}
}
