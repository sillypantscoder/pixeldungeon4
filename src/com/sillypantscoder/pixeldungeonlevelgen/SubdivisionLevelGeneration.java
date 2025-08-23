package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.level.Tile;
import com.sillypantscoder.utils.PathfindingNew;
import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Rect;

/**
 * Generate a level by starting with a large square and subdividing it.
 */
public class SubdivisionLevelGeneration {
	public static void main(String[] args) {
		Tile[][] level = generateLevel(110).tiles;
		// Print level as ASCII art
		for (int y = 0; y < level.length; y++) {
			for (int x = 0; x < level[y].length; x++) {
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
	public static ArrayList<Rect> getDividedRects(int worldSize, int minRoomSize, int maxRoomSize) {
		ArrayList<Rect> rectsToDivide = new ArrayList<Rect>();
		ArrayList<Rect> resultRects = new ArrayList<Rect>();
		rectsToDivide.add(new Rect(0, 0, worldSize, worldSize));
		// Divide
		while (rectsToDivide.size() > 0) {
			Rect r = rectsToDivide.remove(0);
			boolean finishRect = false;
			// If the rect has no area, remove it
			if (r.w == 0 || r.h == 0) {
				// Aaaaaa!
				continue;
			}
			// If the rect is too small to be divided further, finish it
			if (r.w < minRoomSize || r.h < minRoomSize) finishRect = true;
			// If the rect is generally small enough, finish it
			if (r.w < maxRoomSize && r.h < maxRoomSize) finishRect = true;
			// Handle the rect
			if (finishRect) {
				resultRects.add(r);
			} else {
				// Divide the rect
				Rect[] newRects = r.subdivide();
				for (int n = 0; n < newRects.length; n++) {
					rectsToDivide.add(newRects[n]);
				}
			}
		}
		return resultRects;
	}
	public static Pair<Pair<int[], int[]>, Pair<Room, Room>> findStartAndEndPositions(int worldSize, BiFunction<Integer, Integer, List<Room>> getRoomsAtPos) {
		for (int iteration = 0; iteration < 100; iteration++) {
			int[] start = new int[] {
				(int)(Math.random() < 0.5 ? Random.triangular(0, worldSize * 0.25, worldSize) : Random.triangular(0, worldSize * 0.75, worldSize)),
				(int)(Math.random() < 0.5 ? Random.triangular(0, worldSize * 0.25, worldSize) : Random.triangular(0, worldSize * 0.75, worldSize))
			};
			int[] end = new int[] {
				(int)(start[0] > 0.5 ? Random.triangular(0, worldSize * 0.25, worldSize) : Random.triangular(0, worldSize * 0.75, worldSize)),
				(int)(start[1] > 0.5 ? Random.triangular(0, worldSize * 0.25, worldSize) : Random.triangular(0, worldSize * 0.75, worldSize))
			};
			{
				double distance = Math.sqrt(Math.pow(end[0] - start[0], 2) + Math.pow(end[1] - start[1], 2));
				if (distance < worldSize / 2d) continue;
			}
			List<Room> startRooms = getRoomsAtPos.apply(start[0], start[1]);
			List<Room> endRooms = getRoomsAtPos.apply(end[0], end[1]);
			if (startRooms.size() != 1) continue;
			if (endRooms.size() != 1) continue;
			Room startRoom = startRooms.get(0);
			Room endRoom = endRooms.get(0);
			// Return all the data
			return new Pair<Pair<int[], int[]>, Pair<Room, Room>>(new Pair<int[], int[]>(start, end), new Pair<Room, Room>(startRoom, endRoom));
		}
		throw new RuntimeException("Failed to find start and end positions");
	}
	public static Level generateLevel(int worldSize) {
		ArrayList<Room> rooms = new ArrayList<Room>(getDividedRects(worldSize, 7, 18).stream().map((v) -> new Room(v)).collect(Collectors.toList()));
		BiFunction<Integer, Integer, List<Room>> getRoomsAtPos = (x, y) -> {
			return rooms.stream().filter((r) -> {
				return r.rect.collidePoint(x, y);
			}).collect(Collectors.toList());
		};
		// Select start and end positions
		Pair<Pair<int[], int[]>, Pair<Room, Room>> startAndEndPositions = findStartAndEndPositions(worldSize, getRoomsAtPos);
		int[] start = startAndEndPositions.a.a;
		int[] end = startAndEndPositions.a.b;
		// Set-up pathfinding board
		double[][] board = new double[worldSize+1][worldSize+1];
		for (int x = 0; x <= worldSize; x++) {
			for (int y = 0; y <= worldSize; y++) {
				board[x][y] =
					( Math.min(Math.abs(x - (worldSize / 4d)), Math.abs(x - (3 * worldSize / 4d))) / worldSize ) +
					( Math.min(Math.abs(y - (worldSize / 4d)), Math.abs(y - (3 * worldSize / 4d))) / worldSize );
			}
		}
		// Helper function to find a path while avoiding two sets of points
		BiFunction<int[][], int[][], int[][]> findPath = (pointsToAvoid1, pointsToAvoid2) -> {
			Function<int[], Integer> getDistanceFromPointsToAvoid = (p) -> {
				int minDistance = Integer.MAX_VALUE;
				for (int[] point : pointsToAvoid1) {
					double distance = Math.sqrt(Math.pow(point[0] - p[0], 2) + Math.pow(point[1] - p[1], 2));
					if (distance < minDistance) minDistance = (int)(distance);
				}
				for (int[] point : pointsToAvoid2) {
					double distance = Math.sqrt(Math.pow(point[0] - p[0], 2) + Math.pow(point[1] - p[1], 2));
					if (distance < minDistance) minDistance = (int)(distance);
				}
				return minDistance;
			};
			int[][] pathfindingBoard = new int[worldSize+1][worldSize+1];
			for (int x = 0; x <= worldSize; x++) {
				for (int y = 0; y <= worldSize; y++) {
					int distance = getDistanceFromPointsToAvoid.apply(new int[] { x, y });
					int score = Math.max((20 - distance) + Random.randint(-5, 5), 1);
					pathfindingBoard[x][y] = score * score * score * 5;
				}
			}
			int[][] path = PathfindingNew.findPath(pathfindingBoard, start, end, false);
			// Print paths
			for (int y = 0; y < pathfindingBoard[0].length; y++) {
				for (int x = 0; x < pathfindingBoard.length; x++) {
					boolean alreadyHasPos = false;
					for (int[] pos : path) {
						if (pos[0] == x && pos[1] == y) {
							System.out.print("..");
							alreadyHasPos = true;
						}
					}
					if (alreadyHasPos) continue;
					int charIndex = Math.round(pathfindingBoard[x][y] / 100);
					if (charIndex >= 73) charIndex = 72;
					System.out.print("#112233445566778899AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRRSSTTUUVVWWXXYYZZ##".charAt(charIndex));
					System.out.print("#112233445566778899AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRRSSTTUUVVWWXXYYZZ##".charAt(charIndex));
				}
				System.out.println();
			}
			System.out.println("\n");
			return path;
		};
		// Find two paths
		int[][] edgePoints = new int[][] {
			new int[] { 0, 0 },
			new int[] { worldSize / 2, 0 },
			new int[] { worldSize, 0 },
			new int[] { worldSize, worldSize / 2 },
			new int[] { worldSize, worldSize },
			new int[] { worldSize / 2, worldSize },
			new int[] { 0, worldSize },
			new int[] { 0, worldSize / 2 }
		};
		int[][] firstPath = findPath.apply(edgePoints, new int[][] {
			new int[] { worldSize / 2, worldSize / 2 }
		});
		int[][] secondPath = findPath.apply(edgePoints, firstPath);
		// Create rooms at path positions
		HashSet<Room> activeRooms = new HashSet<Room>();
		for (int[] point : firstPath) {
			activeRooms.addAll(getRoomsAtPos.apply(point[0], point[1]));
		}
		for (int[] point : secondPath) {
			activeRooms.addAll(getRoomsAtPos.apply(point[0], point[1]));
		}
		// Create doors
		for (Room r : activeRooms) {
			r.addDoors(firstPath);
			r.addDoors(secondPath);
		}
		// Initialize level
		Level level = new Level((worldSize) + 1, (worldSize) + 1);
		// Draw the rooms
		for (Room r : activeRooms) {
			r.draw(level);
		}
		// for (int x = 0; x <= worldSize; x++) {
		// 	for (int y = 0; y <= worldSize; y++) {
		// 		level.tiles[x][y].state = "normal";
		// 	}
		// }
		// // Draw the rooms
		// for (Room room : activeRooms) {
		// 	// Top
		// 	for (int x = room.rect.left(); x < room.rect.right(); x++) level.tiles[x][room.rect.top()].state = "wall";
		// 	// Bottom
		// 	for (int x = room.rect.left(); x < room.rect.right(); x++) level.tiles[x][room.rect.bottom()].state = "wall";
		// 	// Left
		// 	for (int y = room.rect.top(); y < room.rect.bottom(); y++) level.tiles[room.rect.left()][y].state = "wall";
		// 	// Right
		// 	for (int y = room.rect.top(); y < room.rect.bottom(); y++) level.tiles[room.rect.right()][y].state = "wall";
		// 	// Add doors
		// 	// addDoor(level, resultRects.get(i));
		// 	// if (Math.random() < 0.5) addDoor(level, resultRects.get(i));
		// }
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
