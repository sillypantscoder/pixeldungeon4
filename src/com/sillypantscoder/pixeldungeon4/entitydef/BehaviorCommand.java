package com.sillypantscoder.pixeldungeon4.entitydef;

import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.utils.JSONObject;

public abstract class BehaviorCommand {
	public abstract Optional<Action<?>> execute(Entity target);
	public static BehaviorCommand create(JSONObject object) {
		String type = object.getString("type");
		if (type.equals("return-action")) return ReturnAction.create(object);
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
		public Optional<Action<?>> execute(Entity target) { return Optional.of(Action.create(target, this.actionName, this.timeScale.get(), (int)(this.extraTime.get()))); }
		public static ReturnAction create(JSONObject object) { return new ReturnAction(object.getString("action"), NumberProvider.create(object, "time_scale"), NumberProvider.create(object, "extra_time")); }
	}
}
