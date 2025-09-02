package com.sillypantscoder.pixeldungeon4.entities;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.actions.AttackAction;
import com.sillypantscoder.pixeldungeon4.actions.MoveAction;
import com.sillypantscoder.pixeldungeon4.items.Item;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Random;

public class Player extends LivingEntity {
	public String playerID;
	public Optional<PathfindingTarget> target;
	public Consumer<String[]> sendMessage;
	public ArrayList<TileEntity> visibleEntities;
	public int healingTime;
	public ArrayList<Item> inventory;
	public Player(String playerID, int time, int x, int y, Consumer<String[]> sendMessage) {
		super(time, x, y, 20);
		this.playerID = playerID;
		this.target = Optional.empty();
		this.sendMessage = sendMessage;
		this.visibleEntities = new ArrayList<TileEntity>();
		this.healingTime = time + 10;
		this.inventory = new ArrayList<Item>();
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
				if (level.isLocVisible(this.x, this.y, tileEntity.x, tileEntity.y)) {
					// Create entity if it does not exist
					if (! this.visibleEntities.contains(e)) {
						this.visibleEntities.add(tileEntity);
						String[] data = new String[] {
							"create_entity",
							tileEntity.serialize(false).toString()
						};
						this.sendMessage.accept(data);
					}
				} else {
					// Remove entity if it exists
					if (this.visibleEntities.contains(e)) {
						this.visibleEntities.remove(e);
						String[] data = new String[] {
							"remove_entity",
							tileEntity.id + ""
						};
						this.sendMessage.accept(data);
					}
				}
			}
		}
	}
	public void sendInventory() {
		ArrayList<String> inv = new ArrayList<String>();
		inv.add("set_inventory");
		for (int i = 0; i < this.inventory.size(); i++) {
			Item item = this.inventory.get(i);
			inv.add(item.serialize().toString());
		}
		this.sendMessage.accept(inv.toArray(new String[0]));
	}
	public Optional<Action<?>> getAction(Level level) {
		// Healing
		if (this.time >= this.healingTime) {
			this.healingTime += 128;
			this.health += 1;
			if (this.health > this.maxHealth) this.health = this.maxHealth;
			level.updateEntityHealth.accept(this);
		}
		// Update target
		this.target = this.target.map((v) -> v.update(level, this.x, this.y));
		AtomicReference<Optional<Action<?>>> action = new AtomicReference<Optional<Action<?>>>(Optional.empty());
		// Check for target entity interaction
		this.target.ifPresent((v) -> {
			if (v.getX() == this.x && v.getY() == this.y) {
				if (v instanceof Dewdrop dewdrop) {
					this.target = Optional.empty();
					this.sendMessage.accept(new String[] { "clear_target" });
					action.set(dewdrop.collect(this));
				}
				if (v instanceof DroppedItem item) {
					this.target = Optional.empty();
					this.sendMessage.accept(new String[] { "clear_target" });
					action.set(item.collect(this));
				}
			}
		});
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
			if (path.length <= 2) {
				if (v instanceof LivingEntity targetEntity) {
					// Attack living entity
					this.target = Optional.empty();
					this.sendMessage.accept(new String[] { "clear_target" });
					action.set(Optional.of(new AttackAction(this, targetEntity)));
					return;
				}
			}
			action.set(Optional.of(new MoveAction(this, path[1][0], path[1][1])));
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
	public void afterMove(Level level) {
		if (this.target.map((v) -> (this.x == v.getX()) && (this.y == v.getY()) && (! (v instanceof TileEntity))).orElse(false)) {
			// Clear player target
			this.sendMessage.accept(new String[] { "clear_target" });
			this.sendMessage.accept(new String[] { "set_animation", String.valueOf(this.id), "idle" });
			this.target = Optional.empty();
		}
		this.sendVision(level);
	}
	public int getDamage() { return Random.randint(1, 5); }
	public ArrayList<Item> getAllInventoryItems() {
		ArrayList<Item> items = super.getAllInventoryItems();
		items.addAll(this.inventory);
		return items;
	}
	public JSONObject serialize(boolean allDetails) {
		JSONObject obj = super.serialize(allDetails);
		if (allDetails) obj.setArray("inventory", new ArrayList<Object>(this.inventory.stream().map((v) -> v.serialize()).toList()));
		return obj;
	}
}
