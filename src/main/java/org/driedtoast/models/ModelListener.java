package org.driedtoast.models;

public interface ModelListener<T> {
	
	public enum EventType {
		ADD,
		REMOVE,
		UPDATE;
	}
	
	void trigger(EventType evt, T model);
}
