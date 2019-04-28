package com.protryon.jasm.reflection;

import java.util.concurrent.ConcurrentHashMap;

public class AppClassLoaderBridge {
	public static final ConcurrentHashMap<Integer, byte[]> sunrsrc = new ConcurrentHashMap<Integer, byte[]>();
	public static int nextID = 0;
}
