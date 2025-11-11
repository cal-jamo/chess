package ServerFacade;
import com.google.gson.Gson;
import model.*;


public class ServerFacade {
    private final String serverURL;
    public ServerFacade(String port) {
        this.serverURL = "http://localhost:" + port;
    }
    public static class ServerFacadeException extends Exception {
        public ServerFacadeException(String message) {
            super(message);
        }
    }
    public AuthData register(String username, String password, String email) throws ServerFacadeException {
        // This is just a placeholder, I will implement this
        throw new ServerFacadeException("Not implemented yet");
    }
    public AuthData login(String username, String password) throws ServerFacadeException {
        throw new ServerFacadeException("Not implemented yet");
    }
    public AuthData logout() throws ServerFacadeException {
        throw new ServerFacadeException("Not implemented yet");
    }

    public void clear() throws ServerFacadeException {
        throw new ServerFacadeException("Not implemented yet");
    }
}
