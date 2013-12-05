package org.driedtoast.ui;



import java.lang.reflect.Method;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;

public abstract class AbstractView<T extends Component> {

	protected Container parent;
	
	
	public AbstractView(Container parent) {
		this.parent = parent;
	}
	
	
	public abstract String getViewLocation();
	
	/**
	 * Subclasses should override this to add actions and such to the view post render
	 * 
	 * @param component
	 */
	public void postRender(T component) {	
	}
	
	@SuppressWarnings("unchecked")
	public void render() {
		BXMLSerializer serializer = new BXMLSerializer();
		try {
			Component viewComponent = (Component) serializer.readObject(ViewManager.getView(getViewLocation()));
			serializer.bind(this);
			// parent.clear();
			Method method = parent.getClass().getMethod("setContent", Component.class);
			if (method == null) { 
				method = parent.getClass().getMethod("setView", Component.class);
			}
			method.invoke(parent, viewComponent);
			postRender((T) viewComponent);
			
			// viewComponent.render();
			// Display display = parent.getDisplay();
			// viewComponent.open(display,parent);
		} catch (Exception e) {
			// TODO some alert
			e.printStackTrace();
		}
	}	
	
}
