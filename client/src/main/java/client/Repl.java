package client;
import chess.ChessPiece;
import facade.ServerFacade;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import ui.EscapeSequences;
import websocket.NotiHandler;
import websocket.WSFacade;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import static java.lang.System.out;

public class Repl implements NotiHandler {
    private boolean isLoggedIn = false;
    private final ServerFacade serverFacade;
    private String authToken = null;
    private List<GameData> localGamesList = new ArrayList<>();
    private WSFacade wsFacade;
    private final String serverUrl;
    private ChessGame.TeamColor playerColor;
    private Integer gameJoinedId;



    public Repl(String serverUrl) {
        this.serverUrl = serverUrl;
        this.serverFacade = new ServerFacade(serverUrl);
    }

    public void run() {
        out.println("♕ Welcome to Chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (isLoggedIn) {
                out.print("[LOGGED_IN] >>> ");
            } else {
                out.print("[LOGGED_OUT] >>> ");
            }
            String line = scanner.nextLine();
            String[] tokens = line.split(" ");
            String command = tokens.length > 0 ? tokens[0].toLowerCase() : "help";
            if (isLoggedIn) {
                handlePostLoginCommands(command, tokens, scanner);
            } else {
                handlePreLoginCommands(command, scanner);
                if (command.equals("quit")) {
                    break;
                }
            }
        }
        scanner.close();
        out.println("Exiting Chess.");
    }

    private void handlePostLoginCommands(String command, String[] tokens, Scanner scanner) {
        switch (command) {
            case "help":
                printPostLoginHelp();
                break;
            case "logout":
                logout();
                break;
            case "create":
                createGame(tokens);
                break;
            case "list":
                listGames();
                break;
            case "join":
                joinGame(tokens);
                break;
            case "observe":
                observeGame(tokens);
                break;
            case "leave":
                leave();
                break;
            case "move":
                movePiece(tokens);
                break;
            case "redraw":
                redraw();
                break;
            default:
                out.println("Unknown command. Type 'help' for options.");
                break;
        }
    }

    private void movePiece(String[] tokens) {
        try {
            if (tokens.length > 3) {
                out.println("Error: Usage: move <Start Pos> <End Pos> [Promotion]");
                return;
            }
            if(gameJoinedId == null) {
                out.println("Error: Please join a game");
            }
            String start = tokens[1];
            String end = tokens[2];
            String promotion = (tokens.length > 3) ? tokens[3] : null;
            chess.ChessPosition startPos = parsePos(start);
            chess.ChessPosition endPos = parsePos(end);
            ChessPiece.PieceType promotionPiece = null;
            if (promotion != null) {
                if (promotion.equalsIgnoreCase("QUEEN")) promotionPiece = ChessPiece.PieceType.QUEEN;
                else if (promotion.equalsIgnoreCase("ROOK")) promotionPiece = ChessPiece.PieceType.ROOK;
                else if (promotion.equalsIgnoreCase("KNIGHT")) promotionPiece = ChessPiece.PieceType.KNIGHT;
                else if (promotion.equalsIgnoreCase("BISHOP")) promotionPiece = ChessPiece.PieceType.BISHOP;
            }
            chess.ChessMove move = new chess.ChessMove(startPos, endPos, promotionPiece);
            wsFacade.sendCommand(new websocket.commands.MakeMoveCommand(authToken, gameJoinedId, move));
            out.println("Move sent!");
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }
    private void leave() {
        try {
            if (gameJoinedId != null) {
                wsFacade.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameJoinedId));
                gameJoinedId = null;
                playerColor = null;
                out.println("Left the game.");
            } else {
                out.println("Error: You are not in a game.");
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }
    private void redraw() {
        if (gameJoinedId == null) {
            out.println("Error: You are not in a game.");
        }
        try {
            wsFacade.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameJoinedId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private chess.ChessPosition parsePos(String pos) throws Exception {
        if (pos.length() != 2) throw new Exception("Invalid position: " + pos);
        char colChar = pos.charAt(0);
        char rowChar = pos.charAt(1);
        int col = colChar - 'a' + 1;
        int row = rowChar - '1' + 1;
        if (col < 1 || col > 8 || row < 1 || row > 8) throw new Exception("Position out of bounds: " + pos);
        return new chess.ChessPosition(row, col);
    }

    private void handlePreLoginCommands(String command, Scanner scanner) {
        switch (command) {
            case "help":
                printPreLoginHelp();
                break;
            case "quit":
                break;
            case "login":
                login(scanner);
                break;
            case "register":
                register(scanner);
                break;
            default:
                out.println("Unknown command. Type 'help' for options.");
                break;
        }
    }

    private void register(Scanner scanner) {
        try {
            out.print("Enter username: ");
            String username = scanner.nextLine();
            out.print("Enter password: ");
            String password = scanner.nextLine();
            out.print("Enter email: ");
            String email = scanner.nextLine();

            AuthData authData = serverFacade.register(username, password, email);

            this.authToken = authData.authToken();
            this.isLoggedIn = true;
            out.println("\nRegistration successful. You are now logged in " + username);
            listGames();
            printPostLoginHelp();
        } catch (ServerFacade.ServerFacadeException message) {
            out.println(message.getMessage());
        } catch (Exception message) {
            out.println("An unexpected error occurred while registering: " + message.getMessage());
        }
    }

    private void login(Scanner scanner) {
        try {
            out.print("Enter username: ");
            String username = scanner.nextLine();
            out.print("Enter password: ");
            String password = scanner.nextLine();

            AuthData authSession = serverFacade.login(username, password);
            this.authToken = authSession.authToken();
            this.isLoggedIn = true;
            out.println("\nLogin successful. You are now logged in " + username);
            listGames();
        } catch (ServerFacade.ServerFacadeException message) {
            out.println(message.getMessage());
        } catch (Exception message) {
            out.println("An unexpected error occurred while logging in: " + message.getMessage());
        }
    }

    private void logout() {
        try {
            serverFacade.logout(this.authToken);
            this.isLoggedIn = false;
            this.authToken = null;
            this.localGamesList.clear();
            System.out.println("Logged out successfully.");
        } catch (ServerFacade.ServerFacadeException e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            var listOfGames = serverFacade.listGames(authToken);
            this.localGamesList.clear(); // 1. Clear the old list
            this.localGamesList.addAll(listOfGames);
            out.println("\nList of games: " + localGamesList.size());
            if (localGamesList.isEmpty()) {
                out.println("  No games found.");
            } else {
                for (int i = 0; i < localGamesList.size(); i++) {
                    GameData game = localGamesList.get(i);
                    out.printf("  %d. %s (White: %s, Black: %s)%n",
                            i + 1,
                            game.gameName(),
                            game.whiteUsername() != null ? game.whiteUsername() : "available",
                            game.blackUsername() != null ? game.blackUsername() : "available"
                    );
                }
            }
        } catch (ServerFacade.ServerFacadeException message) {
            out.println(message.getMessage());
        } catch (Exception message) {
            out.println("An unexpected error occurred while logging out: " + message.getMessage());
        }
    }

    private void joinGame(String[] tokens) {
        if (tokens.length != 3) {
            out.println("Error: Incorrect number of arguments.");
            out.println("Usage: join <COLOR> <ID>");
            return;
        }
        String colorInput = tokens[1];
        String idInput = tokens[2];
        int gameNum;
        try {
            gameNum = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            out.println("Error: Invalid Game ID. '" + idInput + "' is not a number.");
            out.println("Usage: join <COLOR> <ID>");
            return;
        }
        String color = colorInput.toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            out.println("Error: Invalid color '" + colorInput + "'. Please choose WHITE or BLACK.");
            return;
        }
        try {
            if (gameNum <= 0 || gameNum > this.localGamesList.size()) {
                out.println("Error: Game ID " + gameNum + " does not exist or is not available.");
                out.println("Run 'list' to see available games.");
                return;
            }
            GameData gameToJoin = this.localGamesList.get(gameNum - 1);
            int gameID = gameToJoin.gameID();

            serverFacade.joinGame(gameID, color, this.authToken);
            out.println("Successfully joined game " + gameToJoin.gameName() + " as " + color);
            if (color.equalsIgnoreCase("WHITE")) {
                this.playerColor = ChessGame.TeamColor.WHITE;
            } else {
                this.playerColor = ChessGame.TeamColor.BLACK;
            }
            this.gameJoinedId = gameID;
            this.wsFacade = new WSFacade(this, serverUrl);
            wsFacade.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));

        } catch (ServerFacade.ServerFacadeException message) {
            out.println(message.getMessage());
        } catch (Exception e) {
            out.println("An unexpected system error occurred: " + e.getMessage());
        }
    }

    private void createGame(String[] tokens) {
        try {
            if (tokens.length < 2) {
                out.println("Error: Please provide a name for the game.");
                out.println("Usage: create <NAME>");
                return;
            }
            String gameName = tokens[1];
            GameData game = serverFacade.createGame(gameName, this.authToken);
            out.println("Game created: " + game.gameName());
        } catch (ServerFacade.ServerFacadeException e) {
            out.println("Failed to create game: " + e.getMessage());
        }
    }

    private void observeGame(String[] tokens) {
        try {
            if (tokens.length < 2) {
                out.println("Error: Please provide a game ID.");
                out.println("Usage: observe <ID>");
                return;
            }
            int gameNumber = Integer.parseInt(tokens[1]);
            GameData gameToJoin = this.localGamesList.get(gameNumber - 1);
            this.playerColor = ChessGame.TeamColor.WHITE;
            this.gameJoinedId = gameToJoin.gameID();
            try {
                this.wsFacade = new WSFacade(this, serverUrl);
                wsFacade.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameToJoin.gameID()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            out.println("Error: Invalid game ID. Run 'list' to see available games.");
        }
    }

    private void printPostLoginHelp() {
        out.println();
        out.println("Available commands:");
        out.println("  help          - Show this message");
        out.println("  logout        - Log out and return to pre-login");
        out.println("  create <NAME> - Create a new game");
        out.println("  list          - List all available games");
        out.println("  join <COLOR> <ID> - Join a game as WHITE or BLACK");
        out.println("  observe <ID>  - Join a game as an observer");
        out.println("  redraw        - Redraw a board");
        out.println("  leave        - Leave a game");
        out.println("  move <e2> <e4>   - Move a piece from one spot to another");
        out.println();
    }
    private void printPreLoginHelp() {
        out.println();
        out.println("Available commands:");
        out.println("  help      - Show this message");
        out.println("  register  - Create your new account");
        out.println("  login     - Log in to your existing account");
        out.println("  quit      - Exit the app");
        out.println();
    }
    private void drawChessboard(GameData game, String perspective) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(EscapeSequences.ERASE_SCREEN);
        ChessGame chessGame = (ChessGame) game.game();
        chess.ChessBoard board = chessGame.getBoard();
        if (perspective.equalsIgnoreCase("WHITE")) {
            drawBoardInternal(out, board, ChessGame.TeamColor.WHITE);
        } else {
            drawBoardInternal(out, board, ChessGame.TeamColor.BLACK);
        }
        out.print(EscapeSequences.RESET_TEXT_COLOR);
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.println();
    }
    private void drawBoardInternal(PrintStream out, chess.ChessBoard board, ChessGame.TeamColor perspective) {
        drawHeader(out, perspective);
        int startRow = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rowIncrement = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;
        int startCol = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int endCol = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int colIncrement = (perspective == ChessGame.TeamColor.WHITE) ? 1 : -1;
        for (int row = startRow; row != endRow + rowIncrement; row += rowIncrement) {
            drawRowLabel(out, row);
            for (int col = startCol; col != endCol + colIncrement; col += colIncrement) {
                boolean isLightSquare = (row + col) % 2 != 0;
                if (isLightSquare) {
                    out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                } else {
                    out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
                }
                String piece = getPieceFromBoard(board, row, col);
                out.print(piece);
            }
            out.print(EscapeSequences.RESET_BG_COLOR);
            drawRowLabel(out, row);
            out.println();
        }
        drawHeader(out, perspective);
    }
    private void drawHeader(PrintStream out, ChessGame.TeamColor perspective) {
        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        out.print(EscapeSequences.EMPTY);
        String[] headers = (perspective == ChessGame.TeamColor.WHITE) ?
                new String[]{" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "} :
                new String[]{" h ", " g ", " f ", " e ", " d ", " c ", " b ", " a "};
        for (String header : headers) {
            out.print(header);
        }
        out.print(EscapeSequences.EMPTY);
        out.print(EscapeSequences.RESET_TEXT_COLOR);
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.println();
    }
    private void drawRowLabel(PrintStream out, int row) {
        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        out.print(" " + row + " ");
        out.print(EscapeSequences.RESET_TEXT_COLOR);
        out.print(EscapeSequences.RESET_BG_COLOR);
    }
    private String getPieceFromBoard(chess.ChessBoard board, int row, int col) {
        chess.ChessPosition position = new chess.ChessPosition(row, col);
        chess.ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            String pieceColor = EscapeSequences.SET_TEXT_COLOR_BLUE;
            return switch (piece.getPieceType()) {
                case ROOK -> pieceColor + EscapeSequences.WHITE_ROOK;
                case KNIGHT -> pieceColor + EscapeSequences.WHITE_KNIGHT;
                case BISHOP -> pieceColor + EscapeSequences.WHITE_BISHOP;
                case QUEEN -> pieceColor + EscapeSequences.WHITE_QUEEN;
                case KING -> pieceColor + EscapeSequences.WHITE_KING;
                case PAWN -> pieceColor + EscapeSequences.WHITE_PAWN;
            };
        } else {
            String pieceColor = EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
            return switch (piece.getPieceType()) {
                case ROOK -> pieceColor + EscapeSequences.BLACK_ROOK;
                case KNIGHT -> pieceColor + EscapeSequences.BLACK_KNIGHT;
                case BISHOP -> pieceColor + EscapeSequences.BLACK_BISHOP;
                case QUEEN -> pieceColor + EscapeSequences.BLACK_QUEEN;
                case KING -> pieceColor + EscapeSequences.BLACK_KING;
                case PAWN -> pieceColor + EscapeSequences.BLACK_PAWN;
            };
        }
    }
    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadGame = (LoadGameMessage) message;
                out.println();
                drawBoardInternal(new PrintStream(System.out, true, StandardCharsets.UTF_8), loadGame.getGame().getBoard(), this.playerColor);
                out.print("\n[LOGGED_IN] >>> ");
            }
            case NOTIFICATION -> {
                NotificationMessage noti = (NotificationMessage) message;
                out.println();
                out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + noti.getNotification() + EscapeSequences.RESET_TEXT_COLOR);
                out.print("[LOGGED_IN] >>> ");
            }
            case ERROR -> {
                ErrorMessage error = (ErrorMessage) message;
                out.println();
                out.println(EscapeSequences.SET_TEXT_COLOR_RED + error.getError() + EscapeSequences.RESET_TEXT_COLOR);
                out.print("[LOGGED_IN] >>> ");
            }
        }
    }
}