package edu.wpi.cs3733.vindemiatrix.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * RDS Database Interface Class
 */
public class SchedulerDatabase {
	// configurations
	public final static String db_uri = "schedulerdb.cu3zcst0aw5e.us-east-1.rds.amazonaws.com";
	public final static String db_port = "3306";
	public final static String db_name = "innodb";
	public final static String db_uname = "db_admin";
	public final static String db_password = "R0BBISitobZ7YzOSFXm7";
		
	// single database connection (not object specific)
	static Connection conn;
	
	/**
	 *	Static connection function that allows for a single shared connection across usages.
	 */
	protected static Connection connect() throws Exception {
		if (conn != null) { return conn; }
		
		try {
			// System.out.println("Initiating connection...");
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://" + 
					db_uri + ":" + db_port + "/" + db_name + "?allowMultiQueries=true",
					db_uname,
					db_password);
			// System.out.println("Successful connection to database.");
			return conn;
		} catch (Exception ex) {
			throw new Exception("Failed to connect to database.");
		}
	}
}
