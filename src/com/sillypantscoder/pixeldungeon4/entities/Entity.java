package com.sillypantscoder.pixeldungeon4.entities;

public abstract class Entity {
	public int time;
	public Entity(int time) {
		this.time = time;
	}
	public abstract boolean takeTurn();
}
