package com.tiendajuegos.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class KeyCipherServiceTest {

    @Test
    void cifrarYDescifrarConservaLaClave() {
        KeyCipherService cipher = new KeyCipherService("clave-secreta-de-pruebas-larga-2026");
        String original = "ABCDE-12345-XYZ";

        String cifrado = cipher.cifrar(original);

        assertNotEquals(original, cifrado);
        assertEquals(original, cipher.descifrar(cifrado));
    }
}
