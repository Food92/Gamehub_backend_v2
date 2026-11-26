# Documentación APIs e Integración — "GameHub Store"

**Título:** Documentación de API e Integración de Microservicios para Gestión de GameHub Store  
**Versión del Documento:** 1.0  
**Fecha de Creación:** 26/11/2026  
**Asignatura:** DSY1104 — Desarrollo Fullstack II (Anexo 2 — Evaluación Parcial 3)  

---

## Descripción General
Este documento proporciona una descripción técnica detallada de los endpoints REST (versiones `v1` y `v2` con HATEOAS) diseñados para gestionar la autenticación JWT, catálogo gamer, categorías, códigos de descuento, órdenes Saga y simulación de pagos dentro de la plataforma **GameHub Store**, así como su integración con el frontend React (`gamehub-v2`) y la persistencia híbrida **PostgreSQL + Firebase Cloud Firestore**.

Está dirigido a desarrolladores full-stack, docentes evaluadores y administradores de sistemas que interactúan con el ecosistema de microservicios a través del API Gateway.

---

## Información General
- **URL Base de la API (API Gateway):** `http://localhost:8091`
- **URL Swagger UI Unificado (OpenAPI 3.0):** `http://localhost:8091/docs/webjars/swagger-ui/index.html`
- **URL Service Discovery (Eureka Server):** `http://localhost:8761`
- **Frontend Web (`gamehub-v2`):** `http://localhost:5173`
- **Base de Datos Relacional:** PostgreSQL (`localhost:5432`, esquema *Database per Service*)
- **Base de Datos NoSQL en la Nube:** Firebase Cloud Firestore (Proyecto `gamehub-db07b` — colecciones `usuarios`, `categorias`, `productos`, `boletas`, `detalle_boleta`)
- **Esquema de Autenticación:** `Authorization: Bearer <JWT>` (HMAC-SHA256 / `HS256`, roles `ROLE_ADMIN`, `ROLE_VENDEDOR`, `ROLE_CLIENTE`)

---

## Documentación de Endpoints

### Endpoints de Autenticación (`authentificator-mscv` — v1)

#### `POST /api/v1/auth/login`
- **Descripción:** Autentica credenciales de usuario contra `auth_service_db` (hash BCrypt) y emite un token JWT firmado junto con el rol y tiempo de expiración.
- **Método:** `POST`
- **Datos de Entrada:**
```json
{
  "email": "cliente@gmail.com",
  "password": "cliente123"
}
```
- **Respuesta:** `200 OK` (retorna `token`, `email`, `rol`, `expira`), `401 Unauthorized` (credenciales inválidas), `403 Forbidden` (cuenta desactivada).
- **Autenticación:** No (endpoint público).
- **Observaciones:** El claim `roles` del JWT es validado por `gateway-mscv` (`OAuth2 Resource Server`) en cada petición posterior.

#### `POST /api/v1/auth/register`
- **Descripción:** Registra una nueva cuenta de acceso con rol `ROLE_CLIENTE` y retorna un JWT inmediato.
- **Método:** `POST`
- **Datos de Entrada:**
```json
{
  "email": "nuevo.cliente@gmail.com",
  "password": "password123"
}
```
- **Respuesta:** `201 Created`, `400 Bad Request` (datos inválidos), `409 Conflict` (correo ya registrado).
- **Autenticación:** No (endpoint público).
- **Observaciones:** En `gamehub-v2`, tras el registro se sincroniza además el perfil del comprador en `POST /api/v1/users`.

#### `POST /api/v1/auth/renovar` (Alias: `POST /api/v1/auth/refresh`)
- **Descripción:** Renueva el token JWT vigente del usuario autenticado antes de su vencimiento sin solicitar nuevamente la contraseña.
- **Método:** `POST`
- **Datos de Entrada:** Cabecera `Authorization: Bearer <JWT_ACTUAL>` (sin body obligatorio).
- **Respuesta:** `200 OK` (nuevo token JWT con expiración extendida), `401 Unauthorized` (token expirado o inválido).
- **Autenticación:** Sí (Cualquier rol autenticado: `ADMIN`, `VENDEDOR`, `CLIENTE`).
- **Observaciones:** Consumido automáticamente por el temporizador proactivo de `useGameHub.js` antes de expirar la sesión.

---

### Endpoints de Productos (`product-mscv` — v1 y v2 HATEOAS)

#### `GET /api/v1/products`
- **Descripción:** Lista todos los productos registrados en el catálogo de GameHub Store.
- **Método:** `GET`
- **Datos de Entrada:** N/A.
- **Respuesta:** `200 OK` (arreglo JSON de `Product`), `500 Internal Server Error`.
- **Autenticación:** No (lectura pública de catálogo).
- **Observaciones:** Retorno liviano utilizado por `gamehub-v2` y enriquecido con stock real desde `GET /api/v1/inventories/stock`.

#### `POST /api/v1/products`
- **Descripción:** Crea un nuevo producto en PostgreSQL (`product_service_db`) y lo sincroniza automáticamente en la colección `productos` de Firebase Cloud Firestore.
- **Método:** `POST`
- **Datos de Entrada:**
```json
{
  "nombreProduct": "Mouse Gamer Wireless Pro",
  "marca": "Logitech",
  "modelo": "G Pro X Superlight 2",
  "precio": 149990,
  "descripcion": "Mouse ultraligero 60g con sensor HERO 2.",
  "idCategory": 2,
  "imagenUrl": "/img/teclado.jpg",
  "mesesGarantia": 12,
  "estado": true,
  "enOferta": true,
  "precioOferta": 129990
}
```
- **Respuesta:** `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`.
- **Autenticación:** Sí (`ROLE_ADMIN`).
- **Observaciones:** Valida la existencia de `idCategory` vía OpenFeign contra `category-mscv`.

#### `GET /api/v2/products` (HATEOAS)
- **Descripción:** Lista los productos en formato HAL/HATEOAS incluyendo enlaces hipermedia `_links` (`self`, actualización y catálogo) generados por `ProductModelAssembler`.
- **Método:** `GET`
- **Datos de Entrada:** N/A.
- **Respuesta:** `200 OK` (`CollectionModel<EntityModel<Product>>`), `500 Internal Server Error`.
- **Autenticación:** No (público).
- **Observaciones:** Representa la versión 2 hipermedia conforme a los estándares REST nivel 3 de madurez de Richardson.

#### `PUT /api/v2/products/{id}` (HATEOAS)
- **Descripción:** Actualiza un producto existente y retorna su representación HATEOAS con enlaces `_links`, sincronizando el cambio en Cloud Firestore (`productos`).
- **Método:** `PUT`
- **Datos de Entrada:**
```json
{
  "nombreProduct": "PlayStation 5 Slim 1TB",
  "marca": "Sony",
  "modelo": "CFI-2000",
  "precio": 549990,
  "idCategory": 1,
  "mesesGarantia": 12,
  "estado": true
}
```
- **Respuesta:** `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`.
- **Autenticación:** Sí (`ROLE_ADMIN`).
- **Observaciones:** Solo accesible con rol `ADMIN`; roles `VENDEDOR` y `CLIENTE` reciben `403 Forbidden`.

---

### Endpoints de Categorías (`category-mscv` — v1 y v2 HATEOAS)

#### `GET /api/v1/categories`
- **Descripción:** Lista todas las categorías gamer disponibles (`Consolas`, `Periféricos`, `Monitores`, `Hardware`).
- **Método:** `GET`
- **Datos de Entrada:** N/A.
- **Respuesta:** `200 OK`, `500 Internal Server Error`.
- **Autenticación:** No (público).
- **Observaciones:** Al iniciar `category-mscv`, `DataLoader` precarga las 4 categorías base y las sincroniza con la colección `categorias` de Cloud Firestore.

#### `POST /api/v1/categories`
- **Descripción:** Crea una nueva categoría en PostgreSQL y la replica en Cloud Firestore (`categorias`).
- **Método:** `POST`
- **Datos de Entrada:**
```json
{
  "nombreCategory": "Sillas Gamer",
  "descripcionCategory": "Sillas ergonómicas reclinables para largas sesiones de juego.",
  "estado": "ACTIVO"
}
```
- **Respuesta:** `201 Created`, `400 Bad Request`, `403 Forbidden`, `409 Conflict` (nombre duplicado).
- **Autenticación:** Sí (`ROLE_ADMIN`).
- **Observaciones:** Rechaza nombres repetidos mediante validación de unicidad en `CategoryServiceImpl`.

#### `GET /api/v2/categories` y `PUT /api/v2/categories/{id}` (HATEOAS)
- **Descripción:** Consulta y actualización de categorías con hipermedia `_links` (`CategoryModelAssembler`).
- **Método:** `GET` / `PUT`
- **Datos de Entrada (`PUT`):** `{"nombreCategory": "Consolas Next-Gen", "descripcionCategory": "Consolas y accesorios oficiales", "estado": "ACTIVO"}`
- **Respuesta:** `200 OK`, `400 Bad Request`, `403 Forbidden` (`PUT`), `404 Not Found`.
- **Autenticación:** `GET` No (público); `PUT` Sí (`ROLE_ADMIN`).
- **Observaciones:** Sincroniza automáticamente los cambios en Firestore (`categorias/{idCategoria}`).

---

### Endpoints de Promociones (`promotion-mscv` — v1)

#### `GET` / `POST /api/v1/promotions/validar`
- **Descripción:** Valida un código promocional contra vigencia, estado activo, monto mínimo y tope de usos, devolviendo el descuento exacto calculado sin consumir el cupón.
- **Método:** `GET` o `POST`
- **Datos de Entrada (Query Params):** `?codigo=GAMER10&subtotal=100000` (también disponible `GAMEHUB5000` para compras desde `$20.000`).
- **Respuesta (`200 OK`):**
```json
{
  "valido": true,
  "codigo": "GAMER10",
  "tipo": "PORCENTAJE",
  "valor": 10.00,
  "descuento": 10000.00,
  "mensaje": "Cupón válido"
}
```
- **Autenticación:** No (público para previsualización en checkout).
- **Observaciones:** Si el código no existe o está expirado retorna `valido: false`, `descuento: 0` y un mensaje claro (`"Cupón no encontrado"` / `"Promoción fuera de vigencia"`).

---

### Endpoints de Órdenes (`order-mscv` — v1)

#### `POST /api/v1/orders`
- **Descripción:** Ejecuta la Saga de creación de boleta: valida usuario (`user-mscv`), precios vigentes (`product-mscv`), cupón opcional (`promotion-mscv`) y reserva stock (`inventory-mscv`). Crea la boleta en estado `PENDIENTE_PAGO` y sincroniza `boletas` y `detalle_boleta` en Cloud Firestore.
- **Método:** `POST`
- **Datos de Entrada:**
```json
{
  "idUsuario": 1,
  "promotionCode": "GAMER10",
  "details": [
    { "idProduct": 1, "cantidad": 1 }
  ]
}
```
- **Respuesta:** `201 Created` (`OrderDTO` con `orderId`, `subtotal`, `descuento`, `total`, `estado: "PENDIENTE_PAGO"`), `400 Bad Request`, `401 Unauthorized`, `403 Forbidden` (`VENDEDOR`), `422 Unprocessable Entity` (sin stock o cupón inválido).
- **Autenticación:** Sí (`ROLE_CLIENTE`, `ROLE_ADMIN`).
- **Observaciones:** Si falla la reserva de inventario en alguna línea, ejecuta compensación Saga automática liberando stock y cupón.

#### `PATCH` / `PUT /api/v1/orders/{orderId}/estado`
- **Descripción:** Actualiza el estado del ciclo de vida de una orden (`PENDIENTE_PAGO`, `PAGADA`, `EN_DESPACHO`, `ENTREGADA`, `CANCELADA`). Al pasar a `PAGADA`, confirma la rebaja definitiva en `inventory-mscv`, confirma el uso del cupón en `promotion-mscv` (si aplica) y actualiza el documento en Cloud Firestore (`boletas`).
- **Método:** `PATCH` o `PUT`
- **Datos de Entrada (Query Param):** `?estado=PAGADA`
- **Respuesta:** `200 OK` (`OrderDTO`), `403 Forbidden`, `404 Not Found`, `422 Unprocessable Entity`.
- **Autenticación:** Sí (`ROLE_ADMIN` vía Gateway; también invocado internamente por `payment-mscv` vía Feign `PUT`).
- **Observaciones:** Acepta `PUT` además de `PATCH` para compatibilidad nativa con el cliente HTTP de OpenFeign en Java 21.

---

### Endpoints de Pagos (`payment-mscv` — v1)

#### `POST /api/v1/payments/simular`
- **Descripción:** Simula una pasarela de pago (Tarjeta de Crédito, Débito, Transferencia o Webpay Simulado). Obtiene automáticamente el total exacto de la orden desde `order-mscv`, valida que esté en `PENDIENTE_PAGO`, registra el pago aprobado (`PAY-XXXXXXXX`) y notifica a `order-mscv` para cambiar el estado a **`PAGADA`** (actualizando PostgreSQL y Firebase Cloud Firestore).
- **Método:** `POST`
- **Datos de Entrada:**
```json
{
  "orderId": 1,
  "tipoPago": "TARJETA_CREDITO",
  "simularRechazo": false
}
```
- **Respuesta (`201 Created`):**
```json
{
  "orderId": 1,
  "monto": 494991.0,
  "tipoPago": "TARJETA_CREDITO",
  "estadoPago": "APROBADO",
  "codigoPago": "PAY-A1B2C3D4",
  "fechaPago": "2026-11-26 01:20:15"
}
```
- **Autenticación:** Sí (`ROLE_CLIENTE`, `ROLE_ADMIN`).
- **Observaciones:** Evita pagos duplicados sobre una misma orden y valida montos con tolerancia decimal (`0.01`).

---

## Integración con el Frontend y Persistencia Híbrida

### Consumo desde `gamehub-v2`
1. **Cliente HTTP Centralizado (`src/services/api.js`):** Todas las peticiones apuntan a `VITE_API_BASE_URL` (`http://localhost:8091/api/v1`), adjuntando automáticamente la cabecera `Authorization: Bearer <token>` leída desde `src/services/authStorage.js`. Ante respuestas `401`, limpia la sesión y dispara `AUTH_EXPIRED_EVENT`; ante `403`, entrega un mensaje controlado de permisos insuficientes.
2. **Servicios de Dominio (`src/services/*.js`):** Separan limpiamente `auth.js`, `productos.js`, `categorias.js`, `ordenes.js` (incluyendo `validarCupon`, `validarCarrito`, `crear` y `simularPago`) y `usuarios.js`.
3. **Control de Acceso y Sesión (`src/hooks/useGameHub.js` + `ProtectedRoute.jsx`):** Renueva el JWT antes de expirar (`POST /api/v1/auth/renovar`), aísla el carrito por cuenta (`gamehubCart_<id>` / `gamehubCart_guest`) y actualiza reactivamente los estados de boleta (`PENDIENTE_PAGO` → `PAGADA`) tanto en `#resultado` como en `#perfil` y `#ordenes`.

### Persistencia Híbrida (PostgreSQL + Firebase Cloud Firestore)
Cada operación crítica se persiste transaccionalmente en **PostgreSQL** y se replica mediante `FirebaseConfig` + `FirestoreSyncService` (SDK `firebase-admin:9.3.0`) hacia **Firebase Cloud Firestore** (`gamehub-db07b`):
- `user-mscv` $\rightarrow$ colección **`usuarios`** (`idUsuario`, `rut`, `nombreCompleto`, `apellidoCompleto`, `correo`, `telefono`, `estado`)
- `category-mscv` $\rightarrow$ colección **`categorias`** (`idCategoria`, `nombre`, `descripcion`, `estado`)
- `product-mscv` $\rightarrow$ colección **`productos`** (`idProducto`, `idCategoria`, `nombre`, `marca`, `modelo`, `precio`, `precioOferta`, `enOferta`, `estado`)
- `order-mscv` $\rightarrow$ colecciones **`boletas`** (`idBoleta`, `idUsuario`, `estado`, `subtotal`, `descuento`, `total`, `codigoPromocion`, `fecha`) y **`detalle_boleta`** (`idDetalle`, `idBoleta`, `idProducto`, `cantidad`, `precioUnitario`, `subtotalLinea`)

---

## Verificación de Entrega — Ejemplos Visuales

**Figura 1**  
*Solicitud de catálogo de productos desde el frontend con respuesta 200 OK en DevTools*  
> **[INSERTAR CAPTURA FIGURA 1 — Borde 1px color negro: Navegador mostrando `http://localhost:5173/#catalogo` con DevTools (pestaña Network) abierto evidenciando `GET http://localhost:8091/api/v1/products` y `GET /api/v1/categories` con código HTTP `200 OK` y payload JSON]**  

*Nota: Evidencia de integración directa entre `gamehub-v2` y el API Gateway (`8091`), cargando catálogo real y stock en vivo. Ruta `http://localhost:5173/#catalogo`.*

**Figura 2**  
*Aplicación de cupón de descuento y simulación de pago exitosa (201 Created → PAGADA)*  
> **[INSERTAR CAPTURA FIGURA 2 — Borde 1px color negro: Navegador en `#resultado` tras aplicar `GAMER10` y presionar "Simular / Confirmar pago", mostrando estado `PAGADA`, código `PAY-...` y en DevTools Network la llamada `POST /api/v1/payments/simular` con respuesta `201 Created`]**  

*Nota: Demuestra el flujo completo Saga desde creación de boleta con descuento hasta aprobación en `payment-mscv` y transición de estado en `order-mscv`. Ruta `http://localhost:5173/#resultado`.*

**Figura 3**  
*Manejo controlado de error en la interfaz ante cupón inválido o acceso denegado (403)*  
> **[INSERTAR CAPTURA FIGURA 3 — Borde 1px color negro: Vista `#checkout` ingresando el cupón inexistente `NOEXISTE` mostrando el mensaje de rechazo controlado ("Cupón no encontrado"), o consola/Swagger mostrando respuesta `403 Forbidden` al intentar acceder a `/api/v1/products` POST con token de rol `CLIENTE`]**  

*Nota: Verifica la validación de reglas de negocio y el control de errores HTTP sin quiebres visuales en la aplicación. Ruta `http://localhost:5173/#checkout`.*

**Figura 4**  
*Sincronización reflejada en Firebase Console (colección boletas en estado PAGADA)*  
> **[INSERTAR CAPTURA FIGURA 4 — Borde 1px color negro: Consola de Firebase Cloud Firestore (`gamehub-db07b`) mostrando las 5 colecciones (`usuarios`, `categorias`, `productos`, `boletas`, `detalle_boleta`) y un documento dentro de `boletas` con `estado: "PAGADA"`, `codigoPromocion`, `descuento` y `total`]**  

*Nota: Confirma la persistencia híbrida en tiempo real tras la simulación de pago en `payment-mscv` y actualización en `order-mscv`. Consola Firebase Firestore `gamehub-db07b`.*

**Figura 5**  
*Portal Swagger UI unificado en el API Gateway con definiciones OpenAPI activas*  
> **[INSERTAR CAPTURA FIGURA 5 — Borde 1px color negro: Navegador en `http://localhost:8091/docs/webjars/swagger-ui/index.html` mostrando el selector desplegable con las definiciones de microservicios (Pagos, Promociones, Órdenes, Productos, Categorías, Usuarios) y endpoints cargados sin errores]**  

*Nota: Evidencia la documentación viva OpenAPI 3.0 (`springdoc-openapi 2.6.0`) agregada desde el Gateway. Ruta `http://localhost:8091/docs/webjars/swagger-ui/index.html`.*
