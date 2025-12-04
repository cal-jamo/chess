package webSocket;

import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WSFacade extends Endpoint {
    Session session;
    NotiHandler notiHandler;
    public WSFacade(Session session, NotiHandler notiHandler, String url) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI uri = new URI(url + "/ws");
            this.notiHandler = notiHandler;
            WebSocketContainer wSContainer = ContainerProvider.getWebSocketContainer();
            this.session = wSContainer.connectToServer(this, uri);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME -> notiHandler.notify(serverMessage);
                        case NOTIFICATION -> notiHandler.notify(serverMessage);
                        case ERROR -> notiHandler.notify(serverMessage);
                    }
                }
            });
        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new Exception("500: " + e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
    }
    public void sendCommand(UserGameCommand command) throws Exception {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException e) {
            throw new Exception("500: " + e.getMessage());
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Closing session " + session.getId());
    }

}