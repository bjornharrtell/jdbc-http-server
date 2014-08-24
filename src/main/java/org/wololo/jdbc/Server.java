package org.wololo.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@ApplicationPath("/")
@WebListener
public class Server extends ResourceConfig implements ServletContextListener {
	
	final static Logger logger = LoggerFactory.getLogger(Server.class);
	
	static HikariDataSource ds;
	
	public Server() throws IOException {
		packages("org.wololo.jdbc.resources");
		register(JacksonFeature.class);
    }
	
	static public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		if (ds != null) ds.close();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		Properties properties = new Properties();
		try {
			properties.load(getClassLoader().getResourceAsStream("hikari.properties"));
		} catch (IOException | java.lang.NullPointerException e) {
			logger.error("FATAL ERROR: Could not load hikari.properties");
			throw new RuntimeException(e);
		}
		HikariConfig config = new HikariConfig(properties);
		ds = new HikariDataSource(config);
	}
}
