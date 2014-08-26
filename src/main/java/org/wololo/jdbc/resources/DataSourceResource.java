package org.wololo.jdbc.resources;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataSourceResource {
	protected Context ctx;
    protected DataSource ds;
	
	DataSourceResource() {
		try {
			ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("jdbc-http-server/db");
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
