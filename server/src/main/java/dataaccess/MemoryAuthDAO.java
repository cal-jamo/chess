package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authTokens = new HashMap<>();

    // method used to create an auth session and store it in my auth tokens storage
    @Override
    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authTokens.put(authToken, authData);
        return authData;
    }

    // Simply takes in an auth token and returns the stored auth data corresponding to the auth token
    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        Object deletedData = authTokens.remove(authToken);

        if (deletedData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    // clear out the hash map
    @Override
    public void clear() {
        authTokens.clear();
    }
}