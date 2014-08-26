package org.wololo.jdbc;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.wololo.jdbc.resources.DatabasesResource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ServerTest extends JerseyTest {

	@Path("hello")
    public static class HelloResource {
        @GET
        public String getHello() {
            return "Hello World!";
        }
    }
	
	@Override
    protected Application configure() {
		String fileName = "hikari.properties";
		try {
			Properties properties = new Properties();
			properties.load(this.getClass().getClassLoader().getResourceAsStream(fileName));
			HikariConfig config = new HikariConfig(properties);
			HikariDataSource ds = new HikariDataSource(config);
			SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
			builder.bind("jdbc-http-server/db", ds);
			builder.activate();
		} catch (IOException | java.lang.NullPointerException | IllegalStateException | NamingException e) {
			//logger.error("FATAL ERROR: Could not load {}", fileName, e);
			throw new RuntimeException(e);
		}
		
		//enable(TestProperties.LOG_TRAFFIC);
        //enable(TestProperties.DUMP_ENTITY);
        
        return new ResourceConfig(DatabasesResource.class);
        //return new ResourceConfig(HelloResource.class);
    }

	@Test
	public void test() {
		final String db = target("db").request().get(String.class);
		assertEquals("{\"type\":\"databases\",\"name\":\"db\",\"children\":[\"test\"]}", db);
	}

}
