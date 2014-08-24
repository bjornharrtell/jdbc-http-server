package model;

public class Schema {
	public String type = "schema";
	public String name;
	public String owner;
	public String[] children = new String[] {
			"functions",
			"tables",
			"sequences",
			"views"};
}
