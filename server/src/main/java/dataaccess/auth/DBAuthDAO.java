package dataaccess.auth;
import dataaccess.DataAccessException;
import model.AuthData;
import java.sql.SQLException;
import java.util.UUID;
import dataaccess.DatabaseManager;

public class DBAuthDAO implements AuthDAO {

    public DBAuthDAO() {
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        var sqlQuery = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(sqlQuery)) {
                statement.setString(1, authData.authToken());
                statement.setString(2, authData.username());
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Failed to create auth: " + e.getMessage());
        }
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var authQuery = "SELECT * FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(authQuery)) {
                statement.setString(1, authToken);
                try (var resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new AuthData(resultSet.getString("authToken"), resultSet.getString("username"));
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Failed to get auth: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var authQuery = "DELETE FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(authQuery)) {
                statement.setString(1, authToken);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Error: Unauthorized");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var authQuery = "DELETE FROM auth";
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(authQuery)) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Failed to clear auth: " + e.getMessage());
        }
    }
}