package org.driedtoast.db.sqllite;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.driedtoast.db.DatabaseService;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class TableMigrator {
	
	private static final Logger logger = Logger.getLogger(TableMigrator.class.getName());
	
	
	private DatabaseService<SqlJetDb> service;
	
	public TableMigrator(DatabaseService<SqlJetDb> service) {
		this.service = service;
	}
	
	public void generateTables(Class<?>[] models) {
		// create tables in the db
		for(Class<?> clazz: models) {
			try {
				createTable(clazz);
			} catch (SqlJetException e) {
				// TODO should throw alert or something
				logger.log(Level.SEVERE, "Failed to create table " + clazz.getSimpleName().toUpperCase(), e);
			}
		}
	}
	
	/**
	 * Alters table or creates table based on the database definitions
	 * 
	 * @param clazz
	 * @param db
	 * @throws SqlJetException
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	protected void createTable(Class<?> clazz) throws SqlJetException {
	  SqlJetDb db = service.getDb();
	  GenericDao<?> dao = new GenericDao(clazz, service);
	  // check the existence of tables and than create a table
	  ISqlJetTable table = null;
	  try {
		  table = db.getTable(dao.tableName());
	  } catch(SqlJetException sqle) {
		  // Assume table is not there
		  logger.log(Level.INFO, "Couldn't find table " + dao.tableName() + " going to create it.", sqle);
	  }
	  if(table != null) {
		  // TODO check diff in columns
		  // table.getDefinition().getColumn(name)
		  return;
	  }
	  db.beginTransaction(SqlJetTransactionMode.WRITE);
	  try {            
	    db.createTable( dao.createTableStatement());
	    List<String> indexStatements = dao.createIndexStatements();
	    for(String indexStatement: indexStatements) {
	    	db.createIndex(indexStatement);
	    }
	  } finally {
	    db.commit();
	  }		
	}

}
