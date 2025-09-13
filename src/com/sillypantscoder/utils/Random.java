package com.sillypantscoder.utils;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Random {
	protected static java.util.Random r = new java.util.Random();
	public static double random() {
		return r.nextDouble(1);
	}
	public static int randint(int start, int end) {
		IntStream s = r.ints(start, end + 1);
		return s.iterator().next();
	}
	public static long randint(long start, long end) {
		LongStream s = r.longs(start, end + 1);
		return s.iterator().next();
	}
	public static double randfloat(double start, double end) {
		DoubleStream s = r.doubles(start, end);
		return s.iterator().next();
	}
	public static double triangular(double start, double center, double end) {
		double u = random();
		if (u < 0.5) {
			return start + Math.sqrt(u * (center - start) * (end - start));
		} else {
			return end - Math.sqrt((1 - u) * (end - center) * (end - start));
		}
	}
	public static<T> T choice(T[] items) {
		return items[randint(0, items.length - 1)];
	}
	public static<T> T choice(List<T> items) {
		if (items.size() == 0) throw new IndexOutOfBoundsException("Cannot choose from empty list");
		return items.get(randint(0, items.size() - 1));
	}
	public static<T> T choice(List<T> items, List<Double> weights) {
		if (items.size() != weights.size()) throw new IllegalArgumentException("Items and weights must be the same size");
		double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
		double r = randfloat(0, totalWeight);
		double cumulativeWeight = 0;
		for (int i = 0; i < items.size(); i++) {
			cumulativeWeight += weights.get(i);
			if (r <= cumulativeWeight) return items.get(i);
		}
		return items.get(items.size() - 1);
	}
	public static<T> T[] shuffle(T[] items) {
		for (int i = 0; i < items.length; i++) {
			int rand_index = randint(0, items.length - 1);
			T original = items[i];
			T other = items[rand_index];
			items[i] = other;
			items[rand_index] = original;
		}
		return items;
	}
	public static int randomInt() {
		return r.nextInt();
	}
}
