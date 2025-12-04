package webSocket;
import websocket.messages.ServerMessage;

public interface NotiHandler {
    void notify(ServerMessage notification);
}
