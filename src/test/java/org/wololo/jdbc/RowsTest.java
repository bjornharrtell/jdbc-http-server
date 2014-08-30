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
import org.wololo.jdbc.resources.RowsResource;

public class RowsTest extends ServerTest {

	@Override
	protected Application configure() {
		return new ResourceConfig(RowsResource.class);
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
	public void testEmpty() throws IOException {
		assertEquals(getJson("Rows"),
				target("db/TEST/schemas/PUBLIC/tables/TEST/rows").request().get(String.class));
	}
	
	@Test
	public void testOne() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
		}
		assertEquals(getJson("RowsOne"),
				target("db/TEST/schemas/PUBLIC/tables/TEST/rows").request().get(String.class));
	}
	@Test
	public void testTwo() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
			statement.execute("insert into test (name) values ('test2')");
		}
		assertEquals(getJson("RowsTwo"),
				target("db/TEST/schemas/PUBLIC/tables/TEST/rows").request().get(String.class));
	}
}
