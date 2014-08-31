package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class ServerRootTest extends ServerTest {
	@Test
	public void test() throws IOException {
		assertEquals(getJson("ServerRoot"), target("").request().get(String.class));
	}
}
