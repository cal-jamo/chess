package dataaccess;

import java.sql.SQLException;

public class DBInitialization {
    public static void initialize() {
        try {
            try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
                throw new RuntimeException(ex);
            }
            try (var connection = DatabaseManager.getConnection()) {
                var createAuthTable = """            
                    CREATE TABLE IF NOT EXISTS auth (
                        username VARCHAR(255) NOT NULL,
                        authToken VARCHAR(255) NOT NULL,
                        PRIMARY KEY (authToken)
                    )""";
                try (var statement = connection.prepareStatement(createAuthTable)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to initialize database auth schema: " + e.getMessage(), e);
        }
    }
}