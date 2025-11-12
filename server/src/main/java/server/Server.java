package server;
import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.DBAuthDAO;
import dataaccess.game.DBGameDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.DBUserDAO;
import dataaccess.user.UserDAO;
import io.javalin.*;
import io.javalin.json.JavalinGson;
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
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });
        javalin.exception(DataAccessException.class, (e, ctx) -> {
            if (e.getMessage().equals("Error: Unauthorized")) {
                ctx.status(401).json(java.util.Map.of("message", e.getMessage()));
            } else {
                ctx.status(500).json(java.util.Map.of("message", String.format("Error: %s", e.getMessage())));
            }
        });
        try {
            DatabaseManager.createDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
        DBInitialization.initialize();
        AuthDAO authDAO = new DBAuthDAO();
        UserDAO userDAO = new DBUserDAO();
        GameDAO gameDAO = new DBGameDAO();
        ResetService resetService = new ResetService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO, gameDAO);
        GameService gameService = new GameService(authDAO, gameDAO);
        javalin.delete("/db", (req) -> {
            try {
                resetService.resetApplication();
                req.status(200);
            } catch (DataAccessException e) {
                req.status(500).json(java.util.Map.of("message", String.format("Error: %s", e.getMessage())));
            }
        });
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
                } else {req.status(500).json(java.util.Map.of("message", String.format("Error: %s", e.getMessage())));}}
        });
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
                else {req.status(500).json(java.util.Map.of("message", String.format("Error: %s", e.getMessage())));}}
        });
        javalin.delete("/session", (req) -> {
            String authToken = req.header("authorization");
            userService.logoutUser(authToken);
            req.status(200);
        });
        javalin.get("/game", (req) -> {
            String authToken = req.header("authorization");
            Collection<GameData> listOfGames = gameService.listGames(authToken);
            req.status(200).json(java.util.Map.of("games", listOfGames));
        });
        javalin.post("/game", (req) -> {
            try {
                String authToken = req.header("authorization");
                var newGame = new Gson().fromJson(req.body(), java.util.Map.class);
                String gameName = (String) newGame.get("gameName");
                int newGameId = gameService.createGame(authToken, gameName);
                req.status(200).json(java.util.Map.of("gameID", newGameId, "gameName", gameName));
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: Bad Request")) {
                    req.status(400).json(java.util.Map.of("message", e.getMessage()));
                } else if (e.getMessage().equals("Error: Unauthorized")) {
                    req.status(401).json(java.util.Map.of("message", e.getMessage()));
                } else {req.status(500).json(java.util.Map.of("message", String.format("Error: %s", e.getMessage())));}}
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
                } else if (e.getMessage().equals("Error: Unauthorized")) {
                    req.status(401).json(java.util.Map.of("message", e.getMessage()));
                } else if (e.getMessage().equals("Error: Color Already Taken")) {
                    req.status(403).json(java.util.Map.of("message", e.getMessage()));
                } else {req.status(500).json(java.util.Map.of("message", String.format("Error: %s", e.getMessage())));}}
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
