package org.wololo.jdbc.resources;

import static org.jooq.impl.DSL.delete;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.update;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jooq.Record;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("db/{databaseName}/schemas/{schemaName}/tables/{tableName}/rows/{id}")
public class RowResource extends DataSourceResource {
	
	final static Logger logger = LoggerFactory.getLogger(RowResource.class);
	
	@PathParam("databaseName") String databaseName;
	@PathParam("schemaName") String schemaName;
	@PathParam("tableName") String tableName;
	@PathParam("id") String id;
	
	@GET
	@Produces("application/json")
	public Map<String, Object> get() throws SQLException {
		try (Connection connection = ds.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			
			String primaryKey = getPrimaryKey(meta);
			final String sql = select(field("*"))
					.from(schemaName + "." + tableName)
					.where(field(primaryKey).equal(id))
					.toString();
			logger.debug(sql);
			
			try (
					final Statement statement = connection.createStatement();
					final ResultSet resultSet = statement.executeQuery(sql)) {
				resultSet.next();
				List<String> columns = getColumns(meta);
				Map<String, Object> row = new HashMap<String, Object>();
				for (String column : columns) {
					row.put(column, resultSet.getObject(column));
				}
				
				return row;
			}
		}
	}
	
	@PUT
	public void put(Map<String, Object> row) throws SQLException {
		try (Connection connection = ds.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			
			String primaryKey = getPrimaryKey(meta);
			
			UpdateSetFirstStep<Record> sqlTemp = update(table(tableName));
			UpdateSetMoreStep<Record> build = null;
			for(Entry<String, Object> entry : row.entrySet()) {
				build = sqlTemp.set(field(entry.getKey()), entry.getValue());
			}
			final String sql = build.where(field(primaryKey).equal(id)).toString();
			logger.debug(sql);
			
			try (final Statement statement = connection.createStatement()) {
				final int result = statement.executeUpdate(sql);
				if (result == 0) {
					throw new RuntimeException("No rows was found and updated with id " + id);
				} else if (result > 1) {
					// TODO: rollback transaction?
					throw new RuntimeException("Multiple rows was found and updated with id " + id);
				}
			}
		}
	}
	
	@DELETE
	public void del() throws SQLException {
		try (Connection connection = ds.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			
			String primaryKey = getPrimaryKey(meta);

			final String sql = delete(table(tableName)).where(field(primaryKey).equal(coercedPK())).toString();
			logger.debug(sql);
			
			try (final Statement statement = connection.createStatement()) {
				final int result = statement.executeUpdate(sql);
				if (result == 0) {
					throw new RuntimeException("No rows was found and deleted with id " + id);
				} else if (result > 1) {
					// TODO: rollback transaction?
					throw new RuntimeException("Multiple rows was found and deleted with id " + id);
				}
			}
		}
	}
	
	Object coercedPK() {
		try { 
	        return Integer.parseInt(id); 
	    } catch(NumberFormatException e) { 
	        return id; 
	    }
	}
	
	List<String> getColumns(DatabaseMetaData meta) throws SQLException {
		try (ResultSet resultSet = meta.getColumns(databaseName, schemaName, tableName, null)) {
			List<String> columnNames = new ArrayList<String>();
			while (resultSet.next()) {
				String columnName = resultSet.getString(4);
				columnNames.add(columnName);
			}
			return columnNames;
		}
	}
	
	String getPrimaryKey(DatabaseMetaData meta) throws SQLException {
		try (ResultSet resultSet = meta.getPrimaryKeys(databaseName, schemaName, tableName)) {
			resultSet.next();
			String columnName = resultSet.getString(4);
			return columnName;
		}
	}
}
