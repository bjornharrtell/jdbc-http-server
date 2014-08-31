package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.wololo.jdbc.resources.DatabasesResource;

public class DatabasesTest extends ServerTest {
	@Test
	public void test() throws IOException {
		assertEquals(getJson("Databases"), target("db").request().get(String.class));
	}
}
