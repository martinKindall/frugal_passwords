package com.morsaprogramando.secret_manager.services;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public class EncryptionService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final static int GCM_IV_LENGTH = 12;
    private final SecretKey secretKey;
    private final byte[] associatedData = "ProtocolVersion1".getBytes(StandardCharsets.UTF_8);

    private final String CIPHER_CONFIG = "AES/GCM/NoPadding";
    private final String HASH_CONFIG = "SHA-256";

    private EncryptionService(String masterPassword) {
        secretKey = new SecretKeySpec(generate32BytesKeyFromPassword(masterPassword), "AES");
    }

    public static EncryptionService create(String masterPassword) {
        return new EncryptionService(masterPassword);
    }

    public byte[] encryptBytes(byte[] textInBytes) throws Exception {

        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(CIPHER_CONFIG);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }

        byte[] cipherText = cipher.doFinal(textInBytes);

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        return byteBuffer.array();
    }

    public byte[] decryptBytes(byte[] cipherMessage) throws Exception {
        final Cipher cipher = Cipher.getInstance(CIPHER_CONFIG);

        AlgorithmParameterSpec gcmIv = new GCMParameterSpec(128, cipherMessage, 0, GCM_IV_LENGTH);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmIv);

        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }
        return cipher.doFinal(cipherMessage, GCM_IV_LENGTH, cipherMessage.length - GCM_IV_LENGTH);
    }

    private byte[] generate32BytesKeyFromPassword(String password) {

        try {
            return MessageDigest.getInstance(HASH_CONFIG).digest(password.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
