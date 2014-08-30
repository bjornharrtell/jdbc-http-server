package org.wololo.jdbc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ServerRootTest.class,
	DatabasesTest.class,
	DatabaseTest.class,
	TablesTest.class,
	RowsTest.class,
	RowTest.class
})
public class AllTests {

}
