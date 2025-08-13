package com.sillypantscoder.pixeldungeon4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.level.SubdivisionLevelGeneration;
import com.sillypantscoder.utils.Random;

public class Game {
	public Level level;
	public HashMap<String, ArrayList<String>> messages;
	public Game() {
		this.level = SubdivisionLevelGeneration.generateLevel();
		this.messages = new HashMap<String, ArrayList<String>>();
	}
	public String loginPlayer() {
		// Get player ID
		String playerID = "P" + Random.randint(1, 10000000);
		// Messages
		messages.put(playerID, new ArrayList<String>());
		{
			String data = "log";
			for (int y = 0; y < level.tiles[0].length; y++) {
				data += "\n";
				for (int x = 0; x < level.tiles.length; x++) {
					switch (level.tiles[x][y].state) {
						case "normal":
							data += "__";
							break;
						case "wall":
							data += "##";
							break;
						case "door":
							data += "][";
							break;
					}
				}
			}
			messages.get(playerID).add(data);
		}
		// Save player ID
		return playerID;
	}
	public Optional<String> getPlayerMessage(String playerID) {
		ArrayList<String> m = messages.get(playerID);
		if (m.isEmpty()) return Optional.empty();
		else return Optional.of(m.get(0));
	}
}
