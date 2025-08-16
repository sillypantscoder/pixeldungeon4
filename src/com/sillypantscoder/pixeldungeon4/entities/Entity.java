package com.sillypantscoder.pixeldungeon4.entities;

import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.level.Level;

public abstract class Entity {
	public int time;
	public Entity(int time) {
		this.time = time;
	}
	public abstract String getTypeID();
	public abstract Optional<Action<?>> getAction(Level level);
}
