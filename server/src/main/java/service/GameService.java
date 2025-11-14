package service;
import dataaccess.*;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import model.AuthData;
import model.GameData;
import model.JoinRequest;
import java.util.Collection;
import java.util.Map;


public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }
    private static final Map<Integer, String> ERROR_MAP = Map.of(
            400, "Error: Bad Request",
            401, "Error: Unauthorized",
            403, "Error: Color Already Taken"
    );

    // take authToken and gameName as parameters, and create and store the game data
    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException(ERROR_MAP.get(401));
        } else if (gameName == null) {
            throw new DataAccessException(ERROR_MAP.get(400));
        }
        return gameDAO.createGame(gameName);
    }

    // take authToken as a param and if auth token is valid return the list of games stored in memory
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException(ERROR_MAP.get(401));
        }
        return gameDAO.listGames();
    }

    public void joinGame(String authToken, JoinRequest request) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException(ERROR_MAP.get(401));
        }

        String playerColor = request.playerColor();
        GameData gameData = gameDAO.getGame(request.gameID());
        if (gameData == null) {
            throw new DataAccessException(ERROR_MAP.get(400));
        }

        if (playerColor == null) {
            return;
        }
        if ((!playerColor.equals("WHITE") && !playerColor.equals("BLACK"))) {
            throw new DataAccessException(ERROR_MAP.get(400));
        }

        if(playerColor.equals("WHITE")) {
            if (gameData.whiteUsername() != null) {
                throw new DataAccessException(ERROR_MAP.get(403));
            }
            gameData = new GameData(gameData.gameID(), authData.username(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (playerColor.equals("BLACK")) {
            if (gameData.blackUsername() != null) {
                throw new DataAccessException(ERROR_MAP.get(403));
            }
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), authData.username(), gameData.gameName(), gameData.game());
        }
        gameDAO.updateGame(gameData.gameID(), gameData);
    }


}