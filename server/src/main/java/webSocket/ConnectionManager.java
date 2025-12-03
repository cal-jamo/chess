package webSocket;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> sessions = new ConcurrentHashMap<>();
    public void add(String authToken, Connection connection) {
        sessions.put(authToken, connection);
    }
}
