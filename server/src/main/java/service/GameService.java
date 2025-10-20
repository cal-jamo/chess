package service;
import dataaccess.*;
import jdk.jshell.spi.ExecutionControl;
import kotlin.NotImplementedError;
import model.GameData;

import javax.xml.crypto.Data;
import java.util.Collection;


public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    // take authToken and gameName as parameters, and create and store the game data
    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: Unauthorized");
        } else if (gameName == null) {
            throw new DataAccessException("Error: Bad Request");
        }
        return gameDAO.createGame(gameName);
    }

    // take authToken as a param and if auth token is valid return the list of games stored in memory
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Error: Unauthorized access");
        }
        return gameDAO.listGames();
    }


}