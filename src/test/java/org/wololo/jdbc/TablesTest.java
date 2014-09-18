package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TablesTest extends ServerTest {
	@Test
	public void test() throws IOException {
		String path = "db/" + identifier("test") + "/schemas/" + identifier("public") + "/tables" ;
		assertEquals(getJson("Tables"),	target(path).request().get(String.class));
	}
}
