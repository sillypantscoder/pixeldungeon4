package com.sillypantscoder.pixeldungeon4.entitydef;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Random;

public abstract class NumberProvider {
	public abstract double get();
	public static NumberProvider create(JSONObject object, String key) {
		if (object.entries_number.containsKey(key)) {
			return new PlainNumber(object.getNumber(key));
		} else {
			return NumberProvider.create(object.getObject(key));
		}
	}
	public static NumberProvider create(JSONObject object) {
		String type = object.getString("type");
		if (type == "math") return NPMath.create(object);
		else if (type == "round") return Round.create(object);
		else if (type == "uniform") return Uniform.create(object);
		else if (type == "uniform-float") return UniformFloat.create(object);
		else if (type == "triangular") return Triangular.create(object);
		else throw new IllegalArgumentException("Unknown number provider type: " + type);
	}
	public static class PlainNumber extends NumberProvider {
		public double n;
		public PlainNumber(double n) {
			this.n = n;
		}
		public double get() { return this.n; }
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
		public double get() { return this.operation.apply(this.argument1.get(), this.argument2.get()); }
		public static NPMath create(JSONObject object) { return new NPMath(MathOperation.valueOf(object.getString("operation")), NumberProvider.create(object, "argument1"), NumberProvider.create(object, "argument2")); }
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
		public double get() { return this.mode.apply(this.argument.get()); }
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
		public double get() { return Random.randint(Math.round(this.min.get()), Math.round(this.max.get())); }
		public static Uniform create(JSONObject object) { return new Uniform(NumberProvider.create(object, "min"), NumberProvider.create(object, "max")); }
	}
	public static class UniformFloat extends NumberProvider {
		public NumberProvider min;
		public NumberProvider max;
		public UniformFloat(NumberProvider min, NumberProvider max) {
			this.min = min;
			this.max = max;
		}
		public double get() { return Random.randfloat(this.min.get(), this.max.get()); }
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
		public double get() { return Random.triangular(this.min.get(), this.center.get(), this.max.get()); }
		public static Triangular create(JSONObject object) { return new Triangular(NumberProvider.create(object, "min"), NumberProvider.create(object, "center"), NumberProvider.create(object, "max")); }
	}
}
