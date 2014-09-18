package org.wololo.jdbc.resources;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import model.Tables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("db/{databaseName}/schemas/{schemaName}/tables")
public class TablesResource extends DataSourceResource {
	final static Logger logger = LoggerFactory.getLogger(TablesResource.class);
	
	@PathParam("databaseName") String databaseName;
	@PathParam("schemaName") String schemaName;
	
	@GET
	@Produces("application/json")
	public Tables get() throws SQLException {
		try (Connection connection = ds.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			Tables tables = new Tables();
			tables.name = schemaName;
			tables.children = getTableNames(meta).toArray(new String[] {});
			return tables;
		}
	}
	
	List<String> getTableNames(DatabaseMetaData meta) throws SQLException {
		List<String> tableNames = new ArrayList<String>();
		try (ResultSet tables = meta.getTables(databaseName, schemaName, null, new String[] {"TABLE"})) {
			while(tables.next()) {
				String tableName = tables.getString(3);
				tableNames.add(tableName);
			}
		}
		return tableNames;
	}
}
