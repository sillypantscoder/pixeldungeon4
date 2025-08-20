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
	public ArrayList<BehaviorCommand> behavior;
	public MonsterType(NumberProvider health, ArrayList<BehaviorCommand> behavior) {
		this.health = health;
		this.behavior = behavior;
	}
	public static HashMap<String, MonsterType> allMonsterTypes = getAllMonsterTypes();
	public static HashMap<String, MonsterType> getAllMonsterTypes() {
		HashMap<String, MonsterType> types = new HashMap<String, MonsterType>();
		for (String name : new File("data/definitions/monster").list()) {
			JSONObject object = JSONObject.create(Utils.readFile("data/definitions/monster/" + name));
			// Unpack JSON object
			// - Health
			NumberProvider healthProvider = NumberProvider.create(object, "health");
			// - Behavior
			ArrayList<BehaviorCommand> behavior = new ArrayList<BehaviorCommand>();
			for (Object o : object.getArray("behavior")) {
				if (o instanceof JSONObject cmd) {
					behavior.add(BehaviorCommand.create(cmd));
				} else {
					throw new RuntimeException("Behavior command is of the wrong type: " + o.toString());
				}
			}
			// Assemble MonsterType object
			MonsterType type = new MonsterType(healthProvider, behavior);
			types.put(name.split("\\.")[0], type);
		}
		return types;
	}
}
