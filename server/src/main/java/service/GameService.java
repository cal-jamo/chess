package service;
import dataaccess.*;
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

    }

    // take authToken as a param and if auth token is valid return the list of games stored in memory
    public Collection<GameData> listGames(String authToken) throws DataAccessException {

    }


}