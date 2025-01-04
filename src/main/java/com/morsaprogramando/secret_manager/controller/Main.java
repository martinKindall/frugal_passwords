package com.morsaprogramando.secret_manager.controller;

import com.morsaprogramando.secret_manager.models.KeystoreData;
import com.morsaprogramando.secret_manager.models.StoredPassword;
import com.morsaprogramando.secret_manager.services.EncryptionService;
import com.morsaprogramando.secret_manager.services.FileManagerService;
import com.morsaprogramando.secret_manager.services.PasswordManagerService;
import com.morsaprogramando.secret_manager.view.InitialMenu;
import com.morsaprogramando.secret_manager.view.KeystoreMenu;
import com.morsaprogramando.secret_manager.view.NewKeystoreMenu;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        InitialMenu.Action action = InitialMenu.INSTANCE.getAction();

        KeystoreData data = switch (action) {
            case InitialMenu.Open __ -> openKeyStore();
            case InitialMenu.Create __ -> createNewKeyStore();
            case InitialMenu.Quit __ -> null;
        };

        if (data == null) System.exit(0);

        PasswordManagerService service = initPasswordService(data.masterPassword());
        FileManagerService fileManagerService = new FileManagerService(data.title());

        List<StoredPassword> passwords = new ArrayList<>();

        if (!data.isNew()) {
            passwords.addAll(service.decodePasswords(
                    fileManagerService.readFile()));
        }

        KeystoreMenu keystoreMenu = new KeystoreMenu(service, passwords, fileManagerService);
    }

    private static KeystoreData createNewKeyStore() {
        return NewKeystoreMenu.INSTANCE.createData();
    }

    private static KeystoreData openKeyStore() {
        return null;
    }


    private static PasswordManagerService initPasswordService(String masterPassword) {
        EncryptionService encryptionService = EncryptionService.create(masterPassword);
        return new PasswordManagerService(encryptionService);
    }

    private static void test() throws Exception {
        String masterPassword = "unpasswordmuyseguro123!";

        // Dependency injection
        EncryptionService encryptionService = EncryptionService.create(masterPassword);
        PasswordManagerService manager = new PasswordManagerService(encryptionService);
        // --------------------

        List<StoredPassword> passwords = List.of(
                new StoredPassword("mail", "user1", "password1"),
                new StoredPassword("bank", "user2", "password2")
        );
        byte[] encodedData = manager.encodePasswords(passwords);

        List<StoredPassword> decodedPasswords = manager.decodePasswords(encodedData);
        decodedPasswords.forEach(pw ->
                System.out.println("Title: " + pw.title() + ", Username: " + pw.username() + ", Password: " + pw.password())
        );
    }
}
