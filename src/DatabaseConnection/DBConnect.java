package DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

    public Connection connection;
    
    public void dbConnection() {
        String url = "jdbc:postgresql://localhost:5432/projet_java";
        String user = "postgres";
        String password = "postgres";
        
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("connected");
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Not connected: " + ex);
        }    
    }
}
