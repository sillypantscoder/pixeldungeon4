package com.sillypantscoder.pixeldungeon4.entities;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.items.Item;
import com.sillypantscoder.utils.JSONObject;

public abstract class LivingEntity extends TileEntity {
	public int health;
	public int maxHealth;
	public Optional<Item> mainHand;
	public LivingEntity(int time, int x, int y, int health) {
		super(time, x, y);
		this.health = health;
		this.maxHealth = health;
		this.mainHand = Optional.empty();
	}
	public abstract int getDamage();
	public void onDeath(Game game) {
		game.addFreshEntity(new DroppedItem(game.level.getNewEntityTime(), x, y, new Item("food")));
	}
	public ArrayList<Item> getAllInventoryItems() {
		ArrayList<Item> items = new ArrayList<Item>();
		this.mainHand.ifPresent((v) -> {
			items.add(v);
		});
		return items;
	}
	public JSONObject serialize(boolean allDetails) {
		JSONObject obj = super.serialize(allDetails);
		obj.setNumber("health", this.health);
		obj.setNumber("maxHealth", this.maxHealth);
		if (allDetails) obj.setObject("mainHand", this.mainHand.map((v) -> v.serialize()).orElseGet(() -> {
			JSONObject noItem = new JSONObject();
			noItem.setBoolean("id", false);
			return noItem;
		}));
		return obj;
	}
}
