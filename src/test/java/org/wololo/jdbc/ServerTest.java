package org.wololo.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.NamingException;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.h2.util.JdbcUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.google.common.io.CharStreams;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public class ServerTest extends JerseyTest {

	static HikariDataSource ds;
	
	@Override
	protected Application configure() {
		ResourceConfig application = new ResourceConfig();
		application.packages("org.wololo.jdbc.resources");
		application.register(GenericExceptionMapper.class);
		return application;
	}
	
	String getJson(String name) throws IOException {
		String path = "/" + TestSettings.RDBM + "/" +
				"expected/" +
				name + ".json";
		try (InputStreamReader inputStreamReader = new InputStreamReader(getClass().getResourceAsStream(path))) {
			return CharStreams.toString(inputStreamReader);
		}
	}
	
	@Before
	public void before() throws SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE TEST (ID SERIAL PRIMARY KEY, NAME VARCHAR)");
		}
	}

	@After
	public void after() throws SQLException {
		try (Connection connection = ds.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE TEST");
		}
	}
	
	public String identifier(String identifier) {
		return TestSettings.UPPERCASE_IDENTIFERS ? identifier.toUpperCase() : identifier;
	}
	
	@BeforeClass
	public static void setupDB() {
		if (ds != null) return;
		try {
			Properties properties = new Properties();
			properties.load(ServerTest.class.getClassLoader().getResourceAsStream(TestSettings.RDBM + ".properties"));
			HikariConfig config = new HikariConfig(properties);
			ds = new HikariDataSource(config);			
			SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
			builder.bind("jdbc/db", ds);
			builder.activate();
		} catch (IOException | java.lang.NullPointerException | IllegalStateException | NamingException e) {
			//logger.error("FATAL ERROR: Could not load {}", fileName, e);
			throw new RuntimeException(e);
		}
	}
}
