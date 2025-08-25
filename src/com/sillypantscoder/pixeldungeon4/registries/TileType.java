package com.sillypantscoder.pixeldungeon4.registries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.entities.Player;
import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Utils;

public class TileType {
	public CollisionType collisionType;
	public boolean canSeeThrough;
	public ArrayList<JSONObject> onEnter;
	public ArrayList<JSONObject> onLeave;
	public TileType(CollisionType collisionType, boolean canSeeThrough, ArrayList<JSONObject> onEnter, ArrayList<JSONObject> onLeave) {
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
			ArrayList<JSONObject> onEnter = new ArrayList<JSONObject>();
			for (Object data : object.getArray("onEnter")) {
				if (data instanceof JSONObject jsonData) {
					onEnter.add(jsonData);
				} else throw new RuntimeException("Tile event object is of the wrong type");
			}
			// - On leave
			ArrayList<JSONObject> onLeave = new ArrayList<JSONObject>();
			for (Object data : object.getArray("onLeave")) {
				if (data instanceof JSONObject jsonData) {
					onLeave.add(jsonData);
				} else throw new RuntimeException("Tile event object is of the wrong type");
			}
			// Assemble TileType object
			TileType type = new TileType(collisionType, canSeeThrough, onEnter, onLeave);
			types.put(name.split("\\.")[0], type);
		}
		return types;
	}
	public static void doAction(JSONObject data, Game game, int x, int y) {
		if (data.getString("type").equals("change-tile")) {
			game.level.tiles[x][y].state = data.getString("tile");
			for (Player p : game.allPlayers()) {
				if (game.level.isLocVisible(x, y, p.x, p.y)) {
					p.sendMessage.accept(new String[] {
						"show_tiles",
						x + " " + y + " " + game.level.tiles[x][y].state
					});
				}
			}
		} else if (data.getString("type").equals("spawn-particles")) {
			for (Player p : game.allPlayers()) {
				if (game.level.isLocVisible(x, y, p.x, p.y)) {
					// Create attacking particles
					p.sendMessage.accept(new String[] {
						"create_particle",
						data.getString("particle_data"),
						x + " " + y
					});
				}
			}
		} else {
			System.err.println("Unknown tile action type: " + data.getString("type"));
		}
	}
}
