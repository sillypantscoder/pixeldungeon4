package com.sillypantscoder.pixeldungeon4.entities;

import com.sillypantscoder.utils.JSONObject;

public abstract class LivingEntity extends TileEntity {
	public int health;
	public int maxHealth;
	public LivingEntity(int time, int x, int y, int health) {
		super(time, x, y);
		this.health = health;
		this.maxHealth = health;
	}
	public JSONObject serialize() {
		JSONObject obj = super.serialize();
		obj.set("health", this.health);
		obj.set("maxHealth", this.maxHealth);
		return obj;
	}
}
