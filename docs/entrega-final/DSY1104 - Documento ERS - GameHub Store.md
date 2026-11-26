# Especificación de Requisitos de Software (ERS) — GameHub Store

**Asignatura:** DSY1104 — Desarrollo Fullstack II (Evaluación Parcial 3)  
**Institución:** Duoc UC  
**Sistema:** GameHub Store — Plataforma E-Commerce Gamer basada en Microservicios  
**Estándar:** IEEE Std 830-1998  
**Versión del Documento:** 1.0  
**Fecha de Creación:** 26/11/2026  

---

## 1. Introducción

### 1.1 Propósito
El presente documento establece la **Especificación de Requisitos de Software (ERS)** bajo el estándar **IEEE 830** para la plataforma **GameHub Store**. Su finalidad es definir de manera completa, trazable y verificable los requisitos funcionales, no funcionales, arquitectura de integración, reglas de negocio y controles de seguridad que rigen el sistema compuesto por el cliente web React (`gamehub-v2`) y el ecosistema distribuido de microservicios Spring Boot (`GameHub-Store001-main`).

Está dirigido a docentes evaluadores, arquitectos de software, desarrolladores full-stack y administradores de plataforma responsables de auditar, mantener y evolucionar la solución.

### 1.2 Alcance del Sistema
**GameHub Store** es una aplicación web de comercio electrónico especializada en consolas, hardware, monitores y periféricos gamer. El sistema abarca:
- **Frontend SPA (`gamehub-v2`):** Construido en React + Vite (`http://localhost:5173`), conectado exclusivamente al API Gateway (`http://localhost:8091/api/v1`) con manejo de sesiones JWT, renovación proactiva de token, protección de rutas por rol y aislamiento de carrito por usuario.
- **Infraestructura de Microservicios:** Service Discovery con **Netflix Eureka** (`http://localhost:8761`), enrutamiento y seguridad perimetral en **Spring Cloud Gateway** (`http://localhost:8091`) y **12 microservicios de dominio** en Spring Boot 3.3.2 / Java 21.
- **Persistencia Híbrida (SQL + NoSQL):**
  - **PostgreSQL** como base de datos transaccional aislada por microservicio (patrón *Database per Service*).
  - **Firebase Cloud Firestore** (proyecto `gamehub-db07b`) como repositorio NoSQL en la nube sincronizado en tiempo real con las 5 colecciones del modelo de negocio: `usuarios`, `categorias`, `productos`, `boletas` y `detalle_boleta`.
- **Orquestación Saga de Compras:** Validación de carrito, reserva de stock (`inventory-mscv`), validación/reserva de códigos promocionales (`promotion-mscv`: `GAMER10`, `GAMEHUB5000`), creación de boleta en estado `PENDIENTE_PAGO` (`order-mscv`) y simulación de pasarela de pago (`payment-mscv`) que transiciona la orden a `PAGADA` actualizando PostgreSQL y Firestore.

### 1.3 Definiciones, Acrónimos y Abreviaturas

| Término / Acrónimo | Definición |
| :--- | :--- |
| **API** | *Application Programming Interface*. Interfaz de programación que expone recursos de negocio mediante HTTP/JSON. |
| **BCrypt** | Función de derivación de clave adaptativa usada para almacenar hashes seguros de contraseñas en PostgreSQL. |
| **CORS** | *Cross-Origin Resource Sharing*. Política de cabeceras HTTP configurada en el Gateway para permitir peticiones desde `localhost:5173`. |
| **ERS** | Especificación de Requisitos de Software (IEEE 830). |
| **Eureka** | Servidor de registro y descubrimiento de microservicios (*Service Discovery*) de Spring Cloud Netflix (`8761`). |
| **Firestore** | Firebase Cloud Firestore, base de datos documental NoSQL en la nube utilizada para sincronización del modelo relacional. |
| **HATEOAS** | *Hypermedia as the Engine of Application State*. Enriquecimiento de respuestas REST v2 con enlaces navegables `_links`. |
| **JWT** | *JSON Web Token*. Token firmado con HMAC-SHA256 (`HS256`) emitido por `authentificator-mscv` y validado en `gateway-mscv`. |
| **OpenAPI / Swagger** | Especificación estándar OpenAPI 3.0 agregada en el Gateway (`http://localhost:8091/docs/webjars/swagger-ui/index.html`). |
| **RBAC** | *Role-Based Access Control*. Control de acceso basado en roles (`ROLE_ADMIN`, `ROLE_VENDEDOR`, `ROLE_CLIENTE`). |
| **REST** | *Representational State Transfer*. Estilo arquitectónico basado en verbos HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`). |
| **Saga** | Patrón de transacciones distribuidas con reserva, confirmación y compensación automática ante fallos entre microservicios. |

### 1.4 Visión General del Documento
La **Sección 2** describe la arquitectura distribuida, los actores/roles, el modelo de datos y las restricciones del entorno. La **Sección 3** detalla los 15 Requisitos Funcionales (`RF-01` a `RF-15`), los Requisitos No Funcionales (`RNF-01` a `RNF-08`) y la Matriz de Trazabilidad Rol vs. Endpoints/Vistas. Este documento se complementa directamente con el **Anexo 2** (Documentación APIs e Integración), **Anexo 3** (Catálogo tabular Excel de APIs) y **Anexo 4** (Manual de Usuario).

---

## 2. Descripción General

### 2.1 Perspectiva del Producto (Arquitectura Distribuida)
GameHub Store sigue una arquitectura de microservicios desacoplados con entrada única a través del **API Gateway**:

| Componente / Microservicio | Puerto | Base de Datos PostgreSQL | Colección Firestore | Responsabilidad Principal |
| :--- | :---: | :--- | :--- | :--- |
| `gamehub-v2` (Frontend React) | `5173` | — | — | Interfaz web responsiva, consumo REST con JWT y control de vistas por rol |
| `eureka-mscv` | `8761` | — | — | Registro y descubrimiento dinámico de microservicios |
| `gateway-mscv` | `8091` | — | — | Enrutamiento, CORS, validación JWT (`OAuth2 Resource Server`), RBAC y Swagger agregado |
| `authentificator-mscv` | `8007` | `auth_service_db` | — | Registro, login, emisión/renovación JWT (`/renovar`, `/refresh`) y cuentas de acceso |
| `user-mscv` | `8002` | `user_service_db` | `usuarios` | Perfiles de usuarios (RUT, correo, teléfono) y direcciones |
| `category-mscv` | `8088` | `category_service_db` | `categorias` | Administración de categorías de productos (API v1 y v2 HATEOAS) |
| `product-mscv` | `8001` | `product_service_db` | `productos` | Catálogo de productos, precios, ofertas y estado (API v1 y v2 HATEOAS) |
| `inventory-mscv` | `8003` | `inventory_service_db` | — | Stock disponible, movimientos y reservas/confirmaciones Saga |
| `promotion-mscv` | `8009` | `promotion_service_db` | — | Cupones de descuento (`GAMER10`, `GAMEHUB5000`), validación y reserva Saga |
| `order-mscv` | `8006` | `order_service_db` | `boletas`, `detalle_boleta` | Validación de carrito, creación de órdenes Saga y ciclo de vida de boletas |
| `payment-mscv` | `8010` | `payment_service_db` | — | Registro y simulación de pasarela de pagos (`APROBADO` → `PAGADA`) |
| `shipping-mscv` | `8005` | `shipping_service_db` | — | Despachos, tracking público y estados de entrega |
| `review-mscv` | `8012` | `review_service_db` | — | Reseñas de productos y moderación |
| `warranty-mscv` | `8004` | `warranty_service_db` | — | Solicitudes de garantía, diagnóstico y resolución |
| `notification-mscv` | `8013` | `notification_service_db` | — | Notificaciones operacionales a clientes |

### 2.2 Modelo de Entidades (Persistencia Híbrida PostgreSQL + Cloud Firestore)
El núcleo transaccional y analítico del negocio modela exactamente las 5 entidades exigidas y sus cardinalidades:
- **`USUARIO`** (`1`) $\longrightarrow$ (`N`) **`BOLETA`** (clave foránea lógica `idUsuario`)
- **`BOLETA`** (`1`) $\longrightarrow$ (`N`) **`DETALLE_BOLETA`** (clave foránea lógica `idBoleta`)
- **`CATEGORIA`** (`1`) $\longrightarrow$ (`N`) **`PRODUCTO`** (clave foránea lógica `idCategoria`)
- **`PRODUCTO`** (`1`) $\longrightarrow$ (`N`) **`DETALLE_BOLETA`** (clave foránea lógica `idProducto`)

### 2.3 Funciones Principales por Rol y Características de los Usuarios

| Rol | Perfil de Usuario | Funciones Habilitadas | Restricciones Clave |
| :--- | :--- | :--- | :--- |
| **Invitado (Sin sesión)** | Visitante anónimo | Explorar `#inicio`, `#catalogo`, `#ofertas`, `#detalle`, `#contacto`; agregar ítems a carrito de invitado; registrarse o iniciar sesión | No ve `#admin` ni `#ordenes`; debe autenticarse como `CLIENTE` para confirmar compras |
| **`ROLE_CLIENTE`** | Comprador registrado | Todo lo de invitado + gestión de carrito aislado por cuenta, validación de cupones (`GAMER10`, `GAMEHUB5000`), creación de boleta (`POST /api/v1/orders`), simulación de pago (`POST /api/v1/payments/simular`) y consulta de su historial en `#perfil` | Sin acceso a `#admin`, `#ordenes` globales ni mutaciones de productos/categorías (`403 Forbidden`) |
| **`ROLE_VENDEDOR`** | Personal de soporte/ventas | Consulta de catálogo en modo solo lectura y visualización completa del listado y detalle de órdenes en `#ordenes` | No puede comprar, no tiene carrito, no puede mutar estados de órdenes ni acceder a `#admin` (`403 Forbidden`) |
| **`ROLE_ADMIN`** | Administrador del sistema | Acceso total a `#admin` y `#ordenes`: CRUD de productos, categorías, usuarios y cuentas/roles; cambio de estado de órdenes, simulación/aprobación de pagos y cancelaciones | Separación operacional: no realiza compras desde carrito de cliente |

### 2.4 Restricciones Técnicas, Suposiciones y Dependencias
- **Restricciones:** Backend desarrollado en Java 21 + Spring Boot 3.3.2 + Spring Cloud 2023.0.3; Frontend en React 19 + Vite 8 manteniendo intactas las hojas de estilo `.css` originales de `gamehub-v2`; secretos gestionados fuera del control de versiones (`.env` y `firebase-service-account.json` excluidos en `.gitignore`).
- **Suposiciones y Dependencias:** Instancia local de PostgreSQL 15+ en `localhost:5432`, conectividad HTTPS saliente hacia Google Cloud Firestore (`gamehub-db07b`), archivo de cuenta de servicio Firebase válido y orden de arranque iniciando por `eureka-mscv`.

---

## 3. Requisitos Específicos

### 3.1 Requisitos Funcionales (RF-01 a RF-15)

| ID | Nombre del Requisito | Descripción Detallada | Componentes Involucrados |
| :--- | :--- | :--- | :--- |
| **RF-01** | Registro de Usuarios | El sistema debe permitir registrar nuevas cuentas con correo único y contraseña (mínimo 6 caracteres) asignando rol `ROLE_CLIENTE`, encriptando la clave con BCrypt y emitiendo un JWT inmediato. | `authentificator-mscv`, `user-mscv`, `Registro.jsx` |
| **RF-02** | Inicio de Sesión JWT | El sistema debe autenticar credenciales en `POST /api/v1/auth/login`, verificar que la cuenta esté activa y retornar un token JWT firmado (`HS256`) junto al rol, correo y expiración. | `authentificator-mscv`, `Perfil.jsx`, `authStorage.js` |
| **RF-03** | Renovación Proactiva de Token | El sistema debe exponer `POST /api/v1/auth/renovar` (y alias `/refresh`) para emitir un nuevo JWT antes de su expiración sin interrumpir la sesión del usuario. | `authentificator-mscv`, `gateway-mscv`, `useGameHub.js` |
| **RF-04** | Control de Acceso RBAC (Backend + Frontend) | El API Gateway debe autorizar cada endpoint según los roles del JWT (`401` sin token, `403` sin privilegios) y el frontend debe ocultar elementos no permitidos y proteger rutas con `ProtectedRoute`. | `gateway-mscv/SecurityConfig`, `ProtectedRoute.jsx`, `Header.jsx`, `roles.js` |
| **RF-05** | Catálogo Público, Filtros y Stock Real | El sistema debe listar productos activos con nombre, categoría, precio, oferta, imagen y stock consolidado desde inventario, permitiendo filtrar por texto y categoría y ver detalle completo. | `product-mscv`, `category-mscv`, `inventory-mscv`, `Catalogo.jsx`, `Detalle.jsx` |
| **RF-06** | Gestión CRUD de Categorías (v1 y v2 HATEOAS) | El administrador debe poder crear, listar, actualizar y desactivar categorías en `/api/v1/categories` y `/api/v2/categories`, precargando 4 categorías gamer iniciales (`Consolas`, `Periféricos`, `Monitores`, `Hardware`). | `category-mscv`, `Admin.jsx` |
| **RF-07** | Gestión CRUD de Productos (v1 y v2 HATEOAS) | El administrador debe poder crear, editar, activar/desactivar y eliminar productos (incluyendo ofertas e imagen), precargando 6 productos gamer iniciales. | `product-mscv`, `Admin.jsx` |
| **RF-08** | Carrito de Compras Aislado por Sesión | El carrito debe persistirse por identificador de usuario (`gamehubCart_<id>` / `gamehubCart_guest`), fusionar ítems del invitado solo al iniciar sesión como `CLIENTE` y limpiarse al cerrar sesión para evitar fuga entre cuentas. | `useGameHub.js`, `Carrito.jsx` |
| **RF-09** | Validación y Precarga de Códigos Promocionales | El sistema debe precargar los cupones vigentes `GAMER10` (10% de descuento) y `GAMEHUB5000` (\$5.000 CLP desde \$20.000), calcular el descuento real en `GET`/`POST /api/v1/promotions/validar` y rechazar cupones inexistentes, inactivos o expirados con mensaje descriptivo. | `promotion-mscv`, `Orden.jsx`, `ordenes.js` |
| **RF-10** | Validación de Carrito y Creación de Boleta (Saga) | Al confirmar compra (`POST /api/v1/orders`), `order-mscv` debe validar usuario, precios vigentes/oferta, stock y cupón; reservar inventario y cupón; calcular `descuento` y `total = subtotal - descuento`; y registrar la boleta en estado `PENDIENTE_PAGO` con compensación automática ante fallos. | `order-mscv`, `inventory-mscv`, `promotion-mscv`, `user-mscv`, `Orden.jsx` |
| **RF-11** | Simulación y Validación de Pasarela de Pago | El sistema debe permitir a un `CLIENTE` o `ADMIN` simular el pago de una orden en `PENDIENTE_PAGO` mediante `POST /api/v1/payments/simular` (o `POST /api/v1/payments`), generando comprobante `PAY-...`, aprobando el pago y notificando a `order-mscv` (`PUT /api/v1/orders/{id}/estado?estado=PAGADA`). | `payment-mscv`, `order-mscv`, `Orden.jsx`, `Perfil.jsx`, `OrdenesPanel.jsx` |
| **RF-12** | Sincronización Híbrida con Firebase Cloud Firestore | Al iniciar los microservicios (`DataLoader`) y tras cada operación de creación, actualización o cambio de estado (`PENDIENTE_PAGO` → `PAGADA`), el sistema debe sincronizar automáticamente las colecciones `usuarios`, `categorias`, `productos`, `boletas` y `detalle_boleta` en Cloud Firestore. | `FirebaseConfig`, `FirestoreSyncService`, `user-mscv`, `category-mscv`, `product-mscv`, `order-mscv` |
| **RF-13** | Historial de Compras del Cliente | El usuario con rol `CLIENTE` debe visualizar en `#perfil` sus compras recientes (`GET /api/v1/orders/user/{userId}`) junto con su estado y la acción para simular el pago de órdenes pendientes. | `order-mscv`, `Perfil.jsx` |
| **RF-14** | Consulta y Gestión Administrativa de Órdenes | El rol `VENDEDOR` debe consultar listado y detalle de órdenes en modo solo lectura; el rol `ADMIN` debe poder filtrar por estado, aprobar/simular pagos, avanzar estados (`PAGADA`, `EN_DESPACHO`, `ENTREGADA`) y cancelar órdenes devolviendo stock. | `order-mscv`, `payment-mscv`, `OrdenesPanel.jsx`, `Admin.jsx` |
| **RF-15** | Administración de Usuarios, Cuentas y Roles | El rol `ADMIN` debe gestionar perfiles en `user-mscv` y cuentas de acceso en `authentificator-mscv` (cambio de rol y activación/desactivación). | `user-mscv`, `authentificator-mscv`, `Admin.jsx` |

### 3.2 Requisitos No Funcionales (RNF)

| ID | Categoría | Especificación Verificable |
| :--- | :--- | :--- |
| **RNF-01** | Seguridad | Autenticación stateless mediante JWT firmado `HS256`, contraseñas encriptadas con BCrypt, autorización centralizada en `gateway-mscv` (`401`/`403`) y exclusión estricta de secretos (`.env`, `firebase-service-account.json`) en `.gitignore`. |
| **RNF-02** | Disponibilidad y Descubrimiento | Todos los microservicios se registran dinámicamente en **Eureka Server (`8761`)**, permitiendo balanceo de carga (`lb://`) y resolución desacoplada entre servicios Feign. |
| **RNF-03** | Interoperabilidad y Documentación | Documentación OpenAPI 3.0 estandarizada con `springdoc-openapi 2.6.0` y agregada en un portal Swagger UI único en `http://localhost:8091/docs/webjars/swagger-ui/index.html`, más soporte HATEOAS en endpoints `/api/v2/**`. |
| **RNF-04** | Tolerancia a Fallos (Persistencia NoSQL) | Si Firebase Cloud Firestore no está accesible temporalmente, las operaciones transaccionales en PostgreSQL continúan operando sin caída del servicio y registrando trazas estructuradas `[Firebase]`. |
| **RNF-05** | Consistencia Transaccional Distribuida | Las órdenes implementan el patrón **Saga** con reintentos programados (`@Scheduled`) de compensación de stock e idempotencia en reservas y confirmaciones. |
| **RNF-06** | Usabilidad y Diseño Responsivo | La interfaz `gamehub-v2` mantiene su diseño visual original sin alteración de archivos `.css`, con feedback claro de carga, validación de formularios y manejo controlado de errores HTTP. |
| **RNF-07** | Rendimiento | La validación de cupones, consultas de catálogo y simulación de pago responden en tiempos inferiores a 2 segundos en entorno local, con pool HikariCP acotado para evitar saturación de conexiones en PostgreSQL. |
| **RNF-08** | Mantenibilidad y Calidad | Código modular separado por capas (Controller, Service, Repository, Client, Config), commits atómicos bajo *Conventional Commits*, compilación verificada en Java 21 / Maven y build productivo validado con Vite + Oxlint (`0` errores). |

### 3.3 Matriz de Trazabilidad (Rol vs. Endpoints y Vistas Permitidas)

| Funcionalidad / Recurso | Vista Frontend (`gamehub-v2`) | Endpoint Gateway (`http://localhost:8091`) | Invitado | `CLIENTE` | `VENDEDOR` | `ADMIN` |
| :--- | :--- | :--- | :---: | :---: | :---: | :---: |
| Login / Registro | `#perfil`, `#registro` | `POST /api/v1/auth/login`, `/register` | Sí | Sí | Sí | Sí |
| Renovar JWT | Automático (`useGameHub`) | `POST /api/v1/auth/renovar`, `/refresh` | No (`401`) | Sí | Sí | Sí |
| Catálogo, Categorías y Stock | `#inicio`, `#catalogo`, `#detalle` | `GET /api/v1/products/**`, `/categories/**`, `/inventories/stock` | Sí | Sí | Sí | Sí |
| Validar Cupón (`GAMER10`, `GAMEHUB5000`) | `#checkout` | `GET` / `POST /api/v1/promotions/validar` | Sí | Sí | Sí | Sí |
| Carrito y Crear Orden | `#carrito`, `#checkout`, `#resultado` | `POST /api/v1/orders` | No (`401`) | Sí | No (`403`) | Sí (API) |
| Simular Pago (`PENDIENTE_PAGO` → `PAGADA`) | `#resultado`, `#perfil`, `#ordenes` | `POST /api/v1/payments/simular` | No (`401`) | Sí | No (`403`) | Sí |
| Ver Mis Órdenes | `#perfil` | `GET /api/v1/orders/user/{userId}` | No (`401`) | Sí | Sí | Sí |
| Listar / Ver Todas las Órdenes | `#ordenes` | `GET /api/v1/orders`, `/orders/{id}` | No (`401`) | No (`403`) | Sí (Lectura) | Sí |
| Cambiar Estado / Cancelar Orden | `#ordenes`, `#admin` | `PATCH`/`PUT /api/v1/orders/{id}/estado`, `DELETE .../cancelar` | No (`401`) | No (`403`) | No (`403`) | Sí |
| CRUD Productos y Categorías | `#admin` | `POST`/`PUT`/`PATCH`/`DELETE /api/v1/products/**`, `/categories/**` | No (`401`) | No (`403`) | No (`403`) | Sí |
| Gestión Usuarios y Cuentas/Roles | `#admin` | `/api/v1/users/**`, `/api/v1/cuentas/**` | No (`401`) | Propio (`/correo`) | No (`403`) | Sí |
| Documentación Swagger UI | Navegador | `GET /docs/webjars/swagger-ui/index.html` | Sí | Sí | Sí | Sí |
