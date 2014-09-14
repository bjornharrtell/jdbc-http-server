package org.wololo.jdbc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceResource {
	final static Logger logger = LoggerFactory.getLogger(DataSourceResource.class);

	static protected DataSource ds;

	DataSourceResource() {
		if (ds == null) {
			
			try {
				logger.info("Check for hikari.properties is in classpath");
				Properties properties = new Properties();
				InputStream is = getClass().getClassLoader().getResourceAsStream("hikari.properties");
				if (is != null) {
					properties.load(is);
					HikariConfig config = new HikariConfig(properties);
					ds = new HikariDataSource(config);
					return;
				} else {
					logger.info("hikari.properties not present in classpath");
				}
			} catch (IOException e) {
				logger.error("Failed to find DataSource");
				throw new RuntimeException(e);
			}
			
			try {
				logger.info("Initial database access");

				logger.info("Looking up DataSource via JDNI");
				String jdniName = System.getProperty("jdbc-http-server.jdni");
				if (jdniName == null) {
					logger.debug("System property 'jdbc-http-server.jdni' not found will use 'jdbc/db' as lookup name");
					jdniName = "jdbc/db";
				}

				Context initialContext = new InitialContext();
				try {
					Context envContext = (Context) initialContext
							.lookup("java:comp/env");
					logger.info("Java EE container found");
					ds = (DataSource) envContext.lookup(jdniName);
					logger.info("DataSource found");
				} catch (NamingException e) {
					logger.warn("Java EE container not found");
					logger.warn("Will assume this is a test run");
					ds = (DataSource) initialContext.lookup(jdniName);
				}

			} catch (NamingException e) {
				logger.error("Failed to find DataSource");
				throw new RuntimeException(e);
			}
		}
	}
}
