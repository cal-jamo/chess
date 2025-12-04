package webSocket;
import com.google.gson.Gson;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.DBAuthDAO;
import dataaccess.game.DBGameDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.DBUserDAO;
import dataaccess.user.UserDAO;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;

public class WebSocketHandler {
    private final ConnectionManager sessions =  new ConnectionManager();
    private final Gson gson = new Gson();
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    public WebSocketHandler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void handleMessage(WsMessageContext ctx) {
        String message = ctx.message();
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> {
                System.out.println("Connect command received");
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
    }
}