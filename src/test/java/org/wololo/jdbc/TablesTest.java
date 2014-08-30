package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.wololo.jdbc.resources.TablesResource;

public class TablesTest extends ServerTest {

	@Override
	protected Application configure() {
		return new ResourceConfig(TablesResource.class);
	}

	@Test
	public void test() throws IOException {
		assertEquals(getJson("Tables"),
				target("db/TEST/schemas/PUBLIC/tables").request().get(String.class));
	}
}
