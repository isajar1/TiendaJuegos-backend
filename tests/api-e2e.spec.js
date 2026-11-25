const { test, expect } = require('@playwright/test');

const correoUnico = `gamer_${Date.now()}@test.com`;
const passwordValida = 'ClaveSegura123';
let tokenCliente = '';

test.describe('Pruebas E2E API Tienda de Juegos (Gherkin)', () => {

  test('E2E-01 [HU-01] Registro exitoso de nuevo cliente', async ({ request }) => {
    // Given un usuario nuevo con datos válidos
    // When envía POST a /api/auth/register
    const res = await request.post('/api/auth/register', {
      data: { nombre: 'Gamer Prueba', correo: correoUnico, contrasena: passwordValida }
    });
    // Then el sistema responde 201 y devuelve un token JWT
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.token).toBeTruthy();
  });

  test('E2E-02 [HU-02] Login exitoso de cliente registrado', async ({ request }) => {
    // Given un cliente ya registrado
    // When envía sus credenciales a /api/auth/login
    const res = await request.post('/api/auth/login', {
      data: { correo: correoUnico, contrasena: passwordValida }
    });
    // Then responde 200 OK con el JWT activo
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.token).toBeTruthy();
    tokenCliente = body.token;
  });

  test('E2E-03 [HU-03] Consultar perfil autenticado', async ({ request }) => {
    // Given un cliente con sesión JWT válida
    // When consulta GET /api/usuarios/me
    const res = await request.get('/api/usuarios/me', {
      headers: { Authorization: `Bearer ${tokenCliente}` }
    });
    // Then devuelve los datos de su perfil
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.correo).toBe(correoUnico);
  });

  test('E2E-04 [HU-04] Listar catálogo público de juegos', async ({ request }) => {
    // Given la tienda en funcionamiento
    // When consulta GET /api/juegos
    const res = await request.get('/api/juegos');
    // Then devuelve la lista de juegos disponibles
    expect(res.status()).toBe(200);
    expect(Array.isArray(await res.json())).toBeTruthy();
  });

  test('E2E-05 [HU-09] Consultar categorías y plataformas del catálogo', async ({ request }) => {
    // Given un usuario explorando filtros
    // When consulta GET /api/categorias y /api/plataformas
    const resCat = await request.get('/api/categorias');
    const resPlat = await request.get('/api/plataformas');
    // Then ambos endpoints responden 200 OK
    expect(resCat.status()).toBe(200);
    expect(resPlat.status()).toBe(200);
  });

  test('E2E-06 [HU-18] Consultar historial de órdenes del cliente', async ({ request }) => {
    // Given un cliente autenticado
    // When consulta GET /api/ordenes/me
    const res = await request.get('/api/ordenes/me', {
      headers: { Authorization: `Bearer ${tokenCliente}` }
    });
    // Then devuelve su historial de órdenes
    expect(res.status()).toBe(200);
    expect(Array.isArray(await res.json())).toBeTruthy();
  });

  test('E2E-07 [Negativo - HU-02] Login rechazado con contraseña incorrecta', async ({ request }) => {
    // Given un correo registrado pero contraseña errónea
    // When intenta iniciar sesión en /api/auth/login
    const res = await request.post('/api/auth/login', {
      data: { correo: correoUnico, contrasena: 'PasswordMala999' }
    });
    // Then el sistema deniega el acceso (401/403)
    expect([401, 403]).toContain(res.status());
  });

  test('E2E-08 [Negativo - HU-08] Cliente sin rol admin no puede crear juegos', async ({ request }) => {
    // Given un usuario con rol cliente (no admin)
    // When intenta hacer POST /api/juegos
    const res = await request.post('/api/juegos', {
      headers: { Authorization: `Bearer ${tokenCliente}` },
      data: { titulo: 'Juego No Autorizado', precio: 50, plataformaId: 1, categoriaId: 1 }
    });
    // Then el sistema bloquea la operación con 403 Forbidden
    expect(res.status()).toBe(403);
  });
});