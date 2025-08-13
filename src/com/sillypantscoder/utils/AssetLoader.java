package com.sillypantscoder.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * This class allows you to load resources from the "src/resources" directory.
 * It will automatically unpack the JAR file if running from a JAR file.
 */
public class AssetLoader {
	public static long runnerID = (long)(Math.random() * 1000000000);
	/**
	 * Gets the location of the root folder of the java files. If running from a JAR file, gets the location of the file. If running from a folder, gets the root `src` folder.
	 * @return Location of the root folder.
	 */
	public static String getRootLocation() {
		try {
			return new File(AssetLoader.class.getProtectionDomain().getCodeSource().getLocation()
				.toURI()).getPath();
		} catch(URISyntaxException e) {
			return "";
		}
	}
	public static boolean isJar() {
		String addr = AssetLoader.class.getResource("AssetLoader.class").getProtocol();
		return addr == "jar";
	}
	/**
	 * Gets the location of the `src` directory. If running from a JAR file, unpacks the JAR file first.
	 * @return Location of the `src` directory.
	 */
	public static String getResourceLocation() {
		try {
			if (isJar()) {
				if (new File("/tmp/java_assetloader_files_" + runnerID).exists()) {
					return "/tmp/java_assetloader_files_" + runnerID;
				}
				System.out.println("assetloader: unpacking jar file...");
				// Create extraction directory:
				ProcessBuilder pb = new ProcessBuilder("mkdir", "java_assetloader_files_" + runnerID);
				pb.directory(new File("/tmp"));
				pb.start().waitFor();
				// Unpack the jar:
				pb = new ProcessBuilder("jar", "xvf", getRootLocation());
				pb.directory(new File("/tmp/java_assetloader_files_" + runnerID));
				pb.start().waitFor();
				System.out.println("assetloader: unpacked jar file to /tmp/java_assetloader_files_" + runnerID);
				return "/tmp/java_assetloader_files_" + runnerID;
			} else {
				return getRootLocation();
			}
		} catch(IOException e) {
			return "";
		} catch(InterruptedException e) {
			return "";
		}
	}
	protected static ArrayList<String> cacheFileNames = new ArrayList<String>();
	protected static ArrayList<File> cacheFiles = new ArrayList<File>();
	public static File getResource(String name) {
		// Check if the file is already cached:
		for (int i = 0; i < cacheFileNames.size(); i++) {
			if (cacheFileNames.get(i).equals(name)) {
				File f = cacheFiles.get(i);
				return f;
			}
		}
		// If not, find the file:
		String root = getResourceLocation() + "/resources";
		String newName = root + "/" + name;
		File f = new File(newName);
		cacheFileNames.add(name);
		cacheFiles.add(f);
		return f;
	}
}
