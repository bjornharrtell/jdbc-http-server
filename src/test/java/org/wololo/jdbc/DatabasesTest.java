package org.wololo.jdbc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class DatabasesTest extends ServerTest {
	@Test
	public void test() throws IOException {
		String result = target("db").request().get(String.class);
		System.out.println(result);
		assertEquals(getJson("Databases"), result);
	}
}
