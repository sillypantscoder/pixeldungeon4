package com.sillypantscoder.pixeldungeon4;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.LivingEntity;
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
		this.level = RoomBuildingLevelGeneration.generateLevel(5);
		this.level.updateEntityHealth = this::updateEntityHealth;
		this.players = new HashMap<String, PlayerData>();
		// spawn some monsters
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(new MonsterSpawner(this.level.getNewEntityTime()));
	}
	public String loginPlayer() {
		// Get player ID
		String playerID = "P" + Random.randomInt();
		// Create player entity
		Player playerEntity = this.level.createPlayerEntity(playerID);
		playerEntity.finishLevel = () -> this.playerFinishLevel(playerEntity);
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
				p.visibleEntities.add(tileEntity);
			}
		}
	}
	public void doEntityTurns() {
		for (int i = 0; i < 64; i++) {
			boolean canContinue = this.level.doEntityTurn(this);
			if (! canContinue) break;
		}
	}
	public List<Player> allPlayers() {
		return this.players.values().stream().map((v) -> v.player).toList();
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
	public void removeEntity(Entity entity) {
		if (entity instanceof TileEntity tileEntity) {
			for (Player player : allPlayers()) {
				if (player.visibleEntities.contains(tileEntity)) {
					player.visibleEntities.remove(tileEntity);
					String[] data = new String[] {
						"remove_entity",
						tileEntity.id + ""
					};
					player.sendMessage.accept(data);
				}
			}
		}
		this.level.entities.remove(entity);
	}
	public void playerFinishLevel(Player p) {
		PlayerData data = this.players.get(p.playerID);
		data.hasWon = true;
		this.removeEntity(p);
		// Check for level completion by all players
		boolean levelCompleted = true;
		for (PlayerData checkData : this.players.values()) {
			if (this.level.entities.contains(checkData.player)) levelCompleted = false;
		}
		if (levelCompleted) {
			// Winning players continue to next level
			this.nextLevel();
		}
	}
	public void nextLevel() {
		for (Player player : allPlayers()) {
			// Send "Descending..." message
			player.sendMessage.accept(new String[] {
				"descending"
			});
		}
		// Generate level
		this.level = RoomBuildingLevelGeneration.generateLevel(35);
		this.level.updateEntityHealth = this::updateEntityHealth;
		// Prepare players and clients for level
		for (PlayerData player : this.players.values()) {
			// Send new level size to client (also erases world)
			player.sendMessage(new String[] {
				"level_size",
				String.valueOf(level.tiles[0].length),
				String.valueOf(level.tiles.length)
			});
			// Set player position (so that entity creation works properly)
			int[] spawnPos = this.level.getSpawnPointForPlayer();
			player.player.x = spawnPos[0]; player.player.y = spawnPos[1];
			player.player.time = 0;
		}
		// Add clients to new level
		for (PlayerData player : this.players.values()) {
			// Spawn player
			this.addFreshEntity(player.player);
			// Set me
			player.sendMessage(new String[] {
				"set_me",
				String.valueOf(player.player.id)
			});
		}
		// Spawn some monsters
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(this.level.createMonsterEntity("rat"));
		this.addFreshEntity(new MonsterSpawner(this.level.getNewEntityTime()));
		// Send new level data to all clients
		for (PlayerData player : this.players.values()) {
			// Vision
			player.player.sendVision(level);
		}
	}
}
