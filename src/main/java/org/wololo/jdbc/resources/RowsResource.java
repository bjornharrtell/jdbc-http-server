package org.wololo.jdbc.resources;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jdbc.Server;

public class RowsResource {
	final static Logger logger = LoggerFactory.getLogger(RowsResource.class);
	
	@GET
	@Produces("application/json")
	public List<Object[]> get(
			@PathParam("databaseName") String databaseName,
			@PathParam("schemaName") String schemaName,
			@PathParam("tableName") String tableName) throws SQLException {
		try (Connection connection = Server.getConnection()) {
			Statement statement = connection.createStatement();
			String sql = "select * from " + schemaName + "." + tableName;
			logger.debug(sql);
			ResultSet resultSet = statement.executeQuery(sql);
			List<Object[]> rows = new ArrayList<Object[]>();
		    while (resultSet.next()) {
		    	Object c1 = resultSet.getObject(1);
		    	Object c2 = resultSet.getObject(2);
		    	Object[] row = new Object[] { c1, c2 };
		    	rows.add(row);
		    }
			
			return rows;
		}
	}
	
	@Path("{id}")
	public RowResource row() {
		return new RowResource();
	}
}
