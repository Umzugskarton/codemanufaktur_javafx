package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbConnectionMySQL implements IDbConnection {
    Connection conn = null;

    public void open(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/mytest?" +
                    "user=root&password=root");
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public void close(){

    }
}
