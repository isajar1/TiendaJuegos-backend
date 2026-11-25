# TiendaJuegos-backend

API REST para el proyecto final de Ingeniería de Software. Cubre las historias HU-01 a HU-22: autenticación, catálogo, carrito, cupones, pago simulado, entrega de claves, deseos, reseñas, historial, administración, reportes y auditoría.

## Contrato de la API

La fuente de verdad es [`API_ENDPOINTS.md`](./API_ENDPOINTS.md). Incluye rutas, verbos, cuerpos, respuestas y reglas de negocio compartidas con el frontend.

### Rutas públicas

- `GET /api/health`
- `POST /api/auth/register` y `POST /api/auth/login`
- `GET /api/juegos`, `GET /api/juegos/{id}`, `GET /api/juegos/{id}/resenas`
- `GET /api/categorias` y `GET /api/plataformas`

El resto de rutas requiere `Authorization: Bearer <token>`; las de administración además exigen el rol `ADMIN`. Las respuestas de error usan `{"message":"..."}` con códigos 400/401/403/404/409/500.

## Seguridad y cifrado

- Contraseñas con BCrypt y sesiones JWT stateless (`app.jwt`).
- Claves digitales cifradas con AES/GCM: IV aleatorio de 12 bytes y formato `base64(iv):base64(ciphertext)` (`app.crypto.secret` → SHA-256). La API solo devuelve claves descifradas al comprador o al administrador.
- Auditoría de altas, cambios, pago, entrega y reportes en `log_auditoria`.

## Variables de entorno

| Variable | Uso |
| --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a MySQL (obligatorias). |
| `JWT_SECRET` | Firma de tokens; usar 32+ caracteres en producción. |
| `CRYPTO_SECRET` | Secreto para derivar la clave AES-256; **configurar en producción**. |
| `JWT_EXPIRATION_MS`, `SERVER_PORT` | Opcionales, con valores por defecto. |

## Base de datos

- `database/schema.sql`: esquema inicial (11 tablas).
- `database/schema_v2.sql`: esquema completo usado por las 22 HU (12 entidades, incluyendo carrito, claves, órdenes, reseñas, deseos y auditoría).
- `database/seed_admin.sql`: usuario administrador de desarrollo (`admin` / `admin123`, hash BCrypt).

## Ejecutar y verificar

Se necesita Java 21, MySQL accesible y Maven Wrapper incluido. Desde esta carpeta:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

### Estado verificado

- `mvn -q clean verify`: **BUILD SUCCESS**.
- **156 tests** (0 fallos, 0 errores, 0 omitidos).
- JaCoCo: **84,30 % de líneas** (784/930), **73,21 % de ramas** (123/168).

Las pruebas E2E requieren el backend y MySQL en ejecución; no se ejecutaron en el entorno donde se generó este estado.
