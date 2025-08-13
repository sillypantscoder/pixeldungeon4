package com.sillypantscoder.pixeldungeon4.registries;

import java.io.File;
import java.util.HashMap;

import com.sillypantscoder.utils.JSON;
import com.sillypantscoder.utils.Utils;

public class TileType {
	public CollisionType collisionType;
	public boolean canSeeThrough;
	public TileType(CollisionType collisionType, boolean canSeeThrough) {
		this.collisionType = collisionType;
		this.canSeeThrough = canSeeThrough;
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
			// - Collision type
			String collisionTypeString = object.get("collisionType", JSON.JString.class).s;
			CollisionType collisionType = CollisionType.NONE;
			if (collisionTypeString.equals("none")) collisionType = CollisionType.NONE;
			else if (collisionTypeString.equals("normal")) collisionType = CollisionType.NORMAL;
			else if (collisionTypeString.equals("wall")) collisionType = CollisionType.WALL;
			else System.err.println("Invalid collision type for tile: " + name);
			// - Can see through
			boolean canSeeThrough = object.get("canSeeThrough", JSON.JBoolean.class).b;
			// Assemble TileType object
			TileType type = new TileType(collisionType, canSeeThrough);
			types.put(name.split("\\.")[0], type);
		}
		return types;
	}
}
