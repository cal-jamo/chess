package client;

import ServerFacade.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() {

        assertDoesNotThrow(() -> facade.clear());
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
