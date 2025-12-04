package webSocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import chess.ChessGame;
import chess.InvalidMoveException;
import chess.ChessMove;

import java.io.IOException;


public class WebSocketHandler {
    private final ConnectionManager sessions = new ConnectionManager();
    private final Gson gson = new Gson();
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    public WebSocketHandler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void handleMessage(WsMessageContext cx) {
        try {
            String message = cx.message();
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), cx);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                    makeMove(moveCommand.getAuthToken(), moveCommand.getGameID(), moveCommand.getMove(), cx);
                }
                case LEAVE -> {
                    leave(command.getAuthToken(), command.getGameID());
                }
                case RESIGN -> {
                    resign(command.getAuthToken(), command.getGameID());
                }
            }
        } catch (Exception e) {
            ErrorMessage errorMessage = new ErrorMessage("Error: " + e.getMessage());
            cx.send(gson.toJson(errorMessage));
        }
    }

    private void makeMove(String authToken, Integer gameID, ChessMove move, WsMessageContext cx) throws Exception {
        GameData gameData = gameDAO.getGame(gameID);
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Bad auth token");
        }
        if (gameData == null) {
            throw new DataAccessException("Bad gameID");
        }
        String username = authData.username();
        ChessGame currGame = gameData.game();
        if (currGame.isGameOver() || currGame.isInCheckmate(currGame.getTeamTurn()) || currGame.isInStalemate(currGame.getTeamTurn())) {
            throw new DataAccessException("Error: Game is over");
        }
        if (currGame.isInCheckmate(currGame.getTeamTurn()) || currGame.isInStalemate(currGame.getTeamTurn())) {
            throw new DataAccessException("Error: Game is over");
        }
        ChessGame.TeamColor currColor = currGame.getBoard().getPiece(move.getStartPosition()).getTeamColor();
        if (currColor == ChessGame.TeamColor.BLACK) {
            if (!username.equals(gameData.blackUsername())) {
                throw new DataAccessException("Error: This piece isn't on your team");
            }
        } else if (currColor == ChessGame.TeamColor.WHITE) {
            if (!username.equals(gameData.whiteUsername())) {
                throw new DataAccessException("Error: This piece isn't on your team");
            }
        } else {
            throw new DataAccessException("Error: You can't move this piece");
        }
        try {
            currGame.makeMove(move);
        } catch (InvalidMoveException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
        GameData gameAfterMove = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), currGame);
        gameDAO.updateGame(gameAfterMove.gameID(), gameAfterMove);
        LoadGameMessage loadGame = new LoadGameMessage(currGame);
        try {
            sessions.broadcast(loadGame, null, gameID);
        } catch (Exception e) {
            throw new IOException("Error: " + e.getMessage());
        }
        ChessGame.TeamColor opponentColor = currGame.getTeamTurn();
        String opponentUsername = (opponentColor == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
        NotificationMessage noti = new NotificationMessage(username + " made move " + move.toString());
        try {
            sessions.broadcast(noti, authToken, gameID);
        } catch (Exception e) {
            throw new IOException("Error: " + e.getMessage());
        }
        if (currGame.isInCheckmate(opponentColor)) {
            NotificationMessage checkmateNoti = new NotificationMessage(opponentUsername + " is in CHECKMATE");
            try {
                sessions.broadcast(checkmateNoti, null, gameID);
            } catch (Exception e) {
                throw new IOException("Error: " + e.getMessage());
            }
        } else if (currGame.isInCheck(opponentColor)) {
            NotificationMessage checkNoti = new NotificationMessage(opponentUsername + " is in CHECK");
            try {
                sessions.broadcast(checkNoti, null, gameID);
            } catch (Exception e) {
                throw new IOException("Error: " + e.getMessage());
            }
        }
    }

    private void resign(String authToken, Integer gameID) throws Exception {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) throw new DataAccessException("Bad auth token");
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) throw new DataAccessException("Bad game ID");
        ChessGame currGame = gameData.game();
        String username = authData.username();
        if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
            throw new DataAccessException("Error: Observer cannot resign");
        }
        if (currGame.isGameOver()) {
            throw new DataAccessException("Error: Game is already over");
        }
        currGame.setGameOver(true);
        GameData updatedGame = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), currGame);
        gameDAO.updateGame(gameID, updatedGame);
        NotificationMessage notification = new NotificationMessage(username + " resigned the game");
        sessions.broadcast(notification, null, gameID); // Pass null to send to EVERYONE
    }

    private void leave(String authToken, Integer gameID) throws Exception {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) throw new DataAccessException("Bad auth token");
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) throw new DataAccessException("Bad game ID");
        sessions.remove(authToken);
        NotificationMessage notification = new NotificationMessage(authData.username() + " left the game");
        sessions.broadcast(notification, authToken, gameID);
        String username = authData.username();
        GameData updatedGame = null;
        if (username.equals(gameData.whiteUsername())) {
            updatedGame = new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (username.equals(gameData.blackUsername())) {
            updatedGame = new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        }
        if (updatedGame != null) {
            gameDAO.updateGame(gameID, updatedGame);
        }
    }

    private void connect(String authToken, Integer gameId, WsMessageContext cx) throws Exception {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Bad auth token");
        }
        GameData gameData = gameDAO.getGame(gameId);
        if (gameData == null) {
            throw new DataAccessException("Bad game ID");
        }
        var connection = new Connection(authToken, cx.session, gameId);
        sessions.add(authToken, connection);
        LoadGameMessage loadGame = new LoadGameMessage(gameData.game());
        cx.send(gson.toJson(loadGame));
        NotificationMessage notification = new NotificationMessage(authData.username() + " joined the game");
        sessions.broadcast(notification, authToken, gameId);
    }
}