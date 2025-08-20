package com.sillypantscoder.pixeldungeon4.entities;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.entitydef.BehaviorCommand;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.registries.MonsterType;

public class Monster extends LivingEntity {
	public String typeID;
	public Optional<PathfindingTarget> target;
	public ArrayList<BehaviorCommand> behavior;
	public Monster(String typeID, int time, int x, int y) {
		super(time, x, y, (int)(Math.round(MonsterType.allMonsterTypes.get(typeID).health.get())));
		this.typeID = typeID;
		this.target = Optional.empty();
		this.behavior = MonsterType.allMonsterTypes.get(typeID).behavior;
	}
	public String getTypeID() { return this.typeID; }
	public Optional<Action<?>> getAction(Level level) {
		// Update target
		this.target = this.target.map((v) -> v.update(level, this.x, this.y));
		// Check if we are at target
		if (this.target.map((v) -> {
			if (v.getX() != this.x) return false;
			if (v.getY() != this.y) return false;
			return true;
		}).orElse(false)) {
			this.target = Optional.empty();
		}
		// Pathfind
		// this.target.ifPresent((v) -> {
		// 	int[][] path = level.findPath(this.x, this.y, v.getX(), v.getY());
		// 	if (path.length == 0) {
		// 		this.target = Optional.empty();
		// 		return;
		// 	}
		// 	action.set(Optional.of(new Action.MoveAction(this, 1, path[1][0], path[1][1])));
		// });
		// Get action from behavior
		for (BehaviorCommand command : this.behavior) {
			Optional<Action<?>> result = command.execute(this);
			if (result.isPresent()) {
				return result;
			}
		}
		throw new RuntimeException("Entity behavior didn't return an action! Entity: " + this.typeID);
	}
}
