import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/compilation";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    // Load the PostgreSQL JDBC driver
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC driver not found", e);
        }
    }

    // Method to get a database connection
    public static Connection initializeDatabase() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Method to close the database connection
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
