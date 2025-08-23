package com.sillypantscoder.pixeldungeonlevelgen;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.Random;

public class Corridor extends AbstractRoom {
	public int[][] positions;
	public Direction startDirection;
	public Direction endDirection;
	public Corridor(int[][] positions, Door startDoor, Door endDoor) {
		this.positions = positions;
		this.startDirection = startDoor.freeDirection.orElseThrow();
		this.endDirection = endDoor.freeDirection.orElseThrow();
		this.doors.add(endDoor);
		endDoor.attempts = 300;
		endDoor.canConnectToCorridor = false;
	}
	public static Corridor generateOnDoor(Door d) {
		// Generate corridor
		ArrayList<int[]> positions = new ArrayList<int[]>();
		int currentX = d.x;
		int currentY = d.y;
		int nSegments = Random.randint(2, 4);
		Direction currentDirection = d.freeDirection.orElseThrow();
		for (int i = 0; i < nSegments; i++) {
			// Generate a segment
			int length = Random.randint(3, 8);
			if (i != 0) currentDirection = currentDirection.random90degTurn();
			for (int j = 0; j < length; j++) {
				currentX += currentDirection.x;
				currentY += currentDirection.y;
				positions.add(new int[] { currentX, currentY });
			}
		}
		// End door
		Direction doorDirection = currentDirection.opposite().randomButNotThisDirection();
		Door door = new Door(currentX + doorDirection.x, currentY + doorDirection.y, doorDirection);
		// Create room
		return new Corridor(positions.toArray(new int[0][2]), d, door);
	}
	public void move(int dx, int dy) {
		super.move(dx, dy);
		for (int i = 0; i < this.positions.length; i++) {
			this.positions[i][0] += dx;
			this.positions[i][1] += dy;
		}
	}
	// public void drawWalls(Level level) {
	// 	if (this.doors.size() < 2) return;
	// 	super.drawWalls(level);
	// }
	// public void drawGround(Level level) {
	// 	if (this.doors.size() < 2) return;
	// 	super.drawGround(level);
	// }
	public void drawDoors(Level level) {
		// if (this.doors.size() < 2) return;
		super.drawDoors(level);
		// ensure start door
		int[] startPos = new int[] {
			this.positions[0][0] - this.startDirection.x,
			this.positions[0][1] - this.startDirection.y
		};
		level.tiles[startPos[0]][startPos[1]].state = "door_closed";
		// ensure end door
		int[] endPos = new int[] {
			this.positions[this.positions.length - 1][0] + this.endDirection.x,
			this.positions[this.positions.length - 1][1] + this.endDirection.y
		};
		level.tiles[endPos[0]][endPos[1]].state = "door_closed";
	}
	public int[][] getCoveredPositions() {
		return this.positions;
	}
	public boolean isValid(boolean strict) { if (strict && this.doors.get(0).freeDirection.isPresent()) return false; return this.doors.size() >= 2; }
}
