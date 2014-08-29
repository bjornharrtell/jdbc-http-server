package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.wololo.jdbc.resources.ServerRootResource;

public class ServerRootTest extends ServerTest {

	@Override
	protected Application configure() {
        return new ResourceConfig(ServerRootResource.class);
	}
	
	@Test
	public void test() throws IOException {
		assertEquals(getJson("ServerRoot"), target("").request().get(String.class));
	}
}
