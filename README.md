# Restaurant Ordering & Table Management — Backend API (Spring Boot)

Backend cho hệ thống gọi món theo bàn, nhân viên xác nhận & quản lý order, trạng thái món, tính tiền & thanh toán.

## 1) Tech Stack
- Java Spring Boot
- MySQL
- Spring Data JPA
- Spring Security (Basic Auth)
- CORS enabled for FE

---

## 2) Run Backend (Dev)

### Prerequisites
- JDK 25
- MySQL running
- Maven

### Start
```bash
mvn clean compile
mvn spring-boot:run
```

Backend default:
- Base URL: `http://localhost:8080`

Health check:
- `GET http://localhost:8080/api/health` → `OK`

---

## 3) Auth (Basic Auth)

Backend dùng **Basic Auth** cho API nội bộ (nhân viên). API public không cần auth.

### Accounts (Dev)
| Role | Username | Password | Dùng cho |
|---|---|---|---|
| ADMIN | admin | admin123 | full access |
| WAITER | waiter | waiter123 | bàn, order, món, trạng thái món |
| CASHIER | cashier | cashier123 | checkout |

### Notes
- FE gọi API nội bộ phải gửi header:
  - `Authorization: Basic base64(username:password)`
- Public endpoints `/api/public/**` không cần auth.

---

## 4) CORS
CORS đã bật (dev mode). FE có thể gọi BE từ origin khác (Vite/React).

Nếu FE chạy Vite: thường là `http://localhost:5173`  
BE: `http://localhost:8080`

---

## 5) Concept & Flow

### Link/QR token
- Mỗi bàn có `qrToken` (cột `tables.qr_token`).
- FE customer route ví dụ:
  - `/table/:token`
- FE customer gọi BE bằng token để submit order.

### Business Flow (MVP)
1. Customer mở link theo token → xem menu → chọn món → submit
2. BE tạo `Order` status = `DRAFT`, `OrderItems` status = `DRAFT`
3. Waiter mở dashboard → xem draft theo bàn → confirm
4. Order chuyển `ACTIVE`
5. Waiter có thể add/update/delete món + đổi trạng thái món
6. Cashier xem bill → checkout → order `COMPLETED`, table `CLEANING`
7. Waiter set bàn về `AVAILABLE` sau khi dọn

---

## 6) API Endpoints

### 6.1 Public APIs (No Auth)

#### Get table info by token
- `GET /api/public/tables/{token}`
- Response:
```json
{
  "tableId": 1,
  "code": "T01",
  "status": "OCCUPIED"
}
```

#### Get public menu
- `GET /api/public/menu`
- Response (example):
```json
[
  {
    "id": 1,
    "categoryId": 1,
    "name": "Trà đào",
    "price": 35000,
    "isAvailable": true,
    "imageUrl": "https://..."
  }
]
```

#### Submit order (customer)
- `POST /api/public/tables/{token}/submit`
- Body:
```json
{
  "customerNote": "Bàn có em bé, ít cay",
  "items": [
    { "menuItemId": 1, "qty": 2, "note": "ít đá" },
    { "menuItemId": 3, "qty": 1, "note": "" }
  ]
}
```
- Response:
```json
{
  "orderId": 10,
  "tableId": 1,
  "status": "DRAFT",
  "message": "Đã gửi yêu cầu gọi món. Vui lòng chờ nhân viên xác nhận."
}
```

---

### 6.2 Internal APIs (Require Basic Auth)

> Tất cả endpoints dưới đây yêu cầu Basic Auth.

#### Tables
**List tables**
- `GET /api/tables` (WAITER/ADMIN)

**Open table**
- `POST /api/tables/{tableId}/open` (WAITER/ADMIN)

**Request bill**
- `POST /api/tables/{tableId}/request-bill` (WAITER/ADMIN)

**Set cleaning**
- `POST /api/tables/{tableId}/set-cleaning` (WAITER/ADMIN)

**Set available**
- `POST /api/tables/{tableId}/set-available` (WAITER/ADMIN)

---

#### Orders (WAITER/ADMIN)

**Get draft order by table**
- `GET /api/orders/draft?tableId={id}`

**Confirm order**
- `POST /api/orders/{orderId}/confirm`

**Get order detail**
- `GET /api/orders/{orderId}`

---

#### Order Items (WAITER/ADMIN)

**Add item**
- `POST /api/orders/{orderId}/items`
- Body:
```json
{ "menuItemId": 1, "qty": 2, "note": "ít cay" }
```

**Update item**
- `PUT /api/orders/{orderId}/items/{itemId}`
- Body:
```json
{ "qty": 3, "note": "không hành" }
```

**Remove item**
- `DELETE /api/orders/{orderId}/items/{itemId}`

---

#### Item Status (WAITER/ADMIN)

**Update item status (single endpoint)**
- `POST /api/orders/{orderId}/items/{itemId}/status`
- Body:
```json
{ "newStatus": "COOKING" }
```

Allowed transitions (MVP):
- `DRAFT → PENDING → COOKING → READY → SERVED`
- `PENDING/COOKING → CANCELED`
- `READY/SERVED/CANCELED` không đổi ngược

Cancel example:
```json
{ "newStatus": "CANCELED", "cancelReason": "Hết món" }
```

---

#### Billing & Checkout

**Get bill**
- `GET /api/orders/{orderId}/bill` (WAITER/ADMIN or CASHIER/ADMIN)

**Checkout**
- `POST /api/orders/{orderId}/checkout` (CASHIER/ADMIN)
- Body:
```json
{
  "method": "CASH",
  "discountAmount": 0,
  "taxAmount": 0,
  "serviceFeeAmount": 0
}
```
- Response:
```json
{
  "paymentId": 99,
  "orderId": 10,
  "totalAmount": 70000,
  "message": "Thanh toán thành công. Bàn chuyển sang CLEANING."
}
```

---

## 7) HTTP Status & Error Response
- `200` OK
- `400` Business rule violated (ví dụ: đổi trạng thái sai)
- `401` Unauthorized (thiếu/ sai Basic Auth)
- `403` Forbidden (đúng auth nhưng sai role)
- `404` Not found

Error body (example):
```json
{ "message": "Không tìm thấy bàn id=1" }
```

---

## 8) FE Dev Notes (React + Tailwind)

### Recommended pages
1. **Customer**
   - Route: `/table/:token`
   - Calls: `/api/public/tables/{token}`, `/api/public/menu`, `/api/public/tables/{token}/submit`

2. **Waiter Dashboard**
   - Route: `/waiter`
   - Calls: `/api/tables`, `/api/orders/draft?tableId=...`, `/api/orders/{id}/confirm`,
     `/api/orders/{id}`, `/api/orders/{id}/items`, `/api/orders/{id}/items/{itemId}/status`

3. **Cashier**
   - Route: `/cashier`
   - Calls: `/api/orders/{id}/bill`, `/api/orders/{id}/checkout`

### Auth handling in FE (Dev)
- Dùng Basic Auth header khi gọi internal API.
- Có thể hardcode trong FE dev hoặc tạo màn login đơn giản.

---

## 9) Demo Script (Quick)
1. Customer: submit order via token
2. Waiter: view draft → confirm
3. Waiter: add item → set status COOKING → READY → SERVED
4. Cashier: bill → checkout
5. Waiter: set table AVAILABLE
