-- DDL del carrito (documentación). Hibernate ddl-auto=update lo crea automáticamente.
USE tienda_juegos;

CREATE TABLE IF NOT EXISTS carrito (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNSIGNED NOT NULL UNIQUE,
    cupon_id BIGINT UNSIGNED NULL,
    CONSTRAINT fk_carrito_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_carrito_cupon FOREIGN KEY (cupon_id) REFERENCES cupones(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS carrito_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    carrito_id BIGINT UNSIGNED NOT NULL,
    juego_id BIGINT UNSIGNED NOT NULL,
    CONSTRAINT uq_carrito_item UNIQUE (carrito_id, juego_id),
    CONSTRAINT fk_carrito_items_carrito FOREIGN KEY (carrito_id) REFERENCES carrito(id) ON DELETE CASCADE,
    CONSTRAINT fk_carrito_items_juego FOREIGN KEY (juego_id) REFERENCES juegos(id) ON DELETE CASCADE
) ENGINE=InnoDB;
