package com.sillypantscoder.pixeldungeon4.entitydef;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Random;

public abstract class NumberProvider {
	public abstract double get(MonsterSituation situation);
	public static NumberProvider create(JSONObject object, String key) {
		if (object.entries_number.containsKey(key)) {
			return new PlainNumber(object.getNumber(key));
		} else if (object.entries_string.containsKey(key)) {
			return new Variable(object.getString(key));
		} else {
			return NumberProvider.create(object.getObject(key));
		}
	}
	public static NumberProvider create(JSONObject object) {
		String type = object.getString("type");
		if (type.equals("math")) return NPMath.create(object);
		else if (type.equals("round")) return Round.create(object);
		else if (type.equals("uniform")) return Uniform.create(object);
		else if (type.equals("uniform-float")) return UniformFloat.create(object);
		else if (type.equals("triangular")) return Triangular.create(object);
		else if (type.equals("if")) return If.create(object);
		else throw new IllegalArgumentException("Unknown number provider type: " + type);
	}
	public static class PlainNumber extends NumberProvider {
		public double n;
		public PlainNumber(double n) {
			this.n = n;
		}
		public double get(MonsterSituation situation) { return this.n; }
	}
	public static class Variable extends NumberProvider {
		public String name;
		public Variable(String name) {
			this.name = name;
		}
		public double get(MonsterSituation situation) { return situation.get(this.name); }
	}
	public static class NPMath extends NumberProvider {
		public MathOperation operation;
		public NumberProvider argument1;
		public NumberProvider argument2;
		public NPMath(MathOperation operation, NumberProvider argument1, NumberProvider argument2) {
			this.operation = operation;
			this.argument1 = argument1;
			this.argument2 = argument2;
		}
		public double get(MonsterSituation situation) { return this.operation.apply(this.argument1.get(situation), this.argument2.get(situation)); }
		public static NPMath create(JSONObject object) { return new NPMath(MathOperation.valueOf(object.getString("operation").toUpperCase()), NumberProvider.create(object, "argument1"), NumberProvider.create(object, "argument2")); }
		public static enum MathOperation {
			ADD((a, b) -> a + b), SUBTRACT((a, b) -> a - b), MULTIPLY((a, b) -> a * b), DIVIDE((a, b) -> a / b), EXPONENT((a, b) -> Math.pow(a, b));
			private BiFunction<Double, Double, Double> func;
			private MathOperation(BiFunction<Double, Double, Double> func) {
				this.func = func;
			}
			public double apply(double argument1, double argument2) { return this.func.apply(argument1, argument2); }
		}
	}
	public static class Round extends NumberProvider {
		public RoundingMode mode;
		public NumberProvider argument;
		public Round(RoundingMode mode, NumberProvider argument) {
			this.mode = mode;
			this.argument = argument;
		}
		public double get(MonsterSituation situation) { return this.mode.apply(this.argument.get(situation)); }
		public static Round create(JSONObject object) { return new Round(RoundingMode.valueOf(object.getString("mode")), NumberProvider.create(object, "argument")); }
		public static enum RoundingMode {
			NORMAL((a) -> (double)(Math.round(a))), FLOOR((a) -> (double)(Math.floor(a))), CEILING((a) -> (double)(Math.ceil(a)));
			private Function<Double, Double> func;
			private RoundingMode(Function<Double, Double> func) {
				this.func = func;
			}
			public double apply(double argument1) { return this.func.apply(argument1); }
		}
	}
	public static class Uniform extends NumberProvider {
		public NumberProvider min;
		public NumberProvider max;
		public Uniform(NumberProvider min, NumberProvider max) {
			this.min = min;
			this.max = max;
		}
		public double get(MonsterSituation situation) { return Random.randint(Math.round(this.min.get(situation)), Math.round(this.max.get(situation))); }
		public static Uniform create(JSONObject object) { return new Uniform(NumberProvider.create(object, "min"), NumberProvider.create(object, "max")); }
	}
	public static class UniformFloat extends NumberProvider {
		public NumberProvider min;
		public NumberProvider max;
		public UniformFloat(NumberProvider min, NumberProvider max) {
			this.min = min;
			this.max = max;
		}
		public double get(MonsterSituation situation) {
			double min = this.min.get(situation);
			double max = this.max.get(situation);
			double generatedFloat = Random.randfloat(min, max);
			return generatedFloat;
		}
		public static UniformFloat create(JSONObject object) { return new UniformFloat(NumberProvider.create(object, "min"), NumberProvider.create(object, "max")); }
	}
	public static class Triangular extends NumberProvider {
		public NumberProvider min;
		public NumberProvider center;
		public NumberProvider max;
		public Triangular(NumberProvider min, NumberProvider center, NumberProvider max) {
			this.min = min;
			this.center = center;
			this.max = max;
		}
		public double get(MonsterSituation situation) { return Random.triangular(this.min.get(situation), this.center.get(situation), this.max.get(situation)); }
		public static Triangular create(JSONObject object) { return new Triangular(NumberProvider.create(object, "min"), NumberProvider.create(object, "center"), NumberProvider.create(object, "max")); }
	}
	public static class If extends NumberProvider {
		public Condition condition;
		public NumberProvider ifTrue;
		public NumberProvider ifFalse;
		public If(Condition condition, NumberProvider ifTrue, NumberProvider ifFalse) {
			this.condition = condition;
			this.ifTrue = ifTrue;
			this.ifFalse = ifFalse;
		}
		public double get(MonsterSituation situation) { if (this.condition.get(situation).result) { return this.ifTrue.get(situation); } else { return this.ifFalse.get(situation); } }
		public static If create(JSONObject object) { return new If(Condition.create(object.getObject("condition")), NumberProvider.create(object, "ifTrue"), NumberProvider.create(object, "ifFalse")); }
	}
}
