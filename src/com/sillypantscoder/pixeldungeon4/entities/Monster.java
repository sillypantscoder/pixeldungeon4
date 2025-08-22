package com.sillypantscoder.pixeldungeon4.entities;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.entitydef.BehaviorCommand;
import com.sillypantscoder.pixeldungeon4.entitydef.CommandResult;
import com.sillypantscoder.pixeldungeon4.entitydef.MonsterSituation;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.pixeldungeon4.registries.MonsterType;

public class Monster extends LivingEntity {
	public static boolean DEBUG_BEHAVIOR = false;
	public String typeID;
	public Optional<PathfindingTarget> target;
	public ArrayList<BehaviorCommand> behavior;
	public Monster(String typeID, int time, int x, int y) {
		super(time, x, y, (int)(Math.round(MonsterType.allMonsterTypes.get(typeID).health.get(new MonsterSituation(null, null, Optional.empty())))));
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
		// Get action from behavior
		if (DEBUG_BEHAVIOR) System.out.println("Behavior for monster: " + this);
		for (BehaviorCommand command : this.behavior) {
			CommandResult<Optional<Action<?>>> result = command.execute(new MonsterSituation(level, this, this.target));
			if (DEBUG_BEHAVIOR) System.out.println(result.debugInfo.replace("\n", "\n\t"));
			if (result.result.isPresent()) {
				return result.result;
			}
		}
		throw new RuntimeException("Entity behavior didn't return an action! Entity: " + this.typeID);
	}
}
