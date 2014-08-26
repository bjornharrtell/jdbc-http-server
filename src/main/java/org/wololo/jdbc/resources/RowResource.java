package org.wololo.jdbc.resources;

import static org.jooq.impl.DSL.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("db/{databaseName}/schemas/{schemaName}/tables/{tableName}/rows/{id}")
public class RowResource extends DataSourceResource {
	
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
