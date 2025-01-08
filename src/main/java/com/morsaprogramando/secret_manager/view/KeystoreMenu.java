package com.morsaprogramando.secret_manager.view;

import com.morsaprogramando.secret_manager.models.StoredPassword;
import com.morsaprogramando.secret_manager.services.FileManagerService;
import com.morsaprogramando.secret_manager.services.PasswordManagerService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class KeystoreMenu {

    private final PasswordManagerService passwordManagerService;
    private final List<StoredPassword> passwords;
    private final FileManagerService fileManagerService;
    private State currentState = State.CHOOSE;

    public void render() {

        while (true) {
            Utils.clearScreen();
            Utils.println("");

            if (currentState == State.EXIT) return;

            displayPasswords();

            switch (currentState) {
                case CHOOSE -> printChooseMenu();
                case CREATE_PASS -> printCreatePassMenu();
                case READ_PASS -> printReadPassMenu();
                case DEL_PASS -> printDeletePassMenu();
                case SAVE -> {
                }
            }
        }

    }

    private void printDeletePassMenu() {
        String selectedTitle;
        StoredPassword selectedPassword;

        outer:
        while (true) {
            try {
                selectedTitle = Utils.readLine("Title to be deleted: ");

                for (StoredPassword password: passwords) {
                    if (Objects.equals(password.title(), selectedTitle)) {
                        selectedPassword = password;
                        break outer;
                    }
                }

                Utils.println("Title not found, try again.");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Utils.println("");
            passwords.remove(selectedPassword);
            Utils.readLine("Password \"" + selectedPassword.title() + "\" was removed. Press Enter to continue...");

            this.currentState = State.CHOOSE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printReadPassMenu() {
        int selectedPassword;

        while (true) {
            try {
                selectedPassword = Utils.readInt("Enter the password ID: ");

                if (selectedPassword < 1 || selectedPassword > passwords.size()) {
                    Utils.println("ID not found, try again.");
                    continue;
                }
                break;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Utils.println("");
            Utils.println(getPasswords().get(selectedPassword - 1).password());
            Utils.readLine("Press enter to hide the password...");

            this.currentState = State.CHOOSE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayPasswords() {
        if (passwords.isEmpty()) {
            Utils.println("No passwords yet, create your first password.");
            printDelimiter('═');
            Utils.println("");
            return;
        }

        System.out.printf("%-20s %-20s %-20s %-20s %-10s%n",
                "Id",
                "Title",
                "Username",
                "Password",
                "Created At");

        printDelimiter('─');

        int id = 0;
        for (StoredPassword password : getPasswords()) {
            System.out.printf("%-20s %-20s %-20s %-20s %-10s%n",
                    ++id,
                    password.title(),
                    password.username(),
                    "******",
                    password.createdAtAsString());
        }

        printDelimiter('═');
        Utils.println("");
    }

    private void printCreatePassMenu() {
        try {

            String title = "";
            boolean alreadyExists = true;

            outer:
            do {
                if (!title.isBlank()) {
                    Utils.println("The title already exists, try another one.");
                }

                title = Utils.readLine("Title: ");

                for (StoredPassword password: passwords) {
                    if (Objects.equals(password.title(),title)) {
                        continue outer;
                    }
                }

                alreadyExists = false;

            } while (alreadyExists);

            String username = Utils.readLine("Username: ");
            String password = Utils.readLine("Password: ");

            StoredPassword storedPassword = new StoredPassword(title, username, password, Instant.now());
            this.passwords.add(storedPassword);

            this.currentState = State.CHOOSE;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printChooseMenu() {
        Utils.println("1. Add a new password");
        Utils.println("2. View existing password");
        Utils.println("3. Delete a password");
        Utils.println("4. Save changes");
        Utils.println("5. Exit");

        try {
            int option = Utils.readInt("Select an option: ");

            if (option == State.READ_PASS.option && passwords.isEmpty()) {
                Utils.println("\nNo password available to read!");
                Utils.readLine("Press (Enter) to continue...");
                this.currentState = State.CHOOSE;
                return;
            }

            this.currentState = State.fromNumber(option);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            Utils.println("Not a valid option! Exiting...");
            System.exit(1);
        }
    }

    public static void printDelimiter(char repeatChar) {
        int length = 100;
        String delimiter = String.valueOf(repeatChar).repeat(length);
        Utils.println(delimiter);
    }

    private List<StoredPassword> getPasswords() {
        return passwords.stream().sorted().toList();
    }

    private enum State {
        CHOOSE(0), CREATE_PASS(1), READ_PASS(2), DEL_PASS(3), SAVE(4), EXIT(5);

        final int option;

        State(int option) {
            this.option = option;
        }

        static State fromNumber(int option) {
            return switch (option) {
                case 0 -> CHOOSE;
                case 1 -> CREATE_PASS;
                case 2 -> READ_PASS;
                case 3 -> DEL_PASS;
                case 4 -> SAVE;
                case 5 -> EXIT;
                default -> throw new IllegalArgumentException("Invalid option: " + option);
            };
        }
    }
}
