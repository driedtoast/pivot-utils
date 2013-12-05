package org.driedtoast.ui;

import java.net.URL;

/**
 * Used to get a predictable view path for the bxml files
 * 
 * @author dmarchant
 *
 */
public class ViewManager {

	public static URL getView(String view) {
		return ViewManager.class.getResource(view);
	}

	
	
	
	// TODO create a stack / card manager?
}
