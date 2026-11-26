# Guion de Estudio y Defensa Oral — DSY1104 Evaluación Parcial 3 (60%)

**Proyecto:** GameHub Store (`GameHub-Store001-main` + `gamehub-v2`)  
**Asignatura:** DSY1104 — Desarrollo Fullstack II (Duoc UC)  
**Fecha:** 26/11/2026  
**Objetivo:** Obtener **100% ("Muy buen desempeño")** en los 6 indicadores de la **Situación Evaluativa 2 (Presentación Individual — 60%)**, respaldando cada afirmación en vivo sobre nuestro código real, Swagger UI, DevTools y Firebase Console.

---

## Preparación Rápida Antes de Entrar a Presentar (2 minutos)

Ten estas **4 pestañas/ventanas** abiertas antes de empezar a hablar:
1. **IntelliJ IDEA** con los microservicios corriendo y estos archivos abiertos en pestañas:
   - `OrderServiceImpl.java` (`order-mscv`)
   - `PaymentServiceImpl.java` (`payment-mscv`)
   - `PromotionService.java` y `DataLoader.java` (`promotion-mscv`)
   - `FirebaseConfig.java` y `FirestoreSyncService.java` (`order-mscv`)
   - `JwtService.java` y `AuthController.java` (`authentificator-mscv`)
   - `SecurityConfig.java` (`gateway-mscv`)
2. **VS Code / Editor** con `gamehub-v2` abierto en:
   - `src/services/api.js`, `src/services/authStorage.js`, `src/services/ordenes.js`
   - `src/hooks/useGameHub.js`
   - `src/logica/roles.js`, `src/components/ProtectedRoute/ProtectedRoute.jsx`, `src/components/Header/Header.jsx`
3. **Navegador** con 4 pestañas listas:
   - `http://localhost:5173` (`gamehub-v2` con DevTools `F12` → pestaña **Network** lista)
   - `http://localhost:8091/docs/webjars/swagger-ui/index.html` (Swagger UI agregado)
   - `http://localhost:8761` (Eureka Server con los microservicios en estado `UP`)
   - **Firebase Console → Cloud Firestore (`gamehub-db07b`)** mostrando las 5 colecciones (`usuarios`, `categorias`, `productos`, `boletas`, `detalle_boleta`).
4. **Cuentas y Cupones a mano:**
   - `admin@gamehub.com` / `admin123` (`ROLE_ADMIN`)
   - `vendedor@gamehub.com` / `vendedor123` (`ROLE_VENDEDOR`)
   - `cliente@gmail.com` / `cliente123` (`ROLE_CLIENTE`)
   - Cupones activos: **`GAMER10`** (`10%`) y **`GAMEHUB5000`** (`$5.000 CLP` desde `$20.000`).

---

## Punto 1 — `IE3.1.2`: Backend, Base de Datos, Lógica de Negocio y Persistencia Híbrida

### Qué decir exactamente frente al profesor
> *"Nuestro backend está construido sobre Java 21 y Spring Boot 3.3.2 con arquitectura de microservicios siguiendo el patrón **Database per Service** en PostgreSQL, complementado con **persistencia híbrida en tiempo real hacia Firebase Cloud Firestore** (`gamehub-db07b`). Cada entidad de negocio está modelada con JPA/Hibernate (`User`, `Category`, `Product`, `Promotion`, `Order`, `DetailOrder`, `Payment`) con validaciones Bean Validation (`@NotNull`, `@NotBlank`, `@Positive`).  
> En la lógica de negocio de compra implementamos el **patrón Saga** en `OrderServiceImpl`: al crear una orden validamos el usuario en `user-mscv`, consultamos precios vigentes u ofertas en `product-mscv`, validamos y reservamos códigos promocionales como `GAMER10` o `GAMEHUB5000` en `promotion-mscv`, y reservamos stock en `inventory-mscv`. Si cualquier paso falla, la Saga ejecuta compensación automática devolviendo stock y cupón.  
> La boleta nace en estado `PENDIENTE_PAGO` y se replica de inmediato en Cloud Firestore en las colecciones `boletas` y `detalle_boleta` (junto con `usuarios`, `categorias` y `productos`). Finalmente, al simular el pago en `payment-mscv` (`POST /api/v1/payments/simular`), se valida el monto con tolerancia decimal, se evita doble pago, se aprueba la transacción (`PAY-...`) y se notifica a `order-mscv` para pasar el estado a **`PAGADA`**, confirmando la rebaja de inventario y actualizando al instante el documento en Firestore."*

### Qué mostrar en vivo
1. **IntelliJ:** Abre `order-mscv/.../services/OrderServiceImpl.java` (líneas `40-76` Saga de creación y líneas `122-133` confirmación a `PAGADA` + `firestoreSyncService.sincronizarBoletaYDetalles(saved)`).
2. **IntelliJ:** Muestra `payment-mscv/.../services/PaymentServiceImpl.java` (`simularPago` y `save`) y `promotion-mscv/.../config/DataLoader.java` (`GAMER10` y `GAMEHUB5000`).
3. **Firebase Console (`gamehub-db07b`):** Muestra las **5 colecciones** (`usuarios`, `categorias`, `productos`, `boletas`, `detalle_boleta`) y un documento de `boletas` cambiando en vivo de `PENDIENTE_PAGO` a `PAGADA` con `codigoPromocion`, `descuento` y `total`.

### 3 preguntas difíciles del profesor (y su respuesta exacta)
1. **¿Por qué usan PostgreSQL y Firebase Cloud Firestore al mismo tiempo? ¿Qué pasa si se cae internet o falla Firestore?**  
   - **Respuesta:** PostgreSQL es nuestra fuente transaccional ACID por microservicio (integridad de stock, reservas Saga y locks pesimistas `FOR UPDATE` en cupones). Firestore actúa como réplica documental NoSQL en la nube para las 5 entidades exigidas. En `FirebaseConfig` y `FirestoreSyncService` manejamos fallos de red con `try-catch` y timeout acotado (15 s): si Firestore no responde, la compra en PostgreSQL **no se cae** y el servicio deja trazabilidad estructurada en logs (`[Firebase]`).
2. **¿Cómo evitan que dos clientes paguen o reserven el último stock/cupón al mismo tiempo?**  
   - **Respuesta:** En `promotion-mscv` usamos bloqueo pesimista JPA (`findByCodeForUpdate`) y reservas por `orderId` idempotentes (`PromotionReservation`). En `payment-mscv` verificamos que no exista ya un pago `APROBADO` para el `orderId` y que la orden esté en `PENDIENTE_PAGO`. En `order-mscv` existe además un reintento programado `@Scheduled(retryCompensations)` para cerrar compensaciones pendientes.
3. **¿Cómo evitan que un cliente manipule el precio o el total desde el navegador?**  
   - **Respuesta:** El frontend **nunca envía precios ni totales** en `POST /api/v1/orders`; solo envía `idUsuario`, `promotionCode` y la lista `[{ idProduct, cantidad }]`. `OrderServiceImpl.save()` consulta el precio real (y `precioOferta` si aplica) directamente a `product-mscv` y calcula `subtotal`, `descuento` y `total` 100% en el servidor. Lo mismo hace `PaymentServiceImpl.simularPago()`, que obtiene el `total` oficial directo desde `order-mscv`.

---

## Punto 2 — `IE3.2.3`: API REST en Spring Boot, CRUD (`v1`/`v2 HATEOAS`) y Swagger UI

### Qué decir exactamente frente al profesor
> *"Todos los microservicios exponen APIs RESTful siguiendo las convenciones HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) con códigos de estado semánticos (`200 OK`, `201 Created`, `204 No Content`, `400`, `401`, `403`, `404`, `422`) y manejo centralizado de excepciones (`ApiExceptionHandler`).  
> Además, implementamos versionado real en nuestros recursos núcleo: **v1** (`/api/v1/products`, `/api/v1/categories`, `/api/v1/users`) optimizada para consumo ligero desde el frontend, y **v2 con HATEOAS** (`/api/v2/products`, `/api/v2/categories`, `/api/v2/users`) usando `Spring HATEOAS` (`EntityModel`, `CollectionModel` y `ModelAssembler`) que inyecta enlaces navegables `_links`.  
> Para la documentación interactiva estandarizamos todos los módulos en `springdoc-openapi 2.6.0` + `swagger-models-jakarta 2.2.22` y configuramos un **portal Swagger UI unificado en el API Gateway** (`http://localhost:8091/docs/webjars/swagger-ui/index.html`) desde donde podemos probar todos los microservicios con botón `Authorize` (Bearer JWT)."*

### Qué mostrar en vivo
1. **Swagger UI (`http://localhost:8091/docs/webjars/swagger-ui/index.html`):**
   - Despliega el selector superior (*Select a definition*) mostrando las 12 definiciones cargando sin errores.
   - Ejecuta `GET /api/v2/products` o `GET /api/v2/categories` para mostrar la respuesta JSON con `_links` (HATEOAS).
   - Ejecuta `GET /api/v1/promotions/validar?codigo=GAMER10&subtotal=100000` (y luego con `NOEXISTE` para mostrar el rechazo controlado).
2. **IntelliJ:** Muestra brevemente `ControllerCategoryV2.java` o `ProductControllerV2.java` y `gateway-mscv/src/main/resources/application.yml` (sección `springdoc.swagger-ui.urls`).

### 3 preguntas difíciles del profesor (y su respuesta exacta)
1. **¿Qué diferencia técnica real hay entre `/api/v1/products` y `/api/v2/products`?**  
   - **Respuesta:** `/api/v1/products` retorna DTOs/entidades JSON planas (nivel 2 de madurez de Richardson), mientras que `/api/v2/products` implementa nivel 3 (**HATEOAS**): usa `ProductModelAssembler` para envolver cada recurso en `EntityModel<Product>` y la lista en `CollectionModel`, agregando enlaces `_links` (`self`, listado, operaciones relacionadas) para que el cliente descubra acciones dinámicamente.
2. **¿Por qué concentraron Swagger en el Gateway (`8091`) en lugar de entrar puerto por puerto?**  
   - **Respuesta:** En arquitectura de microservicios los puertos internos (`8001..8088`) no deberían exponerse directamente al consumidor. El Gateway agrega los `/v3/api-docs` de cada servicio mediante rutas `/api-docs/<servicio>` con `lb://<servicio>` y `SetPath=/v3/api-docs`, dando un único punto de documentación y pruebas con CORS y JWT ya integrados.
3. **¿Por qué `/api/v1/orders/{orderId}/estado` acepta tanto `PATCH` como `PUT`?**  
   - **Respuesta:** El cliente HTTP estándar de Java (`HttpURLConnection`) que utiliza OpenFeign por defecto no soporta el verbo `PATCH` sin librerías externas (`ProtocolException: Invalid HTTP method: PATCH`). Soportar `{RequestMethod.PATCH, RequestMethod.PUT}` mantiene compatibilidad con clientes REST/Swagger (`PATCH`) y permite que `payment-mscv` invoque a `order-mscv` con `PUT` de forma estable y nativa.

---

## Punto 3 — `IE3.2.4`: Integración Backend ↔ Frontend y Eficiencia del Flujo

### Qué decir exactamente frente al profesor
> *"La comunicación entre el frontend `gamehub-v2` y los 12 microservicios está completamente desacoplada a través de un único punto de entrada: el **API Gateway (`http://localhost:8091`)**, que resuelve dinámicamente las instancias registradas en **Eureka Server (`http://localhost:8761`)** mediante `lb://`. Internamente, los microservicios colaboran de forma declarativa con **Spring Cloud OpenFeign** (por ejemplo: `order-mscv` consulta `user-mscv`, `product-mscv`, `promotion-mscv` e `inventory-mscv`; y `payment-mscv` consulta y actualiza `order-mscv`).  
> En `gamehub-v2`, centralizamos todas las llamadas HTTP en `src/services/api.js` (usando `VITE_API_URL`) y separamos capas de servicios por dominio (`productos.js`, `categorias.js`, `ordenes.js`, `auth.js`, `usuarios.js`). Para optimizar el rendimiento usamos `Promise.allSettled` al cargar catálogo + categorías + stock en lote (`/api/v1/inventories/stock?productoIds=...`) y `AbortController` para cancelar peticiones obsoletas al cambiar de vista."*

### Qué mostrar en vivo
1. **Eureka (`http://localhost:8761`):** Muestra todos los microservicios registrados `UP`.
2. **Navegador (`http://localhost:5173` + DevTools `F12` → Network):**
   - Recarga `#catalogo` y muestra las llamadas paralelas `200 OK` hacia `localhost:8091/api/v1/products`, `/categories` y `/inventories/stock`.
   - Haz una compra rápida como `cliente@gmail.com` aplicando `GAMER10`, presiona **Confirmar compra** (`POST /api/v1/orders` → `201`) y luego **Simular / Confirmar pago** (`POST /api/v1/payments/simular` → `201`), mostrando el cambio inmediato de `PENDIENTE_PAGO` a `PAGADA` en la UI y en Firebase Console.
3. **VS Code:** Muestra `src/services/api.js` y `src/services/ordenes.js`.

### 3 preguntas difíciles del profesor (y su respuesta exacta)
1. **¿Qué ocurre si un microservicio auxiliar como `inventory-mscv` no responde al cargar el catálogo?**  
   - **Respuesta:** En `useGameHub.js` (`recargarCatalogo`), la consulta de stock tiene degradación elegante (`.catch(() => [])`) y categorías/productos usan `Promise.allSettled`. Así la tienda sigue mostrando el catálogo sin pantallas en blanco, mientras que al confirmar compra (`POST /api/v1/orders`) la validación de stock sí es estricta para proteger la integridad de ventas.
2. **¿Cómo evitaron errores de CORS entre Vite (`5173`) y los microservicios?**  
   - **Respuesta:** Configuramos `CorsConfigurationSource` de forma centralizada en `gateway-mscv/SecurityConfig.java` permitiendo orígenes `localhost:5173`/`127.0.0.1:5173`, métodos `GET, POST, PUT, PATCH, DELETE, OPTIONS` y cabeceras `Authorization, Content-Type`, además de permitir `OPTIONS /**` sin autenticación para preflight CORS.
3. **¿Cómo evitan fugas de memoria o estados desactualizados si el usuario cambia rápido de vista mientras cargan datos?**  
   - **Respuesta:** En los `useEffect` de `useGameHub.js` pasamos `AbortController` (`signal`) a `fetch` en `api.js` y abortamos la petición en la función de limpieza del efecto, ignorando respuestas si `signal.aborted` es verdadero.

---

## Punto 4 — `IE3.3.4`: Autenticación en Backend con JWT y Autorización por Roles

### Qué decir exactamente frente al profesor
> *"Implementamos seguridad stateless en dos capas: emisión/gestión de identidad en `authentificator-mscv` y validación/autorización perimetral en `gateway-mscv`.  
> En `authentificator-mscv`, las contraseñas nunca se guardan en texto plano: se cifran con **BCrypt** (`PasswordEncoder`). Al autenticar en `POST /api/v1/auth/login`, `JwtService` genera un token firmado con **HMAC-SHA256 (`HS256`)** con expiración de 1 hora (`3600s`), incluyendo los claims `sub` (correo), `userId`, `roles` (`ROLE_ADMIN`, `ROLE_VENDEDOR`, `ROLE_CLIENTE`), `iat` y `exp`. Además exponemos `POST /api/v1/auth/renovar` (y alias `/refresh`) para renovar tokens válidos sin reingresar credenciales.  
> En `gateway-mscv/SecurityConfig.java` configuramos Spring Security como **OAuth2 Resource Server Reactivo**, que valida la firma `HS256`, extrae las autoridades del claim `roles` y aplica reglas RBAC estrictas por verbo HTTP y ruta: por ejemplo, catálogo público en `GET`, mutaciones de productos/categorías exclusivas de `ADMIN`, creación de órdenes y simulación de pago para `CLIENTE`/`ADMIN`, y órdenes globales en solo lectura para `VENDEDOR`."*

### Qué mostrar en vivo
1. **IntelliJ:** Abre `authentificator-mscv/.../security/JwtService.java` (líneas `27-46`: claims y `HS256`) y `AuthController.java` (`/login`, `/register`, `/renovar`, `/refresh`).
2. **IntelliJ:** Abre `gateway-mscv/.../security/SecurityConfig.java` (líneas `63-151`: `NimbusReactiveJwtDecoder`, `JwtGrantedAuthoritiesConverter` y matriz de `pathMatchers`).
3. **Swagger UI:** Haz login con `cliente@gmail.com`, pega el JWT en `Authorize` e intenta ejecutar `POST /api/v1/products` → muestra **`403 Forbidden`** en vivo (y sin token → **`401 Unauthorized`**).

### 3 preguntas difíciles del profesor (y su respuesta exacta)
1. **¿Cuál es la diferencia entre un error `401 Unauthorized` y `403 Forbidden` en su sistema?**  
   - **Respuesta:** `401 Unauthorized` ocurre cuando el cliente no envía token JWT, el token tiene firma inválida o ya expiró (`exp` vencido). `403 Forbidden` ocurre cuando el token **sí es válido**, pero el rol del usuario no tiene permiso para esa ruta/verbo HTTP (por ejemplo, un `CLIENTE` o `VENDEDOR` intentando crear un producto en `POST /api/v1/products`).
2. **¿Dónde está guardado el secreto `JWT_SECRET`? ¿Está subido en GitHub?**  
   - **Respuesta:** No está en GitHub. El secreto se carga desde el archivo local `.env` mediante `spring.config.import: optional:file:./.env[.properties],optional:file:../.env[.properties]`, y tanto `.env` como `firebase-service-account.json` están explícitamente excluidos en `.gitignore`.
3. **¿Cómo manejan una cuenta desactivada por el administrador si intenta iniciar sesión o renovar token?**  
   - **Respuesta:** `AuthService` verifica `Boolean.TRUE.equals(cuenta.getEstado())` tanto en `login()` como en `renovarToken()`. Si el administrador desactivó la cuenta, el backend rechaza el acceso con `403 Forbidden` y no emite nuevos tokens.

---

## Punto 5 — `IE3.3.5`: Gestión Segura de Sesiones en Frontend

### Qué decir exactamente frente al profesor
> *"En `gamehub-v2`, la gestión de sesiones está centralizada en `src/services/authStorage.js`, `src/services/api.js` y `src/hooks/useGameHub.js`. Al iniciar sesión guardamos el JWT y los datos de sesión en `localStorage`, lo que permite que la sesión persista limpiamente al recargar la página (`F5`) mediante `leerSesion()`.  
> Sin embargo, **nunca confiamos ciegamente en los datos guardados**: cada vez que `obtenerToken()` o `leerSesion()` lee el token, decodifica el payload Base64URL, verifica el claim `exp` (`tokenEstaExpirado`), y prioriza el rol y el correo extraídos directamente de los claims del JWT. Si el token expiró, limpia la sesión automáticamente.  
> Además implementamos **renovación silenciosa proactiva**: un `useEffect` en `useGameHub.js` calcula el tiempo restante del JWT y llama automáticamente a `POST /api/v1/auth/renovar` antes de vencer. Si el backend alguna vez responde `401`, `api.js` dispara el evento global `AUTH_EXPIRED_EVENT`, limpiando sesión, estados sensibles y redirigiendo a `#perfil`. Por último, el carrito está **estrictamente aislado por usuario** (`gamehubCart_<id>` y `gamehubCart_guest`) y se limpia al cerrar sesión para que nunca se mezclen carritos entre cuentas o roles."*

### Qué mostrar en vivo
1. **VS Code:** Abre `src/services/authStorage.js` (`decodificarJwt`, `tokenEstaExpirado`, `obtenerToken`, `leerSesion`, `notificarSesionExpirada`) y `src/hooks/useGameHub.js` (líneas `217-288`: listener `AUTH_EXPIRED_EVENT` y temporizador de renovación proactiva con `authService.renovar()`).
2. **Navegador (`http://localhost:5173`):**
   - Estando logueado como `cliente@gmail.com`, presiona **`F5`** → demuestra que la sesión y el carrito persisten.
   - Abre DevTools → **Application → Local Storage** → muestra `gamehubToken`, `gamehubUsuario` y `gamehubCart_<id>`.
   - Cierra sesión e inicia como `admin@gamehub.com` → demuestra que el carrito del cliente **no aparece** en la sesión del administrador.

### 3 preguntas difíciles del profesor (y su respuesta exacta)
1. **¿Qué pasa si un usuario edita `gamehubUsuario` en `localStorage` desde DevTools y se cambia el rol a `ADMIN`?**  
   - **Respuesta:** Hay doble barrera: primero, en el frontend `leerSesion()` lee el rol prioritariamente desde los claims del JWT (`claims.roles`), ignorando la manipulación de `gamehubUsuario`. Segundo, aunque alguien fabricara un JWT falso en el navegador, la firma `HS256` fallaría en `gateway-mscv` y el servidor rechazaría toda petición protegida con `401 Unauthorized` o `403 Forbidden`.
2. **¿Por qué implementaron un evento personalizado (`AUTH_EXPIRED_EVENT`) entre `api.js` y `useGameHub.js`?**  
   - **Respuesta:** `api.js` es un módulo utilitario fuera del árbol de React y no puede llamar `setState` directamente. Al despachar `window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT))` cuando llega un `401`, el hook `useGameHub` reacciona de inmediato limpiando usuario, carrito, órdenes y redirigiendo a `#perfil` con un mensaje claro.
3. **¿Qué sucede con los productos que un invitado agregó al carrito antes de iniciar sesión?**  
   - **Respuesta:** Se guardan temporalmente bajo `gamehubCart_guest`. Si inicia sesión como `CLIENTE`, `fusionarCarritos()` combina esos ítems con su carrito personal (`gamehubCart_<id>`) y elimina la clave de invitado. Si inicia sesión como `ADMIN` o `VENDEDOR`, esos roles no heredan el carrito y su estado en memoria queda vacío.

---

## Punto 6 — `IE3.3.6`: Restricciones de Acceso e Interfaces por Rol en Frontend

### Qué decir exactamente frente al profesor
> *"Las restricciones de acceso en `gamehub-v2` combinan tres mecanismos complementarios definidos de forma centralizada en `src/logica/roles.js`:  
> 1. **Ocultamiento real del DOM en navegación (`Header.jsx`):** `obtenerEnlacesNavegacion(rol)` devuelve únicamente los enlaces permitidos para el rol activo. Un invitado o cliente **no ve en el DOM** los enlaces *Administración* ni *Órdenes*; un `VENDEDOR` solo ve *Productos*, *Órdenes* y *Perfil*; y un `ADMIN` ve *Órdenes* y *Administración* sin *Carrito*.  
> 2. **Protección de rutas (`ProtectedRoute.jsx` + `VistaActual.jsx` + `hashchange`):** Si un usuario no autorizado escribe manualmente `#admin` o `#ordenes` en la barra de direcciones, `ProtectedRoute` retorna `null` (no renderiza el componente protegido) y redirige automáticamente a la ruta permitida por defecto (`rutaPorDefecto(rol)`).  
> 3. **Restricción granular de acciones por rol:** En `OrdenesPanel.jsx`, el rol `VENDEDOR` accede en modo estrictamente solo lectura (`puedeGestionar={false}`), ocultando botones de cambio de estado, simulación de pago y cancelación, mientras que `ADMIN` dispone de todas las acciones. Y en compras, solo `CLIENTE` puede ejecutar `finalizarCompra`."*

### Qué mostrar en vivo
1. **VS Code:** Abre `src/logica/roles.js` (`RUTAS_POR_ROL`, `obtenerEnlacesNavegacion`), `src/components/ProtectedRoute/ProtectedRoute.jsx` y `src/components/Header/Header.jsx`.
2. **Demostración en vivo de los 3 roles en `http://localhost:5173`:**
   - **Invitado / `CLIENTE`:** Muestra que no existen *Órdenes* ni *Administración*; escribe `#admin` en la URL → rebota automáticamente a `#catalogo` / `#inicio`.
   - **`VENDEDOR` (`vendedor@gamehub.com`):** Entra a `#ordenes` → muestra el listado y detalle en **solo lectura** sin botones de editar/cancelar ni acceso a `#admin`.
   - **`ADMIN` (`admin@gamehub.com`):** Entra a `#admin` → muestra las 4 pestañas operativas (*Productos*, *Categorías*, *Órdenes*, *Usuarios*) y que no tiene *Carrito*.

### 3 preguntas difíciles del profesor (y su respuesta exacta)
1. **¿Basta con ocultar los botones en React para que la aplicación sea segura?**  
   - **Respuesta:** No. El control en el frontend (`Header`, `ProtectedRoute`, `roles.js`) mejora la experiencia de usuario y evita exponer vistas innecesarias, pero la seguridad real está garantizada en el backend: `gateway-mscv/SecurityConfig.java` valida el JWT y los roles en **cada petición HTTP**, por lo que un atacante usando Postman o `curl` igualmente recibe `401` o `403`.
2. **¿Por qué usan `ProtectedRoute` retornando `null` además de validar `hashchange` en `useGameHub.js`?**  
   - **Respuesta:** Es defensa en profundidad en la UI: `useGameHub.js` deriva `pagina` filtrando con `rutaPermitida(paginaEstado, rol)` y escucha `hashchange`, mientras que `ProtectedRoute` garantiza a nivel de componente que si el estado de rol cambia (por ejemplo, al expirar sesión), jamás se produzca un parpadeo renderizando contenido administrativo en el DOM.
3. **¿Cómo evitan que `ADMIN` o `VENDEDOR` agreguen productos al carrito o creen una compra?**  
   - **Respuesta:** Con `puedeComprarConRol(rol)` en `roles.js`, que retorna `true` únicamente para `CLIENTE` o invitado. `VistaActual` pasa `soloLectura={!puedeComprar}` a `Catalogo` y `Detalle` (ocultando el botón *Agregar*), bloquea `#carrito` y `#checkout` con `ProtectedRoute`, y además `finalizarCompra()` valida explícitamente `esCliente(rol)` antes de llamar a `POST /api/v1/orders`.
