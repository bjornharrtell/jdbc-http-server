package org.wololo.jdbc.resources;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import model.Databases;

import org.wololo.jdbc.Server;

@Path("db")
public class DatabasesResource {
	@GET
	@Produces("application/json")
	public Databases get() throws SQLException {
		try (Connection connection = Server.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			Databases databases = new Databases();
			databases.children = getCatalogNames(meta).toArray(new String[] {});
			return databases;
		}
	}
	
	List<String> getCatalogNames(DatabaseMetaData meta) throws SQLException {
		List<String> catalogNames = new ArrayList<String>();
		try (ResultSet catalogs = meta.getCatalogs()) {
			while(catalogs.next()) {
				String catalog = catalogs.getString(1);
				catalogNames.add(catalog);
			}
		}
		return catalogNames;
	}
	
	@Path("{databaseName}")
	public DatabaseResource database() {
		return new DatabaseResource();
	}
}
