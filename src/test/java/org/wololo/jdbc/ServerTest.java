package org.wololo.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.NamingException;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.google.common.io.CharStreams;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ServerTest extends JerseyTest {

	static HikariDataSource ds;
	
	String getJson(String name) throws IOException {
		return CharStreams.toString(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("h2/" + name + ".json")));
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
	
	@BeforeClass
	public static void setupDB() {
		if (ds != null) return;
		try {
			Properties properties = new Properties();
			properties.load(ServerTest.class.getClassLoader().getResourceAsStream("h2/hikari.properties"));
			HikariConfig config = new HikariConfig(properties);
			ds = new HikariDataSource(config);
			SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
			builder.bind("jdbc-http-server/db", ds);
			builder.activate();
		} catch (IOException | java.lang.NullPointerException | IllegalStateException | NamingException e) {
			//logger.error("FATAL ERROR: Could not load {}", fileName, e);
			throw new RuntimeException(e);
		}
	}
}
