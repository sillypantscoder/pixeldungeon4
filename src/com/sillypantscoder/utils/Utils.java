package com.sillypantscoder.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class contains various random utilities used by the rest of the code.
 */
public class Utils {
	public static<T> void log(T item) {
		System.out.println(getLog(item));
	}
	public static<T> String getLog(T item) {
		if (item instanceof String) return "\"" + item + "\"";
		else if (item instanceof Number) return item.toString();
		else if (item instanceof Object[] itemList) return logArray(itemList);
		else if (item instanceof int[] itemList) return logArray(itemList);
		else if (item == null) return "null";
		else {
			String s = item.toString();
			return s;
		}
	}
	public static<T> String logArray(T[] items) {
		String result = "Object[" + items.length + "] {";
		String[] strItems = new String[items.length];
		for (var i = 0; i < items.length; i++) strItems[i] = getLog(items[i]);
		for (var i = 0; i < strItems.length; i++) {
			if (i != 0) {
				result += ", ";
			} else result += " ";
			result += strItems[i];
		}
		result += " }";
		return result;
	}
	public static String logArray(int[] items) {
		String result = "int[" + items.length + "] {";
		String[] strItems = new String[items.length];
		for (var i = 0; i < items.length; i++) strItems[i] = items[i] + "";
		for (var i = 0; i < strItems.length; i++) {
			if (i != 0) {
				result += ", ";
			} else result += " ";
			result += getLog(strItems[i]);
		}
		result += " }";
		return result;
	}
	public static String readFile(File file) {
		try {
			Scanner fileReader = new Scanner(file);
			String allData = "";
			while (fileReader.hasNextLine()) {
				String data = fileReader.nextLine();
				allData += data + "\n";
			}
			fileReader.close();
			// Parse the data
			return allData;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "Error finding file...";
		}
	}
	public static String readFile(String filename) {
		return readFile(new File(filename));
	}
	public static String getResource(String filename) {
		return readFile(AssetLoader.getResource(filename));
	}
	public static String decodeURIComponent(String in) {
		try {
			return URLDecoder.decode(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return in;
		}
	}
	public static double map(double x, double fromStart, double fromEnd, double toStart, double toEnd) {
		return (x - fromStart) / (fromEnd - fromStart) * (toEnd - toStart) + toStart;
	}
	public static double[] normalize(double x, double y) {
		double dist = Math.sqrt((x * x) + (y * y));
		double newX = x / dist;
		double newY = y / dist;
		return new double[] { newX, newY };
	}
	public static<T> ArrayList<T> arrayToArrayList(T[] items) {
		ArrayList<T> arr = new ArrayList<T>();
		for (T item : items) {
			arr.add(item);
		}
		return arr;
	}
	public static byte[] zipFiles(Map<String, byte[]> files) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ZipOutputStream zipStream = new ZipOutputStream(stream);
		for (String filename : files.keySet()) {
			try {
				ZipEntry entry = new ZipEntry(filename);
				zipStream.putNextEntry(entry);
				zipStream.write(files.get(filename));
				zipStream.closeEntry();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			zipStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream.toByteArray();
	}
}
