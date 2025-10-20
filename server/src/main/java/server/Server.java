package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import model.AuthData;
import model.UserData;
import service.ResetService;
import service.UserService;

import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

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

        // user endpoint for registering a user
        javalin.post("/user", (req) -> {
            try {
                UserData userData = new Gson().fromJson(req.body(), UserData.class);
                AuthData authData = userService.registerUser(userData);
                req.status(200).json(authData);
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: Username already exists")) {
                    req.status(403).json(java.util.Map.of("message", e.getMessage()));
                } else if (e.getMessage().equals("Error: Username and password cannot be null")) {
                    req.status(400).json(java.util.Map.of("message", e.getMessage()));
                } else {
                    req.status(500).json(java.util.Map.of("message", e.getMessage()));
                }
            }
        });

        // endpoint for post method to log in a user
        javalin.post("/session", (req) -> {

        });

        // endpoint for delete method to logout a user
        javalin.delete("/session", (req) -> {
            try {
                AuthData authData = new Gson().fromJson(req.body(), AuthData.class);
                userService.logoutUser(authData.authToken());
            } catch (DataAccessException e) {
                req.status(403).json(java.util.Map.of("message", e.getMessage()));
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
