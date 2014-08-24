package org.wololo.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@ApplicationPath("/")
public class Server extends ResourceConfig {
	
	static HikariDataSource ds;
	
	public Server() throws IOException {
		Properties properties = new Properties();
		properties.load(getClassLoader().getResourceAsStream("hikari.properties"));
		HikariConfig config = new HikariConfig(properties);
		ds = new HikariDataSource(config);
		
		packages("org.wololo.jdbc.resources");
		register(JacksonFeature.class);
    }
	
	static public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
}
