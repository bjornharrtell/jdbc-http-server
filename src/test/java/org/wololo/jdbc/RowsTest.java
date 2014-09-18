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
	String path = "db/" + identifier("test") + "/schemas/" + identifier("public") + "/tables/" + identifier("test") + "/rows";
	
	@Test
	public void testEmpty() throws IOException {
		
		assertEquals(getJson("Rows"), target(path).request().get(String.class));
	}
	
	@Test
	public void testOne() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
		}
		assertEquals(getJson("RowsOne"), target(path).request().get(String.class));
	}
	
	@Test
	public void testTwo() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
			statement.execute("insert into test (name) values ('test2')");
		}
		assertEquals(getJson("RowsTwo"), target(path).request().get(String.class));
	}
	
	@Test
	public void testTwoSelectTwo() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
			statement.execute("insert into test (name) values ('test2')");
		}
		assertEquals(getJson("RowsTwo"), target(path).queryParam("select", "id,name").request().get(String.class));
	}
	
	@Test
	public void testTwoWhere() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
			statement.execute("insert into test (name) values ('test2')");
		}
		assertEquals(getJson("RowsTwoWhere"), target(path).queryParam("where", "name='test2'").request().get(String.class));
	}
	
	@Test
	public void testTwoSelect() throws IOException, SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("insert into test (name) values ('test')");
			statement.execute("insert into test (name) values ('test2')");
		}
		assertEquals(getJson("RowsTwoSelect"), target(path).queryParam("select", "id").request().get(String.class));
	}
		
	@Test
	public void testPost() throws IOException, SQLException {
		Entity<String> entity = Entity.json(getJson("Row"));
		Response response = target(path).request().post(entity);
		assertEquals(204, response.getStatus());
		assertEquals(getJson("Row"), target(path + "/1").request().get(String.class));
	}
	
	@Test
	public void testPostFailUniquePK() throws IOException, SQLException {
		Entity<String> entity = Entity.json(getJson("Row"));
		Response response = target(path).request().post(entity);
		assertEquals(204, response.getStatus());
		response = target(path).request().post(entity);
		assertEquals(500, response.getStatus());
	}
}
