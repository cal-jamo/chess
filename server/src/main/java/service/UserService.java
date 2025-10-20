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
}