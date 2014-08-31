package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class RowsTest extends ServerTest {
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
		
	@Test
	public void testPost() throws IOException, SQLException {
		Entity<String> entity = Entity.json(getJson("Row"));
		Response response = target("db/TEST/schemas/PUBLIC/tables/TEST/rows").request().post(entity);
		assertEquals(204, response.getStatus());
		assertEquals(getJson("Row"),
				target("db/TEST/schemas/PUBLIC/tables/TEST/rows/1").request().get(String.class));
	}
	
	@Test
	public void testPostFailUniquePK() throws IOException, SQLException {
		Entity<String> entity = Entity.json(getJson("Row"));
		Response response = target("db/TEST/schemas/PUBLIC/tables/TEST/rows").request().post(entity);
		assertEquals(204, response.getStatus());
		response = target("db/TEST/schemas/PUBLIC/tables/TEST/rows").request().post(entity);
		assertEquals(500, response.getStatus());
	}
}
