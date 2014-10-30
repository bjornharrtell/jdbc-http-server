package org.wololo.jdbc.resources;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.jooq.Comparator;
import org.jooq.Condition;
import org.jooq.Cursor;
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
	
	static final Pattern wherePattern = Pattern.compile("(\\w+)([<>!=])(\\w+|'\\w+')");
	static final Map<String, Comparator> comparatorMap = new HashMap<String, Comparator>();
	static {
		comparatorMap.put("=", Comparator.EQUALS);
		comparatorMap.put("!=", Comparator.NOT_EQUALS);
		comparatorMap.put(">", Comparator.GREATER);
		comparatorMap.put("=>", Comparator.GREATER_OR_EQUAL);
		comparatorMap.put("<", Comparator.LESS);
		comparatorMap.put("=<", Comparator.LESS_OR_EQUAL);
		comparatorMap.put("LIKE", Comparator.LIKE);
	}
	
	@GET
	@Produces("application/json")
	public StreamingOutput get(
			@DefaultValue("*") @QueryParam("select") final String select,
			@DefaultValue("") @QueryParam("where") final String where,
			@DefaultValue("0") @QueryParam("limit") final String limit,
			@DefaultValue("0") @QueryParam("offset") final String offset,
			@DefaultValue("") @QueryParam("orderby") final String orderby) throws SQLException {
		
		final List<Field<Object>> fields = parseSelectParam(select);
		
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				writeRows(output, 
						fields, 
						where, 
						parseNumericParam(limit), 
						parseNumericParam(offset), 
						orderby);
			}
		};
	}
	
	@POST
	public void post(Map<String, Object> row) throws SQLException {
		InsertSetStep<Record> step = create.insertInto(table(tableName));
		InsertSetMoreStep<Record> query = null;
		for(Entry<String, Object> entry : row.entrySet()) {
			query = step.set(field(entry.getKey()), entry.getValue());
		}
		logger.debug(query.getSQL());
		
		final int result = query.execute();
		if (result != 1) throw new RuntimeException("Unexpected result " + result + " (expected 1)");
	}
	
	List<Field<Object>> parseSelectParam(final String select) throws SQLException {
		if (select.equals("*")) {
			return getFields();
		} else {
			String[] columns = select.split(",");
			List<Field<Object>> fields = new ArrayList<Field<Object>>();
			for (String column : columns) {
				fields.add(field(column));
			}
			return fields;
		}
	};
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
	
	Condition parseWhere(String where) {
		Matcher matcher = wherePattern.matcher(where);
		if (matcher.matches() != true) {
			throw new RuntimeException("Unexpected where clause (must conform to a op b)");
		}
		int groups = matcher.groupCount();
		
		if (groups == 3) {
			String a = matcher.group(1);
			String op = matcher.group(2);
			String b = matcher.group(3);
			return field(a).compare(comparatorMap.get(op), field(b));
		} else {
			throw new RuntimeException("Unexpected where clause (must conform to a op b)");
		}
	}
	
	void writeRows(
			final OutputStream output,
			final List<Field<Object>> fields,
			final String where,
			final int limit,
			final int offset,
			final String orderby) throws IOException {
			
		SelectJoinStep<Record> query = create.select(fields).from(schemaName + "." + tableName);
		
		if (where.length()>0) {
			query.where(parseWhere(where));
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
		
		logger.debug(query.getSQL());
		final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(output, JsonEncoding.UTF8);
		jsonGenerator.writeStartArray();
		
		final Cursor<Record> cursor = query.fetchLazy();
		try {
			while (cursor.hasNext()) {
				writeRow(jsonGenerator, cursor.fetchOne());
			}
		} catch (SQLException e) {
			logger.error("Error when reading row data from ResultSet", e);
		} finally {
			cursor.close();
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		}
	}
	
	void writeRow(final JsonGenerator jsonGenerator, final Record record) throws SQLException, JsonGenerationException, IOException {
		jsonGenerator.writeStartArray();
		for (Object value : record.intoArray()) {
			jsonGenerator.writeObject(value);
		}
		jsonGenerator.writeEndArray();
	}
	
	List<Field<Object>> getFields() throws SQLException {
		try (Connection connection = ds.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			try (ResultSet resultSet = meta.getColumns(databaseName, schemaName, tableName, null)) {
				List<Field<Object>> fields = new ArrayList<Field<Object>>();
				while (resultSet.next()) {
					String columnName = resultSet.getString(4);
					fields.add(field(columnName));
				}
				return fields;
			}
		}
	}
}
