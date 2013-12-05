package org.driedtoast.db.sqllite;

import java.io.File;

public class TestDatabaseService extends SqlLiteDatabaseService {
	
	public TestDatabaseService(Class<?>[] models) {
		super(models);
	}

	@Override
	public void startup() throws Exception {
		super.startup(true);
	}
	
	@Override
	public String getDatabaseUrl() {
	   return ".." + File.pathSeparator + "do-test-data.db";
	}
	
	@Override
	protected void runMigrations() {
		
	}
	
}
