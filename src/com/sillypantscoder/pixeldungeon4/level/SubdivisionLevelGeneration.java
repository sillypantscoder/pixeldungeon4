package com.sillypantscoder.pixeldungeon4.level;

import java.util.ArrayList;

import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Rect;

/**
 * Generate a level by starting with a large square and subdividing it.
 */
public class SubdivisionLevelGeneration {
	public static void main(String[] args) {
		Tile[][] level = generateLevel(60).tiles;
		// Print level as ASCII art
		for (int y = 0; y < level.length; y++) {
			for (int x = 0; x < level[y].length; x++) {
				if (level[x][y].state.equals("wall")) System.out.print("#");
				else System.out.print(".");
			}
			System.out.println();
		}
	}
	public static ArrayList<Rect> getDividedRects(int worldSize, int minRoomSize, int maxRoomSize) {
		ArrayList<Rect> rectsToDivide = new ArrayList<Rect>();
		ArrayList<Rect> resultRects = new ArrayList<Rect>();
		rectsToDivide.add(new Rect(0, 0, worldSize, worldSize));
		// Divide
		while (rectsToDivide.size() > 0) {
			@SuppressWarnings("unchecked")
			ArrayList<Rect> loopRects = (ArrayList<Rect>)(rectsToDivide.clone());
			rectsToDivide = new ArrayList<Rect>();
			for (int i = 0; i < loopRects.size(); i++) {
				Rect r = loopRects.get(i);
				boolean finishRect = false;
				// If the rect has no area, remove it
				if (r.w == 0 || r.h == 0) {
					// Aaaaaa!
					rectsToDivide.remove(r);
					continue;
				}
				// If the rect is too small to be divided further, finish it
				if (r.w < minRoomSize || r.h < minRoomSize) finishRect = true;
				// If the rect is generally small enough, finish it
				if (r.w < maxRoomSize && r.h < maxRoomSize) finishRect = true;
				// Handle the rect
				if (finishRect) {
					rectsToDivide.remove(r);
					resultRects.add(r);
				} else {
					// Divide the rect
					Rect[] newRects = r.subdivide();
					for (int n = 0; n < newRects.length; n++) {
						rectsToDivide.add(newRects[n]);
					}
				}
			}
		}
		return resultRects;
	}
	public static void addDoor(Level level, Rect rect) {
		int nDoors = 1 + (int)(Math.round(Math.random() * 2));
		for (int d = 0; d < nDoors; d++) {
			int doorSide = (int)(Math.round(Math.random() * 3));
			int doorX = 0;
			int doorY = 0;
			switch (doorSide) {
				case 0:
					// Top
					doorX = Random.randint((rect.left() * 2) + 1, (rect.right() * 2) - 1);
					doorY = rect.top() * 2;
					level.tiles[doorX][doorY].state = "door_closed";
					break;
				case 1:
					// Bottom
					doorX = Random.randint((rect.left() * 2) + 1, (rect.right() * 2) - 1);
					doorY = rect.bottom() * 2;
					level.tiles[doorX][doorY].state = "door_closed";
					break;
				case 2:
					// Left
					doorX = rect.left() * 2;
					doorY = Random.randint((rect.top() * 2) + 1, (rect.bottom() * 2) - 1);
					level.tiles[doorX][doorY].state = "door_closed";
					break;
				case 3:
					// Right
					doorX = rect.right() * 2;
					doorY = Random.randint((rect.top() * 2) + 1, (rect.bottom() * 2) - 1);
					level.tiles[doorX][doorY].state = "door_closed";
					break;
			}
		}
	}
	public static Level generateLevel(int worldSize) {
		worldSize /= 2;
		ArrayList<Rect> resultRects = getDividedRects(worldSize, 3, 7);
		// Create floor
		Level level = new Level((worldSize * 2) + 1, (worldSize * 2) + 1);
		for (int x = 0; x <= worldSize * 2; x++) {
			for (int y = 0; y <= worldSize * 2; y++) {
				level.tiles[x][y].state = "normal";
			}
		}
		// Draw the rooms
		for (int i = 0; i < resultRects.size(); i++) {
			// Top
			for (int x = resultRects.get(i).left() * 2; x < resultRects.get(i).right() * 2; x++) level.tiles[x][resultRects.get(i).top() * 2].state = "wall";
			// Bottom
			for (int x = resultRects.get(i).left() * 2; x < resultRects.get(i).right() * 2; x++) level.tiles[x][resultRects.get(i).bottom() * 2].state = "wall";
			// Left
			for (int y = resultRects.get(i).top() * 2; y < resultRects.get(i).bottom() * 2; y++) level.tiles[resultRects.get(i).left() * 2][y].state = "wall";
			// Right
			for (int y = resultRects.get(i).top() * 2; y < resultRects.get(i).bottom() * 2; y++) level.tiles[resultRects.get(i).right() * 2][y].state = "wall";
			// Add doors
			addDoor(level, resultRects.get(i));
		}
		// Draw the border
		for (int i = 0; i < level.tiles.length; i++) {
			level.tiles[i][0].state = "wall";
			level.tiles[i][level.tiles[i].length - 1].state = "wall";
			level.tiles[0][i].state = "wall";
			level.tiles[level.tiles.length - 1][i].state = "wall";
		}
		// Finish
		return level;
	}
}
