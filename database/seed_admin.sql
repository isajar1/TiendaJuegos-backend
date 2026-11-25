-- Usuario administrador de desarrollo: admin@tiendajuegos.com / admin123
-- Hash BCrypt (coste 10) de "admin123", generado con BCryptPasswordEncoder.
-- Idempotente: si el correo ya existe, restablece nombre, contraseña y rol.
USE tienda_juegos;

INSERT INTO usuarios (nombre, correo, contrasena_hash, rol, fecha_registro)
VALUES ('Administrador', 'admin@tiendajuegos.com',
        '$2a$10$DEh4StA6duxPtqOFMnngLORCV62smyUlQGMhlavKtK6Gm1rhfbDei',
        'admin', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    contrasena_hash = VALUES(contrasena_hash),
    rol = 'admin';
