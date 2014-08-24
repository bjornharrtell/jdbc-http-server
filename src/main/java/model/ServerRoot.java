package model;

public class ServerRoot {
	public String type = "server_root";
	public int version;
	public String version_human;
	public String description;
	public String[] children = new String[] { "db" };
}
