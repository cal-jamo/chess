package dataaccess.user;
import dataaccess.DataAccessException;
import model.UserData;

public class DBUserDAO implements UserDAO {

    @Override
    public void insertUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }
}