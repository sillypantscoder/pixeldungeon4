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
		String playerID = "P" + Random.randomLong();
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
		{
			// Send tiles (to be removed)
			for (int y = 0; y < level.tiles[0].length; y++) {
				String[] data = new String[level.tiles[0].length + 1];
				data[0] = "show_tiles";
				for (int x = 0; x < level.tiles.length; x++) {
					data[x + 1] = x + " " + y + " " + level.tiles[x][y].state;
				}
				messages.get(playerID).add(data);
			}
		}
		// Create player entity
		Player playerEntity = this.createPlayerEntity(playerID);
		{
			// Send entities
			for (Entity e : this.level.entities) {
				if (e instanceof TileEntity tileEntity) {
					String[] data = new String[] {
						"create_entity",
						tileEntity.serialize().toString()
					};
					messages.get(playerID).add(data);
				}
			}
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
			"data/definitions/tile",
			"data/textures/tile"
		}) {
			for (String filename : new File(folder_name).list()) {
				byte[] fileContents = Utils.readFileBinary(new File(folder_name, filename));
				data.put(folder_name + "/" + filename, fileContents);
			}
		}
		return data;
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
			boolean canContinue = this.level.doEntityTurn();
			if (! canContinue) break;
		}
	}
}
