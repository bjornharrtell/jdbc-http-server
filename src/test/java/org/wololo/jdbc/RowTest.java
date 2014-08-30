package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.wololo.jdbc.resources.RowResource;

public class RowTest extends ServerTest {

	@Override
	protected Application configure() {
		return new ResourceConfig(RowResource.class);
	}	

	@Test
	public void testGet() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
		}
		assertEquals(getJson("Row"),
				target("db/TEST/schemas/PUBLIC/tables/TEST/rows/1").request().get(String.class));
	}
}
