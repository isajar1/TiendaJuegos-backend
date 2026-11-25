# Esquema de MySQL

`schema.sql` crea el esquema inicial (11 tablas), restricciones, indices y datos de plataformas y categorias.

`schema_v2.sql` es el esquema completo utilizado por las 22 historias de usuario: usuarios, juegos, plataformas, categorias, carrito e items, claves digitales, cupones, ordenes y detalles, resenas, lista de deseos y auditoria. Usar este script para una instalacion nueva.

`seed_admin.sql` crea el usuario administrador de desarrollo (`admin` / `admin123`) con hash BCrypt. Cambiar la contrasena en cualquier entorno que no sea local.

Con Docker Compose, el script se ejecuta automaticamente al crear el volumen de MySQL por primera vez. Para reinicializar una base local de desarrollo, ejecuta desde `Infraestructura`:

```powershell
docker compose down -v
docker compose up --build
```

El primer comando elimina los datos locales del volumen MySQL. No se debe usar contra una base con datos que se necesiten conservar.
