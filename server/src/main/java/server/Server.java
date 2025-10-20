package server;

import dataaccess.*;
import io.javalin.*;
import service.ResetService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        AuthDAO authDAO = new MemoryAuthDAO();
        UserDAO userDAO = new MemoryUserDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        ResetService resetService = new ResetService(userDAO, authDAO, gameDAO);

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", (req) -> {
            try {
                resetService.resetApplication();
                req.status(200);
            } catch (DataAccessException e){
                req.status(500);
            }
        }
        );


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
