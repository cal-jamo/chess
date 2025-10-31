package dataaccess;

import dataaccess.*;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.DBAuthDAO;
import dataaccess.game.DBGameDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.DBUserDAO;
import dataaccess.user.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTests {

    private static UserDAO userDAO;
    private static AuthDAO authDAO;
    private static GameDAO gameDAO;

    @BeforeAll
    public static void setup() throws DataAccessException {
        DBInitialization.initialize();
        userDAO = new DBUserDAO();
        authDAO = new DBAuthDAO();
        gameDAO = new DBGameDAO();
    }

    @BeforeEach
    public void clearAllTables() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
    @Nested
    @DisplayName("DBAuthDAO Tests")
    class AuthDAOTests {

        @Test
        @DisplayName("Positive createAuth")
        public void createAuthPositive() throws DataAccessException {
            AuthData auth = authDAO.createAuth("cwjamo");

            assertNotNull(auth);
            assertNotNull(auth.authToken());
            assertEquals("cwjamo", auth.username());
        }

        @Test
        @DisplayName("Negative createAuth: null username")
        public void createAuthNegative() throws DataAccessException {
            assertThrows(DataAccessException.class, () -> {
                authDAO.createAuth(null);
            });
        }

        @Test
        @DisplayName("Positive getAuth")
        public void getAuthPositive() throws DataAccessException {
            AuthData createdAuth = authDAO.createAuth("cwjamo");
            AuthData fromDB = authDAO.getAuth(createdAuth.authToken());
            assertNotNull(fromDB);
            assertEquals(createdAuth, fromDB);
        }

        @Test
        @DisplayName("Negative getAuth: bad auth token")
        public void getAuthNegative() throws DataAccessException {
            AuthData fromDB = authDAO.getAuth("iHeartBYUBball");
            assertNull(fromDB);
        }

        @Test
        @DisplayName("Positive deleteAuth")
        public void deleteAuthPositive() throws DataAccessException {
            AuthData createdAuth = authDAO.createAuth("cwjamo");
            assertNotNull(authDAO.getAuth(createdAuth.authToken()));
            authDAO.deleteAuth(createdAuth.authToken());
            assertNull(authDAO.getAuth(createdAuth.authToken()));
        }

        @Test
        @DisplayName("Negative deleteAuth: bad auth")
        public void deleteAuthNegative() throws DataAccessException {
            assertDoesNotThrow(() -> {
                authDAO.deleteAuth("iHeartBYUBball");
            });
        }

        @Test
        @DisplayName("Positive clear Auth")
        public void clearAuthPositive() throws DataAccessException {
            authDAO.createAuth("cwjamo");
            authDAO.clear();
            AuthData auth = authDAO.createAuth("cwjamo");
            assertNotNull(authDAO.getAuth(auth.authToken()));
            authDAO.clear();
            assertNull(authDAO.getAuth(auth.authToken()));
        }
    }
    @Nested
    @DisplayName("DBUserDAO Tests")
    class UserDAOTests {

        @Test
        @DisplayName("Positive insertUser")
        public void insertUserPositive() throws DataAccessException {
            UserData user = new UserData("cwjamo", "halogamer", "cwjamo@email.com");
            userDAO.insertUser(user);

            UserData fromDB = userDAO.getUser("cwjamo");
            assertNotNull(fromDB);
            assertEquals(user.username(), fromDB.username());
            assertEquals(user.email(), fromDB.email());
            assertEquals(user.password(), fromDB.password());
        }

        @Test
        @DisplayName("Negative insertUser: username taken")
        public void insertUserNegative() throws DataAccessException {
            UserData user = new UserData("cwjamo", "halogamer", "cwjamo@email.com");
            userDAO.insertUser(user);
            UserData duplicateUser = new UserData("cwjamo", "Halogamer", "cjwamo@email.com");
            assertThrows(DataAccessException.class, () -> {
                userDAO.insertUser(duplicateUser);
            });
        }

        @Test
        @DisplayName("Positive getUser")
        public void getUserPositive() throws DataAccessException {
            UserData user = new UserData("cwjamo", "halogamer", "kpdh@calvin.com");
            userDAO.insertUser(user);
            UserData fromDB = userDAO.getUser("cwjamo");
            assertEquals(user, fromDB);
        }

        @Test
        @DisplayName("Negative getUser: user doesnt exist")
        public void getUserNegative() throws DataAccessException {
            UserData fromDB = userDAO.getUser("ajDB");
            assertNull(fromDB);
        }

        @Test
        @DisplayName("Positive clear Users")
        public void clearUserPositive() throws DataAccessException {
            UserData user = new UserData("cwjamo", "halogamer", "kpdh@calvin.com");
            userDAO.insertUser(user);
            userDAO.clear();
            UserData fromDB = userDAO.getUser("cwjamo");
            assertNull(fromDB);
        }
    }
}
