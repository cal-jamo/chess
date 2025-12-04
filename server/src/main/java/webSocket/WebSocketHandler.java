package webSocket;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.LoadGameMessage;


public class WebSocketHandler {
    private final ConnectionManager sessions = new ConnectionManager();
    private final Gson gson = new Gson();
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;
    public WebSocketHandler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }
    public void handleMessage(WsMessageContext cx) throws DataAccessException {
        try {
            String message = cx.message();
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> {
                    connect(command.getAuthToken(), command.getGameID(), cx);
                }
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                    System.out.println("Make Move command received");
                }
                case LEAVE -> {
                    System.out.println("Leave command received");
                }
                case RESIGN -> {
                    System.out.println("Resign command received");
                }
            }
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    private void connect(String authToken, Integer gameId, WsMessageContext cx) throws DataAccessException {
        try {
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                throw new DataAccessException("Auth data not found");
            }
            GameData gameData = gameDAO.getGame(gameId);
            if (gameData == null) {
                throw new DataAccessException("Game not found");
            }
            var connection = new Connection(authToken, cx.session, gameId);
            sessions.add(authToken, connection);
            LoadGameMessage loadGame = new LoadGameMessage(gameData.game());
            cx.send(loadGame);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());

        }

    }

}