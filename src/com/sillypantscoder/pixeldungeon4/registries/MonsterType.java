package com.sillypantscoder.pixeldungeon4.registries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.sillypantscoder.pixeldungeon4.entitydef.BehaviorCommand;
import com.sillypantscoder.pixeldungeon4.entitydef.NumberProvider;
import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Utils;

public class MonsterType {
	public NumberProvider health;
	public NumberProvider damage;
	public ArrayList<BehaviorCommand> behavior;
	public ArrayList<LootTableItem> loot;
	public MonsterType(NumberProvider health, NumberProvider damage, ArrayList<BehaviorCommand> behavior, ArrayList<LootTableItem> loot) {
		this.health = health;
		this.damage = damage;
		this.behavior = behavior;
		this.loot = loot;
	}
	public static HashMap<String, MonsterType> allMonsterTypes = getAllMonsterTypes();
	public static HashMap<String, MonsterType> getAllMonsterTypes() {
		HashMap<String, MonsterType> types = new HashMap<String, MonsterType>();
		for (String name : new File("data/definitions/monster").list()) {
			JSONObject object = JSONObject.create(Utils.readFile("data/definitions/monster/" + name));
			// Unpack JSON object
			// - Health
			NumberProvider healthProvider = NumberProvider.create(object, "health");
			// - Damage
			NumberProvider damageProvider = NumberProvider.create(object, "damage");
			// - Behavior
			ArrayList<BehaviorCommand> behavior = new ArrayList<BehaviorCommand>();
			for (Object o : object.getArray("behavior")) {
				if (o instanceof JSONObject cmd) {
					behavior.add(BehaviorCommand.create(cmd));
				} else {
					throw new RuntimeException("Behavior command is of the wrong type: " + o.toString());
				}
			}
			// - Loot
			ArrayList<LootTableItem> loot = new ArrayList<LootTableItem>();
			for (Object o : object.getArray("loot")) {
				if (o instanceof JSONObject cmd) {
					loot.add(LootTableItem.create(cmd));
				} else {
					throw new RuntimeException("Behavior command is of the wrong type: " + o.toString());
				}
			}
			// Assemble MonsterType object
			MonsterType type = new MonsterType(healthProvider, damageProvider, behavior, loot);
			types.put(name.split("\\.")[0], type);
		}
		return types;
	}
	public static record LootTableItem(String item, double weight) {
		public static LootTableItem create(JSONObject object) {
			return new LootTableItem(object.getString("item"), object.getNumber("weight"));
		}
	}
}
