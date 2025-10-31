package dataaccess.game;
import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBGameDAO implements GameDAO {
    private int nextGameID = 1;

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextGameID;
        ChessGame newChessGame = new ChessGame();
        String newGameJson = new Gson().toJson(newChessGame);
        var gameQuery = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?, ?)";
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(gameQuery)) {
                statement.setInt(1, gameID);
                statement.setNull(2, Types.VARCHAR);
                statement.setNull(3, Types.VARCHAR);
                statement.setString(4, gameName);
                statement.setString(5, newGameJson);
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
                        String gameJson = resultSet.getString("chessGame");
                        ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
                        return new GameData(
                                resultSet.getInt("gameID"),
                                resultSet.getString("whiteUsername"),
                                resultSet.getString("blackUsername"),
                                resultSet.getString("gameName"),
                                game
                        );
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Failed to get game: " + e.getMessage());
        }
    }


    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var gameQuery = "SELECT * FROM games";
        List<GameData> games = new ArrayList<>();
        try (var connection = DatabaseManager.getConnection()) {
            try (var statement = connection.prepareStatement(gameQuery)) {
                try (var resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String gameJson = resultSet.getString("chessGame");
                        ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
                        games.add(new GameData(
                                resultSet.getInt("gameID"),
                                resultSet.getString("whiteUsername"),
                                resultSet.getString("blackUsername"),
                                resultSet.getString("gameName"),
                                game
                        ));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Failed to list games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}