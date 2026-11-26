# Manual de Usuario — "GameHub Store"

**Versión del Documento:** 1.0  
**Fecha de Creación:** 26/11/2026  
**Asignatura:** DSY1104 — Desarrollo Fullstack II (Anexo 4 — Evaluación Parcial 3)  

---

> **(Borrar esta hoja antes de la entrega final)**  
> **Evidencia de la documentación:**  
> Cada imagen añadida a este documento debe documentarse estrictamente en el siguiente orden:  
> 1. **Número figura** (negrita, ej. **Figura 1**)  
> 2. **Título figura** (cursiva, ej. *Formulario de inicio de sesión en GameHub Store*)  
> 3. **Imagen** (con **borde de 1px, color negro**)  
> 4. **Nota con detalles** (cursiva debajo de la imagen, indicando ruta y comportamiento mostrado).

---

## Introducción

### Propósito del documento
Este manual tiene como propósito guiar paso a paso a los usuarios finales (**Clientes**, **Vendedores** y **Administradores**) en la instalación, configuración inicial y operación diaria de la plataforma **GameHub Store**. Cubre desde el acceso inicial con autenticación segura JWT hasta la compra completa con cupones de descuento, la simulación de pasarela de pago y la administración integral del catálogo y las órdenes.

### Descripción del sistema
**GameHub Store** es una tienda online de equipamiento gamer (consolas, periféricos, monitores y hardware de alto rendimiento) construida sobre una arquitectura distribuida de microservicios Spring Boot con persistencia híbrida (**PostgreSQL + Firebase Cloud Firestore**) y una aplicación web moderna desarrollada en React (`gamehub-v2`).

El sistema gestiona:
- Navegación pública del catálogo con stock en tiempo real y filtros por categoría o palabra clave.
- Registro e inicio de sesión protegido mediante **JSON Web Tokens (JWT)** con control de acceso por roles (`CLIENTE`, `VENDEDOR`, `ADMIN`).
- Carrito de compras aislado por sesión de usuario, aplicación de códigos de descuento (`GAMER10`, `GAMEHUB5000`), creación de boletas (`PENDIENTE_PAGO`) y simulación de pago (`PAGADA`) sincronizada automáticamente en Cloud Firestore.
- Panel de consulta de órdenes para vendedores (solo lectura) y panel administrativo completo para gestión de productos, categorías, órdenes y usuarios.

### Cómo usar este manual
- Si necesitas levantar el entorno local por primera vez, revisa **Requisitos del Sistema** e **Instalación y Configuración**.
- Si el sistema ya se encuentra en ejecución, dirígete directamente a **Comenzando** y **Uso del Software**, seleccionando los apartados correspondientes a tu rol (`CLIENTE`, `VENDEDOR` o `ADMIN`).

---

## Requisitos del Sistema

### Hardware necesario
- **Procesador:** Intel Core i5 / AMD Ryzen 5 (4 núcleos o superior recomendado para ejecutar los microservicios en paralelo).
- **Memoria RAM:** Mínimo 8 GB (16 GB recomendados para ejecutar IntelliJ IDEA, PostgreSQL y Node.js simultáneamente).
- **Espacio en disco:** Al menos 4 GB libres para dependencias Maven, paquetes `node_modules` y bases de datos PostgreSQL.
- **Conectividad:** Acceso estable a internet para sincronización con Firebase Cloud Firestore (`gamehub-db07b`).

### Software necesario
- **Sistema Operativo:** Windows 10/11, macOS o Linux.
- **Java Development Kit (JDK):** Java 21 (ej. Microsoft OpenJDK 21).
- **Gestor de Base de Datos:** PostgreSQL 15 o superior en ejecución en `localhost:5432`.
- **IDE recomendado (Backend):** IntelliJ IDEA con soporte Maven y Lombok habilitado.
- **Entorno Frontend:** Node.js 20+ y npm 10+.
- **Navegador Web:** Google Chrome, Microsoft Edge o Mozilla Firefox actualizado.
- **Credenciales Cloud:** Archivo `firebase-service-account.json` ubicado en la raíz del backend (`GameHub-Store001-main/`).

---

## Instalación y Configuración

### Instrucciones de instalación paso a paso

1. **Preparar las bases de datos en PostgreSQL:**  
   Ejecutar el script `sql/create_databases.sql` en PostgreSQL para crear las bases de datos independientes de cada microservicio (`auth_service_db`, `user_service_db`, `category_service_db`, `product_service_db`, `inventory_service_db`, `promotion_service_db`, `order_service_db`, `payment_service_db`, etc.).
2. **Configurar variables de entorno y credenciales Firebase:**  
   En la raíz del backend (`GameHub-Store001-main/`), verificar la existencia del archivo `.env` (con credenciales locales `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y `FIREBASE_CREDENTIALS_PATH`) y del archivo `firebase-service-account.json`.
3. **Levantar los microservicios en IntelliJ IDEA (en orden):**
   - Primero iniciar **`eureka-mscv`** (puerto `8761`) y verificar en `http://localhost:8761` que el panel de Eureka esté activo.
   - Luego iniciar los microservicios de dominio: **`authentificator-mscv`** (`8007`), **`user-mscv`** (`8002`), **`category-mscv`** (`8088`), **`product-mscv`** (`8001`), **`inventory-mscv`** (`8003`), **`promotion-mscv`** (`8009`), **`order-mscv`** (`8006`), **`payment-mscv`** (`8010`), **`shipping-mscv`** (`8005`), **`review-mscv`** (`8012`), **`warranty-mscv`** (`8004`) y **`notification-mscv`** (`8013`).
   - Finalmente iniciar **`gateway-mscv`** (puerto `8091`), que centraliza la API REST y la documentación Swagger UI en `http://localhost:8091/docs/webjars/swagger-ui/index.html`.
4. **Instalar y levantar el Frontend (`gamehub-v2`):**  
   Abrir una terminal en la carpeta `gamehub-v2/` y ejecutar:
   ```bash
   npm install
   npm run dev
   ```
   Abrir la aplicación en el navegador en `http://localhost:5173`.

### Configuración inicial
Al arrancar por primera vez, los componentes `DataLoader` del backend precargan automáticamente datos iniciales en PostgreSQL y los sincronizan con Firebase Cloud Firestore:

- **Cuentas de prueba precargadas (`authentificator-mscv`):**
  - **Administrador (`ADMIN`):** `admin@gamehub.com` / `admin123`
  - **Vendedor (`VENDEDOR`):** `vendedor@gamehub.com` / `vendedor123`
  - **Cliente (`CLIENTE`):** `cliente@gmail.com` / `cliente123`
- **Catálogo semilla:** 4 categorías (`Consolas`, `Periféricos`, `Monitores`, `Hardware`) y 6 productos gamer con stock inicial precargado.
- **Cupones de descuento activos precargados (`promotion-mscv`):**
  - **`GAMER10`**: 10% de descuento sobre el subtotal (sin mínimo, tope \$100.000 CLP).
  - **`GAMEHUB5000`**: \$5.000 CLP de descuento fijo (en compras desde \$20.000 CLP).

---

## Comenzando

### Crear una cuenta / Iniciar sesión
Para comprar o acceder a módulos según tu rol, haz clic en **Perfil** (o **Iniciar sesión**) en la barra de navegación superior (`http://localhost:5173/#perfil`).

- **Iniciar sesión:** Ingresa tu correo electrónico y contraseña en el formulario de acceso y presiona **Iniciar sesión**. El sistema obtiene un JWT y redirige automáticamente según tu rol (`#admin` para Administrador, `#catalogo` para Cliente/Vendedor).
- **Crear cuenta nueva:** Si eres un usuario nuevo, haz clic en **Crear cuenta nueva** (`http://localhost:5173/#registro`), completa nombre, apellido, RUT, teléfono, correo y contraseña (mínimo 6 caracteres y confirmación) y presiona **Registrarse**. Tu cuenta se creará automáticamente con rol `CLIENTE`.

**Figura 1**  
*Formulario de inicio de sesión con autenticación JWT en GameHub Store*  
> **[INSERTAR CAPTURA FIGURA 1 — Borde 1px color negro: Vista `#perfil` sin sesión iniciada mostrando el formulario "Iniciar sesión" (campos Correo electrónico, Contraseña, botón "Iniciar sesión" y botón "Crear cuenta nueva")]**  

*Nota: Pantalla de autenticación inicial de GameHub Store donde el usuario ingresa sus credenciales para obtener su token JWT desde `/api/v1/auth/login`. Ruta `http://localhost:5173/#perfil`.*

**Figura 2**  
*Formulario de registro de nueva cuenta de cliente*  
> **[INSERTAR CAPTURA FIGURA 2 — Borde 1px color negro: Vista `#registro` mostrando el formulario de creación de cuenta con los campos de datos personales, correo y contraseña]**  

*Nota: Al completar el formulario se registra la cuenta en `authentificator-mscv` (`ROLE_CLIENTE`), se crea el perfil comprador en `user-mscv` y se inicia sesión automáticamente. Ruta `http://localhost:5173/#registro`.*

### Interfaz de usuario inicial
Al acceder a `http://localhost:5173/#inicio`, el usuario visualiza tres áreas principales:
1. **Barra de navegación superior (Header):** Logo de GameHub Store, accesos dinámicos según rol (Inicio, Catálogo, Ofertas, Contacto, Carrito con contador de ítems, Perfil y, únicamente para roles autorizados, Órdenes y Administración).
2. **Sección principal (Hero y Productos Destacados):** Presentación de la tienda, acceso directo al catálogo y tarjetas de equipamiento destacado con precio, oferta y botón de detalle/compra.
3. **Pie de página (Footer):** Información institucional y enlaces rápidos de navegación.

**Figura 3**  
*Interfaz principal de inicio de GameHub Store*  
> **[INSERTAR CAPTURA FIGURA 3 — Borde 1px color negro: Pantalla principal `#inicio` mostrando la barra de navegación superior, sección Hero y grilla de productos destacados]**  

*Nota: Vista de bienvenida pública del sistema; los enlaces de Administración y Órdenes permanecen ocultos para usuarios invitados o clientes. Ruta `http://localhost:5173/#inicio`.*

---

## Uso del Software

### 1. Catálogo, búsqueda y detalle de producto
- **Descripción textual:** Desde **Catálogo** (`#catalogo`) el usuario puede explorar todos los productos activos, buscar por nombre en tiempo real y filtrar por categoría (`Consolas`, `Periféricos`, `Monitores`, `Hardware`). Al seleccionar un producto se muestra su ficha detallada (`#detalle`) con marca, modelo, garantía, stock disponible y precio de oferta si aplica. Los clientes pueden presionar **Agregar al carrito**.
- **Navegación:** Menú superior → **Catálogo** → barra de búsqueda / selector de categoría → clic en tarjeta para **Ver detalle**.

**Figura 4**  
*Catálogo gamer con filtros de búsqueda, categoría y stock en tiempo real*  
> **[INSERTAR CAPTURA FIGURA 4 — Borde 1px color negro: Vista `#catalogo` mostrando la barra de búsqueda, selector de categoría y tarjetas de productos con precios y botón "Agregar"]**  

*Nota: Los productos y categorías se cargan en vivo desde `product-mscv`, `category-mscv` e `inventory-mscv` a través del Gateway. Ruta `http://localhost:5173/#catalogo`.*

### 2. Carrito aislado por sesión y aplicación de códigos de descuento
- **Descripción textual:** El botón **Carrito** (`#carrito`) muestra los productos seleccionados por la cuenta activa (aislados por usuario en `localStorage`). Desde allí es posible incrementar o disminuir cantidades respetando el stock disponible, eliminar artículos o vaciar el carrito. Al presionar **Continuar con la compra** se accede a `#checkout`, donde el cliente completa sus datos de envío e ingresa un cupón promocional (`GAMER10` o `GAMEHUB5000`). Al presionar **Validar cupón**, el sistema consulta a `promotion-mscv`, calcula el descuento real y actualiza el total estimado antes de confirmar la compra.
- **Navegación:** Menú superior → **Carrito** → **Continuar con la compra** → ingresar código en *Código de promoción* → **Validar cupón** → **Confirmar compra**.

**Figura 5**  
*Checkout de compra con validación de código promocional GAMER10*  
> **[INSERTAR CAPTURA FIGURA 5 — Borde 1px color negro: Vista `#checkout` mostrando el resumen con "Descuento (GAMER10): -$...", el campo Código de promoción con `GAMER10`, el botón "Validar cupón" y el mensaje de confirmación del descuento aplicado]**  

*Nota: Al validar `GAMER10` o `GAMEHUB5000`, `promotion-mscv` verifica vigencia, estado activo y monto mínimo, reflejando el descuento en tiempo real antes de confirmar la orden. Ruta `http://localhost:5173/#checkout`.*

### 3. Confirmación de compra y simulación de pasarela de pago (`PENDIENTE_PAGO` → `PAGADA`)
- **Descripción textual:** Al presionar **Confirmar compra**, el backend ejecuta la Saga de creación en `order-mscv`, reserva inventario y cupón, y registra la boleta en estado **`PENDIENTE_PAGO`** (tanto en PostgreSQL como en Cloud Firestore). En la pantalla de confirmación (`#resultado`), el cliente visualiza su número de pedido (`GH-<id>`), el estado `PENDIENTE_PAGO`, un selector de **Método de pago simulado** (`Tarjeta de Crédito`, `Tarjeta de Débito`, `Transferencia Bancaria`, `Webpay Simulado`) y el botón **Simular / Confirmar pago**. Al presionarlo, `payment-mscv` aprueba la transacción (`PAY-XXXXXXXX`) y actualiza automáticamente la orden a **`PAGADA`** en pantalla y en Firebase Cloud Firestore (`boletas`).
- **Navegación:** `#checkout` → **Confirmar compra** → `#resultado` → elegir método de pago → **Simular / Confirmar pago**.

**Figura 6**  
*Confirmación de boleta y simulación exitosa de pago en estado PAGADA*  
> **[INSERTAR CAPTURA FIGURA 6 — Borde 1px color negro: Vista `#resultado` mostrando la orden aprobada con el título "✓ Pago aprobado y orden confirmada", pill de estado "PAGADA", comprobante "PAY-...", descuento aplicado y total pagado]**  

*Nota: Al presionar "Simular / Confirmar pago", `payment-mscv` registra el pago `APROBADO` en `/api/v1/payments/simular` y notifica a `order-mscv`, actualizando el estado a `PAGADA` en PostgreSQL y en la colección `boletas` de Cloud Firestore. Ruta `http://localhost:5173/#resultado`.*

### 4. Perfil del cliente e historial de compras
- **Descripción textual:** Desde **Perfil** (`#perfil`), el cliente autenticado visualiza sus datos, su rol asignado y la tabla **Mis compras recientes** con el número de orden, fecha, estado (`PENDIENTE_PAGO` o `PAGADA`) y total. Si alguna orden quedó pendiente de pago, dispone del botón **Simular pago** directamente en la fila para completarla. También cuenta con el botón **Cerrar sesión**, que limpia el token JWT y el carrito en memoria.
- **Navegación:** Menú superior → **Perfil** → tabla **Mis compras recientes** → botón **Simular pago** (en órdenes pendientes) o **Cerrar sesión**.

**Figura 7**  
*Perfil de cliente con historial de compras y opción de pago para órdenes pendientes*  
> **[INSERTAR CAPTURA FIGURA 7 — Borde 1px color negro: Vista `#perfil` autenticado como `CLIENTE` mostrando nombre, correo, pill "Rol: Cliente", tabla "Mis compras recientes" con estados PAGADA/PENDIENTE_PAGO y botón "Cerrar sesión"]**  

*Nota: El cliente puede revisar sus boletas históricas consultadas desde `/api/v1/orders/user/{userId}` y regularizar cualquier orden pendiente de pago. Ruta `http://localhost:5173/#perfil`.*

### 5. Módulo de Órdenes para Vendedor (Solo lectura)
- **Descripción textual:** Al iniciar sesión con `vendedor@gamehub.com`, el sistema habilita el menú **Órdenes** (`#ordenes`) en modo estrictamente de **solo lectura**. El vendedor puede filtrar órdenes por estado (`TODAS`, `PENDIENTE_PAGO`, `PAGADA`, `EN_DESPACHO`, `ENTREGADA`, `CANCELADA`) y abrir **Ver detalle** para revisar los productos, subtotal, descuento y total, sin acceso a botones de modificación, cancelación ni al panel administrativo.
- **Navegación:** Menú superior → **Órdenes** → selector de filtro por estado → botón **Ver detalle**.

**Figura 8**  
*Panel de consulta de órdenes en modo solo lectura para rol Vendedor*  
> **[INSERTAR CAPTURA FIGURA 8 — Borde 1px color negro: Vista `#ordenes` autenticado como `vendedor@gamehub.com` mostrando el listado de órdenes, el mensaje de solo lectura y la ausencia de botones de eliminación o enlace Administración]**  

*Nota: Cumpliendo con el control RBAC, el rol `VENDEDOR` únicamente visualiza órdenes y catálogo; cualquier intento de acceso a `#admin` redirige automáticamente. Ruta `http://localhost:5173/#ordenes`.*

### 6. Panel de Administración (`ADMIN`)
- **Descripción textual:** Al iniciar sesión con `admin@gamehub.com`, el sistema redirige automáticamente al **Panel de Administración** (`#admin`), organizado en cuatro pestañas operativas:
  - **Productos:** Crear, editar, activar/desactivar y eliminar productos del catálogo.
  - **Categorías:** Crear, editar y desactivar categorías.
  - **Órdenes:** Consultar todas las boletas, ver detalle, ejecutar **Simular pago (APROBADO)**, cambiar estados (`PAGADA`, `EN_DESPACHO`, `ENTREGADA`) o cancelar órdenes devolviendo stock.
  - **Usuarios y Cuentas:** Crear/editar perfiles de usuario, activar/desactivar cuentas y cambiar roles (`ROLE_ADMIN`, `ROLE_VENDEDOR`, `ROLE_CLIENTE`).
- **Navegación:** Menú superior → **Administración** → pestañas **Productos** / **Categorías** / **Órdenes** / **Usuarios**.

**Figura 9**  
*Panel administrativo de GameHub Store para gestión de catálogo, órdenes y usuarios*  
> **[INSERTAR CAPTURA FIGURA 9 — Borde 1px color negro: Vista `#admin` autenticado como `admin@gamehub.com` mostrando las pestañas Productos, Categorías, Órdenes y Usuarios junto con la tabla de gestión activa]**  

*Nota: Al iniciar sesión como administrador, el sistema redirige al panel administrativo con control completo sobre las entidades sincronizadas en PostgreSQL y Firebase Cloud Firestore. Ruta `http://localhost:5173/#admin`.*

### 7. Navegación por el software (Menús según rol)
La barra de navegación superior adapta dinámicamente los accesos visibles según el rol contenido en el JWT:
- **Invitado:** `Inicio` · `Catálogo` · `Ofertas` · `Contacto` · `Carrito` · `Perfil`
- **Cliente (`ROLE_CLIENTE`):** `Inicio` · `Catálogo` · `Ofertas` · `Contacto` · `Carrito` · `Perfil`
- **Vendedor (`ROLE_VENDEDOR`):** `Catálogo` · `Órdenes` · `Perfil`
- **Administrador (`ROLE_ADMIN`):** `Inicio` · `Catálogo` · `Ofertas` · `Órdenes` · `Administración` · `Perfil`

**Figura 10**  
*Barra de navegación adaptativa del encabezado según rol autenticado*  
> **[INSERTAR CAPTURA FIGURA 10 — Borde 1px color negro: Encabezado superior de `gamehub-v2` mostrando los menús habilitados para el rol Administrador (Órdenes y Administración visibles, Carrito oculto)]**  

*Nota: La interfaz oculta del DOM las rutas no autorizadas y respalda la navegación mediante `ProtectedRoute` y reglas de autorización en `gateway-mscv`. Ruta `http://localhost:5173/#admin`.*
