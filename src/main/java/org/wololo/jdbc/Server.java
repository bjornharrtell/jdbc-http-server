package org.wololo.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@ApplicationPath("/")
@WebListener
public class Server extends ResourceConfig implements ServletContextListener {
	
	final static Logger logger = LoggerFactory.getLogger(Server.class);
	
	HikariDataSource ds;
	
	public Server() throws IOException {
		packages("org.wololo.jdbc.resources");
		register(JacksonFeature.class);
    }

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if (ds != null) ds.close();
		
		// Now deregister JDBC drivers in this context's ClassLoader:
	    // Get the webapp's ClassLoader
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    // Loop through all drivers
	    Enumeration<Driver> drivers = DriverManager.getDrivers();
	    while (drivers.hasMoreElements()) {
	        Driver driver = drivers.nextElement();
	        if (driver.getClass().getClassLoader() == cl) {
	            // This driver was registered by the webapp's ClassLoader, so deregister it:
	            try {
	                logger.info("Deregistering JDBC driver {}", driver);
	                DriverManager.deregisterDriver(driver);
	            } catch (SQLException ex) {
	            	logger.error("Error deregistering JDBC driver {}", driver, ex);
	            }
	        } else {
	            // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
	        	logger.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
	        }
	    }
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// TODO: configure data source in container instead...		
		String fileName = "hikari.properties";
		try {
			Properties properties = new Properties();
			properties.load(getClassLoader().getResourceAsStream(fileName));
			HikariConfig config = new HikariConfig(properties);
			ds = new HikariDataSource(config);
			SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
			builder.bind("jdbc-http-server/db", ds);
			builder.activate();
			//builder.de
		} catch (IOException | java.lang.NullPointerException | IllegalStateException | NamingException e) {
			logger.error("FATAL ERROR: Could not load {}", fileName, e);
			throw new RuntimeException(e);
		}
		
	}
}
