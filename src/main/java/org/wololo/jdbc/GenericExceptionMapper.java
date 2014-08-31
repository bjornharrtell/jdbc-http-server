package org.wololo.jdbc;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
	final static Logger logger = LoggerFactory
			.getLogger(GenericExceptionMapper.class);

	public Response toResponse(Exception ex) {
		logger.error("Unhandled exception in JAX-RS handler", ex);
		return Response.status(500)
				.entity(ex.getMessage())
				.type("text/plain")
				.build();
	}
}