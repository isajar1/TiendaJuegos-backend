# Contrato de la API - Tienda Juegos

Base: `http://localhost:8080`. Todas las respuestas de error usan `{"message":"..."}`.
Los endpoints marcados como **públicos** no requieren token; el resto exige
`Authorization: Bearer <JWT>`. Los endpoints **admin** además exigen el rol `admin`.

## Modelos de respuesta

```ts
Juego = {
  id: number; titulo: string; descripcion: string; precio: number;
  region: string; plataforma: string; categoria: string; activo: boolean;
  clavesDisponibles?: number; calificacionPromedio?: number; totalResenas?: number
}
Categoria = { id: number; nombre: string }
Plataforma = { id: number; nombre: string }
AdminCreado = { id: number; nombre: string; correo: string; rol: string }
Cupon = { id: number; codigo: string; porcentajeDescuento: number; fechaExpiracion: string; activo: boolean }

CarritoItem = { juegoId: number; titulo: string; precio: number }
Carrito = { items: CarritoItem[]; subtotal: number; descuento: number; total: number; cupon: string | null }

OrdenItem = { juegoId: number; titulo: string; precioUnitario: number; clave: string }
Orden = { id: number; fechaCreacion: string; total: number; estadoPago: string; cupon: string | null; items: OrdenItem[] }

Resena = { id: number; juegoId: number; usuarioId: number; nombreUsuario: string; calificacion: number; comentario: string; fecha: string }
Deseo = { juegoId: number; titulo: string; precio: number }

ReporteVenta = { juegoId: number; titulo: string; unidades: number; ingresos: number }
ReporteCliente = { usuarioId: number; nombre: string; ordenes: number; total: number }
```

## Autenticación (público, ya implementado)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/api/auth/register` | `{nombre, correo, contrasena}` | 201 `AuthResponse` |
| POST | `/api/auth/login` | `{correo, contrasena}` | 200 `AuthResponse` |
| GET | `/api/health` | — | 200 `{"status":"UP"}` |

`AuthResponse = {id, nombre, correo, rol, token}`. Errores: 400 validación, 401 credenciales, 409 correo duplicado.

El registro público **siempre** crea clientes. El administrador de desarrollo se crea con `database/seed_admin.sql` (`admin@tiendajuegos.com` / `admin123`); el script es idempotente y restablece nombre, contraseña y rol.

## Altas de administradores (admin)

| Método | Ruta | Acceso | Body | Respuesta |
|---|---|---|---|---|
| POST | `/api/admin/usuarios/administradores` | admin | `{nombre, correo, contrasena}` | 201 `AdminCreado` (sin token) |

Solo una cuenta con rol `admin` autenticada puede crear otro administrador: el controlador exige `@PreAuthorize("hasRole('ADMIN')")` y el servicio comprueba de nuevo el rol y que la cuenta no esté eliminada. Un cliente recibe **403**, un correo ya registrado **409** y datos inválidos **400**. La respuesta no devuelve el JWT del nuevo usuario: este debe iniciar sesión con su contraseña. Quién crea al admin recibe el panel con la pestaña «Usuarios».

## Cuenta del usuario autenticado

| Método | Ruta | Acceso | Respuesta |
|---|---|---|---|
| DELETE | `/api/usuarios/mi-cuenta` | usuario autenticado | 204 (sin cuerpo) |

Eliminación manual de la cuenta: se anonimiza el registro (`Usuario eliminado`, correo `eliminado-{id}@invalid.local`, contraseña aleatoria, `eliminado=true` y `fecha_eliminacion`), se borran carrito y deseos y se registra la acción en auditoría. El inicio de sesión con la cuenta eliminada devuelve 401. Órdenes, reseñas y claves entregadas se conservan asociadas al mismo registro para no afectar los reportes. La operación es idempotente: repetirla sobre una cuenta ya eliminada devuelve 204 sin cambios.

## Catálogo, categorías y plataformas

| Método | Ruta | Acceso | Body / query | Respuesta |
|---|---|---|---|---|
| GET | `/api/juegos` | público | `q`, `categoria`, `plataforma`, `region` (opcionales; `categoria` y `plataforma` aceptan **nombre o ID**, con comparación de nombre insensible a mayúsculas) | 200 `Juego[]` (solo activos) |
| GET | `/api/juegos/{id}` | público | — | 200 `Juego` / 404 |
| POST | `/api/juegos` | admin | `{titulo, descripcion, precio, region, plataformaId, categoriaId}` | 201 `Juego` |
| PUT | `/api/juegos/{id}` | admin | mismo body | 200 `Juego` / 404 |
| DELETE | `/api/juegos/{id}` | admin | — | 204 (desactivación lógica) |
| POST | `/api/juegos/{id}/claves` | admin | `{claves: string[]}` | 201 `{creadas: number}` |
| GET | `/api/juegos/{id}/claves` | admin | — | 200 `{disponibles, vendidas}` |
| GET | `/api/categorias` | público | — | 200 `Categoria[]` |
| POST | `/api/categorias` | admin | `{nombre}` | 201 `Categoria` / 409 duplicado |
| GET | `/api/plataformas` | público | — | 200 `Plataforma[]` |
| POST | `/api/plataformas` | admin | `{nombre}` | 201 `Plataforma` / 409 duplicado |

Las claves se guardan cifradas con AES/GCM; nunca se devuelven en el catálogo. `GET /api/juegos/{id}/claves` está cubierto por el matcher público de `GET /api/juegos/**`, pero `@PreAuthorize("hasRole('ADMIN')")` sigue exigiendo rol admin en ejecución.

Las claves cargadas antes de este proyecto están en el **formato heredado** (un único bloque base64 con `iv || ciphertext`, cifrado con el secreto JWT de desarrollo). `CryptoService.decrypt` acepta ese formato y el actual para que las claves disponibles y las ya vendidas se sigan entregando; `encrypt` siempre escribe el formato nuevo `base64(iv):base64(ciphertext)` con `CRYPTO_SECRET`. El secreto antiguo se puede indicar con `CRYPTO_LEGACY_SECRET` (por defecto, el valor de desarrollo de `JWT_SECRET`). Si una clave no se puede descifrar, la API responde 400 en lugar de entregar un texto incorrecto.

## Cupones y carrito (autenticado)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/cupones` (admin) | — | 200 `Cupon[]` |
| POST | `/api/cupones` (admin) | `{codigo, porcentajeDescuento, fechaExpiracion, activo?}` | 201 `Cupon` |
| PUT | `/api/cupones/{id}` (admin) | `{activo: boolean}` | 200 `Cupon` |
| GET | `/api/carrito` | — | 200 `Carrito` |
| POST | `/api/carrito/items` | `{juegoId}` | 200 `Carrito` |
| DELETE | `/api/carrito/items/{juegoId}` | — | 200 `Carrito` |
| DELETE | `/api/carrito` | — | 204 |
| POST | `/api/carrito/cupon` | `{codigo}` | 200 `Carrito` |

Reglas: no se puede agregar un juego sin claves disponibles; el cupón debe existir,
estar activo, no vencer y aplicarse a un carrito no vacío (si no, **400**).
`descuento = subtotal * porcentaje / 100`; `total = subtotal - descuento`.

## Pago, historial y órdenes (autenticado)

| Método | Ruta | Acceso | Body | Respuesta |
|---|---|---|---|---|
| POST | `/api/checkout/pagar` | cliente | `{aprobar?: boolean}` (por defecto `true`) | 200 `Orden` pagada con claves descifradas; si `aprobar=false`, 200 `Orden` rechazada y el carrito se conserva |
| GET | `/api/ordenes/mias` | cliente | — | 200 `Orden[]` |
| GET | `/api/ordenes/mias/{id}` | cliente | — | 200 `Orden` / 404 |
| GET | `/api/ordenes` | admin | — | 200 `Orden[]` |
| GET | `/api/ordenes/{id}` | admin | — | 200 `Orden` / 404 |
| PUT | `/api/ordenes/{id}/estado` | admin | `{estadoPago}` | 200 `Orden` |

El pago simulado bloquea una clave disponible por juego, la asocia a la orden, marca la clave como `vendida` y entrega la clave descifrada en la respuesta de la orden (también en los listados de histórico y administración; nunca se devuelve el ciphertext). Con `aprobar=false` se devuelve la orden rechazada con los items del carrito y `clave: ""`; no se crean detalles ni se consumen claves o se vacía el carrito.

## Deseos y reseñas (autenticado)

| Método | Ruta | Acceso | Body | Respuesta |
|---|---|---|---|---|
| GET | `/api/deseos` | cliente | — | 200 `Deseo[]` |
| POST | `/api/deseos` | cliente | `{juegoId}` | 200 `Deseo[]` (201 si se creó) |
| DELETE | `/api/deseos/{juegoId}` | cliente | — | 200 `Deseo[]` |
| GET | `/api/juegos/{id}/resenas` | público | — | 200 `Resena[]` |
| POST | `/api/juegos/{id}/resenas` | cliente | `{calificacion, comentario}` | 201 `Resena` / 409 si no compró o ya reseñó |
| DELETE | `/api/resenas/{id}` | cliente/admin | — | 204 |

## Reportes (admin)

| Método | Ruta | Query | Respuesta |
|---|---|---|---|
| GET | `/api/reportes/ventas-juegos` | `desde`, `hasta` (ISO `yyyy-MM-dd`, opcionales) | 200 `ReporteVenta[]` |
| GET | `/api/reportes/compras-clientes` | `desde`, `hasta` | 200 `ReporteCliente[]` |

Si `desde > hasta` → 400. Sin datos → 200 `[]`. Solo cuentan órdenes `pagado`. Si se omite alguna fecha se usa un rango amplio (`1900-01-01` a `2100-12-31`).

## Auditoría

Se registra en `logs_auditoria` cada alta/edición de juego, carga de claves, cupón,
modificación del carrito, pago, entrega de clave, wishlist, reseña y cambio de estado de orden.
