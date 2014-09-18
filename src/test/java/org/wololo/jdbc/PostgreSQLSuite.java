package org.wololo.jdbc;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DatabasesTest.class, DatabaseTest.class, RowsTest.class,
		RowTest.class, TablesTest.class })
public class PostgreSQLSuite {
	
	@BeforeClass 
    public static void setUpClass() {
		TestSettings.RDBM = "pg";
		TestSettings.UPPERCASE_IDENTIFERS = false;
    }
}
