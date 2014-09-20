package org.wololo.jdbc.resources;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jooq.DeleteConditionStep;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("db/{databaseName}/schemas/{schemaName}/tables/{tableName}/rows/{id}")
public class RowResource extends DataSourceResource {
	
	final static Logger logger = LoggerFactory.getLogger(RowResource.class);
	
	@PathParam("databaseName") String databaseName;
	@PathParam("schemaName") String schemaName;
	@PathParam("tableName") String tableName;
	@PathParam("id") String id;
	
	@GET
	@Produces("application/json")
	public Map<String, Object> get() throws SQLException {
		List<Field<Object>> fields = getFields();
		SelectConditionStep<Record> query = create.select(fields)
				.from(schemaName + "." + tableName)
				.where(getPK().equal(id));
		logger.debug(query.getSQL());
		
		Record record = query.fetchOne();

		Map<String, Object> row = new HashMap<String, Object>();
		for (Field<Object> field : fields) {
			row.put(field.toString(), record.getValue(field));
		}
		
		return row;
	}
	
	@PUT
	public void put(Map<String, Object> row) throws SQLException {
		UpdateSetFirstStep<Record> sqlTemp = create.update(table(tableName));
		UpdateSetMoreStep<Record> build = null;
		for(Entry<String, Object> entry : row.entrySet()) {
			build = sqlTemp.set(field(entry.getKey()), entry.getValue());
		}
		UpdateConditionStep<Record> query = build.where(getPK().equal(id));
		logger.debug(query.getSQL());
		final int result = query.execute();
		
		if (result == 0) {
			throw new RuntimeException("No rows was found and updated with id " + id);
		} else if (result > 1) {
			// TODO: rollback transaction?
			throw new RuntimeException("Multiple rows was found and updated with id " + id);
		}
	}
	
	@DELETE
	public void del() throws SQLException {
		DeleteConditionStep<Record> query = create.delete(table(tableName)).where(getPK().equal(coercedPK()));
		logger.debug(query.getSQL());
		final int result = query.execute();
		
		if (result == 0) {
			throw new RuntimeException("No rows was found and deleted with id " + id);
		} else if (result > 1) {
			// TODO: rollback transaction?
			throw new RuntimeException("Multiple rows was found and deleted with id " + id);
		}		
	}
	
	Object coercedPK() {
		try { 
	        return Integer.parseInt(id); 
	    } catch(NumberFormatException e) { 
	        return id; 
	    }
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
	
	Field<Object> getPK() throws SQLException {
		try (Connection connection = ds.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			try (ResultSet resultSet = meta.getPrimaryKeys(databaseName, schemaName, tableName)) {
				resultSet.next();
				String columnName = resultSet.getString(4);
				return field(columnName);
			}
		}
	}
}
