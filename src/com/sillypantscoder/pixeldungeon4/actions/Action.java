package com.sillypantscoder.pixeldungeon4.actions;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.LivingEntity;
import com.sillypantscoder.pixeldungeon4.entities.Monster;
import com.sillypantscoder.pixeldungeon4.entities.PathfindingTarget;
import com.sillypantscoder.pixeldungeon4.level.Level;

public abstract class Action<T extends Entity> {
	public T entity;
	public int time;
	public Action(T target, int time) {
		this.entity = target;
		this.time = time;
	}
	public abstract void execute(Game game);
	public static Action<?> create(Level level, Monster e, String actionName, double timeScale, int extraTime) {
		Action<?> action = null;
		if (actionName.equals("wait")) {
			action = new WaitAction(e);
		} else if (actionName.equals("pathfind")) {
			PathfindingTarget target = e.target.orElseThrow();
			int[][] path = level.findPath(e.x, e.y, target.getX(), target.getY());
			if (path.length > 0) {
				action = new MoveAction(e, path[1][0], path[1][1]);
			}
		} else if (actionName.equals("attack")) {
			PathfindingTarget target = e.target.orElseThrow();
			if (target instanceof LivingEntity targetEntity) {
				action = new AttackAction(e, targetEntity);
			}
		}
		if (action == null) throw new IllegalArgumentException("Unknown action name: " + actionName);
		// System.out.print("(" + action.time + " * " + timeScale + ") + " + extraTime);
		action.time = (int)(action.time * timeScale) + extraTime;
		// System.out.println(" => " + action.time);
		return action;
	}
}
