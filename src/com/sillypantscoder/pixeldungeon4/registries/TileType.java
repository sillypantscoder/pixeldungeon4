package com.sillypantscoder.pixeldungeon4.registries;

import java.io.File;
import java.util.HashMap;

import com.sillypantscoder.utils.JSON;
import com.sillypantscoder.utils.Utils;

public class TileType {
	public CollisionType type;
	public TileType(CollisionType type) {
		this.type = type;
	}
	public static enum CollisionType {
		NONE, NORMAL, WALL
	}
	public static HashMap<String, TileType> allTileTypes = getAllTileTypes();
	public static HashMap<String, TileType> getAllTileTypes() {
		HashMap<String, TileType> types = new HashMap<String, TileType>();
		for (String name : new File("data/definitions/tile").list()) {
			JSON.JObject<?> object = JSON.JObject.parse(Utils.readFile("data/definitions/tile/" + name));
			// Unpack JSON object
			String typeString = object.get("type", JSON.JString.class).s;
			CollisionType collisionType = CollisionType.NONE;
			if (typeString == "normal") collisionType = CollisionType.NORMAL;
			if (typeString == "wall") collisionType = CollisionType.WALL;
			// Assemble TileType object
			TileType type = new TileType(collisionType);
			types.put(name, type);
		}
		return types;
	}
}
