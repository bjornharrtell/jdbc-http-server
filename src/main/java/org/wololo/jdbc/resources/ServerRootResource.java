package org.wololo.jdbc.resources;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import model.ServerRoot;

@Path("")
public class ServerRootResource extends DataSourceResource {
	@GET
	@Produces("application/json")
	public ServerRoot get() throws SQLException {
		try (Connection connection = ds.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			ServerRoot serverRoot = new ServerRoot();
			serverRoot.version = meta.getDatabaseMajorVersion() * 100 + meta.getDatabaseMinorVersion() * 10;
			serverRoot.version_human = meta.getDatabaseProductVersion();
			serverRoot.description = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion() + " Server";
			return serverRoot;
		}
	}
}
