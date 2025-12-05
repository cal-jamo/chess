package dataaccess.auth;
import dataaccess.DataAccessException;
import model.AuthData;
import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authTokens = new HashMap<>();

    @Override
    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authTokens.put(authToken, authData);
        return authData;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        Object deletedData = authTokens.remove(authToken);

        if (deletedData == null) {
            throw new DataAccessException("Error: Unauthorized");
        }
    }

    @Override
    public void clear() {
        authTokens.clear();
    }
}