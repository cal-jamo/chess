package client;
import ServerFacade.ServerFacade;

import java.util.Scanner;

public class Repl {
    private boolean isLoggedIn = false;
    private final ServerFacade serverFacade;

    public Repl(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to Chess. Type 'help' to get started.");
        Scanner scanner = new Scanner(System.in);
        while (true) {

            if (false /* I will use isLoggedIn later */) {
                System.out.print("[LOGGED_IN] >>> ");
            } else {
                System.out.print("[LOGGED_OUT] >>> ");
            }

            String line = scanner.nextLine();
            String[] tokens = line.split(" ");
            String command = tokens[0].toLowerCase();

            switch (command) {
                case "help":
                    System.out.println("You typed 'help'.");
                    break;
                case "quit":
                    System.out.println("Exiting Chess.");
                    return;
                case "login":
                    System.out.println("You typed 'login'.");
                    break;
                case "register":
                    System.out.println("You typed 'register'.");
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for options.");
                    break;
            }
        }
    }
}