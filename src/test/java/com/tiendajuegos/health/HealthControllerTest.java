package com.tiendajuegos.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void returnsUpStatus() {
        HealthController controller = new HealthController();

        assertEquals("UP", controller.health().getBody().get("status"));
    }
}
