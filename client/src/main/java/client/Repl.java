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

    private void handlePreLoginCommands(String command, String[] tokens, Scanner scanner) {
        switch (command) {
            case "help":
                printPreLoginHelp();
                break;
            case "quit":
                break;
            case "login":
                // login(scanner);
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
            // printPostLoginHelp(); we will implement this later
        } catch (ServerFacade.ServerFacadeException message) {
            System.out.println("ServerFacade.ServerFacadeException: " + message.getMessage());
        } catch (Exception message) {
            System.out.println("An unexpected error occurred while registering: " + message.getMessage());
        }

    }

    private void login(Scanner scanner) {

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