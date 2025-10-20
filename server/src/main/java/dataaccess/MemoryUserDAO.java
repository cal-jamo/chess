package dataaccess;


import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> storedUsers = new HashMap<>();

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (storedUsers.containsKey(user.username())) {
            throw new DataAccessException("Error: Username already taken");
        }
        storedUsers.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return storedUsers.get(username);
    }

    @Override
    public void clear() {
        storedUsers.clear();
    }
}