package service;
import dataaccess.*;
import model.AuthData;
import model.UserData;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    // Additional methods to manage users can be added here
    public AuthData registerUser(UserData user) throws DataAccessException{
        if (user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: Username and password cannot be null");
        }

        if (userDAO.getUser(user.username()) != null) {
            throw new DataAccessException("Error: Username already exists");
        }

        userDAO.insertUser(user);
        return authDAO.createAuth(user.username());
    }

    public AuthData loginUser(UserData user) throws DataAccessException{
        if (user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: Username and password cannot be null");
        }
        UserData existingUser = userDAO.getUser(user.username());
        if (existingUser == null || !user.password().equals(existingUser.password())) {
            throw new DataAccessException("Error: Username and password does not match");
        }
        return authDAO.createAuth(user.username());
    }

    public void logoutUser(String AuthToken) throws DataAccessException{
        authDAO.deleteAuth(AuthToken);
    }
}