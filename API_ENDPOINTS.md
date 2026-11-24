# API REST – Tienda de Juegos

Todas las rutas responden JSON. Las rutas protegidas exigen `Authorization: Bearer <token JWT>`.
Los roles son `cliente` y `admin` (`ROLE_CLIENTE` / `ROLE_ADMIN` en el token).

## Autenticación y perfil

| Método | Ruta | HU | Acceso | Descripción |
|---|---|---|---|---|
| POST | `/api/auth/register` | HU-01 | público | Crea cuenta con rol `cliente`. 201 + token. |
| POST | `/api/auth/login` | HU-02 | público | Devuelve datos del usuario + JWT. |
| GET | `/api/usuarios/me` | HU-03 | autenticado | Datos del perfil actual. |
| PUT | `/api/usuarios/me` | HU-03 | autenticado | Edita nombre y/o contraseña (requiere `contrasenaActual`). |

## Catálogo

| Método | Ruta | HU | Acceso | Descripción |
|---|---|---|---|---|
| GET | `/api/juegos` | HU-04, 05, 06 | público | Lista juegos activos. Filtros: `titulo`, `plataformaId`, `categoriaId`, `region`, `minPrecio`, `maxPrecio`, `estado`. |
| GET | `/api/juegos/{id}` | HU-07 | público | Detalle con plataforma, categoría, promedio de reseñas y disponibilidad. |
| POST | `/api/juegos` | HU-08 | admin | Crea juego. |
| PUT | `/api/juegos/{id}` | HU-08 | admin | Edita juego. |
| DELETE | `/api/juegos/{id}` | HU-08 | admin | Deshabilita juego (borrado lógico). |
| GET | `/api/categorias` | HU-09 | público | Lista categorías. |
| POST | `/api/categorias` | HU-09 | admin | Crea categoría (`{"nombre":"..."}`). |
| GET | `/api/plataformas` | HU-09 | público | Lista plataformas. |
| POST | `/api/plataformas` | HU-09 | admin | Crea plataforma (`{"nombre":"..."}`). |

## Inventario y promociones

| Método | Ruta | HU | Acceso | Descripción |
|---|---|---|---|---|
| POST | `/api/juegos/{juegoId}/claves` | HU-10 | admin | Carga claves (`{"claves":["ABC","DEF"]}`). Se guardan cifradas; responde `{ "clavesCargadas": n }`. |
| GET | `/api/cupones` | HU-11 | admin | Lista cupones. |
| POST | `/api/cupones` | HU-11 | admin | Crea cupón: `codigo`, `porcentajeDescuento`, `fechaExpiracion`, `activo`. |
| DELETE | `/api/cupones/{id}` | HU-11 | admin | Desactiva cupón. |

## Compra

| Método | Ruta | HU | Acceso | Descripción |
|---|---|---|---|---|
| POST | `/api/carrito` | HU-12, 13 | autenticado | Calcula items, subtotal, descuento y total. Body: `{"items":[{"juegoId":1}],"codigoCupon":"..."}`. |
| POST | `/api/ordenes` | HU-14 | cliente | Confirma la compra. Body: `{"items":[{"juegoId":1}],"codigoCupon":"...","aprobarPago":true}`. |
| GET | `/api/ordenes/{id}` | HU-15 | dueño o admin | Detalle de la orden; incluye la clave si el pago fue aprobado. |
| GET | `/api/deseos` | HU-16 | autenticado | Lista de deseos. |
| POST | `/api/deseos` | HU-16 | autenticado | Agrega juego (`{"juegoId":1}`). |
| DELETE | `/api/deseos/{juegoId}` | HU-16 | autenticado | Quita juego de la lista. |

## Reseñas, historial y reportes

| Método | Ruta | HU | Acceso | Descripción |
|---|---|---|---|---|
| GET | `/api/juegos/{juegoId}/resenas` | HU-17 | público | Reseñas del juego. |
| POST | `/api/juegos/{juegoId}/resenas` | HU-17 | cliente que compró | `{"calificacion":5,"comentario":"..."}`. |
| GET | `/api/ordenes/me` | HU-18 | cliente | Historial de compras del cliente. |
| GET | `/api/ordenes` | HU-19 | admin | Todas las órdenes. |
| GET | `/api/reportes/ventas-por-juego?desde=2026-01-01&hasta=2026-12-31` | HU-20, 22 | admin | Ventas agrupadas por juego. |
| GET | `/api/reportes/compras-por-cliente?desde=...&hasta=...` | HU-21, 22 | admin | Compras agrupadas por cliente. |
| GET | `/api/auditoria` | trazabilidad | admin | Registro de acciones sensibles. |
| GET | `/api/health` | — | público | `{"status":"UP"}`. |

## Respuestas de error

Los errores de dominio devuelven `{"message":"..."}` con el estado HTTP adecuado:
`400` datos inválidos, `401` no autenticado, `403` sin permisos, `404` no encontrado, `409` duplicado o sin stock.
