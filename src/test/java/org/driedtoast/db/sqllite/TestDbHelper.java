package org.driedtoast.db.sqllite;

import org.driedtoast.db.DatabaseService;
import org.driedtoast.db.TestModel;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class TestDbHelper {
	
	public static DatabaseService<SqlJetDb> createDatabaseService() throws Exception {
		TestDatabaseService dbservice = new TestDatabaseService(new Class<?>[] {TestModel.class});
		dbservice.startup();
		return dbservice;
	}

}
