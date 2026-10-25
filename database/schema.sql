-- MySQL 8.4+ is required.
CREATE DATABASE IF NOT EXISTS tienda_juegos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE tienda_juegos;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(160) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    rol ENUM('cliente', 'admin') NOT NULL DEFAULT 'cliente',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS plataformas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS juegos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(180) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(12,2) NOT NULL,
    region VARCHAR(80) NOT NULL DEFAULT 'Global',
    plataforma_id BIGINT UNSIGNED NOT NULL,
    categoria_id BIGINT UNSIGNED NOT NULL,
    estado ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo',
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_juegos_precio CHECK (precio >= 0),
    CONSTRAINT fk_juegos_plataforma FOREIGN KEY (plataforma_id) REFERENCES plataformas(id),
    CONSTRAINT fk_juegos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cupones (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    porcentaje_descuento DECIMAL(5,2) NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_cupones_descuento CHECK (porcentaje_descuento > 0 AND porcentaje_descuento <= 100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ordenes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNSIGNED NOT NULL,
    cupon_id BIGINT UNSIGNED NULL,
    total DECIMAL(12,2) NOT NULL,
    estado_pago ENUM('pendiente', 'pagado', 'rechazado', 'reembolsado') NOT NULL DEFAULT 'pendiente',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ordenes_total CHECK (total >= 0),
    CONSTRAINT fk_ordenes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_ordenes_cupon FOREIGN KEY (cupon_id) REFERENCES cupones(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS claves_digitales (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    juego_id BIGINT UNSIGNED NOT NULL,
    clave_cifrada TEXT NOT NULL,
    estado ENUM('disponible', 'vendida') NOT NULL DEFAULT 'disponible',
    orden_id BIGINT UNSIGNED NULL,
    CONSTRAINT fk_claves_juego FOREIGN KEY (juego_id) REFERENCES juegos(id),
    CONSTRAINT fk_claves_orden FOREIGN KEY (orden_id) REFERENCES ordenes(id),
    CONSTRAINT chk_claves_estado_orden CHECK (
        (estado = 'disponible' AND orden_id IS NULL) OR
        (estado = 'vendida' AND orden_id IS NOT NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalles_orden (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    orden_id BIGINT UNSIGNED NOT NULL,
    juego_id BIGINT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    clave_id BIGINT UNSIGNED NOT NULL UNIQUE,
    CONSTRAINT chk_detalles_orden_precio CHECK (precio_unitario >= 0),
    CONSTRAINT uq_detalle_orden_juego UNIQUE (orden_id, juego_id),
    CONSTRAINT fk_detalles_orden FOREIGN KEY (orden_id) REFERENCES ordenes(id),
    CONSTRAINT fk_detalles_juego FOREIGN KEY (juego_id) REFERENCES juegos(id),
    CONSTRAINT fk_detalles_clave FOREIGN KEY (clave_id) REFERENCES claves_digitales(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS resenas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    juego_id BIGINT UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NOT NULL,
    calificacion TINYINT UNSIGNED NOT NULL,
    comentario TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_resenas_calificacion CHECK (calificacion BETWEEN 1 AND 5),
    CONSTRAINT uq_resena_usuario_juego UNIQUE (usuario_id, juego_id),
    CONSTRAINT fk_resenas_juego FOREIGN KEY (juego_id) REFERENCES juegos(id),
    CONSTRAINT fk_resenas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS listas_deseos (
    usuario_id BIGINT UNSIGNED NOT NULL,
    juego_id BIGINT UNSIGNED NOT NULL,
    fecha_agregado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, juego_id),
    CONSTRAINT fk_lista_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_lista_juego FOREIGN KEY (juego_id) REFERENCES juegos(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS logs_auditoria (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNSIGNED NULL,
    accion VARCHAR(120) NOT NULL,
    entidad VARCHAR(80) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_juegos_plataforma ON juegos(plataforma_id);
CREATE INDEX idx_juegos_categoria ON juegos(categoria_id);
CREATE INDEX idx_ordenes_usuario ON ordenes(usuario_id);
CREATE INDEX idx_claves_juego_estado ON claves_digitales(juego_id, estado);
CREATE INDEX idx_logs_usuario_fecha ON logs_auditoria(usuario_id, fecha_hora);

INSERT IGNORE INTO plataformas (nombre) VALUES ('Steam'), ('Xbox'), ('PlayStation'), ('PC');
INSERT IGNORE INTO categorias (nombre) VALUES ('Accion'), ('RPG'), ('Deportes'), ('Aventura'), ('Estrategia'), ('Simulacion');
