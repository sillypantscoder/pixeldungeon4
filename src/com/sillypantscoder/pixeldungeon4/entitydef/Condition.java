package com.sillypantscoder.pixeldungeon4.entitydef;

import java.util.function.BiFunction;

import com.sillypantscoder.utils.JSONObject;

public abstract class Condition {
	public abstract boolean get();
	public static Condition create(JSONObject object) {
		String type = object.getString("type");
		if (type == "and") return And.create(object);
		else if (type == "or") return Or.create(object);
		else if (type == "not") return Not.create(object);
		else if (type.endsWith("equal-to") || type.endsWith("-than")) return Comparison.create(object);
		else throw new IllegalArgumentException("Unknown condition type: " + type);
	}
	public static class And extends Condition {
		public Condition argument1;
		public Condition argument2;
		public And(Condition argument1, Condition argument2) {
			this.argument1 = argument1;
			this.argument2 = argument2;
		}
		public boolean get() { return this.argument1.get() && this.argument2.get(); }
		public static And create(JSONObject object) { return new And(Condition.create(object.getObject("argument1")), Condition.create(object.getObject("argument2"))); }
	}
	public static class Or extends Condition {
		public Condition argument1;
		public Condition argument2;
		public Or(Condition argument1, Condition argument2) {
			this.argument1 = argument1;
			this.argument2 = argument2;
		}
		public boolean get() { return this.argument1.get() || this.argument2.get(); }
		public static Or create(JSONObject object) { return new Or(Condition.create(object.getObject("argument1")), Condition.create(object.getObject("argument2"))); }
	}
	public static class Not extends Condition {
		public Condition argument;
		public Not(Condition argument) {
			this.argument = argument;
		}
		public boolean get() { return ! this.argument.get(); }
		public static Not create(JSONObject object) { return new Not(Condition.create(object.getObject("argument"))); }
	}
	public static class Comparison extends Condition {
		public CompareType type;
		public NumberProvider argument1;
		public NumberProvider argument2;
		public Comparison(CompareType type, NumberProvider argument1, NumberProvider argument2) {
			this.type = type;
			this.argument1 = argument2;
			this.argument2 = argument2;
		}
		public boolean get() { return this.type.apply(this.argument1.get(), this.argument2.get()); }
		public static Comparison create(JSONObject object) { return new Comparison(CompareType.valueOf(object.getString("type")), NumberProvider.create(object, "argument1"), NumberProvider.create(object, "argument2")); }
		public static enum CompareType {
			LESS_THAN((a, b) -> a < b), GREATER_THAN((a, b) -> a > b), LESS_THAN_EQUAL_TO((a, b) -> a <= b), GREATER_THAN_EQUAL_TO((a, b) -> a >= b), EQUAL_TO((a, b) -> a == b);
			private BiFunction<Double, Double, Boolean> func;
			private CompareType(BiFunction<Double, Double, Boolean> func) {
				this.func = func;
			}
			public boolean apply(double argument1, double argument2) { return this.func.apply(argument1, argument2); }
		}
	}
}
