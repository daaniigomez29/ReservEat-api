# Guía de pruebas en Postman — Restaurant API

Documentación de endpoints y escenarios para validar el sistema de **mesas, floor-plan, reservas con asignación greedy, gestión del owner e idempotencia**.

- **Base URL:** `http://localhost:8080/api/v1` (puerto `8080`, context-path `/api/v1`)
- **Colección:** importa `restaurant-api.postman_collection.json` en Postman (Import → File).
- **Auth:** JWT Bearer. La colección guarda los tokens automáticamente al hacer login.

---

## Cómo arrancar

1. Levanta la app y MySQL.
2. En Postman, **Import** → selecciona `postman/restaurant-api.postman_collection.json`.
3. La colección trae variables propias (`baseUrl`, `ownerToken`, `restaurantId`, etc.) y un script que calcula `futureSlot` (hoy + 30 días, 20:00) en cada ejecución. No tienes que tocar fechas.

> **Nota importante sobre las mesas:** el seeder de mesas por defecto (`DefaultTableSeeder`) solo se ejecuta **al arrancar** la app, sobre restaurantes que ya existían y no tenían mesas. Un restaurante que crees **después** del arranque nace **sin mesas**. Por eso el flujo correcto es: crear restaurante → crear mesas → reservar. Si reservas antes de crear mesas, obtendrás `409 No table available` (correcto).

---

## Orden recomendado (carpetas de la colección)

| Orden | Carpeta / Request | Qué hace |
|------|-------------------|----------|
| 1 | `0 - Auth` → **Register Owner + Restaurant (una vez)** | Crea la cuenta del propietario **y** su restaurante en un solo paso. Guarda `ownerToken` (con `ROLE_OWNER`) y `restaurantId`. |
| 2 | `0 - Auth` → Register Customer (una vez) → **Login Customer** | Crea y autentica al cliente. Guarda `customerToken`. |
| 3 | `2 - Tables` → M1 (2), M2 (4), M3 (6) | Define el plano de mesas. Guarda los ids. |
| 4 | `4 - Reservations (customer)` → Create Reservation | El cliente reserva (solo nº de personas). |
| 5 | `5 - Reservation management (owner)` | El owner ve la mesa asignada y gestiona estados. |
| 6 | `6 - Test scenarios` | Validaciones automáticas de los requisitos. |

> **Registro de owner:** el restaurante ya **no** se crea con `POST /restaurants`, sino dentro de `POST /auth/register/owner` (cuenta + restaurante atómico). Ese endpoint devuelve un token que **ya incluye `ROLE_OWNER`**, necesario para gestionar mesas y floor-plan, y el `restaurantId` recién creado.
>
> **Re-runs:** `register/owner` devuelve `201` la primera vez; si el email ya existe → `400/409`. En una sesión nueva usa **Login Owner** (da el token `OWNER`) y luego **Resolver restaurantId del owner**, que busca en `GET /restaurants` el restaurante cuyo `ownerId` coincide con el tuyo y guarda `restaurantId`.

---

## Catálogo de endpoints

### Auth (`/auth`) — público
| Método | Ruta | Body | Notas |
|---|---|---|---|
| POST | `/auth/register` | `{email, username, name, password, tlf}` | Registro de **usuario común**. Siempre rol `USER` (el rol **no** se acepta del cliente). Devuelve `token`. |
| POST | `/auth/register/owner` | `{ account:{...RegisterRequest}, restaurant:{...CreateRestaurantRequest} }` | Registro de **propietario**: crea cuenta + restaurante de forma atómica, asigna `OWNER` server-side. Devuelve `token` con `ROLE_OWNER` + `restaurantId`. |
| POST | `/auth/login` | `{email, password}` | Devuelve `token` + `refreshToken`. |
| POST | `/auth/refresh` | `{refreshToken}` | Renueva el token. |

> **Seguridad de roles:** el rol nunca viaja en el body de un registro público. `USER` lo fija el servidor en `/auth/register`; `OWNER` se asigna en `/auth/register/owner` **porque estás creando un restaurante de verdad**. `ADMIN` no es self-service. Para trabajadores (futuro), un endpoint protegido que solo pueda usar el owner del restaurante.

### Restaurantes (`/restaurants`)
| Método | Ruta | Auth | Notas |
|---|---|---|---|
| POST | `/restaurants` | USER/ADMIN | Crea un restaurante suelto (el llamante queda como `ownerId`). En el alta de owner no se usa: el restaurante se crea en `/auth/register/owner`. Nota: un token solo-`OWNER` **no** entra aquí (matcher actual = USER/ADMIN). |
| GET | `/restaurants` | público | Lista todos. |
| GET | `/restaurants/{id}` | público | Detalle. |
| GET | `/restaurants/search?name=&city=&province=&cuisineType=&dietaryOption=&maxPrice=` | público | Búsqueda con filtros opcionales. |
| PUT | `/restaurants/{id}` | OWNER/ADMIN | Actualiza. |
| DELETE | `/restaurants/{id}` | OWNER/ADMIN | Elimina. |

### Mesas (`/restaurants/{id}/tables`) — **requiere `ROLE_OWNER` o `ROLE_ADMIN`**
| Método | Ruta | Body |
|---|---|---|
| POST | `/restaurants/{id}/tables` | `{label, capacity, minCapacity?, zone?, shape, x, y, width, height, rotation}` |
| GET | `/restaurants/{id}/tables` | — |
| GET | `/restaurants/{id}/tables/{tableId}` | — |
| PUT | `/restaurants/{id}/tables/{tableId}` | igual que POST + `active` |
| DELETE | `/restaurants/{id}/tables/{tableId}` | — (soft-delete: `active=false`) |

- `shape`: `RECTANGLE` | `SQUARE` | `CIRCLE`
- Coordenadas en **unidades lógicas 0–1000** (el frontend las escala a píxeles). Debe cumplirse `x+width ≤ 1000` y `y+height ≤ 1000`.

### Floor-plan (`/restaurants/{id}/floor-plan`) — **solo owner/admin**
| Método | Ruta | Notas |
|---|---|---|
| GET | `/restaurants/{id}/floor-plan?at=YYYY-MM-DDTHH:mm` | Estado de cada mesa en ese instante. Sin `at` = ahora. |

Respuesta: `planWidth/planHeight` + lista de mesas con `x,y,width,height,rotation,shape` y `status` (`FREE`/`PENDING`/`RESERVED`/`SEATED`), más `reservationId`, `bookerEmail`, `occupiedUntil` si está ocupada.

### Reservas (`/reservations`)
| Método | Ruta | Auth | Notas |
|---|---|---|---|
| POST | `/reservations` | cliente | Body `{restaurantId, startDate, partySize, bookerEmail?}`. Nace `CONFIRMED` con mesa ya asignada (greedy). **El cliente no ve la mesa.** |
| GET | `/reservations/{id}` | dueño de la reserva/admin | Detalle. |
| GET | `/reservations/my?page=&size=` | cliente | Mis reservas (paginado). |
| GET | `/reservations/restaurant/{id}` | owner/admin | Reservas del restaurante. **Incluye `tableId` y `tableLabel`.** |
| POST | `/reservations/{id}/cancel` | dueño/admin | Cancela. |
| PATCH | `/reservations/{id}/assign-table` | owner/admin | Body `{tableId}`. Reasigna mesa. |
| PATCH | `/reservations/{id}/seat` | owner/admin | `CONFIRMED → SEATED`. |
| PATCH | `/reservations/{id}/complete` | owner/admin | `SEATED`/`CONFIRMED → COMPLETED`. |
| PATCH | `/reservations/{id}/no-show` | owner/admin | `CONFIRMED → NO_SHOW`. |

`startDate` debe ir en formato `yyyy-MM-dd'T'HH:mm` (p. ej. `2026-07-16T20:00`) y ser futura.

### Menús (`/menus`)
| Método | Ruta | Auth |
|---|---|---|
| POST | `/menus/restaurant/{restaurantId}` | owner/admin |
| GET | `/menus/restaurant/{restaurantId}` | público |
| POST | `/menus/categories` (`{name, menuId}`) | owner/admin |
| POST | `/menus/items` (`{name, description?, price, menuCategoryId}`) | owner/admin |
| DELETE | `/menus/items/{itemId}` / `/menus/categories/{categoryId}` | owner/admin |

---

## Escenarios que validan los requisitos

La carpeta `6 - Test scenarios` automatiza estos con aserciones (pestaña **Test results** en Postman). Prerrequisito: haber hecho login (owner y customer) y tener `restaurantId` + las 3 mesas (M1=2, M2=4, M3=6).

> Cada escenario reserva en un **día distinto** (`futureSlot`, `slotBestfit`, `slotDedup`, calculados automáticamente a hoy+30/31/32 a las 20:00) para que no se quiten mesas entre sí. Así puedes ejecutarlos encadenados sin limpiar la BD.

### A) Asignación diferida + best-fit
**Requisito:** el cliente solo dice cuántos van; el sistema asigna la mesa más pequeña que quepa.
- `A) Best-fit: party 4` → crea reserva de 4 personas en `slotBestfit`. Espera `201` y `status=CONFIRMED`.
- `A.2) Verificar best-fit = M2` → como owner, comprueba que la reserva quedó en **M2 (capacidad 4)**, no en M3 (6). Demuestra el best-fit (minimiza fragmentación). Se usa party 4 (y no 3) para que el resultado sea inequívoco aunque hayas editado M1 a capacidad 3.

### B) El cliente no ve mesas
**Requisito:** experiencia simple para el cliente.
- En `Create Reservation`, la aserción comprueba que el JSON **no trae `tableId`** (es `null`/ausente). El owner, en cambio, sí lo ve en `/reservations/restaurant/{id}`.

### C) Aforo por mesas (no por capacidad total)
**Requisito:** la capacidad se reparte en mesas reales.
- `B) Conflicto de aforo: party 50` → reserva de 50 personas. No hay mesa que quepa → espera `409` y mensaje `No table available`. (Aunque `size` del restaurante fuera grande, lo que manda son las mesas.)

### D) Idempotencia (doble-click)
**Requisito:** un doble-click no debe crear una reserva fantasma.
- `C.1) Doble-click 1ª llamada` → crea en `slotDedup` y guarda el id.
- `C.2) Doble-click 2ª llamada` (lánzala **en < 10 s**) → debe devolver **el mismo id**. La aserción lo verifica. Si esperas > 10 s, se crea una reserva nueva (comportamiento esperado: ya no es un accidente).

### E) Concurrencia real (opcional, fuera de Postman)
**Requisito:** dos personas a la vez no se llevan la misma mesa.
- El lock pessimistic serializa. Para forzar la carrera, usa este PowerShell (dos peticiones en paralelo):

```powershell
$token = "PEGA_TU_customerToken"
$body = '{"restaurantId":1,"startDate":"2026-07-16T20:00","partySize":2}'
$headers = @{ Authorization = "Bearer $token" }
1..2 | ForEach-Object {
  Start-Job -ScriptBlock {
    param($b,$h)
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/reservations" `
      -Method Post -Body $b -Headers $h -ContentType "application/json"
  } -ArgumentList $body,$headers
} | Wait-Job | Receive-Job
```
Nunca verás dos reservas en la misma mesa. Si hay mesas de sobra, podrías ver dos reservas distintas (salvo que entren dentro de la ventana de dedup).

### F) Máquina de estados del owner
**Requisito:** el owner gestiona el ciclo de vida.
- `seat` sobre una reserva `CONFIRMED` → `SEATED`.
- `complete` sobre `SEATED` → `COMPLETED`.
- Intentar `seat` sobre una ya `COMPLETED`/`CANCELLED` → `400` (transición inválida). Pruébalo manualmente para ver la validación de dominio.
- `assign-table` a una mesa ocupada en esa franja → `409`; a una mesa que no quepa → `400`.

---

## Códigos de respuesta esperados

| Código | Cuándo |
|---|---|
| 200 | GET / PATCH / acciones con éxito |
| 201 | Creación (restaurante, mesa, reserva). En dedup, el replay devuelve 201 con la reserva original |
| 400 | Validación de body inválida, transición de estado inválida, **permiso denegado en mesas/floor-plan/acciones-owner de reservas**, mesa que no cabe o que no pertenece al restaurante, **y mesa inexistente** (ver nota abajo) |
| 401 | Falta o caduca el token |
| 403 | Falta el rol exigido por Spring Security: `tables/**` y `floor-plan/**` (sin `ROLE_OWNER`/`ROLE_ADMIN`), `PUT`/`DELETE /restaurants/**`, `/admin/**` |
| 404 | Restaurante o reserva inexistente |
| 409 | Sin mesa disponible / mesa ya ocupada al reasignar / email ya registrado |

> **Cómo se reparten 400 vs 403 en este proyecto (con tu `SecurityConfig` actual):**
> - **403 (capa de seguridad):** mesas y floor-plan exigen `hasAnyRole("OWNER","ADMIN")`. Un token sin ese rol (p. ej. un `USER` común o el cliente) se corta **antes** de llegar al servicio → **403**.
> - **400 (capa de servicio):** si el rol es correcto pero el usuario **no es el owner de ESE restaurante** (p. ej. el owner A intentando tocar el restaurante de B), pasa el filtro pero `assertRestaurantManager` lanza `DomainException` → **400**.
>
> Es decir: la seguridad por rol (403) es la primera barrera; la pertenencia concreta del restaurante (400) es la segunda. Las acciones owner de reservas (`/reservations/{id}/seat`, etc.) no tienen matcher por rol, así que ahí el rechazo por no-owner sigue siendo **400**.

> **Quirk conocido — mesa inexistente devuelve 400:** `TableNotFoundException` extiende `DomainException` y **no tiene handler dedicado**, así que cae en el de `DomainException` → **400**. En cambio `RestaurantNotFoundException` y `ReservationNotFoundException` sí tienen handler propio → **404**. Es una inconsistencia menor (lo coherente sería 404 para la mesa). Si quieres, se arregla en 1 línea añadiendo un `@ExceptionHandler(TableNotFoundException.class)` que devuelva `NOT_FOUND`.
