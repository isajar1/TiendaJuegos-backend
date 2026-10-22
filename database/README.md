# Esquema de MySQL

`schema.sql` crea las tablas, restricciones, indices y datos iniciales de plataformas y categorias para la tienda.

Con Docker Compose, el script se ejecuta automaticamente al crear el volumen de MySQL por primera vez. Para reinicializar una base local de desarrollo, ejecuta desde `Infraestructura`:

```powershell
docker compose down -v
docker compose up --build
```

El primer comando elimina los datos locales del volumen MySQL. No se debe usar contra una base con datos que se necesiten conservar.
