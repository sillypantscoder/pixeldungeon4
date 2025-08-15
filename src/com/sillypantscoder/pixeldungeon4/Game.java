package com.sillypantscoder.pixeldungeon4;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.Player;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.level.SubdivisionLevelGeneration;
import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Utils;

public class Game {
	public Level level;
	public HashMap<String, ArrayList<String[]>> messages;
	public Game() {
		this.level = SubdivisionLevelGeneration.generateLevel(30);
		this.messages = new HashMap<String, ArrayList<String[]>>();
	}
	public String loginPlayer() {
		// Get player ID
		String playerID = "P" + Random.randomInt();
		// Messages
		messages.put(playerID, new ArrayList<String[]>());
		{
			// Send level size
			messages.get(playerID).add(new String[] {
				"level_size",
				String.valueOf(level.tiles.length),
				String.valueOf(level.tiles[0].length)
			});
		}
		// Create player entity
		Player playerEntity = this.createPlayerEntity(playerID);
		{
			// Send vision (tiles + entities)
			this.sendPlayerVision(playerEntity);
		}
		this.addFreshEntity(playerEntity);
		{
			String[] data = new String[] {
				"set_me",
				String.valueOf(playerEntity.id)
			};
			messages.get(playerID).add(data);
		}
		// Save player ID
		return playerID;
	}
	public Map<String, byte[]> getAllData() {
		HashMap<String, byte[]> data = new HashMap<String, byte[]>();
		for (String folder_name : new String[] {
			"data/definitions/entity_spritesheets",
			"data/definitions/tile",
			"data/textures/entity",
			"data/textures/tile"
		}) {
			for (String filename : new File(folder_name).list()) {
				byte[] fileContents = Utils.readFileBinary(new File(folder_name, filename));
				data.put(folder_name + "/" + filename, fileContents);
			}
		}
		return data;
	}
	public Player getPlayerByID(String playerID) {
		for (Entity entity : this.level.entities) {
			if (entity instanceof Player player && player.playerID.equals(playerID)) {
				return player;
			}
		}
		throw new RuntimeException("Player with ID '" + playerID + "' not found");
	}
	public Player createPlayerEntity(String playerID) {
		int[] spawnPoint = this.level.getSpawnPoint();
		return new Player(playerID, this.level.getNewEntityTime(), spawnPoint[0], spawnPoint[1]);
	}
	public void addFreshEntity(Entity e) {
		// *Not related to Minecraft
		this.level.entities.add(e);
		if (e instanceof TileEntity tileEntity) {
			for (String playerID : this.messages.keySet()) {
				String[] data = new String[] {
					"create_entity",
					tileEntity.serialize().toString()
				};
				messages.get(playerID).add(data);
			}
		}
	}
	public void doEntityTurns() {
		for (int i = 0; i < 16; i++) {
			boolean canContinue = this.level.doEntityTurn(this);
			if (! canContinue) break;
		}
	}
	public void sendPlayerVision(Player p) {
		// Tiles
		ArrayList<String> vision = new ArrayList<String>();
		vision.add("show_tiles");
		for (int y = 0; y < level.tiles[0].length; y++) {
			for (int x = 0; x < level.tiles.length; x++) {
				if (! this.level.isLocVisible(p.x, p.y, x, y)) continue;
				vision.add(x + " " + y + " " + level.tiles[x][y].state);
			}
		}
		messages.get(p.playerID).add(vision.toArray(new String[0]));
		// Entities
		for (Entity e : this.level.entities) {
			if (e instanceof TileEntity tileEntity) {
				if (! this.level.isLocVisible(p.x, p.y, tileEntity.x, tileEntity.y)) continue;
				String[] data = new String[] {
					"create_entity",
					tileEntity.serialize().toString()
				};
				messages.get(p.playerID).add(data);
			}
		}
	}
}
