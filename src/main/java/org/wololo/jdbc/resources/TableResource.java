package org.wololo.jdbc.resources;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import model.Table;

import org.wololo.jdbc.Server;

@Path("db/{databaseName}/schemas/{schemaName}/tables/{tableName}")
public class TableResource {

	@PathParam("tableName") String tableName;
	
	@GET
	@Produces("application/json")
	public Table get() throws SQLException {
		try (Connection connection = Server.getConnection()) {
			Table table = new Table();
			table.name = tableName;
			return table;
		}
	}
}
