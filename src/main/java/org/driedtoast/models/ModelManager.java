package org.driedtoast.models;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.driedtoast.models.ModelListener.EventType;

public class ModelManager {

	private static ModelManager manager = new ModelManager();
	
	private Map<Class<?>, Set<ModelListener<?>>> listenerMap = new Hashtable<Class<?>, Set<ModelListener<?>>>();
	
	private ModelManager() {		
	}
	
	public static ModelManager getManager() {
		if (manager == null) {
			manager = new ModelManager();
		}
		return manager; 
	}
	
	
	/**
	 * Adds a listener for a particular model
	 * 
	 */
	public void addListener(Class<?> modelClazz, ModelListener<?> listener) {
		Set<ModelListener<?>> listeners = null;
		if ((listeners = listenerMap.get(modelClazz)) == null) {
			listeners = new HashSet<ModelListener<?>>();
			listenerMap.put(modelClazz, listeners);
		}
		
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> void triggerEvent(EventType event, T model) {
		Class<?> objectType = model.getClass();
		Set<ModelListener<?>> listeners = listenerMap.get(objectType);
		if (listeners == null) {
			return;
		}
		Iterator<ModelListener<?>> iterator = listeners.iterator();
		while(iterator.hasNext()) {
			ModelListener<T> listener = (ModelListener<T>)iterator.next();
			listener.trigger(event, model);
		}
	}
	
}
