package com.sillypantscoder.utils;

import java.util.ArrayList;
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
	public static<T> T choice(ArrayList<T> items) {
		return items.get(randint(0, items.size() - 1));
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
