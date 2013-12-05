package org.driedtoast.models;

import org.driedtoast.db.Primary;

/**
 * Store configuration data for application setup
 * 
 * @author dmarchant
 *
 */
public class Configuration {

	@Primary
	private String key;
	
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
