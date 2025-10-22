package service;
import dataaccess.*;
import jdk.jshell.spi.ExecutionControl;
import kotlin.NotImplementedError;
import model.AuthData;
import model.GameData;
import model.JoinRequest;

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

    // join game is a method that will take
    public void joinGame(String authToken, JoinRequest request) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData.authToken() == null) {
            throw new DataAccessException("Error: Unauthorized");
        }
        if (request.gameId() == 0 || request.playerColor() == null) {
            throw new DataAccessException("Error: Bad Request");
        }
        System.out.print(request);
        GameData gameData = gameDAO.getGame(request.gameId());
        String playerColor = request.playerColor();
        if(playerColor.equals("WHITE")) {
            if (gameData.whiteUsername() != null) {
                throw new DataAccessException("Error: Color already taken");
            }
            gameData = new GameData(gameData.gameID(), authData.username(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (playerColor.equals("BLACK")) {
            if (gameData.blackUsername() != null) {
                throw new DataAccessException("Error: Color already taken");
            }
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), authData.username(), gameData.gameName(), gameData.game());
        }
        gameDAO.updateGame(gameData.gameID(), gameData);
    }


}