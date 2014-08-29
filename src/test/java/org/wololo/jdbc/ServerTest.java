package org.wololo.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.naming.NamingException;

import org.glassfish.jersey.test.JerseyTest;
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
