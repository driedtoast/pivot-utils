package org.driedtoast.db.sqllite;

import java.io.File;

import org.driedtoast.db.DatabaseService;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SqlLiteDatabaseService implements DatabaseService<SqlJetDb> {

	public static final String DB_FILE = ".." + File.separator + "app-data.db"; 
	private SqlJetDb db = null;
	private Class<?>[] models;
	
	public SqlLiteDatabaseService(Class<?>[] models) {
		this.models = models;
	}
	
	/**
	 * Starts up the database to be used within the application
	 * 
	 * @throws Exception
	 */
	public void startup() throws Exception {
		startup(false);
	}
	public void startup(boolean reset) throws Exception {
		if (reset) {
		  File dbFile = new File(getDatabaseUrl());
		  dbFile.delete();
		}
		db = SqlJetDb.open(new File(getDatabaseUrl()), true);	
		// db.getOptions().setAutovacuum(true);
		db.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
		  db.getOptions().setUserVersion(1);
		} finally {
		  db.commit();
		}
		runMigrations();
	}
	
	protected void runMigrations() {
		TableMigrator migrator = new TableMigrator(this);
		migrator.generateTables(models);		
	}

	public String getDatabaseUrl() {
		return DB_FILE;
	}
	
	public SqlJetDb getDb()
	{
		return this.db;
	}
	
	/**
	 * Shutdown the db
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception {
		if(db != null) {
			db.close();
		}
	}
	
	
}
