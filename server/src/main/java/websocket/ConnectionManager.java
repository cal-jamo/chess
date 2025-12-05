package websocket;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> sessions = new ConcurrentHashMap<>();
    public void add(String authToken, Connection connection) {
        sessions.put(authToken, connection);
    }
    public void remove(String authToken) {
        sessions.remove(authToken);
    }
    public void broadcast(ServerMessage notification, String authToken, Integer gameId) throws IOException {
        var removeList = new ArrayList<String>();
        for (var sesh : sessions.values()) {
            if(sesh.session.isOpen()) {
                if(sesh.gameId.equals(gameId)) {
                    if(!sesh.authToken.equals(authToken)) {
                        try {
                            sesh.send(new Gson().toJson(notification));
                        } catch (IOException e) {
                            removeList.add(sesh.authToken);
                        }
                    }
                }
            } else {
                removeList.add(sesh.authToken);
            }
        }
        for(var session : removeList) {
            sessions.remove(session);
        }
    }
}
