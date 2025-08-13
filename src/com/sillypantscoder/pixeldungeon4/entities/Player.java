package com.sillypantscoder.pixeldungeon4.entities;

import com.sillypantscoder.utils.JSON;

public class Player extends LivingEntity {
	public String playerID;
	public Player(String playerID, int time, int x, int y) {
		super(time, x, y, 10);
	}
	public boolean takeTurn() {
		return false;
	}
	public JSON.JObject<?> serialize() {
		// TODO: Better JSON
		return JSON.JObject.create(new String[] {
			"id", "type", "x", "y", "health", "maxHealth"
		}, new JSON.JValue[] {
			new JSON.JNumber(this.id),
			new JSON.JString("player"),
			new JSON.JNumber(this.x),
			new JSON.JNumber(this.y),
			new JSON.JNumber(this.health),
			new JSON.JNumber(this.maxHealth)
		});
	}
}
