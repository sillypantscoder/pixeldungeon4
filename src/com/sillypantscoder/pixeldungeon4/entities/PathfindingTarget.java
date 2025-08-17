package com.sillypantscoder.pixeldungeon4.entities;

import com.sillypantscoder.pixeldungeon4.level.Level;

public interface PathfindingTarget {
	public int getX();
	public int getY();
	public static record StaticPosition(int x, int y) implements PathfindingTarget {
		public StaticPosition(PathfindingTarget target) { this(target.getX(), target.getY()); }
		public int getX() { return x; }
		public int getY() { return y; }
	}
	public default PathfindingTarget update(Level level, int sourceX, int sourceY) {
		boolean canSee = level.isLocVisible(this.getX(), this.getY(), sourceX, sourceY);
		if (canSee) return this;
		else return new StaticPosition(this);
	}
}
