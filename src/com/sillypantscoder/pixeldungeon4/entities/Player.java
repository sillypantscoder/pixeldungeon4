package com.sillypantscoder.pixeldungeon4.entities;

import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.JSONObject;

public class Player extends LivingEntity {
	public String playerID;
	public Player(String playerID, int time, int x, int y) {
		super(time, x, y, 10);
		this.playerID = playerID;
	}
	public String getTypeID() { return "player"; }
	public Optional<Action<?>> getAction(Level level) {
		if (this.x >= level.tiles.length - 1) return Optional.empty();
		Action<TileEntity> action = new Action.MoveAction(this, 1, x+1, y);
		return Optional.of(action);
	}
	public JSONObject serialize() {
		JSONObject obj = super.serialize();
		obj.set("health", this.health);
		obj.set("maxHealth", this.maxHealth);
		return obj;
	}
}
