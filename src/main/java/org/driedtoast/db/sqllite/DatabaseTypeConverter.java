package org.driedtoast.db.sqllite;

import java.util.Date;

import org.driedtoast.util.StringUtil;

public class DatabaseTypeConverter {

	public static final Class<?>[] SUPPORTED_TYPES = {String.class, Boolean.class, Date.class, Integer.class, Long.class};
	
	/**
	 * Converts from the database to object type
	 * 
	 */
	public static Object from(Object value, Class<?> clazz) {
		if (value == null) {
			return value;
		}
		if(value.getClass().getSuperclass().equals(Number.class)) {
			Number number = (Number)value;
			if(clazz.equals(Integer.class)) {
				value = number.intValue();
			} else if(clazz.equals(Long.class)) {
				value = number.longValue();
			}
		}
		
		if(clazz.equals(Boolean.class)) {
			return (Integer.parseInt(value.toString()) == 1);
		} else if(clazz.equals(Date.class)) {
			return StringUtil.toDate(value.toString());
		}
		return value;		
	}

	
	/**
	 * Convert boolean to db type
	 * 
	 */
	public static Object to(Boolean bool) {
		if (bool) {
			return 1;
		}
		return 0;
	}
	
	public static Object to(Object value) {
		return value;
	}
	
	public static Object to(Date date) {
		return StringUtil.fromDate(date);
	}
}
