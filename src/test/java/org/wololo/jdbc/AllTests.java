package org.wololo.jdbc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ServerTest.class,
	ServerRootTest.class,
	DatabasesTest.class,
	DatabaseTest.class,
	TablesTest.class
})
public class AllTests {

}
