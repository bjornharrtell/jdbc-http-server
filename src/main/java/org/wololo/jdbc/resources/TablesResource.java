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
import org.wololo.jdbc.Server;

public class TablesResource {
	final static Logger logger = LoggerFactory.getLogger(TablesResource.class);
	
	@GET
	@Produces("application/json")
	public Tables get(@PathParam("catalogName") String catalogName, @PathParam("schemaName") String schemaName) throws SQLException {
		try (Connection connection = Server.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			Tables tables = new Tables();
			tables.name = schemaName;
			tables.children = getTableNames(meta, catalogName, schemaName).toArray(new String[] {});
			return tables;
		}
	}
	
	List<String> getTableNames(DatabaseMetaData meta, String catalog, String schemaName) throws SQLException {
		List<String> tableNames = new ArrayList<String>();
		try (ResultSet tables = meta.getTables(catalog, schemaName, null, null)) {
			while(tables.next()) {
				String tableName = tables.getString(3);
				tableNames.add(tableName);
			}
		}
		return tableNames;
	}
	
	@Path("{tableName}")
	public TableResource table() {
		return new TableResource();
	}
}