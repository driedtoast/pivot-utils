package org.driedtoast.db.sqllite;

import junit.framework.TestCase;

import org.driedtoast.db.DatabaseService;
import org.driedtoast.db.TestModel;
import org.junit.Test;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class TableMigratorTest extends TestCase {
	
	@Test
	public void testTableCreation() throws Exception {
		DatabaseService<SqlJetDb> service = TestDbHelper.createDatabaseService();
		TableMigrator migrator = new TableMigrator(service);
		migrator.createTable(TestModel.class);
		assertNotNull(service.getDb().getTable("TESTMODEL"));
	}
	
	
	@Test
	@SuppressWarnings({"unchecked","rawtypes"})
	public void testModelTableCreation() throws Exception {
		DatabaseService<SqlJetDb> service = TestDbHelper.createDatabaseService();
		TableMigrator migrator = new TableMigrator(service);
		Class<?>[] models = {TestModel.class};
		migrator.generateTables(models);
		for(Class<?> modelClass: models) {
			GenericDao<?> dao = new GenericDao(modelClass, service);
			assertNotNull(service.getDb().getTable(dao.tableName()));
		}
	
	}
}
