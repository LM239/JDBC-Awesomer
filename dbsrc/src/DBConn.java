import java.sql.*;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;

public abstract class DBConn {
    protected Connection conn;
    public DBConn () {
    }
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            // Properties for user and password. Here the user and password are both 'paulr'
            Properties p = new Properties();
            p.put("user", "olavhdi_java");
            p.put("password", "passord123");
            conn = DriverManager.getConnection("jdbc:mysql://mysql.stud.ntnu.no/olavhdi_db2?autoReconnect=true&useSSL=false",p);
        } catch (Exception e)
        {
            throw new RuntimeException("Unable to connect", e);
        }
    }
}
