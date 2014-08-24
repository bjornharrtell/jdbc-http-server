package org.wololo.jdbc.resources;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import model.Schema;

import org.wololo.jdbc.Server;

public class SchemaResource {
	@GET
	@Produces("application/json")
	public Schema get(@PathParam("schemaName") String schemaName) throws SQLException {
		try (Connection connection = Server.getConnection()) {
			Schema schema = new Schema();
			schema.name = schemaName;
			return schema;
		}
	}
	
	@Path("tables")
	public TablesResource tables() {
		return new TablesResource();
	}
}
