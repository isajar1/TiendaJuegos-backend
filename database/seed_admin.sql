-- Usuario administrador de prueba: admin@tiendajuegos.com / admin123
-- Hash BCrypt generado con org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder (coste 10).
USE tienda_juegos;

INSERT INTO usuarios (nombre, correo, contrasena_hash, rol)
VALUES ('Administrador', 'admin@tiendajuegos.com',
        '$2a$10$vXW7qE8E5J6L5yzikokZGe1S/hzH59vb3AqNdb7GUhe6JoMuFDcVK', 'admin')
ON DUPLICATE KEY UPDATE rol = 'admin';
