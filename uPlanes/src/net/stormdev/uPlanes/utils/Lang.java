package net.stormdev.uPlanes.utils;

import net.stormdev.uPlanes.main.main;

public class Lang {
public static String get(String key){
    String val = getRaw(key);
    val = Colors.colorise(val);
	return val;
}
public static String getRaw(String key){
	if(!main.lang.contains(key)){
		return key;
	}
	return main.lang.getString(key);
}
}
