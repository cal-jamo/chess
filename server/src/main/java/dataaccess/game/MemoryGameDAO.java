package dataaccess.game;
import dataaccess.DataAccessException;
import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> storedGames = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(String gameName) {
        int gameID = nextGameID;
        storedGames.put(gameID, new GameData(gameID, null, null, gameName, null));
        nextGameID += 1;
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return storedGames.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return storedGames.values();
    }

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