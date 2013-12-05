package org.driedtoast.util;

public class CollectionUtil {
	
	/**
	 * Joins a string array based on the delimiter
	 * 
	 * @param strings
	 * @return
	 */
	public static String join(String[] strings) {
		return join(strings, ",");
	}
	public static String join(String[] strings, String delim) {
		return join(strings, ",", false);
	}
	public static String join(String[] strings, String delim, boolean toUpper) {
		StringBuilder sb = new StringBuilder();
		boolean start = true;
		for(String string: strings) {
			if (start) { start = false; } else { sb.append(delim); }
			String appendString = toUpper ? string.toUpperCase() : string;
			sb.append(appendString);
		}
		return sb.toString();
	}
	
	
	
	public static boolean contains(Class<?>[] classes, Class<?> clazz) {
		for(Class<?> cls: classes) {
			if (cls.equals(clazz)) {
				return true;
			}
		}
		return false;
	}
}
