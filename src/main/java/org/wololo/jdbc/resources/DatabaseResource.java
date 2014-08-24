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

import model.Database;

import org.wololo.jdbc.Server;

@Path("db/{databaseName}")
public class DatabaseResource {
	
	@PathParam("databaseName") String databaseName;
	
	@GET
	@Produces("application/json")
	public Database get() throws SQLException {
		try (Connection connection = Server.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			Database database = new Database();
			database.name = databaseName;
			database.children = getSchemaNames(meta).toArray(new String[] {});
			return database;
		}
	}
	
	List<String> getSchemaNames(DatabaseMetaData meta) throws SQLException {
		List<String> schemaNames = new ArrayList<String>();
		try (ResultSet schemas = meta.getSchemas(databaseName, null)) {
			while(schemas.next()) {
				String schemaName = schemas.getString(1);
				schemaNames.add("schemas/" + schemaName);
			}
		}
		return schemaNames;
	}
}
