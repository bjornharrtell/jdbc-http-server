package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wololo.jdbc.resources.DatabaseResource;

public class DatabaseTest extends ServerTest {
	@Test
	public void test() throws IOException {
		assertEquals(getJson("Database"),
				target("db/TEST").request().get(String.class));
	}
}
