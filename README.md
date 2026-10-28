# TiendaJuegos-backend

REST API para el proyecto final de Ingeniería de Software.

## Autenticacion

Las rutas publicas de autenticacion son:

- `POST /api/auth/register`: recibe `nombre`, `correo` y `contrasena`. Crea usuarios con el rol `cliente` y almacena la contrasena usando BCrypt.
- `POST /api/auth/login`: recibe `correo` y `contrasena`. Devuelve los datos basicos del usuario y un token JWT.

Las rutas restantes requieren el encabezado `Authorization: Bearer <token>`.

## Variables de entorno

Usa `.env.example` como guia. Para ejecutar la aplicacion se requieren `DB_URL`, `DB_USER`, `DB_PASSWORD` y un `JWT_SECRET` de al menos 32 caracteres.

## Ejecutar el backend

El repositorio incluye Maven Wrapper para que cada integrante use la misma version de Maven sin instalarla globalmente. En PowerShell, desde esta carpeta, ejecuta:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

En el primer uso se descargan Maven y las dependencias del proyecto; despues se reutilizan localmente. Se necesita Java 21 y una conexion a internet para la descarga inicial.
