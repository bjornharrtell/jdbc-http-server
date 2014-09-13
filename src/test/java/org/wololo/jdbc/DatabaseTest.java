package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class DatabaseTest extends ServerTest {
	@Test
	public void test() throws IOException {
		assertEquals(getJson("Database"),
				target("db/TEST").request().get(String.class));
	}
}
