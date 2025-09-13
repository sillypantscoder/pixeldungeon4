package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;
import java.util.Arrays;
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
		Tile[][] level = generateLevel(45).tiles;
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
	public static void createPatchOval(Level l, String[] replaceables, String[] rare_replaceables, String tile, double cx, double cy, double w, double h) {
		for (int x = 0; x < l.tiles.length; x++) {
			for (int y = 0; y < l.tiles.length; y++) {
				if (x < 0 || y < 0 || x >= l.tiles.length || y >= l.tiles[0].length) continue;
				if (! (
					Arrays.asList(replaceables).contains(l.tiles[x][y].state) ||
					(Arrays.asList(rare_replaceables).contains(l.tiles[x][y].state) && Math.random() < 0.05)
				)) continue;
				double xDiff = Math.pow(x - cx, 2) / (w * w);
				double yDiff = Math.pow(y - cy, 2) / (h * h);
				if (xDiff + yDiff < 1) {
					l.tiles[x][y].state = tile;
				}
			}
		}
	}
	public static void createGrassPatch(Level l, int x, int y) {
		int patches = Random.randint(1, 4);
		int shortPatches = Random.randint(0, patches);
		for (int i = 0; i < patches; i++) {
			String fillTile = "grass_tall";
			if (i < shortPatches) fillTile = "grass_short";
			createPatchOval(l, new String[] { "normal" }, new String[] { "door_closed" }, fillTile,
				Random.randfloat(x - 1, x + 1),
				Random.randfloat(x - 1, x + 1),
				Random.randfloat(1, 2.5),
				Random.randfloat(1, 2.5)
			);
		}
	}
	public static ArrayList<AbstractRoom> generateRooms(int worldSize) {
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
		ArrayList<AbstractRoom> rooms = new ArrayList<AbstractRoom>();
		{
			Room startRoom = new Room(new Rect(0, 0, (int)(Random.triangular(4, 8, 9)), (int)(Random.triangular(4, 8, 9))));
			rooms.add(startRoom);
			startRoom.addDoors(getNumberOfDoors.get());
		}
		// Generation Loop
		while (true) {
			// Choose a random free door
			ArrayList<Door> freeDoors = new ArrayList<Door>();
			for (AbstractRoom r : rooms) {
				for (Door door : r.doors) {
					if (door.freeDirection.isPresent()) freeDoors.add(door);
				}
			}
			if (freeDoors.size() == 0) break;
			Door selectedDoor = Random.choice(freeDoors);
			// Generate a room here
			AbstractRoom r = AbstractRoom.generateOnDoor(selectedDoor);
			// Add the selected door to the new room as closed
			Door copyDoor = new Door(selectedDoor.x, selectedDoor.y, null);
			r.doors.add(copyDoor);
			// Ensure this room does not intersect any other rooms
			boolean validRoom = true;
			for (AbstractRoom r2 : rooms) {
				if (r.intersectsWith(r2)) validRoom = false;
			}
			if (! r.isValid(false)) validRoom = false;
			if (! validRoom) {
				selectedDoor.attempts -= 1;
				if (selectedDoor.attempts <= 0) {
					// Remove this door
					for (AbstractRoom sourceRoom : rooms) {
						for (Door d : new ArrayList<Door>(sourceRoom.doors)) {
							if (d.x == selectedDoor.x && d.y == selectedDoor.y) {
								sourceRoom.doors.remove(d);
							}
						}
					}
					doorsLeft.set(doorsLeft.get() + 0.5d);
				}
				continue;
			}
			// Save room and close attached door
			rooms.add(r);
			selectedDoor.freeDirection = Optional.empty();
			// Add some doors to the new room
			if (r instanceof Room _r) _r.addDoors(getNumberOfDoors.get());
		}
		// Remove rooms that became invalid
		for (AbstractRoom r : new ArrayList<AbstractRoom>(rooms)) {
			if (! r.isValid(true)) {
				// Remove all doors
				for (Door removeDoor : r.doors) {
					for (AbstractRoom checkRoom : rooms) {
						if (checkRoom == r) continue;
						for (Door d : new ArrayList<Door>(checkRoom.doors)) {
							if (d.x == removeDoor.x && d.y == removeDoor.y) {
								checkRoom.doors.remove(d);
							}
						}
					}
				}
				// Remove this room
				rooms.remove(r);
			}
		}
		// Find start and end rooms
		{
			ArrayList<Room> candidates = new ArrayList<Room>();
			for (AbstractRoom r : rooms) {
				if (r instanceof Room r2) candidates.add(r2);
			}
			// Reference room
			Room centerRoom = Random.choice(candidates);
			// Start room
			ArrayList<Double> weights = new ArrayList<Double>();
			for (Room r : candidates) {
				double distanceX = (r.rect.x + (r.rect.w / 2)) - (centerRoom.rect.x + (centerRoom.rect.w / 2));
				double distanceY = (r.rect.y + (r.rect.h / 2)) - (centerRoom.rect.y + (centerRoom.rect.h / 2));
				double distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
				weights.add(distanceSquared);
			}
			Room startingRoom = Random.choice(candidates, weights);
			// Setup start room
			startingRoom.type = RoomType.ENTRY;
			candidates.remove(startingRoom);
			// End room
			weights = new ArrayList<Double>();
			for (Room r : candidates) {
				double distanceX = (r.rect.x + (r.rect.w / 2)) - (startingRoom.rect.x + (startingRoom.rect.w / 2));
				double distanceY = (r.rect.y + (r.rect.h / 2)) - (startingRoom.rect.y + (startingRoom.rect.h / 2));
				double distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
				weights.add(distanceSquared);
			}
			Room endingRoom = Random.choice(candidates, weights);
			// Setup end room
			endingRoom.type = RoomType.EXIT;
		}
		// Return room list
		return rooms;
	}
	public static Level generateLevel(int worldSize) {
		ArrayList<AbstractRoom> rooms = generateRooms(worldSize);
		// Move rooms so no coordinates are negative
		{
			int minX = 0;
			int minY = 0;
			for (AbstractRoom r : rooms) {
				int[][] positions = r.getCoveredPositions();
				for (int[] pos : positions) {
					if (pos[0] - 1 < minX) minX = pos[0] - 1;
					if (pos[1] - 1 < minY) minY = pos[1] - 1;
				}
			}
			for (AbstractRoom r : rooms) {
				r.move(-minX, -minY);
			}
		}
		// Initialize level
		int maxX = 0;
		int maxY = 0;
		for (AbstractRoom r : rooms) {
			int[][] positions = r.getCoveredPositions();
			for (int[] pos : positions) {
				if (pos[0] + 1 > maxX) maxX = pos[0] + 1;
				if (pos[1] + 1 > maxY) maxY = pos[1] + 1;
			}
		}
		Level level = new Level(maxX + 1, maxY + 1);
		// Draw the rooms
		for (AbstractRoom r : rooms) {
			r.drawWalls(level);
		}
		for (AbstractRoom r : rooms) {
			r.drawGround(level);
		}
		for (AbstractRoom r : rooms) {
			r.drawDoors(level);
		}
		// Decorations
		for (AbstractRoom r : rooms) {
			for (int[] pos : r.getCoveredPositions()) {
				if (Math.random() < 0.06) {
					createGrassPatch(level, pos[0], pos[1]);
				}
			}
		}
		for (AbstractRoom r : rooms) {
			r.drawDecorations(level);
		}
		// Finish
		return level;
	}
}
