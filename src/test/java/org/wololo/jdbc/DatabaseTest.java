package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class DatabaseTest extends ServerTest {
	@Test
	public void test() throws IOException {
		String path = "db/" + identifier("test");
		assertEquals(getJson("Database"), target(path).request().get(String.class));
	}
}
