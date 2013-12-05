package org.driedtoast.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StringUtil {
	
	private static final Logger logger = Logger.getLogger(StringUtil.class.getName());

	/**
	 * Uppers first letter 
	 * @param str
	 * @return
	 */
	public static String upperFirst(String str) {
		if (str == null) {
			return str;
		}
		return ("" + str.charAt(0)).toUpperCase() + str.substring(1);
	}

	
	protected static SimpleDateFormat getDateFormatter() {
		SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
		return formatter;
	}
	
	public static Date toDate(String str) {
		SimpleDateFormat formatter = getDateFormatter();		
		try {
			return formatter.parse(str);
		} catch (ParseException e) {
			logger.log(Level.INFO, "Date is not formatting correctly for " + str, e);
			return new Date();
		}
	}
	
	public static String fromDate(Date date) {
		SimpleDateFormat formatter = getDateFormatter();	
		return formatter.format(date);
	}
}
