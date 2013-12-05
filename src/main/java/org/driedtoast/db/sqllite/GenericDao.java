package org.driedtoast.db.sqllite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.driedtoast.db.DatabaseService;
import org.driedtoast.db.Indexed;
import org.driedtoast.db.Primary;
import org.driedtoast.util.CollectionUtil;
import org.driedtoast.util.StringUtil;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * Simple Data access object that takes a model and figures out what to do with
 * the rest.
 * 
 * @author dmarchant
 */
public class GenericDao<T> {

	private static final Logger logger = Logger.getLogger(GenericDao.class
			.getName());
	public static final String ID = "id";

	private Class<T> modelClass;
	private DatabaseService<SqlJetDb> service;
	private Map<Class<?>, String> types = new HashMap<Class<?>, String>();
	private Map<String, Method> readMethodCache = new HashMap<String, Method>();
	private List<String> fieldOrder = new ArrayList<String>();

	public GenericDao(Class<T> clazz, DatabaseService<SqlJetDb> db) {
		modelClass = clazz;
		this.service = db;
		initialize();
	}

	public void initialize() {
		types.put(String.class, "TEXT");
		types.put(Date.class, "TEXT");
		types.put(Number.class, "INTEGER");
		types.put(Integer.class, "INTEGER");
		types.put(Double.class, "REAL");

		Field[] fields = modelClass.getDeclaredFields();
		for (Field field : fields) {
			if (field.getModifiers() != Modifier.PRIVATE)
				continue;
			String name = field.getName();
			if (!CollectionUtil.contains(DatabaseTypeConverter.SUPPORTED_TYPES, field.getType())) 
				continue;
			String getMethod = "get" + StringUtil.upperFirst(name);
			try {
				Method method = modelClass.getMethod(getMethod);
				readMethodCache.put(name, method);
				fieldOrder.add(name);
			} catch (Exception e) {
				logger.log(Level.INFO, "Issue with getting getmethod for "
						+ name, e);
			}
		}
	}

	public String tableName() {
		return modelClass.getSimpleName().toUpperCase();
	}

	/**
	 * Returns a Create table string. Example: CREATE TABLE test (id TEXT NOT
	 * NULL PRIMARY KEY, name TEXT)
	 * 
	 * @return
	 */
	protected String createTableStatement() {
		StringBuilder createSql = new StringBuilder();
		Field[] fields = modelClass.getDeclaredFields();
		createSql.append("CREATE TABLE ");
		createSql.append(tableName());
		createSql.append("(");
		boolean start = true;
		for (Field field : fields) {
			if (field.getModifiers() != Modifier.PRIVATE)
				continue;

			if (start) {
				start = false;
			} else {
				createSql.append(",");
			}
			createSql.append(field.getName().toUpperCase());
			createSql.append(" ");
			createSql.append(types.get(field.getType()));
			if (field.getAnnotation(Primary.class) != null) {
				createSql.append("NOT NULL PRIMARY KEY");
			}
		}
		createSql.append(");");
		return createSql.toString();
	}

	/**
	 * Creates a list of index create statements based on the Indexed annotation
	 * 
	 * Need to create an index for ever type of query that needs to be made.
	 * 
	 * TODO figure out where clauses + index creation?
	 */
	protected List<String> createIndexStatements() {
		List<String> indexes = new ArrayList<String>();

		Field[] fields = modelClass.getDeclaredFields();
		for (Field field : fields) {
			if (field.getModifiers() != Modifier.PRIVATE) {
				continue;
			}
			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(Primary.class)) {
					StringBuilder indexSql = new StringBuilder();
					indexSql.append("CREATE INDEX ");
					indexSql.append(tableName());
					indexSql.append("_");
					indexSql.append(ID.toUpperCase());
					indexSql.append(" ON ");
					indexSql.append(tableName());
					indexSql.append("(");
					indexSql.append(ID.toUpperCase());
					indexSql.append(")");
					indexes.add(indexSql.toString());
					continue;
				}
				if (!annotation.annotationType().equals(Indexed.class))
					continue;

				Indexed indexed = (Indexed) annotation;
				StringBuilder indexSql = new StringBuilder();
				indexSql.append("CREATE INDEX ");
				indexSql.append(tableName());
				indexSql.append("_");
				indexSql.append(indexed.name().toUpperCase());
				indexSql.append(" ON ");
				indexSql.append(tableName());
				indexSql.append("(");
				indexSql.append(CollectionUtil.join(indexed.fieldNames(), ",",
						true));
				indexSql.append(")");
				indexes.add(indexSql.toString());
			}
		}
		return indexes;
	}

	/**
	 * Inserts the model into the db
	 * 
	 * @param model
	 * @return
	 */
	public boolean insert(T model) {
		SqlJetDb db = service.getDb();
		try {
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				ISqlJetTable table = db.getTable(tableName());
				List<Object> values = getDbValues(model, true);
				table.insert(values.toArray());
			} finally {
				db.commit();
			}
			return true;
		} catch (Exception sqle) {
			sqle.printStackTrace();
			logger.log(Level.SEVERE, "Issue with inserting record ", sqle);
		}
		return false;
	}

	/**
	 * Updates the model
	 * 
	 * @param model
	 * @return
	 */
	public boolean update(T model) {
		SqlJetDb db = service.getDb();
		try {
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				ISqlJetTable table = db.getTable(tableName());
				Method method = readMethodCache.get(ID.toLowerCase());
				Object id = method.invoke(model);
				
				ISqlJetCursor cursor = table.lookup(tableName() + "_" + ID.toUpperCase(), id);
				if (cursor.getRowCount() == 0) {
					return false;
				}
				List<Object> values = getDbValues(model, true);
				cursor.first();
				cursor.goTo(cursor.getRowId());
				cursor.update(values.toArray());
				cursor.close();
				return true;
			} finally {
				db.commit();
			}
		} catch (Exception sqle) {
			logger.log(Level.SEVERE, "Issue with selecting record ", sqle);
		}

		return false;
	}
	
	/**
	 * Finds objects by an index suffix and returns the results
	 * 
	 */
	public List<T> findByIndex(String index, Object...values) {
		List<T> results = new ArrayList<T>();
		
		SqlJetDb db = service.getDb();
		try {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			try {
				ISqlJetTable table = db.getTable(tableName());
				ISqlJetCursor cursor = table.lookup(
						tableName() + "_" + index.toUpperCase(), values);
				if (cursor.getRowCount() == 0) {
					return results;
				}
				do {
					results.add(createModelFromDb(cursor));
				} while (cursor.next());
				cursor.close();
				return results;
			} finally {
				db.commit();
			}

		} catch (Exception sqle) {
			logger.log(Level.SEVERE, "Issue with selecting record ", sqle);
		}
		return results;
	}

	/**
	 * Finds a specific model based on id
	 * 
	 * @param id
	 * @return
	 */
	public T find(String id) {
		SqlJetDb db = service.getDb();
		try {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			try {
				ISqlJetTable table = db.getTable(tableName());
				ISqlJetCursor cursor = table.lookup(
						tableName() + "_" + ID.toUpperCase(), id);
				if (cursor.getRowCount() == 0) {
					return null;
				}
				cursor.first();
				T model = createModelFromDb(cursor);
				cursor.close();
				return model;
			} finally {
				db.commit();
			}

		} catch (Exception sqle) {
			logger.log(Level.SEVERE, "Issue with selecting record ", sqle);
		}
		return null;
	}
	
	public long count() {
		long count = 0;
		SqlJetDb db = service.getDb();
		try {
			try {
			  db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			  ISqlJetTable table = db.getTable(tableName());
			  ISqlJetCursor cursor = table.open();
			  return cursor.getRowCount();			  
			} finally {
			  db.commit();
			}
		} catch (Exception sqle) {
			logger.log(Level.SEVERE, "Issue with selecting record ", sqle);
		}
		return count;
	}
				
	
	public List<T> list(int count) {
		List<T> results = new ArrayList<T>();
		
		SqlJetDb db = service.getDb();
		try {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			try {
				ISqlJetTable table = db.getTable(tableName());
				ISqlJetCursor cursor = table.open();
				if (cursor.getRowCount() == 0) {
					return results;
				}
				int rowcount = 0;
				do {
					if(rowcount >= count) {
						break;
					}
					results.add(createModelFromDb(cursor));
					rowcount++;
				} while (cursor.next());
				cursor.close();
				return results;
			} finally {
				db.commit();
			}

		} catch (Exception sqle) {
			logger.log(Level.SEVERE, "Issue with selecting record ", sqle);
		}
		return results;
	}
	
	
	protected List<Object> getDbValues(T model, boolean generateId) throws Exception {
		List<Object> values = new ArrayList<Object>();
		for (String field : fieldOrder) {
			Method method = readMethodCache.get(field);
			Object value = method.invoke(model);
			if (field.equals(ID) && value == null && generateId) {
				value = UUID.randomUUID().toString();
				setModelAttribute(model,field, value);
			} else {
				value = DatabaseTypeConverter.to(method.getReturnType().cast(value));
			}
			values.add(value);
		}
		return values;
	}
	
	protected T createModelFromDb(ISqlJetCursor cursor) throws Exception {
		T model = modelClass.cast(modelClass.newInstance());
		for (String field: fieldOrder) {
			setModelAttribute(model,field, cursor.getValue(field));
		}
		return model;
	}
	
	protected void setModelAttribute(T model, String field, Object value) throws Exception {
		Class<?> fieldType = modelClass.getDeclaredField(field)
				.getType();
		Method setMethod = modelClass.getMethod("set"
				+ StringUtil.upperFirst(field), fieldType);
		value = DatabaseTypeConverter.from(value, fieldType);
		setMethod.invoke(model, fieldType.cast(value));
	}

}
