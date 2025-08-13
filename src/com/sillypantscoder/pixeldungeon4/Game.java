package com.sillypantscoder.pixeldungeon4;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.level.SubdivisionLevelGeneration;
import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Utils;

public class Game {
	public Level level;
	public HashMap<String, ArrayList<String[]>> messages;
	public Game() {
		this.level = SubdivisionLevelGeneration.generateLevel();
		this.messages = new HashMap<String, ArrayList<String[]>>();
	}
	public String loginPlayer() {
		// Get player ID
		String playerID = "P" + Random.randint(1, 10000000);
		// Messages
		messages.put(playerID, new ArrayList<String[]>());
		{
			String[] data = new String[level.tiles[0].length + 1];
			data[0] = "log";
			for (int y = 0; y < level.tiles[0].length; y++) {
				String row = "";
				for (int x = 0; x < level.tiles.length; x++) {
					switch (level.tiles[x][y].state) {
						case "normal":
							row += "__";
							break;
						case "wall":
							row += "##";
							break;
						case "door":
							row += "][";
							break;
					}
				}
				data[y + 1] = row;
			}
			messages.get(playerID).add(data);
		}
		{
			messages.get(playerID).add(new String[] {
				"level_size",
				String.valueOf(level.tiles.length),
				String.valueOf(level.tiles[0].length)
			});
		}
		// Save player ID
		return playerID;
	}
	// public Optional<String> getPlayerMessage(String playerID) {
	// 	ArrayList<String> m = messages.get(playerID);
	// 	if (m.isEmpty()) return Optional.empty();
	// 	else return Optional.of(m.get(0));
	// }
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
}
