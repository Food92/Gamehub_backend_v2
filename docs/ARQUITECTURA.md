# Ecosistema GameHub Store

```mermaid
flowchart LR
  Cliente --> GW[API Gateway :8091
JWT + rutas]
  GW --> Eureka[Eureka :8761]
  GW --> Auth[Authentication :8007]
  GW --> User[User :8002]
  GW --> Product[Product :8001]
  GW --> Category[Category :8088]
  GW --> Inventory[Inventory :8003]
  GW --> Order[Order :8006]
  GW --> Payment[Payment :8010]
  GW --> Shipping[Shipping :8005]
  GW --> Promotion[Promotion :8009]
  GW --> Review[Review :8012]
  GW --> Warranty[Warranty :8004]
  GW --> Notification[Notification :8013]
  Order --> User
  Order --> Product
  Order --> Inventory
  Order --> Promotion
  Product --> Category
  Review --> Order
  Notification --> User
  Auth -. JWT .-> GW
```

Order persiste estados de saga y compensa reservas idempotentes de Inventory y Promotion. Cada microservicio usa PostgreSQL con un esquema propio dentro de `gamehub_store`. El Gateway y los servicios leen credenciales desde `.env`; Eureka resuelve nombres para rutas y clientes Feign.
