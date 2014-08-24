package org.wololo.jdbc.resources;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import model.Schema;

import org.wololo.jdbc.Server;

@Path("db/{databaseName}/schemas/{schemaName}")
public class SchemaResource {
	
	@PathParam("schemaName") String schemaName;
	
	@GET
	@Produces("application/json")
	public Schema get() throws SQLException {
		try (Connection connection = Server.getConnection()) {
			Schema schema = new Schema();
			schema.name = schemaName;
			return schema;
		}
	}
}
