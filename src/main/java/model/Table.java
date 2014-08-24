package model;

public class Table {
	public String type = "table";
	public String name;
	public String owner;
	public String database;
	public String schema;
	public String tablespace;
	public String[] children = new String[] { "rows"};
}
