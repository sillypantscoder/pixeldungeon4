package com.sillypantscoder.pixeldungeon4.entities;

public abstract class LivingEntity extends TileEntity {
	public int health;
	public int maxHealth;
	public LivingEntity(int time, int x, int y, int health) {
		super(time, x, y);
		this.health = health;
		this.maxHealth = health;
	}
}
