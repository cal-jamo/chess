package client;
import ServerFacade.ServerFacade;
import model.AuthData;
import model.GameData;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Repl {
    private boolean isLoggedIn = false;
    private final ServerFacade serverFacade;
    private String authToken = null;
    private List<GameData> localGamesList = new ArrayList<>();


    public Repl(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to Chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (isLoggedIn) {
                System.out.print("[LOGGED_IN] >>> ");
            } else {
                System.out.print("[LOGGED_OUT] >>> ");
            }
            String line = scanner.nextLine();
            String[] tokens = line.split(" ");
            String command = tokens.length > 0 ? tokens[0].toLowerCase() : "help";
            if (isLoggedIn) {
                handlePostLoginCommands(command, tokens, scanner);
            } else {
                handlePreLoginCommands(command, tokens, scanner);
                if (command.equals("quit")) {
                    break;
                }
            }
        }
        scanner.close();
        System.out.println("Exiting Chess.");
    }

    private void handlePostLoginCommands(String command, String[] tokens, Scanner scanner) {
        switch (command) {
            case "help":
                printPostLoginHelp();
                break;
            case "logout":
                isLoggedIn = false;
                this.authToken = null;
                this.localGamesList.clear();
                logout(scanner);
                break;
            case "create":
                createGame(tokens);
                break;
            case "list":
                listGames();
                break;
            case "joinGame":
                //joinGame(scanner);
                break;
            case "observeGame":
                //observeGame(scanner);
                break;
            default:
                System.out.println("Unknown command. Type 'help' for options.");
                break;
        }
    }

    private void handlePreLoginCommands(String command, String[] tokens, Scanner scanner) {
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
                System.out.println("Unknown command. Type 'help' for options.");
                break;
        }
    }

    private void register(Scanner scanner) {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            AuthData authData = serverFacade.register(username, password, email);

            this.authToken = authData.authToken();
            this.isLoggedIn = true;
            System.out.println("\nRegistration successful. You are now logged in " + username);
            printPostLoginHelp();
        } catch (ServerFacade.ServerFacadeException message) {
            System.out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
        } catch (Exception message) {
            System.out.println("An unexpected error occurred while registering: " + message.getMessage());
        }
    }

    private void login(Scanner scanner) {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            AuthData authSession = serverFacade.login(username, password);
            this.authToken = authSession.authToken();
            this.isLoggedIn = true;
            System.out.println("\nLogin successful. You are now logged in " + username);
        } catch (ServerFacade.ServerFacadeException message) {
            System.out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
        } catch (Exception message) {
            System.out.println("An unexpected error occurred while logging in: " + message.getMessage());
        }
    }

    private void logout(Scanner scanner) {
        try {
            serverFacade.logout(this.authToken);
        } catch (ServerFacade.ServerFacadeException message) {
            System.out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
        } catch (Exception message) {
            System.out.println("An unexpected error occurred while logging out: " + message.getMessage());
        }
    }

    private void listGames() {
        try {
            var listOfGames = serverFacade.listGames(authToken);
            this.localGamesList.addAll(listOfGames);
            System.out.println("\nList of games: " + localGamesList.size());
            if (localGamesList.isEmpty()) {
                System.out.println("  No games found.");
            } else {
                for (int i = 0; i < localGamesList.size(); i++) {
                    GameData game = localGamesList.get(i);
                    System.out.printf("  %d. %s (White: %s, Black: %s)%n",
                            i + 1,
                            game.gameName(),
                            game.whiteUsername() != null ? game.whiteUsername() : "available",
                            game.blackUsername() != null ? game.blackUsername() : "available"
                    );
                }
            }
        } catch (ServerFacade.ServerFacadeException message) {
            System.out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
        } catch (Exception message) {
            System.out.println("An unexpected error occurred while logging out: " + message.getMessage());
        }
    }

    private void createGame(String[] tokens) {
        try {
            if (tokens.length < 2) {
                System.out.println("Error: Please provide a name for the game.");
                System.out.println("Usage: create <NAME>");
                return;
            }
            String gameName = tokens[1];
            GameData game = serverFacade.createGame(gameName, this.authToken);
            System.out.println("Game created: " + game.gameName() + " (ID: " + game.gameID() + ")");
        } catch (ServerFacade.ServerFacadeException e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private void printPostLoginHelp() {
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  help          - Show this message");
        System.out.println("  logout        - Log out and return to pre-login");
        System.out.println("  create <NAME> - Create a new game");
        System.out.println("  list          - List all available games");
        System.out.println("  play <COLOR> <ID> - Join a game as WHITE or BLACK");
        System.out.println("  observe <ID>  - Join a game as an observer");
        System.out.println();
    }

    private void printPreLoginHelp() {
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  help      - Show this message");
        System.out.println("  register  - Create your new account");
        System.out.println("  login     - Log in to your existing account");
        System.out.println("  quit      - Exit the app");
        System.out.println();
    }
}