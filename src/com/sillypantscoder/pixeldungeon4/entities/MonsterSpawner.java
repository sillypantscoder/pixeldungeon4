package com.sillypantscoder.pixeldungeon4.entities;

import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.level.Level;

public class MonsterSpawner extends Entity {
	public MonsterSpawner(int time) {
		super(time);
	}
	public String getTypeID() {
		return "monster_spawner";
	}
	public Optional<Action<?>> getAction(Level level) {
		return Optional.of(new SpawnMonster(this, "rat"));
	}
	public static class SpawnMonster extends Action<MonsterSpawner> {
		public String monsterType;
		public SpawnMonster(MonsterSpawner target, String monsterType) {
			super(target, 256);
			this.monsterType = monsterType;
		}
		public void execute(Game game) {
			// Create entity
			int[] spawnPoint = game.level.getSpawnPoint();
			Monster newEntity = new Monster(monsterType, game.level.getNewEntityTime(), spawnPoint[0], spawnPoint[1]);
			// Add to world
			game.level.entities.add(newEntity);
			// Increase time
			this.entity.time += this.time;
			// Send to clients
			for (Player p : game.allPlayers()) {
				if (game.level.isLocVisible(newEntity.x, newEntity.y, p.x, p.y)) {
					// Create entity
					p.visibleEntities.add(newEntity);
					String[] data = new String[] {
						"create_entity",
						newEntity.serialize().toString()
					};
					p.sendMessage.accept(data);
				}
			}
		}
	}
}
