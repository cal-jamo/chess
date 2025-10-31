package dataaccess;

import java.sql.SQLException;

public class DBInitialization {
    public static void initialize() {
        try {
            try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
                throw new RuntimeException(ex);
            }
            try (var connection = DatabaseManager.getConnection()) {
                var initAuthTable = """            
                    CREATE TABLE IF NOT EXISTS auth (
                        username VARCHAR(255) NOT NULL,
                        authToken VARCHAR(255) NOT NULL,
                        PRIMARY KEY (authToken)
                    )""";
                try (var statement = connection.prepareStatement(initAuthTable)) {
                    statement.executeUpdate();
                }
                var initUserTable = """            
                    CREATE TABLE IF NOT EXISTS users (
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255),
                        PRIMARY KEY (username)
                    )""";
                try (var statement = connection.prepareStatement(initUserTable)) {
                    statement.executeUpdate();
                }
                var initGamesTable = """            
                    CREATE TABLE IF NOT EXISTS games (
                        gameID INTEGER NOT NULL,
                        whiteUsername VARCHAR(255) NOT NULL,
                        blackUsername VARCHAR(255) NOT NULL,
                        gameName VARCHAR(255) NOT NULL,
                        chessGame TEXT NOT NULL,
                        PRIMARY KEY (gameID)
                    )""";
                try (var statement = connection.prepareStatement(initGamesTable)) {
                    statement.executeUpdate();
                }
                // This isn't required, but I thought it would be fun to put on my chess application
                var initTipsTable = """            
                    CREATE TABLE IF NOT EXISTS facts (
                        id INTEGER PRIMARY KEY AUTO_INCREMENT,
                        category VARCHAR(50) NOT NULL,
                        factText TEXT NOT NULL
                    )""";
                try (var statement = connection.prepareStatement(initTipsTable)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to initialize database auth schema: " + e.getMessage(), e);
        }
    }
}