package com.sillypantscoder.pixeldungeon4;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.LivingEntity;
import com.sillypantscoder.pixeldungeon4.entities.Monster;
import com.sillypantscoder.pixeldungeon4.entities.MonsterSpawner;
import com.sillypantscoder.pixeldungeon4.entities.Player;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeonlevelgen.RoomBuildingLevelGeneration;
import com.sillypantscoder.utils.Random;
import com.sillypantscoder.utils.Utils;

public class Game {
	public Level level;
	public HashMap<String, PlayerData> players;
	public Game() {
		this.level = RoomBuildingLevelGeneration.generateLevel(45);
		this.level.updateEntityHealth = this::updateEntityHealth;
		this.players = new HashMap<String, PlayerData>();
		// spawn some rats
		this.addFreshEntity(this.createMonsterEntity("rat"));
		this.addFreshEntity(this.createMonsterEntity("rat"));
		this.addFreshEntity(new MonsterSpawner(this.level.getNewEntityTime()));
	}
	public String loginPlayer() {
		// Get player ID
		String playerID = "P" + Random.randomInt();
		// Create player entity
		Player playerEntity = this.createPlayerEntity(playerID);
		this.addFreshEntity(playerEntity);
		// Messages
		players.put(playerID, new PlayerData(playerEntity));
		{
			// Send level size
			players.get(playerID).sendMessage(new String[] {
				"level_size",
				String.valueOf(level.tiles[0].length),
				String.valueOf(level.tiles.length)
			});
		}
		{
			// Vision
			playerEntity.sendVision(level);
		}
		{
			// Set me
			String[] data = new String[] {
				"set_me",
				String.valueOf(playerEntity.id)
			};
			players.get(playerID).sendMessage(data);
		}
		// Save player ID
		return playerID;
	}
	public Map<String, byte[]> getAllData() {
		HashMap<String, byte[]> data = new HashMap<String, byte[]>();
		for (String folder_name : new String[] {
			"data/definitions/entity_spritesheets",
			"data/definitions/item",
			"data/definitions/monster",
			"data/definitions/tile",
			"data/textures/entity",
			"data/textures/item",
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
		return new Player(playerID, this.level.getNewEntityTime(), spawnPoint[0], spawnPoint[1], null);
	}
	public Monster createMonsterEntity(String monsterID) {
		int[] spawnPoint = this.level.getSpawnPoint();
		return new Monster(monsterID, this.level.getNewEntityTime(), spawnPoint[0], spawnPoint[1]);
	}
	public void addFreshEntity(Entity e) {
		// *Not related to Minecraft
		this.level.entities.add(e);
		if (e instanceof TileEntity tileEntity) {
			for (Player p : allPlayers()) {
				// Check if player can see it
				if (! this.level.isLocVisible(p.x, p.y, tileEntity.x, tileEntity.y)) continue;
				// Send message!
				String[] data = new String[] {
					"create_entity",
					tileEntity.serialize(p == e).toString()
				};
				p.sendMessage.accept(data);
			}
		}
	}
	public void doEntityTurns() {
		for (int i = 0; i < 16; i++) {
			boolean canContinue = this.level.doEntityTurn(this);
			if (! canContinue) break;
		}
	}
	public List<Player> allPlayers() {
		return this.players.values().stream().map((v) -> v.player()).toList();
	}
	public void updateEntityHealth(LivingEntity e) {
		if (e.health <= 0) {
			// Entity on death trigger
			e.onDeath(this);
			// Send entity death to clients
			for (Player player : allPlayers()) {
				if (player == e || level.isLocVisible(player.x, player.y, e.x, e.y)) {
					// Send death
					player.sendMessage.accept(new String[] {
						"entity_death",
						String.valueOf(e.id)
					});
				}
			}
			// Register entity death
			// this has to be afterwards so a player's death is sent to that player
			level.entities.remove(e);
		} else {
			for (Player player : allPlayers()) {
				if (player == e || level.isLocVisible(player.x, player.y, e.x, e.y)) {
					// Set new target health
					player.sendMessage.accept(new String[] {
						"set_health",
						String.valueOf(e.id),
						String.valueOf(e.health)
					});
				}
			}
		}
	}
}
