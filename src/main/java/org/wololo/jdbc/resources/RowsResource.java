package org.wololo.jdbc.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jdbc.Server;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

@Path("db/{databaseName}/schemas/{schemaName}/tables/{tableName}/rows")
public class RowsResource {
	final static Logger logger = LoggerFactory.getLogger(RowsResource.class);

	@PathParam("databaseName") String databaseName;
	@PathParam("schemaName") String schemaName;
	@PathParam("tableName") String tableName;
	
	@GET
	@Produces("application/json")
	public StreamingOutput get() throws SQLException {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				writeRows(output);
			}
		};
	}
	
	void writeRows(final OutputStream output) throws IOException {
		final String sql = "select * from " + schemaName + "." + tableName;
		logger.debug(sql);
		final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(output, JsonEncoding.UTF8);
		jsonGenerator.writeStartArray();
		try (
				final Connection connection = Server.getConnection();
				final Statement statement = connection.createStatement();
				final ResultSet resultSet = statement.executeQuery(sql)) {
			while (resultSet.next()) {
				writeRow(jsonGenerator, resultSet);
			}
		} catch (SQLException e) {
			logger.error("SQLException when reading row data from ResultSet", e);
			// TODO: think about how to gracefully handle eventual SQLExceptions
		} finally {
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		}
	}
	
	void writeRow(final JsonGenerator jsonGenerator, final ResultSet resultSet) throws SQLException, JsonGenerationException, IOException {
		jsonGenerator.writeStartArray();
		Object c1 = resultSet.getObject(1);
		Object c2 = resultSet.getObject(2);
		jsonGenerator.writeObject(c1);
		jsonGenerator.writeObject(c2);
		jsonGenerator.writeEndArray();
	}

	@Path("{id}")
	public RowResource row() {
		return new RowResource();
	}
}
