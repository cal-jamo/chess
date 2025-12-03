package webSocket;
import com.google.gson.Gson;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;

public class WebSocketHandler {
    private final ConnectionManager sessions =  new ConnectionManager();
    private final Gson gson = new Gson();
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