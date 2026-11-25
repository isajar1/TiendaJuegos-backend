package com.tiendajuegos.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tiendajuegos.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService("test-secret-muy-largo-para-derivar-clave-aes-256-2026");
    }

    @Test
    void encryptThenDecryptReturnsOriginalKey() {
        String original = "ABCD-EFGH-IJKL-1234";

        String cifrado = cryptoService.encrypt(original);

        assertEquals(original, cryptoService.decrypt(cifrado));
    }

    @Test
    void encryptUsesRandomTwelveByteIv() {
        String primero = cryptoService.encrypt("CLAVE-UNICA");
        String segundo = cryptoService.encrypt("CLAVE-UNICA");

        assertNotEquals(primero, segundo);
        assertTrue(primero.contains(":"));
        String[] partes = primero.split(":", -1);
        assertEquals(2, partes.length);
        assertEquals(12, Base64.getDecoder().decode(partes[0]).length);
        assertFalse(Base64.getDecoder().decode(partes[1]).length == 0);
    }

    @Test
    void decryptRejectsMalformedValue() {
        ApiException exception = assertThrows(ApiException.class,
                () -> cryptoService.decrypt("no-es-base64:abc"));

        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void decryptRejectsTamperedCiphertext() {
        String cifrado = cryptoService.encrypt("CLAVE");
        String[] partes = cifrado.split(":", -1);
        byte[] texto = Base64.getDecoder().decode(partes[1]);
        texto[0] ^= 0x01;
        String manipulado = partes[0] + ":" + Base64.getEncoder().encodeToString(texto);

        assertThrows(ApiException.class, () -> cryptoService.decrypt(manipulado));
    }

    @Test
    void nullValuesArePreserved() {
        assertNull(cryptoService.encrypt(null));
        assertNull(cryptoService.decrypt(null));
    }

    @Test
    void unicodeKeysAreSupported() {
        String original = "CLAVE-Ñ-€-" + new String("áéíóú".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        assertEquals(original, cryptoService.decrypt(cryptoService.encrypt(original)));
    }
}
