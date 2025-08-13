package com.sillypantscoder.utils;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Random {
	protected static java.util.Random r = new java.util.Random();
	public static int randint(int start, int end) {
		IntStream s = r.ints(start, end + 1);
		return s.iterator().next();
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
	public static long randomLong() {
		return r.nextLong();
	}
}
