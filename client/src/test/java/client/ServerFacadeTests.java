package client;

import ServerFacade.ServerFacade;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static AuthData authData;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setup() throws ServerFacade.ServerFacadeException {
        facade.clear();
    }

    @Test
    public void clearDB() throws ServerFacade.ServerFacadeException {
        facade.clear();
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Register Fails: User Already Exists")
    public void registerFailsExistingUser() {
        assertDoesNotThrow(() ->
                facade.register("cwjamo", "password", "cwjamo@email.com")
        );
        ServerFacade.ServerFacadeException exception = assertThrows(
                ServerFacade.ServerFacadeException.class,
                () -> facade.register("cwjamo", "password", "cwjamo@email.com")
        );
        assertNotNull(exception.getMessage(), "Error message should not be null");
    }

    @Test
    @DisplayName("Register Success")
    public void registerSuccess() {
        AuthData authData = assertDoesNotThrow(() ->
                facade.register("cwjamo", "password", "cwjamo@email.com")
        );
        assertNotNull(authData, "AuthData object should not be null");
        assertNotNull(authData.authToken(), "AuthToken should not be null");
        assertEquals("cwjamo", authData.username(), "Username should match the one registered");
    }

    @Test
    @DisplayName("Login Success")
    public void loginSuccess() throws ServerFacade.ServerFacadeException {
        facade.clear();
        facade.register("cal", "password", "cal@email.com");
        AuthData authData = facade.login("cal", "password");
        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals("cal", authData.username());
    }

    @Test
    @DisplayName("Login Fails: Bad password")
    public void loginFailsBadPassword() {
        assertDoesNotThrow(() -> facade.register("cwjamo", "password", "cwjamo@email.com"));
        ServerFacade.ServerFacadeException exception = assertThrows(
                ServerFacade.ServerFacadeException.class,
                () -> facade.login("cwjamo", "byu6767")
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Logout Success")
    public void logoutSuccess() {
        AuthData auth = assertDoesNotThrow(() -> facade.register("cwjamo", "password", "cwjamo@email.com"));
        String authToken = auth.authToken();
        assertDoesNotThrow(() -> facade.logout(authToken));
    }

    @Test
    @DisplayName("Logout Fails: Bad auth")
    public void logoutFailsBadAuth() {
        String authToken = "i-love-byu-basketball";
        ServerFacade.ServerFacadeException exception = assertThrows(
                ServerFacade.ServerFacadeException.class,
                () -> facade.logout(authToken)
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Create Game Success")
    public void createGameSuccess() {
        try {
            var authData = facade.register("cwjamo", "pass", "cwjamo@email.com");
            String authToken = authData.authToken();

            var gameData = facade.createGame("MyNewGame", authToken);
            System.out.println(gameData);

            assertNotNull(gameData);
            assertNotNull(gameData.gameID());
            assertTrue(gameData.gameID() > 0);
            assertEquals("MyNewGame", gameData.gameName());

        } catch (ServerFacade.ServerFacadeException e) {
            fail("Should not have thrown exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Create Game Fail (Bad Token)")
    public void createGameFailBadToken() {
        assertThrows(ServerFacade.ServerFacadeException.class, () -> {
            facade.createGame("FailGame", "this-is-a-bad-token");
        }, "Creating a game with an invalid token should throw an exception.");
    }

    @Test
    @DisplayName("List Games Success")
    public void listGamesSuccess() {
        try {
            var authData = facade.register("cwjamo", "pass", "cwjamo@email.com");
            String authToken = authData.authToken();
            facade.createGame("MyNewGame-1", authToken);
            facade.createGame("MyNewGame-2", authToken);
            Collection<GameData> listOfGames = facade.listGames(authToken);
            assertNotNull(listOfGames);
            assertEquals(2, listOfGames.size(), "Should have found two games.");
            assertTrue(listOfGames.stream().anyMatch(game -> game.gameName().equals("MyNewGame-1")));
            assertTrue(listOfGames.stream().anyMatch(game -> game.gameName().equals("MyNewGame-2")));
        } catch (ServerFacade.ServerFacadeException e) {
            fail("Should not have thrown exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("List Games Fail (Bad Token)")
    public void listGamesFailBadToken() {
        assertThrows(ServerFacade.ServerFacadeException.class, () -> {
            facade.listGames("this-is-a-bad-token");
        }, "Listing games with an invalid token should throw an exception.");
    }

    @Test
    @DisplayName("Join Game Success")
    public void joinGameSuccess() {
        try {
            var authData = facade.register("cwjamo", "pass", "cwjamo@email.com");
            String authToken = authData.authToken();
            GameData createdGame = facade.createGame("MyNewGame-1", authToken);
            int gameID = createdGame.gameID();
            assertDoesNotThrow(() -> facade.joinGame(gameID, "BLACK", authToken));

        } catch (ServerFacade.ServerFacadeException e) {
            fail("Should not have thrown exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Join Game Fail: Bad Game ID")
    public void joinGameFailBadGameID() throws ServerFacade.ServerFacadeException {
        var authData = facade.register("cwjamo", "pass", "cwjamo@email.com");
        String authToken = authData.authToken();
        int nonExistentGameID = 676767;

        assertThrows(ServerFacade.ServerFacadeException.class, () -> {
            facade.joinGame(nonExistentGameID, "BLACK", authToken);
        }, "Joining a non-existent game should throw an exception.");
    }

}
