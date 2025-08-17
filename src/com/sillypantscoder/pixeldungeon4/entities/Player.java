package com.sillypantscoder.pixeldungeon4.entities;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.JSONObject;

public class Player extends LivingEntity {
	public String playerID;
	public Optional<PathfindingTarget> target;
	public Consumer<String[]> sendMessage;
	public Player(String playerID, int time, int x, int y, Consumer<String[]> sendMessage) {
		super(time, x, y, 10);
		this.playerID = playerID;
		this.target = Optional.empty();
		this.sendMessage = sendMessage;
	}
	public String getTypeID() { return "player"; }
	public void sendVision(Level level) {
		// Tiles
		ArrayList<String> vision = new ArrayList<String>();
		vision.add("show_tiles");
		for (int y = 0; y < level.tiles[0].length; y++) {
			for (int x = 0; x < level.tiles.length; x++) {
				if (! level.isLocVisible(this.x, this.y, x, y)) continue;
				vision.add(x + " " + y + " " + level.tiles[x][y].state);
			}
		}
		this.sendMessage.accept(vision.toArray(new String[0]));
		// Entities
		for (Entity e : level.entities) {
			if (e instanceof TileEntity tileEntity) {
				if (! level.isLocVisible(this.x, this.y, tileEntity.x, tileEntity.y)) continue;
				String[] data = new String[] {
					"create_entity",
					tileEntity.serialize().toString()
				};
				this.sendMessage.accept(data);
			}
		}
	}
	public Optional<Action<?>> getAction(Level level) {
		// Update target
		this.target = this.target.map((v) -> v.update(level, this.x, this.y));
		AtomicReference<Optional<Action<?>>> action = new AtomicReference<Optional<Action<?>>>(Optional.empty());
		// Check if we are at target
		if (this.target.map((v) -> {
			if (v.getX() != this.x) return false;
			if (v.getY() != this.y) return false;
			return true;
		}).orElse(false)) {
			this.target = Optional.empty();
			this.sendMessage.accept(new String[] { "clear_target" });
		}
		// Pathfind
		this.target.ifPresent((v) -> {
			int[][] path = level.findPath(this.x, this.y, v.getX(), v.getY());
			if (path.length == 0) {
				this.target = Optional.empty();
				this.sendMessage.accept(new String[] { "clear_target" });
				return;
			}
			action.set(Optional.of(new Action.MoveAction(this, 1, path[1][0], path[1][1])));
		});
		return action.get();
	}
	public void setTarget(Level level, int x, int y) {
		this.target = Optional.empty();
		for (Entity e : level.entities) {
			if (e instanceof TileEntity t) {
				if (t.x == x && t.y == y) {
					this.target = Optional.of(t);
				}
			}
		}
		if (this.target.isEmpty()) {
			this.target = Optional.of(new PathfindingTarget.StaticPosition(x, y));
		}
	}
	public JSONObject serialize() {
		JSONObject obj = super.serialize();
		obj.set("health", this.health);
		obj.set("maxHealth", this.maxHealth);
		return obj;
	}
}
