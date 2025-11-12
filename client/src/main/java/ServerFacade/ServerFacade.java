package ServerFacade;
import com.google.gson.Gson;
import com.sun.net.httpserver.Request;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class ServerFacade {
    private final String serverURL;
    public ServerFacade(int port) {
        this.serverURL = "http://localhost:" + port;
    }
    private <T> T makeRequest(String method, String path, Object requestBody, String authToken, Class<T> responseClass) throws ServerFacadeException {
        try {
            URL url = (new URI(this.serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (authToken != null) {
                http.setRequestProperty("authorization", authToken);
            }
            http.setRequestProperty("Content-Type", "application/json");
            if (requestBody != null) {
                try (OutputStream os = http.getOutputStream()) {
                    var jsonBody = new Gson().toJson(requestBody);
                    os.write(jsonBody.getBytes("UTF-8"));
                }
            }
            http.connect();
            return handleResponse(http, responseClass);

        } catch (Exception ex) {
            throw new ServerFacadeException(ex.getMessage());
        }
    }
    private <T> T handleResponse(HttpURLConnection http, Class<T> responseClass) throws IOException, ServerFacadeException {
        if (http.getResponseCode() >= 400) {
            String errorMessage;
            try (InputStream respBody = http.getErrorStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                var errorResponse = new Gson().fromJson(reader, java.util.Map.class);
                errorMessage = (String) errorResponse.get("message");
            }
            throw new ServerFacadeException(errorMessage);
        }
        T response = null;
        if (responseClass != null) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                response = new Gson().fromJson(reader, responseClass);
            }
        }
        return response;
    }

    public static class ServerFacadeException extends Exception {
        public ServerFacadeException(String message) {
            super(message);
        }
    }

    public AuthData register(String username, String password, String email) throws ServerFacadeException {
        UserData requestInfo = new UserData(username, password, email);
        return this.makeRequest("POST", "/user", requestInfo, null, AuthData.class);
    }
    public AuthData login(String username, String password) throws ServerFacadeException {
        UserData requestInfo = new UserData(username, password, null);
        return this.makeRequest("POST", "/session", requestInfo, null, AuthData.class);
    }
    public void logout(String authToken) throws ServerFacadeException {
        throw new ServerFacadeException("Not implemented yet");
    }

    public void clear() throws ServerFacadeException {
        this.makeRequest("DELETE", "/db", null, null, null);
    }
}
