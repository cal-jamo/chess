package websocket;
import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public String authToken;
    public Session session;
    public Integer gameId;
    public Connection(String authToken, Session session, Integer gameId) {
        this.session = session;
        this.authToken = authToken;
        this.gameId = gameId;
    }
    public synchronized void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}