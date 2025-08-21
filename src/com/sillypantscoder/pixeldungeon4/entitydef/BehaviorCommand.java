package com.sillypantscoder.pixeldungeon4.entitydef;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.PathfindingTarget;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;
import com.sillypantscoder.pixeldungeon4.level.Tile;
import com.sillypantscoder.pixeldungeon4.registries.TileType;
import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Random;

public abstract class BehaviorCommand {
	public abstract Optional<Action<?>> execute(MonsterSituation situation);
	public static BehaviorCommand create(JSONObject object) {
		String type = object.getString("type");
		if (type.equals("return-action")) return ReturnAction.create(object);
		else if (type.equals("remove-target")) return new RemoveTarget();
		else if (type.equals("choose-target-position")) return ChooseTargetPosition.create(object);
		else if (type.equals("find-target-entity")) return FindTargetEntity.create(object);
		else if (type.equals("if")) return If.create(object);
		else throw new IllegalArgumentException("Unknown behavior command: " + type);
	}
	public static class ReturnAction extends BehaviorCommand {
		public String actionName;
		public NumberProvider timeScale;
		public NumberProvider extraTime;
		public ReturnAction(String actionName, NumberProvider timeScale, NumberProvider extraTime) {
			this.actionName = actionName;
			this.timeScale = timeScale;
			this.extraTime = extraTime;
		}
		public Optional<Action<?>> execute(MonsterSituation situation) { return Optional.of(Action.create(situation.level, situation.self, this.actionName, this.timeScale.get(situation), (int)(this.extraTime.get(situation)))); }
		public static ReturnAction create(JSONObject object) { return new ReturnAction(object.getString("action"), NumberProvider.create(object, "time_scale"), NumberProvider.create(object, "extra_time")); }
	}
	public static class RemoveTarget extends BehaviorCommand {
		public RemoveTarget() {}
		public Optional<Action<?>> execute(MonsterSituation situation) {
			situation.self.target = Optional.empty();
			return Optional.empty();
		}
	}
	public static class ChooseTargetPosition extends BehaviorCommand {
		public NumberProvider minDistance;
		public NumberProvider maxDistance;
		public ChooseTargetPosition(NumberProvider minDistance, NumberProvider maxDistance) {
			this.minDistance = minDistance;
			this.maxDistance = maxDistance;
		}
		public Optional<Action<?>> execute(MonsterSituation situation) {
			for (int i = 0; i < 50; i++) {
				double minDistance = this.minDistance.get(situation);
				double maxDistance = this.maxDistance.get(situation);
				int x = (int)(Random.randfloat(situation.self.x - maxDistance, situation.self.x + maxDistance));
				int y = (int)(Random.randfloat(situation.self.y - maxDistance, situation.self.y + maxDistance));
				// Verify that the chosen location is within given distance bounds
				double distance = Math.sqrt(Math.pow(x - situation.self.x, 2) + Math.pow(y - situation.self.y, 2));
				if (distance < minDistance || distance > maxDistance) continue;
				// Verify that the tile is within the level bounds
				if (x < 0 || y < 0 || x >= situation.level.tiles.length || y >= situation.level.tiles[0].length) continue;
				// Verify that this tile is walkable
				Tile tile = situation.level.getTile(x, y);
				if (tile.getData().collisionType != TileType.CollisionType.NORMAL) continue;
				// Verify that the tile is visible from the entity's position
				if (! situation.level.isLocVisible(situation.self.x, situation.self.y, x, y)) continue;
				// Verify that it is possible to pathfind to the tile
				int[][] path = situation.level.findPath(situation.self.x, situation.self.y, x, y);
				if (path.length == 0) continue;
				// Save position and exit!
				situation.self.target = Optional.of(new PathfindingTarget.StaticPosition(x, y));
				System.out.println("target set to: " + x + ", " + y);
				return Optional.empty();
			}
			return Optional.empty();
		}
		public static ChooseTargetPosition create(JSONObject object) { return new ChooseTargetPosition(NumberProvider.create(object, "min_distance"), NumberProvider.create(object, "max_distance")); }
	}
	public static class FindTargetEntity extends BehaviorCommand {
		public NumberProvider entityWeight;
		public FindTargetEntity(NumberProvider entityWeight) {
			this.entityWeight = entityWeight;
		}
		public Optional<Action<?>> execute(MonsterSituation situation) {
			// Get all entities
			ArrayList<TileEntity> entities = new ArrayList<TileEntity>();
			for (Entity e : situation.level.entities) {
				if (e instanceof TileEntity tileEntity) {
					// Check if this entity is visible
					if (situation.level.isLocVisible(situation.self.x, situation.self.y, tileEntity.x, tileEntity.y)) {
						entities.add(tileEntity);
					}
				}
			}
			// Find entity with best score
			TileEntity bestScoreEntity = null;
			double bestScore = Double.MIN_VALUE;
			for (TileEntity entity : entities) {
				// Get score
				double score = this.entityWeight.get(new MonsterSituation(situation, entity));
				// Check if this is the best score
				if (bestScoreEntity == null || score > bestScore) {
					bestScoreEntity = entity;
					bestScore = score;
				}
			}
			// If we found a target, set it and return an action
			if (bestScoreEntity != null) {
				situation.self.target = Optional.of(bestScoreEntity);
				return Optional.empty();
			}
			return Optional.empty();
		}
		public static FindTargetEntity create(JSONObject object) { return new FindTargetEntity(NumberProvider.create(object, "entity_weight")); }
	}
	public static class If extends BehaviorCommand {
		public Condition condition;
		public ArrayList<BehaviorCommand> ifTrue;
		public ArrayList<BehaviorCommand> ifFalse;
		public If(Condition condition, ArrayList<BehaviorCommand> ifTrue, ArrayList<BehaviorCommand> ifFalse) {
			this.condition = condition;
			this.ifTrue = ifTrue;
			this.ifFalse = ifFalse;
		}
		public Optional<Action<?>> execute(MonsterSituation situation) {
			ArrayList<BehaviorCommand> commands = new ArrayList<BehaviorCommand>();
			// Evaluate condition
			boolean conditionPassed = this.condition.get(situation);
			if (conditionPassed) commands.addAll(this.ifTrue);
			else commands.addAll(this.ifFalse);
			// Execute commands
			for (BehaviorCommand command : commands) {
				Optional<Action<?>> result = command.execute(new MonsterSituation(situation.level, situation.self, situation.self.target));
				if (result.isPresent()) {
					return result;
				}
			}
			return Optional.empty();
		}
		public static If create(JSONObject object) {
			Condition condition = Condition.create(object.getObject("condition"));
			// If true commands
			ArrayList<BehaviorCommand> ifTrue = new ArrayList<BehaviorCommand>();
			for (Object o : object.getArray("ifTrue")) {
				if (o instanceof JSONObject cmd) {
					ifTrue.add(BehaviorCommand.create(cmd));
				}
			}
			// If false commands
			ArrayList<BehaviorCommand> ifFalse = new ArrayList<BehaviorCommand>();
			for (Object o : object.getArray("ifFalse")) {
				if (o instanceof JSONObject cmd) {
					ifFalse.add(BehaviorCommand.create(cmd));
				}
			}
			// Finish
			return new If(condition, ifTrue, ifFalse);
		}
	}
}
