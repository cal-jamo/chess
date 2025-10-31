package dataaccess.game;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBGameDAO implements GameDAO {
    private int nextGameID = 1;
    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextGameID;
        var gameQuery = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName) VALUES (?, ?, ?, ?)";
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(gameQuery)) {
                statement.setInt(1, gameID);
                statement.setNull(2, java.sql.Types.VARCHAR); // How to properly set NULL for whiteUsername
                statement.setNull(3, java.sql.Types.VARCHAR); // How to properly set NULL for blackUsername
                statement.setString(4, gameName);
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Failed to insert game: " + e.getMessage());
        }
        nextGameID += 1;
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var gameQuery = "SELECT * FROM games WHERE gameID = ?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(gameQuery)) {
                statement.setInt(1, gameID);
                try (var resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new GameData(resultSet.getInt("gameID"), resultSet.getString("whiteUsername"), resultSet.getString("blackUsername"), resultSet.getString("gameName"));
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Failed to get user: " + e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}