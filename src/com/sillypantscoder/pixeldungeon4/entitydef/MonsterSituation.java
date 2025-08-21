package com.sillypantscoder.pixeldungeon4.entitydef;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.entities.LivingEntity;
import com.sillypantscoder.pixeldungeon4.entities.Monster;
import com.sillypantscoder.pixeldungeon4.entities.PathfindingTarget;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;
import com.sillypantscoder.pixeldungeon4.level.Level;

public class MonsterSituation {
	public Level level;
	public Monster self;
	public Optional<PathfindingTarget> target;
	public Optional<TileEntity> current;
	public MonsterSituation(Level level, Monster self, Optional<PathfindingTarget> target) {
		this.level = level;
		this.self = self;
		this.target = target;
		this.current = Optional.empty();
	}
	public MonsterSituation(MonsterSituation situation, TileEntity current) {
		this.level = situation.level;
		this.self = situation.self;
		this.target = situation.target;
		this.current = Optional.of(current);
	}
	public double get_target_distance() {
		if (target.isEmpty()) return 0;
		return Math.sqrt(Math.pow(self.x - target.get().getX(), 2) + Math.pow(self.y - target.get().getY(), 2));
	}
	public double get_target_pathDistance() {
		if (target.isEmpty()) return 0;
		int[][] path = this.level.findPath(this.self.x, this.self.y, target.get().getX(), target.get().getY());
		return path.length;
	}
	public double get_current_distance() {
		if (current.isEmpty()) return 0;
		return Math.sqrt(Math.pow(self.x - current.get().getX(), 2) + Math.pow(self.y - current.get().getY(), 2));
	}
	public double get_current_pathDistance() {
		if (current.isEmpty()) return 0;
		int[][] path = this.level.findPath(this.self.x, this.self.y, current.get().getX(), current.get().getY());
		return path.length;
	}
	public double get(String name) {
		if (name.equals("self.health")) return this.self.health;
		if (name.equals("target.distance")) return this.get_target_distance();
		if (name.equals("target.pathDistance")) return this.get_target_pathDistance();
		if (name.equals("target.health")) return this.target.map((v) -> (v instanceof LivingEntity te ? te.health : 0)).orElse(0);
		if (name.equals("current.distance")) return this.get_current_distance();
		if (name.equals("current.pathDistance")) return this.get_current_pathDistance();
		if (name.equals("current.health")) return this.current.map((v) -> (v instanceof LivingEntity te ? te.health : 0)).orElse(0);
		throw new NoSuchElementException("No such variable");
	}
}
