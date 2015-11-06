package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class RowTest extends ServerTest {
	String path = "db/" + identifier("test") + "/schemas/" + identifier("public") + "/tables/" + identifier("test") + "/rows/1";
	
	@Test
	public void testGET() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name, time) values ('test', '2004-10-19 10:23:54')");
		}
		assertEquals(getJson("Row"), target(path).request().get(String.class));
	}
	
	@Test
	public void testPUT() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name, time) values ('test', '2004-10-19 10:23:54')");
		}
		Entity<String> entity = Entity.json(getJson("RowPUT"));
		Response response = target(path).request().put(entity);
		assertEquals(204, response.getStatus());
		assertEquals(getJson("RowPUT"), target(path).request().get(String.class));
	}
	
	@Test
	public void testDELETE() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name, time) values ('test, '2004-10-19 10:23:54')");
		}
		Response response = target(path).request().delete();
		assertEquals(204, response.getStatus());
	}
	
	@Test
	public void testDELETEFailOnMissingPK() throws IOException, SQLException {
		Response response = target(path).request().delete();
		assertEquals(500, response.getStatus());
	}

}
