package com.morsaprogramando.secret_manager.controller;

import com.morsaprogramando.secret_manager.models.KeystoreData;
import com.morsaprogramando.secret_manager.models.StoredPassword;
import com.morsaprogramando.secret_manager.services.EncryptionService;
import com.morsaprogramando.secret_manager.services.FileManagerService;
import com.morsaprogramando.secret_manager.services.PasswordManagerService;
import com.morsaprogramando.secret_manager.view.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public enum Controller {
    INSTANCE;

    public void execute() {
        KeystoreData data = initKeyStore();

        if (data == null) return;

        ServicesAndPasswords servicesAndPasswords = initServicesAndPasswords(data);

        KeystoreMenu keystoreMenu = new KeystoreMenu(
                servicesAndPasswords.passwordManagerService(),
                servicesAndPasswords.passwords(),
                servicesAndPasswords.fileManagerService());

        keystoreMenu.render();
    }

    private PasswordManagerService initPasswordService(String masterPassword) {
        EncryptionService encryptionService = EncryptionService.create(masterPassword);
        return new PasswordManagerService(encryptionService);
    }

    private KeystoreData initKeyStore() {
        InitialMenu.Action action = InitialMenu.INSTANCE.getAction();

        return switch (action) {
            case InitialMenu.Open __ -> OpenKeystoreMenu.INSTANCE.getData();
            case InitialMenu.Create __ -> CreateKeystoreMenu.INSTANCE.createData();
            case InitialMenu.Quit __ -> null;
        };
    }

    private ServicesAndPasswords initServicesAndPasswords(KeystoreData data) {
        PasswordManagerService passwordService = initPasswordService(data.masterPassword());
        FileManagerService fileManagerService = new FileManagerService(data.keyStoreName());

        List<StoredPassword> passwords = data.isNew() ?
                new ArrayList<>() :
                getPasswordsFromKeystore(passwordService, fileManagerService);

        return new ServicesAndPasswords(passwordService, fileManagerService, passwords);
    }

    private List<StoredPassword> getPasswordsFromKeystore(PasswordManagerService passwordService,
                                                                 FileManagerService fileManagerService) {
        try {
            return passwordService.decodePasswords(fileManagerService.readFile());

        } catch (FileNotFoundException e) {
            Utils.println("The keystore was not found. Try again writing the correct keystore name (without extension).");
            System.exit(1);
        } catch (Exception e) {
            Utils.println("The password is incorrect or the file is corrupted.");
            System.exit(1);
        }

        return null;
    }

    private record ServicesAndPasswords(
            PasswordManagerService passwordManagerService,
            FileManagerService fileManagerService,
            List<StoredPassword> passwords
    ) {}
}
