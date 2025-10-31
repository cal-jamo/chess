package service;
import dataaccess.*;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }
    public AuthData registerUser(UserData user) throws DataAccessException{
        if (user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: Username and password cannot be null");
        }
        if (userDAO.getUser(user.username()) != null) {
            throw new DataAccessException("Error: Username already exists");
        }
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData userToCreate = new UserData(user.username(), hashedPassword, user.email());
        userDAO.insertUser(userToCreate);
        return authDAO.createAuth(user.username());
    }
    public AuthData loginUser(UserData userData) throws DataAccessException{
        if (userData.username() == null || userData.password() == null) {
            throw new DataAccessException("Error: Username and Password cannot be null");
        }
        UserData existingUser = userDAO.getUser(userData.username());
        if (existingUser == null || !userData.password().equals(existingUser.password())) {
            throw new DataAccessException("Error: Username and password does not match");
        }
        return authDAO.createAuth(userData.username());
    }
    public void logoutUser(String authToken) throws DataAccessException{
        authDAO.deleteAuth(authToken);
    }
}