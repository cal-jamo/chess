package service;

import dataaccess.*;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.MemoryUserDAO;
import dataaccess.user.UserDAO;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Collection;

public class ServiceTests {

    private static UserDAO userDAO;
    private static AuthDAO authDAO;
    private static GameDAO gameDAO;
    private static UserService userService;
    private static GameService gameService;
    private static ResetService resetService;

    @BeforeAll
    public static void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO, gameDAO);
        gameService = new GameService(authDAO, gameDAO);
        resetService = new ResetService(userDAO, authDAO, gameDAO);
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        resetService.resetApplication();
    }

    @Test
    @Order(1)
    @DisplayName("register positive")
    public void registerPositive() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        AuthData authData = userService.registerUser(userToRegister);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals("cwjamo", authData.username());
    }
    @Test
    @Order(2)
    @DisplayName("register negative: Username Taken")
    public void registerNameTaken() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        AuthData authData = userService.registerUser(userToRegister);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals("cwjamo", authData.username());
        UserData secondUserToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        Assertions.assertThrows(DataAccessException.class, () -> {userService.registerUser(secondUserToRegister);});
    }
    @Test
    @Order(3)
    @DisplayName("register negative: null username")
    public void registerNullUsername() throws DataAccessException {
        UserData userToRegister = new UserData(null, "halogamer", "cwjamo@byu.edu");
        Assertions.assertThrows(DataAccessException.class, () -> {userService.registerUser(userToRegister);});
    }
    @Test
    @Order(4)
    @DisplayName("Login user positive")
    public void loginPositive() throws DataAccessException {
        UserData userObj = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userObj);
        AuthData authData = userService.loginUser(userObj);
        Assertions.assertNotNull(authData.authToken());
    }
    @Test
    @Order(5)
    @DisplayName("Login user negative: Password doesn't match")
    public void loginPasswordNotMatch() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userToRegister);
        UserData userLogin = new UserData("cwjamo", "halogamers", "cwjamo@byu.edu");
        Assertions.assertThrows(DataAccessException.class, () -> {userService.loginUser(userLogin);});
    }
    @Test
    @Order(6)
    @DisplayName("Logout user positive")
    public void logoutPositive() throws DataAccessException {
        UserData userObj = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userObj);
        AuthData authData = userService.loginUser(userObj);
        Assertions.assertNotNull(authData.authToken());
        Assertions.assertDoesNotThrow(() -> userService.logoutUser(authData.authToken()));
    }
    @Test
    @Order(7)
    @DisplayName("Logout user negative: bad auth")
    public void logoutBadAuth() {
        Assertions.assertThrows(DataAccessException.class, () -> userService.logoutUser("iLoveByuBasketball"));
    }
    @Test
    @Order(8)
    @DisplayName("Reset success")
    public void resetSuccess() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userToRegister);
        AuthData loginData = userService.loginUser(userToRegister);
        Assertions.assertNotNull(loginData.authToken());
        gameService.createGame(loginData.authToken(), "testGame");
        resetService.resetApplication();
        UserData userLogin = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        Assertions.assertThrows(DataAccessException.class, () -> {userService.loginUser(userLogin);});
    }
    @Test
    @Order(9)
    @DisplayName("Create game positive")
    public void createGamePositive() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userToRegister);
        AuthData loginData = userService.loginUser(userToRegister);
        Assertions.assertNotNull(loginData.authToken());
        int gameCreated = gameService.createGame(loginData.authToken(), "testGame");
        Assertions.assertNotEquals(0, gameCreated);
    }
    @Test
    @Order(10)
    @DisplayName("Create game negative: null game name")
    public void createGameNegative() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userToRegister);
        AuthData loginData = userService.loginUser(userToRegister);
        Assertions.assertNotNull(loginData.authToken());
        Assertions.assertThrows(DataAccessException.class, () -> {gameService.createGame(loginData.authToken(), null);});
    }
    @Test
    @Order(11)
    @DisplayName("Join game positive")
    public void joinGamePositive() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userToRegister);
        AuthData loginData = userService.loginUser(userToRegister);
        Assertions.assertNotNull(loginData.authToken());
        int gameCreated = gameService.createGame(loginData.authToken(), "testGame");
        JoinRequest request = new JoinRequest("WHITE", gameCreated);
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(loginData.authToken(), request));
    }
    @Test
    @Order(12)
    @DisplayName("Join game negative: no color given")
    public void joinGameNegative() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userToRegister);
        AuthData loginData = userService.loginUser(userToRegister);
        Assertions.assertNotNull(loginData.authToken());
        int gameCreated = gameService.createGame(loginData.authToken(), "testGame");
        JoinRequest request = new JoinRequest("", gameCreated);
        Assertions.assertThrows(DataAccessException.class, () -> gameService.joinGame(loginData.authToken(), request));
    }
    @Test
    @Order(13)
    @DisplayName("List game positive")
    public void listGamePositive() throws DataAccessException {
        UserData userToRegister = new UserData("cwjamo", "halogamer", "cwjamo@byu.edu");
        userService.registerUser(userToRegister);
        AuthData loginData = userService.loginUser(userToRegister);
        Assertions.assertNotNull(loginData.authToken());
        gameService.createGame(loginData.authToken(), "testGame");
        Collection<GameData> gamesList = gameService.listGames(loginData.authToken());
        Assertions.assertEquals(1, gamesList.size());
    }
    @Test
    @Order(14)
    @DisplayName("List game negative: bad auth")
    public void listGameNegative() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class, () -> gameService.listGames("iLoveByuBasketball"));
    }
}
