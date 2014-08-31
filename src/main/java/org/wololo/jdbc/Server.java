package org.wololo.jdbc;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class Server extends ResourceConfig {
	
	final static Logger logger = LoggerFactory.getLogger(Server.class);
	
	public Server() throws IOException, SQLException {
		packages("org.wololo.jdbc.resources");
		register(JacksonFeature.class);		
		register(GenericExceptionMapper.class);
    }
}
