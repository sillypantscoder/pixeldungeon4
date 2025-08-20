package com.sillypantscoder.pixeldungeon4;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.Monster;
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
		// spawn a rat
		this.addFreshEntity(this.createMonsterEntity("rat"));
		this.addFreshEntity(this.createMonsterEntity("rat"));
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
		playerEntity.sendVision(level);
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
	public void clearMessages(String playerID) {
		ArrayList<String[]> messageList = new ArrayList<String[]>();
		this.messages.put(playerID, messageList);
		Player p = this.getPlayerByID(playerID);
		p.sendMessage = messageList::add;
	}
	public Map<String, byte[]> getAllData() {
		HashMap<String, byte[]> data = new HashMap<String, byte[]>();
		for (String folder_name : new String[] {
			"data/definitions/entity_spritesheets",
			"data/definitions/monster",
			"data/definitions/tile",
			"data/textures/entity",
			"data/textures/special",
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
		return new Player(playerID, this.level.getNewEntityTime(), spawnPoint[0], spawnPoint[1], messages.get(playerID)::add);
	}
	public Monster createMonsterEntity(String monsterID) {
		int[] spawnPoint = this.level.getSpawnPoint();
		return new Monster(monsterID, this.level.getNewEntityTime(), spawnPoint[0], spawnPoint[1]);
	}
	public void addFreshEntity(Entity e) {
		// *Not related to Minecraft
		this.level.entities.add(e);
		if (e instanceof TileEntity tileEntity) {
			for (String playerID : this.messages.keySet()) {
				// Check if player can see it
				Player p = this.getPlayerByID(playerID);
				if (! this.level.isLocVisible(p.x, p.y, tileEntity.x, tileEntity.y)) continue;
				// Send message!
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
}
