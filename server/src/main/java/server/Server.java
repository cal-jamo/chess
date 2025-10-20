package server;

import dataaccess.*;
import io.javalin.*;
import model.AuthData;
import model.UserData;
import service.ResetService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        AuthDAO authDAO = new MemoryAuthDAO();
        UserDAO userDAO = new MemoryUserDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        ResetService resetService = new ResetService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO, gameDAO);

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

        javalin.post("/user", (req) -> {
            try {
                UserData userData = req.bodyAsClass(UserData.class);
                AuthData authData = userService.registerUser(userData);
                req.json(authData);
                req.status(200);
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Username already exists")) {
                    req.status(409);
                } else if (e.getMessage().equals("Username and password cannot be null")) {
                    req.status(400);
                } else {
                    req.status(500);
                }
            }
        });


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
