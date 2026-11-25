package com.tiendajuegos.crypto;

import com.tiendajuegos.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Cifrado AES/GCM de las claves digitales: base64(iv) + ":" + base64(ciphertext). */
@Service
public class CryptoService {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final String SEPARATOR = ":";

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public CryptoService(@Value("${app.crypto.secret:change-this-crypto-secret-with-a-long-random-value-2026}") String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            this.key = new SecretKeySpec(digest, "AES");
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo inicializar el cifrado", exception);
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + SEPARATOR
                    + Base64.getEncoder().encodeToString(ciphertext);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo cifrar la clave", exception);
        }
    }

    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        try {
            String[] parts = stored.split(SEPARATOR, -1);
            if (parts.length != 2) {
                throw ApiException.badRequest("Clave digital con formato inválido");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[1]);
            if (iv.length != IV_BYTES) {
                throw ApiException.badRequest("Clave digital con formato inválido");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("Clave digital con formato inválido");
        } catch (GeneralSecurityException exception) {
            throw ApiException.badRequest("No se pudo descifrar la clave digital");
        }
    }
}
