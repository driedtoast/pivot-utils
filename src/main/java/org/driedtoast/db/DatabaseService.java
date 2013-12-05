package org.driedtoast.db;

public interface DatabaseService<T> {

	public void startup() throws Exception;
	public void startup(boolean reset) throws Exception;
	public void shutdown() throws Exception;
	public T getDb();	
}
