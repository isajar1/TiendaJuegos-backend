# Esquema de MySQL

`schema.sql` crea el esquema inicial (11 tablas), restricciones, indices y datos de plataformas y categorias.

`schema_v2.sql` es el esquema completo utilizado por las 22 historias de usuario: usuarios, juegos, plataformas, categorias, carrito e items, claves digitales, cupones, ordenes y detalles, resenas, lista de deseos y auditoria. Usar este script para una instalacion nueva.

`seed_admin.sql` crea el usuario administrador de desarrollo (`admin@tiendajuegos.com` / `admin123`) con hash BCrypt. Es idempotente: si el correo ya existe, restablece nombre, contrasena y rol, asi que sirve tambien para recuperar el acceso. Cambiar la contrasena en cualquier entorno que no sea local. Ademas, un admin autenticado puede crear otros administradores desde el panel (`POST /api/admin/usuarios/administradores`).

La eliminacion manual de cuenta anonimiza el registro en lugar de borrarlo: `usuarios` pasa a tener las columnas `eliminado` (booleano, por defecto `false`) y `fecha_eliminacion` (nula hasta que se elimina la cuenta). Con `ddl-auto=update` Hibernate las agrega automaticamente a una base existente; en una instalacion nueva se crean solas.

Con Docker Compose, el script se ejecuta automaticamente al crear el volumen de MySQL por primera vez. Para reinicializar una base local de desarrollo, ejecuta desde `Infraestructura`:

```powershell
docker compose down -v
docker compose up --build
```

El primer comando elimina los datos locales del volumen MySQL. No se debe usar contra una base con datos que se necesiten conservar.
