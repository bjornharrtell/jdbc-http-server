package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TablesTest extends ServerTest {
	@Test
	public void test() throws IOException {
		assertEquals(getJson("Tables"),
				target("db/TEST/schemas/PUBLIC/tables").request().get(String.class));
	}
}
