package client;
import ServerFacade.ServerFacade;
import model.AuthData;

import java.rmi.ServerException;
import java.util.Scanner;

public class Repl {
    private boolean isLoggedIn = false;
    private final ServerFacade serverFacade;
    private String authToken = null;

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
                // handlePostloginCommands(command, tokens, scanner);
                if (command.equals("logout")) {
                    isLoggedIn = false;
                    this.authToken = null;
                    System.out.println("Logged out successfully.");
                } else {
                    System.out.println("Post-login commands not implemented yet.");
                }
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
            case "logout":
                isLoggedIn = false;
                this.authToken = null;
                //logout(scanner);
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

    private void printPostLoginHelp() {
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  help        - Show this message");
        System.out.println("  createGame  - Create a new game");
        System.out.println("  joinGame    - Join an existing game");
        System.out.println("  listGames   - List all available games");
        System.out.println("  quit        - Exit the app");
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