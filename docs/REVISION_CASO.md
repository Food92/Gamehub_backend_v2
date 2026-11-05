# Revisión frente al Caso Semestral GameHub Store

La pauta del PDF describe el trabajo evaluado (Parcial 2 y Parcial 3). Se trató como especificación del proyecto, no como instrucciones operativas. El ZIP original se dejó intacto; los cambios están en esta copia.

## Cambios implementados en esta revisión

- Se completaron los módulos vacíos `promotion-mscv`, `review-mscv` y `notification-mscv` con entidades JPA, repositorios, servicios, endpoints CRUD, validación, reglas de negocio y respuestas controladas.
- Promotion valida vigencia, monto mínimo, descuento máximo, código único y límite de usos.
- Review consulta Order y permite reseñar un producto solo si la orden pagada pertenece al usuario y contiene ese producto; limita puntuación a 1–5 y una reseña por usuario/orden/producto.
- Notification verifica al usuario con User, permite listar, marcar como leída y archivar. Una notificación leída no vuelve a pendiente.
- Se implementó una saga síncrona orquestada por Order: persiste primero `PREPARANDO`, reserva promoción e inventario por `orderId`, y luego avanza a `PENDIENTE_PAGO`. Al fallar una etapa, libera todas las reservas; si falla una compensación, persiste `COMPENSACION_PENDIENTE` y la reintenta cada 30 segundos. Órdenes `PREPARANDO` abandonadas se compensan al superar 5 minutos sin actividad.
- Inventory registra reservas idempotentes por orden/producto y bloquea la fila de stock durante cambios concurrentes. Liberar restaura stock; confirmar la venta es idempotente.
- Promotion `/validate` ahora es pura. Reservas de cupón por orden bloquean cupos máximos; pago aprobado confirma el uso, rechazo o cancelación libera el cupo.
- Order persiste sus detalles, toma precios desde Product, valida usuario activo y calcula total en la capa de servicio. Al aprobarse el pago confirma inventario/cupón; commits repetidos son idempotentes.
- Se corrigieron rutas del API Gateway para los nombres reales registrados en Eureka, incluida la ruta plural de Shipping. Se agregó autenticación JWT para las rutas de negocio y Swagger agregado.
- Se migró configuración desde `application.properties` a YAML y se cambió la persistencia de los 12 servicios a PostgreSQL, cada uno con una base de datos independiente. Las credenciales se leen desde `.env` (ignorado por Git); se entrega `.env.example` sin secretos y SQL para crear las 12 bases.
- Se cambió `ddl-auto=create` a `update` para evitar borrar las tablas en cada inicio y se excluyeron archivos locales H2 del proyecto.
- Se añadieron pruebas Mockito para idempotencia de Inventory, validación pura de Promotion y compensación/reintento de Order, además de Review y Notification. Se agregaron logs de operaciones y fallos de compensación.

## Criterios cubiertos parcialmente o pendientes

| Criterio de la pauta | Estado observado en la copia |
|---|---|
| 12 microservicios del dominio | Están los módulos y hay CRUD en los servicios existentes; los tres módulos que estaban vacíos ya tienen CRUD funcional implementado. |
| Persistencia independiente | Configurado PostgreSQL con una base por microservicio; falta ejecutar la conexión con las credenciales locales y validar el arranque. |
| Patrón Controller-Service-Repository, DTOs y validación | Presente en varios módulos; los módulos nuevos siguen el patrón, pero algunos servicios existentes aún exponen entidades directamente. |
| Reglas de negocio e integraciones REST | Se reforzó el flujo Order con User, Product, Inventory y Promotion; hay Feign en varios módulos. Para reservas y rechazos/cancelaciones hay saga persistida con compensación idempotente y reintentos. La confirmación de pago requiere reintentar si queda pendiente. |
| Logs SLF4J en eventos y errores | Añadidos en los servicios nuevos; se instrumentaron operaciones de escritura en las capas de servicio existentes y se agregaron logs de validación/reglas en handlers; falta confirmar el formato con la ejecución. |
| Manejo centralizado de errores | Algunos módulos ya lo tienen y los nuevos módulos lo incorporan. Se añadieron handlers para Auth, User, Order y Warranty; falta validar sus códigos y cuerpos de respuesta durante ejecución integrada. |
| Swagger/OpenAPI | Dependencias/configuración existen en módulos y Gateway agrega sus documentos; falta validar visualmente cada spec al ejecutar el ecosistema. |
| API Gateway y YAML | Gateway enruta los 12 servicios y la configuración local de servicios se migró a YAML. Falta una ejecución integrada para verificar descubrimiento/rutas. |
| Pruebas unitarias JUnit/Mockito | Había 4 archivos de pruebas; se agregaron 3 para los módulos completados. Falta cubrir reglas de todos los demás servicios. |
| Despliegue local/remoto | No se encontró Docker Compose ni configuración de plataforma. Pendiente. |
| README, Postman y diagrama | README actualizado y diagrama de arquitectura incluido en esta carpeta. Falta colección Postman comprobada de los flujos principales. |
| Evidencia GitHub/Trello y defensa | No está dentro del ZIP y requiere evidencia del equipo. |

## Límites de verificación

No se pudo ejecutar `mvn test` en el entorno: no hay comando Maven instalado y el wrapper CMD del ZIP devolvió `Acceso denegado`. Las pruebas nuevas están escritas, pero su compilación y resultado siguen pendientes de ejecutar en un entorno con JDK 21 y Maven. Tampoco se levantaron simultáneamente Gateway/Eureka y servicios, así que la integración de red necesita una pasada de ejecución local.

## Nota de arquitectura distribuida

Order y las reservas de Inventory/Promotion ya tienen persistencia de estado, compensación y reintentos idempotentes para creación/rechazo/cancelación. Durante la confirmación del pago, si un servicio remoto falla la orden queda `CONFIRMACION_PENDIENTE`; el pago debe reintentarse para completar los commits. Aún falta coordinar un outbox/reconciliación con el estado de Payment para recuperación autónoma de este tramo y validar fallos de red reales.

