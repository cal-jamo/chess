package dataaccess;

import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> storedGames = new HashMap<>();
    private int newGameID = 1;

    // function to create a game. A game name is passed in, an ID is created and assigned, and a new chess game is initialized
    @Override
    public int createGame(String gameName) {
        int gameID = newGameID++;
        storedGames.put(gameID, new GameData(gameID, null, null, gameName, new chess.ChessGame()));
        return gameID;
    }

    // getGame finds a certain game in storedGames at the gameID passed in.
    @Override
    public GameData getGame(int gameID) {
        return storedGames.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return storedGames.values();
    }

    // this takes in a gameID so we can find the game in storedGames and updates it with new game data
    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if (!storedGames.containsKey(gameID)) {
            throw new DataAccessException("Error: bad request");
        }
        storedGames.put(gameID, game);
    }

    @Override
    public void clear() {
        storedGames.clear();
    }
}