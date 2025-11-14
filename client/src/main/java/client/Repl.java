package client;
import ServerFacade.ServerFacade;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import ui.EscapeSequences;

import static java.lang.System.out;

public class Repl {
    private boolean isLoggedIn = false;
    private final ServerFacade serverFacade;
    private String authToken = null;
    private List<GameData> localGamesList = new ArrayList<>();


    public Repl(String serverUrl) {
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
            default:
                out.println("Unknown command. Type 'help' for options.");
                break;
        }
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
            out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
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
            out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
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
            out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
        } catch (Exception message) {
            out.println("An unexpected error occurred while logging out: " + message.getMessage());
        }
    }

    private void joinGame(String[] tokens) {
        try {
            if (tokens.length > 3) {
                out.println("Error: Please provide a color and game ID.");
                out.println("Usage: play <COLOR> <ID>");
                return;
            }
            String color = tokens[1];
            int gameNum = Integer.parseInt(tokens[2]);
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                out.println("Error: Invalid color. Please choose WHITE or BLACK.");
                return;
            }
            GameData gameToJoin = this.localGamesList.get(gameNum - 1);
            int gameID = gameToJoin.gameID();

            serverFacade.joinGame(gameID, color, this.authToken);
            out.println("Successfully joined game " + gameToJoin.gameName() + " as " + color);
            drawChessboard(color);
        } catch (ServerFacade.ServerFacadeException message) {
            out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
        } catch (Exception message) {
            out.println("An unexpected error occurred while joining game: " + message.getMessage());
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
            out.println("Game created: " + game.gameName() + " (ID: " + game.gameID() + ")");
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
            int gameID = gameToJoin.gameID();
            serverFacade.joinGame(gameID, null, this.authToken);
            out.println("Successfully observing game " + gameToJoin.gameName());
            drawChessboard("WHITE");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            out.println("Error: Invalid game ID. Run 'list' to see available games.");
        } catch (ServerFacade.ServerFacadeException e) {
            out.println("Failed to observe game: " + e.getMessage());
        }
    }

    private void printPostLoginHelp() {
        out.println();
        out.println("Available commands:");
        out.println("  help          - Show this message");
        out.println("  logout        - Log out and return to pre-login");
        out.println("  create <NAME> - Create a new game");
        out.println("  list          - List all available games");
        out.println("  play <COLOR> <ID> - Join a game as WHITE or BLACK");
        out.println("  observe <ID>  - Join a game as an observer");
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
    private void drawChessboard(String perspective) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(EscapeSequences.ERASE_SCREEN);
        if (perspective.equalsIgnoreCase("WHITE")) {
            drawBoardInternal(out, ChessGame.TeamColor.WHITE);
        } else {
            drawBoardInternal(out, ChessGame.TeamColor.BLACK);
        }
        out.print(EscapeSequences.RESET_TEXT_COLOR);
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.println();
    }

    private void drawBoardInternal(PrintStream out, ChessGame.TeamColor perspective) {
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
                String piece = getInitialPiece(row, col);
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

    private String getInitialPiece(int row, int col) {
        if (row == 2) return EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.WHITE_PAWN;
        if (row == 1) {
            String pieceColor = EscapeSequences.SET_TEXT_COLOR_BLUE;
            if (col == 1 || col == 8) return pieceColor + EscapeSequences.WHITE_ROOK;
            if (col == 2 || col == 7) return pieceColor + EscapeSequences.WHITE_KNIGHT;
            if (col == 3 || col == 6) return pieceColor + EscapeSequences.WHITE_BISHOP;
            if (col == 4) return pieceColor + EscapeSequences.WHITE_QUEEN;
            if (col == 5) return pieceColor + EscapeSequences.WHITE_KING;
        }
        if (row == 7) return EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.BLACK_PAWN;
        if (row == 8) {
            String pieceColor = EscapeSequences.SET_TEXT_COLOR_BLACK;
            if (col == 1 || col == 8) return pieceColor + EscapeSequences.BLACK_ROOK;
            if (col == 2 || col == 7) return pieceColor + EscapeSequences.BLACK_KNIGHT;
            if (col == 3 || col == 6) return pieceColor + EscapeSequences.BLACK_BISHOP;
            if (col == 4) return pieceColor + EscapeSequences.BLACK_QUEEN;
            if (col == 5) return pieceColor + EscapeSequences.BLACK_KING;
        }

        return EscapeSequences.EMPTY;
    }
}