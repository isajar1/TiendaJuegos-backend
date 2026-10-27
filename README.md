# TiendaJuegos-backend

REST API para el proyecto final de Ingeniería de Software.

## Autenticacion

Las rutas publicas de autenticacion son:

- `POST /api/auth/register`: recibe `nombre`, `correo` y `contrasena`. Crea usuarios con el rol `cliente` y almacena la contrasena usando BCrypt.
- `POST /api/auth/login`: recibe `correo` y `contrasena`. Devuelve los datos basicos del usuario y un token JWT.

Las rutas restantes requieren el encabezado `Authorization: Bearer <token>`.

## Variables de entorno

Usa `.env.example` como guia. Para ejecutar la aplicacion se requieren `DB_URL`, `DB_USER`, `DB_PASSWORD` y un `JWT_SECRET` de al menos 32 caracteres.
