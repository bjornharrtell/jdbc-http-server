package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wololo.jdbc.resources.DatabaseResource;
import org.wololo.jdbc.resources.TablesResource;

public class TablesTest extends ServerTest {

	@Override
	protected Application configure() {
		return new ResourceConfig(TablesResource.class);
	}

	@Before
	public void before() throws SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("create table test (id serial, name varchar)");
		}
	}

	@After
	public void after() throws SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("drop table test");
		}
	}

	@Test
	public void test() throws IOException {
		assertEquals(getJson("Tables"),
				target("db/TEST/schemas/PUBLIC/tables").request().get(String.class));
	}
}
