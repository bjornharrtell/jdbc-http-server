package org.wololo.jdbc.resources;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.insertInto;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

@Path("db/{databaseName}/schemas/{schemaName}/tables/{tableName}/rows")
public class RowsResource extends DataSourceResource {
	final static Logger logger = LoggerFactory.getLogger(RowsResource.class);

	@PathParam("databaseName") String databaseName;
	@PathParam("schemaName") String schemaName;
	@PathParam("tableName") String tableName;
	
	@GET
	@Produces("application/json")
	public StreamingOutput get(
			@DefaultValue("*") @QueryParam("select") final String select,
			@DefaultValue("") @QueryParam("where") final String where,
			@DefaultValue("0") @QueryParam("limit") final String limit) throws SQLException {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				writeRows(output, select, where, parseLimitParam(limit));
			}
		};
	}
	
	@POST
	public void post(Map<String, Object> row) throws SQLException {
		try (Connection connection = ds.getConnection()) {
			InsertSetStep<Record> sqlTemp = insertInto(table(tableName));
			InsertSetMoreStep<Record> build = null;
			for(Entry<String, Object> entry : row.entrySet()) {
				build = sqlTemp.set(field(entry.getKey()), entry.getValue());
			}
			final String sql = build.toString();
			logger.debug(sql);
			
			try (final Statement statement = connection.createStatement()) {
				final int result = statement.executeUpdate(sql);
				// TODO: check that result is as expected
			}
		}
	}
	
	Field<Object>[] parseSelectParam(final String select) {
		if (select.equals("*")) {
			return new Field[] { field(select) };
		} else {
			String[] columns = select.split(",");
			List<Field> fields = new ArrayList<Field>();
			for (String column : columns) {
				fields.add(field(column));
			}
			return fields.toArray(new Field[] { });
		}
	};
		
	int parseLimitParam(final String limit) {
		if (limit.equals("0")) {
			return 0;
		} else {
			return Integer.parseInt(limit);
		}
	}
	
	void writeRows(
			final OutputStream output,
			final String select,
			final String where,
			final int limit) throws IOException {
		
		Field<Object>[] fields = parseSelectParam(select);
		SelectJoinStep<Record> query = select(fields).from(schemaName + "." + tableName);
		
		if (where.length()>0) {
			query.where(where);
		}
		
		if (limit>0) {
			query.limit(limit);
		}
		
		final String sql = query.toString();
		logger.debug(sql);
		final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(output, JsonEncoding.UTF8);
		jsonGenerator.writeStartArray();
		try (
				final Connection connection = ds.getConnection();
				final Statement statement = connection.createStatement();
				final ResultSet resultSet = statement.executeQuery(sql)) {
			DatabaseMetaData meta = connection.getMetaData();
			List<String> columns = getColumns(meta);
			int columnsTotal = (select.equals("*")) ? columns.size() : fields.length;
			while (resultSet.next()) {
				writeRow(jsonGenerator, resultSet, columnsTotal);
			}
		} catch (SQLException e) {
			logger.error("SQLException when reading row data from ResultSet", e);
			// TODO: think about how to gracefully handle eventual SQLExceptions
		} finally {
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		}
	}
	
	void writeRow(final JsonGenerator jsonGenerator, final ResultSet resultSet, int columnsTotal) throws SQLException, JsonGenerationException, IOException {
		jsonGenerator.writeStartArray();
		for (int i = 1; i <= columnsTotal; i++) {
			jsonGenerator.writeObject(resultSet.getObject(i));
		}
		jsonGenerator.writeEndArray();
	}
	
	List<String> getColumns(DatabaseMetaData meta) throws SQLException {
		try (ResultSet resultSet = meta.getColumns(databaseName, schemaName, tableName, null)) {
			List<String> columnNames = new ArrayList<String>();
			while (resultSet.next()) {
				String columnName = resultSet.getString(4);
				columnNames.add(columnName);
			}
			return columnNames;
		}
	}
}
