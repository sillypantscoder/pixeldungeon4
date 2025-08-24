package com.sillypantscoder.pixeldungeon4.registries;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Utils;

public class TileType {
	public CollisionType collisionType;
	public boolean canSeeThrough;
	public Optional<String> onEnter;
	public Optional<String> onLeave;
	public TileType(CollisionType collisionType, boolean canSeeThrough, Optional<String> onEnter, Optional<String> onLeave) {
		this.collisionType = collisionType;
		this.canSeeThrough = canSeeThrough;
		this.onEnter = onEnter;
		this.onLeave = onLeave;
	}
	public static enum CollisionType {
		NONE, NORMAL, WALL
	}
	public static HashMap<String, TileType> allTileTypes = getAllTileTypes();
	public static HashMap<String, TileType> getAllTileTypes() {
		HashMap<String, TileType> types = new HashMap<String, TileType>();
		for (String name : new File("data/definitions/tile").list()) {
			JSONObject object = JSONObject.create(Utils.readFile("data/definitions/tile/" + name));
			// Unpack JSON object
			// - Collision type
			String collisionTypeString = object.getString("collisionType");
			CollisionType collisionType = CollisionType.NONE;
			if (collisionTypeString.equals("none")) collisionType = CollisionType.NONE;
			else if (collisionTypeString.equals("normal")) collisionType = CollisionType.NORMAL;
			else if (collisionTypeString.equals("wall")) collisionType = CollisionType.WALL;
			else System.err.println("Invalid collision type for tile: " + name);
			// - Can see through
			boolean canSeeThrough = object.getBoolean("canSeeThrough");
			// - On enter
			Optional<String> onEnter = Optional.empty();
			if (object.entries_object.containsKey("onEnter")) {
				JSONObject onEnterObject = object.getObject("onEnter");
				onEnter = Optional.of(onEnterObject.getString("tile"));
			}
			// - On leave
			Optional<String> onLeave = Optional.empty();
			if (object.entries_object.containsKey("onLeave")) {
				JSONObject onLeaveObject = object.getObject("onLeave");
				onLeave = Optional.of(onLeaveObject.getString("tile"));
			}
			// Assemble TileType object
			TileType type = new TileType(collisionType, canSeeThrough, onEnter, onLeave);
			types.put(name.split("\\.")[0], type);
		}
		return types;
	}
}
