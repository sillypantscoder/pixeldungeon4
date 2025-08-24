package com.sillypantscoder.pixeldungeon4.entitydef;

import java.util.Optional;
import java.util.function.BiFunction;

import com.sillypantscoder.pixeldungeon4.entities.LivingEntity;
import com.sillypantscoder.pixeldungeon4.entities.Monster;
import com.sillypantscoder.pixeldungeon4.entities.PathfindingTarget;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;
import com.sillypantscoder.utils.JSONObject;

public abstract class Condition {
	public abstract CommandResult<Boolean> get(MonsterSituation situation);
	public static Condition create(JSONObject object) {
		String type = object.getString("type");
		if (type.equals("and")) return And.create(object);
		else if (type.equals("or")) return Or.create(object);
		else if (type.equals("not")) return Not.create(object);
		else if (type.endsWith("equal-to") || type.endsWith("-than")) return Comparison.create(object);
		else if (type.equals("has-target")) return HasTarget.create(object);
		else if (type.equals("is-entity-type")) return IsEntityType.create(object);
		else throw new IllegalArgumentException("Unknown condition type: " + type);
	}
	public static class And extends Condition {
		public Condition argument1;
		public Condition argument2;
		public And(Condition argument1, Condition argument2) {
			this.argument1 = argument1;
			this.argument2 = argument2;
		}
		public CommandResult<Boolean> get(MonsterSituation situation) {
			CommandResult<Boolean> result = new CommandResult<Boolean>(false, "And");
			return result.setResult(result.addSubResult("Argument 1", this.argument1.get(situation)) && result.addSubResult("Argument 2", this.argument2.get(situation)));
		}
		public static And create(JSONObject object) { return new And(Condition.create(object.getObject("argument1")), Condition.create(object.getObject("argument2"))); }
	}
	public static class Or extends Condition {
		public Condition argument1;
		public Condition argument2;
		public Or(Condition argument1, Condition argument2) {
			this.argument1 = argument1;
			this.argument2 = argument2;
		}
		public CommandResult<Boolean> get(MonsterSituation situation) {
			CommandResult<Boolean> result = new CommandResult<Boolean>(false, "Or");
			return result.setResult(result.addSubResult("Argument 1", this.argument1.get(situation)) || result.addSubResult("Argument 2", this.argument2.get(situation)));
		}
		public static Or create(JSONObject object) { return new Or(Condition.create(object.getObject("argument1")), Condition.create(object.getObject("argument2"))); }
	}
	public static class Not extends Condition {
		public Condition argument;
		public Not(Condition argument) {
			this.argument = argument;
		}
		public CommandResult<Boolean> get(MonsterSituation situation) {
			CommandResult<Boolean> result = new CommandResult<Boolean>(false, "Not");
			return result.setResult(! result.addSubResult("Argument", this.argument.get(situation)));
		}
		public static Not create(JSONObject object) { return new Not(Condition.create(object.getObject("argument"))); }
	}
	public static class Comparison extends Condition {
		public CompareType type;
		public NumberProvider argument1;
		public NumberProvider argument2;
		public Comparison(CompareType type, NumberProvider argument1, NumberProvider argument2) {
			this.type = type;
			this.argument1 = argument1;
			this.argument2 = argument2;
		}
		public CommandResult<Boolean> get(MonsterSituation situation) {
			CommandResult<Boolean> result = new CommandResult<Boolean>(false, this.type.name().charAt(0) + this.type.name().substring(1).toLowerCase().replace("_", " "));
			return result.setResult(this.type.apply(result.addSubResult("Argument 1", this.argument1.get(situation)), result.addSubResult("Argument 2", this.argument2.get(situation))));
		}
		public static Comparison create(JSONObject object) { return new Comparison(CompareType.valueOf(object.getString("type").toUpperCase().replace("-", "_")), NumberProvider.create(object, "argument1"), NumberProvider.create(object, "argument2")); }
		public static enum CompareType {
			LESS_THAN((a, b) -> a < b), GREATER_THAN((a, b) -> a > b), LESS_THAN_EQUAL_TO((a, b) -> a <= b), GREATER_THAN_EQUAL_TO((a, b) -> a >= b), EQUAL_TO((a, b) -> a == b);
			private BiFunction<Double, Double, Boolean> func;
			private CompareType(BiFunction<Double, Double, Boolean> func) {
				this.func = func;
			}
			public boolean apply(double argument1, double argument2) { return this.func.apply(argument1, argument2); }
		}
	}
	public static class HasTarget extends Condition {
		public CompareType minimumTargetType;
		public HasTarget(CompareType minimumTargetType) {
			this.minimumTargetType = minimumTargetType;
		}
		public CommandResult<Boolean> get(MonsterSituation situation) {
			CommandResult<Boolean> result = new CommandResult<Boolean>(false, "Has Target (type: " + this.minimumTargetType.name().toLowerCase() + ")");
			return result.setResult(this.minimumTargetType.includes(situation.self, situation.target));
		}
		public static HasTarget create(JSONObject object) { return new HasTarget(CompareType.valueOf(object.getString("minimum_target_type").toUpperCase())); }
		public static enum CompareType {
			ANY((m, v) -> v.isPresent()), ENTITY((m, v) -> v.map((t) -> t instanceof TileEntity).orElse(false)), LIVING_ENTITY((m, v) -> v.map((t) -> t instanceof LivingEntity).orElse(false)),
				ATTACKABLE((m, v) -> v.map((t) -> {
					if (t instanceof LivingEntity livingEntity) {
						if (Math.abs(livingEntity.x - m.x) <= 1 && Math.abs(livingEntity.y - m.y) <= 1) {
							return true;
						}
					}
					return false;
				}).orElse(false));
			private BiFunction<Monster, Optional<PathfindingTarget>, Boolean> func;
			private CompareType(BiFunction<Monster, Optional<PathfindingTarget>, Boolean> func) {
				this.func = func;
			}
			public boolean includes(Monster src, Optional<PathfindingTarget> target) { return this.func.apply(src, target); }
		}
	}
	public static class IsEntityType extends Condition {
		public boolean useCurrentTarget;
		public String entityType;
		public IsEntityType(boolean useCurrentTarget, String entityType) {
			this.useCurrentTarget = useCurrentTarget;
			this.entityType = entityType;
		}
		public CommandResult<Boolean> get(MonsterSituation situation) {
			CommandResult<Boolean> result = new CommandResult<Boolean>(false, "Is Entity Type (using current target: " + this.useCurrentTarget + ", entity type: " + this.entityType + ")");
			return result.setResult(checkIsEntityType((this.useCurrentTarget ? situation.current : situation.target).orElse(null), entityType));
		}
		public static IsEntityType create(JSONObject object) { return new IsEntityType(object.getBoolean("use_current_target"), object.getString("entity_type")); }
		public static boolean checkIsEntityType(PathfindingTarget target, String type) {
			if (target == null) return false;
			if (target instanceof TileEntity targetEntity) {
				// Check targetEntity for type
				if (type.equals(targetEntity.getTypeID())) {
					return true;
				} else {
					return false;
				}
			} else return false;
		}
	}
}
