package com.tiendajuegos.crypto;

import com.tiendajuegos.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cifrado AES/GCM de las claves digitales.
 *
 * <p>Formato actual: {@code base64(iv) + ":" + base64(ciphertext)}.</p>
 * <p>Formato heredado (datos cargados antes de este proyecto): un único bloque base64 con
 * {@code iv || ciphertext}, cifrado con el secreto JWT por defecto. Se acepta solo al leer
 * para que las claves ya vendidas y las disponibles sigan siendo entregables.</p>
 */
@Service
public class CryptoService {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final String SEPARATOR = ":";
    private static final String SECRETO_LEGACY =
            "change-this-development-secret-key-to-a-long-random-value-2026";

    private final SecretKeySpec key;
    private final SecretKeySpec legacyKey;
    private final SecureRandom random = new SecureRandom();

    /** Constructor directo para pruebas: usa el secreto heredado por defecto. */
    public CryptoService(String secret) {
        this(secret, SECRETO_LEGACY);
    }

    @Autowired
    public CryptoService(
            @Value("${app.crypto.secret:change-this-crypto-secret-with-a-long-random-value-2026}") String secret,
            @Value("${app.crypto.legacy-secret:" + SECRETO_LEGACY + "}") String legacySecret) {
        this.key = derivarClave(secret);
        this.legacyKey = derivarClave(legacySecret);
    }

    private static SecretKeySpec derivarClave(String secreto) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secreto.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
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
        if (stored.contains(SEPARATOR)) {
            return descifrar(stored.split(SEPARATOR, -1), key, false);
        }
        // Claves heredadas: primero se prueba el secreto actual y luego el antiguo.
        try {
            return descifrar(new String[]{stored}, key, true);
        } catch (ApiException conSecretoActual) {
            return descifrar(new String[]{stored}, legacyKey, true);
        }
    }

    private String descifrar(String[] partes, SecretKeySpec clave, boolean concatenado) {
        try {
            byte[] iv;
            byte[] ciphertext;
            if (concatenado) {
                byte[] todos = Base64.getDecoder().decode(partes[0].trim());
                if (todos.length <= IV_BYTES) {
                    throw ApiException.badRequest("Clave digital con formato inválido");
                }
                iv = Arrays.copyOfRange(todos, 0, IV_BYTES);
                ciphertext = Arrays.copyOfRange(todos, IV_BYTES, todos.length);
            } else {
                if (partes.length != 2) {
                    throw ApiException.badRequest("Clave digital con formato inválido");
                }
                iv = Base64.getDecoder().decode(partes[0]);
                ciphertext = Base64.getDecoder().decode(partes[1]);
                if (iv.length != IV_BYTES) {
                    throw ApiException.badRequest("Clave digital con formato inválido");
                }
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("Clave digital con formato inválido");
        } catch (GeneralSecurityException exception) {
            throw ApiException.badRequest("No se pudo descifrar la clave digital");
        }
    }
}
