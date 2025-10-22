package server;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.GameService;
import service.ResetService;
import service.UserService;
import model.JoinRequest;
import java.util.Collection;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        AuthDAO authDAO = new MemoryAuthDAO();
        UserDAO userDAO = new MemoryUserDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        ResetService resetService = new ResetService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO, gameDAO);
        GameService gameService = new GameService(authDAO, gameDAO);

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
            try {
                var userData = new Gson().fromJson(req.body(), UserData.class);
                var authSession = userService.loginUser(userData);
                req.status(200).json(authSession);
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: Username and Password cannot be null")) {
                    req.status(400).json(java.util.Map.of("message", e.getMessage()));
                } else if (e.getMessage().equals("Error: Username and password does not match")) {
                    req.status(401).json(java.util.Map.of("message", e.getMessage()));
                }
                else {
                    req.status(500).json(java.util.Map.of("message", e.getMessage()));
                }
            }
        });

        // endpoint for delete method to logout a user
        javalin.delete("/session", (req) -> {
            try {
                String authToken = req.header("authorization");
                userService.logoutUser(authToken);
                req.status(200);
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: unauthorized")) {
                    req.status(401).json(java.util.Map.of("message", e.getMessage()));
                } else {
                    req.status(500).json(java.util.Map.of("message", String.format("Error: %s", e.getMessage())));
                }
            }
        });

        // endpoint to list all games stored in memory
        javalin.get("/game", (req) -> {
            try {
                String authToken = req.header("authorization");
                Collection<GameData> listOfGames = gameService.listGames(authToken);
                req.status(200).json(java.util.Map.of("games", listOfGames));
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: unauthorized")) {
                    req.status(401).json(java.util.Map.of("message", e.getMessage()));
                } else {
                    req.status(500).json(java.util.Map.of("message", e.getMessage()));
                }

            }
        });

        // endpoint to create a new game given a valid auth token and game name
        javalin.post("/game", (req) -> {
            try {
                String authToken = req.header("authorization");
                var newGame = new Gson().fromJson(req.body(), java.util.Map.class);
                String gameName = (String) newGame.get("gameName");
                int newGameId = gameService.createGame(authToken, gameName);
                req.status(200).json(java.util.Map.of("gameID", newGameId));
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: Bad Request")) {
                    req.status(400).json(java.util.Map.of("message", e.getMessage()));
                } else if (e.getMessage().equals("Error: Unauthorized")) {
                    req.status(401).json(java.util.Map.of("message", e.getMessage()));
                } else {
                    req.status(500).json(java.util.Map.of("message", e.getMessage()));
                }

            }
        });

        javalin.put("/game", (req) -> {
            try {
                String authToken = req.header("authorization");
                var joinGameRequest = new Gson().fromJson(req.body(), JoinRequest.class);
                gameService.joinGame(authToken, joinGameRequest);
                req.status(200);
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: Bad Request")) {
                    req.status(400).json(java.util.Map.of("message", e.getMessage()));
                    System.out.print(req.status());
                } else if (e.getMessage().equals("Error: Unauthorized")) {
                    req.status(401).json(java.util.Map.of("message", e.getMessage()));
                } else if (e.getMessage().equals("Error: Color already taken")) {
                    req.status(403).json(java.util.Map.of("message", e.getMessage()));
                } else {
                    req.status(500).json(java.util.Map.of("message", e.getMessage()));
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
