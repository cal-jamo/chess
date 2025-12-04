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
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

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

    public void handleMessage(WsMessageContext cx) {
        try {
            String message = cx.message();
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), cx);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                    System.out.println("Make Move command received");
                }
                case LEAVE -> System.out.println("Leave command received");
                case RESIGN -> System.out.println("Resign command received");
            }
        } catch (Exception e) {
            ErrorMessage errorMessage = new ErrorMessage("Error: " + e.getMessage());
            cx.send(gson.toJson(errorMessage));
        }
    }

    private void connect(String authToken, Integer gameId, WsMessageContext cx) throws Exception {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Bad auth token");
        }
        GameData gameData = gameDAO.getGame(gameId);
        if (gameData == null) {
            throw new DataAccessException("Bad game ID");
        }

        var connection = new Connection(authToken, cx.session, gameId);
        sessions.add(authToken, connection);

        LoadGameMessage loadGame = new LoadGameMessage(gameData.game());
        cx.send(gson.toJson(loadGame));

        NotificationMessage notification = new NotificationMessage(authData.username() + " joined the game");
        sessions.broadcast(notification, authToken, gameId);
    }
}