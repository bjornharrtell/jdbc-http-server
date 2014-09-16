package org.wololo.jdbc.resources;

import static org.jooq.impl.DSL.field;
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
import org.jooq.SelectOffsetStep;
import org.jooq.SortField;
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
			@DefaultValue("0") @QueryParam("limit") final String limit,
			@DefaultValue("0") @QueryParam("offset") final String offset,
			@DefaultValue("") @QueryParam("orderby") final String orderby) throws SQLException {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				writeRows(output, 
						select, 
						where, 
						parseNumericParam(limit), 
						parseNumericParam(offset), 
						orderby);
			}
		};
	}
	
	@POST
	public void post(Map<String, Object> row) throws SQLException {
		try (Connection connection = ds.getConnection()) {
			InsertSetStep<Record> sqlTemp = create.insertInto(table(tableName));
			InsertSetMoreStep<Record> build = null;
			for(Entry<String, Object> entry : row.entrySet()) {
				build = sqlTemp.set(field(entry.getKey()), entry.getValue());
			}
			final String sql = build.getSQL();
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
	
	SortField<Object>[] parseOrderbyParam(final String orderby) {
		if (orderby.equals("")) {
			return new SortField[] { };
		} else {
			String[] parts = orderby.split(",");
			List<SortField> fields = new ArrayList<SortField>();
			for (String part : parts) {
				String[] subparts = part.split(" ");
				if (subparts.length == 1) {
					fields.add(field(part).asc());
				} else if (subparts.length == 2) {
					if (subparts[1].equals("ASC")) {
						fields.add(field(subparts[0]).asc());
					} else {
						fields.add(field(subparts[0]).desc());
					}
				}
				
			}
			return fields.toArray(new SortField[] { });
		}
	};
		
	int parseNumericParam(final String limit) {
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
			final int limit,
			final int offset,
			final String orderby) throws IOException {
			
		Field<Object>[] fields = parseSelectParam(select);
		
		SelectJoinStep<Record> query = create.select(fields).from(schemaName + "." + tableName);
		
		if (where.length()>0) {
			query.where(where);
		}
		
		if (limit>0) {
			SelectOffsetStep<Record> offsetStep = query.limit(limit);
			if (offset>0) {
				offsetStep.offset(offset);
			}
		}
		
		if (orderby.length()>0) {
			query.orderBy(parseOrderbyParam(orderby));
		}
		
		final String sql = query.getSQL();
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
