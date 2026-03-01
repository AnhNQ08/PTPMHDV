# 📋 HƯỚNG DẪN TEST TOÀN BỘ CHỨC NĂNG

> Tài liệu hướng dẫn kiểm thử đầy đủ tất cả chức năng của hệ thống **Spring Boot Microservices Demo**.
> Sử dụng **Postman** hoặc **curl** để thực hiện test.

---

## 📑 Mục lục

1. [Điều kiện tiên quyết](#1-điều-kiện-tiên-quyết)
2. [Test Service Discovery (Eureka Server)](#2-test-service-discovery-eureka-server)
3. [Test Auth Service — Đăng nhập & Tạo JWT Token](#3-test-auth-service--đăng-nhập--tạo-jwt-token)
4. [Test API Gateway — Routing & JWT Filter](#4-test-api-gateway--routing--jwt-filter)
5. [Test User Service — Lấy danh sách người dùng](#5-test-user-service--lấy-danh-sách-người-dùng)
6. [Test Load Balancing](#6-test-load-balancing)
7. [Test Global Logging Filter](#7-test-global-logging-filter)
8. [Test Edge Cases & Error Handling](#8-test-edge-cases--error-handling)
9. [Bảng tổng hợp Test Cases](#9-bảng-tổng-hợp-test-cases)
10. [Checklist Test](#10-checklist-test)

---

## 1. Điều kiện tiên quyết

### 1.1. Yêu cầu hệ thống
- **Java 17+** đã cài đặt → kiểm tra: `java -version`
- **Maven** đã cài đặt → kiểm tra: `mvn -version`
- **Postman** hoặc **curl** để gửi request

### 1.2. Khởi động hệ thống (đúng thứ tự)

> ⚠️ **Quan trọng:** Phải khởi động **đúng thứ tự** bên dưới. Mỗi service chạy trên một terminal riêng biệt.

| Bước | Service | Lệnh | Port |
|------|---------|-------|------|
| 1 | Eureka Server | `cd d:\PTPMHDV\eureka-server && mvn spring-boot:run` | 8761 |
| 2 | Auth Service | `cd d:\PTPMHDV\auth-service && mvn spring-boot:run` | 9001 |
| 3 | User Service (Instance 1) | `cd d:\PTPMHDV\user-service && mvn spring-boot:run` | 9002 |
| 4 | User Service (Instance 2) | `cd d:\PTPMHDV\user-service && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9003"` | 9003 |
| 5 | API Gateway | `cd d:\PTPMHDV\api-gateway && mvn spring-boot:run` | 8080 |

### 1.3. Xác nhận hệ thống đã sẵn sàng

Mở trình duyệt truy cập **http://localhost:8761** — kiểm tra tất cả các service đã đăng ký:

- ✅ `AUTH-SERVICE` — 1 instance
- ✅ `USER-SERVICE` — 2 instances (port 9002 & 9003)
- ✅ `API-GATEWAY` — 1 instance

---

## 2. Test Service Discovery (Eureka Server)

### TC-01: Eureka Dashboard hoạt động

| Mục | Chi tiết |
|-----|----------|
| **Mô tả** | Kiểm tra Eureka Server khởi động và Dashboard hiển thị đúng |
| **URL** | `http://localhost:8761` |
| **Phương thức** | Mở bằng **trình duyệt** |
| **Kết quả mong đợi** | Trang Eureka Dashboard hiển thị, có danh sách **Instances currently registered** |

### TC-02: Tất cả service đăng ký thành công

| Mục | Chi tiết |
|-----|----------|
| **Mô tả** | Xác nhận tất cả service đã đăng ký với Eureka |
| **Cách kiểm tra** | Trên trang Eureka Dashboard, kiểm tra bảng **Instances currently registered with Eureka** |
| **Kết quả mong đợi** | Có **4 instances** thuộc **3 applications**: |

```
Application               Instances
────────────────────────────────────────
API-GATEWAY               1 instance
AUTH-SERVICE               1 instance
USER-SERVICE              2 instances
```

### TC-03: Eureka REST API hoạt động

```bash
curl http://localhost:8761/eureka/apps -H "Accept: application/json"
```

| Mục | Chi tiết |
|-----|----------|
| **Kết quả mong đợi** | Trả về JSON chứa thông tin tất cả các service đã đăng ký |
| **HTTP Status** | `200 OK` |

---

## 3. Test Auth Service — Đăng nhập & Tạo JWT Token

### 3.1. Tài khoản Mock có sẵn

| Username | Password | Ghi chú |
|----------|----------|---------|
| `admin` | `admin123` | Tài khoản admin |
| `user` | `user123` | Tài khoản user thường |
| `demo` | `demo123` | Tài khoản demo |

---

### TC-04: Đăng nhập thành công với tài khoản `admin`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Postman setup:**
| Mục | Giá trị |
|-----|---------|
| Method | `POST` |
| URL | `http://localhost:8080/api/auth/login` |
| Headers | `Content-Type: application/json` |
| Body (raw JSON) | `{"username": "admin", "password": "admin123"}` |

**Kết quả mong đợi:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...<token_dài>",
  "username": "admin",
  "message": "Login successful!"
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| `token` | Chuỗi JWT dạng `eyJ...` (3 phần cách nhau bởi dấu `.`) |
| `username` | `"admin"` |
| `message` | `"Login successful!"` |

> 💡 **Lưu ý:** Sao chép giá trị `token` để dùng cho các test case tiếp theo.

---

### TC-05: Đăng nhập thành công với tài khoản `user`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}'
```

**Kết quả mong đợi:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "username": "user",
  "message": "Login successful!"
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| `username` | `"user"` |

---

### TC-06: Đăng nhập thành công với tài khoản `demo`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo123"}'
```

**Kết quả mong đợi:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "username": "demo",
  "message": "Login successful!"
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| `username` | `"demo"` |

---

### TC-07: Đăng nhập thất bại — Sai mật khẩu

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "wrongpassword"}'
```

**Kết quả mong đợi:**
```json
{
  "message": "Invalid username or password"
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| `message` | `"Invalid username or password"` |

---

### TC-08: Đăng nhập thất bại — Tài khoản không tồn tại

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "hacker", "password": "123456"}'
```

**Kết quả mong đợi:**
```json
{
  "message": "Invalid username or password"
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |

---

### TC-09: Đăng nhập thất bại — Thiếu trường username

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"password": "admin123"}'
```

**Kết quả mong đợi:**

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| Response | Thông báo lỗi `"Invalid username or password"` |

---

### TC-10: Đăng nhập thất bại — Thiếu trường password

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin"}'
```

**Kết quả mong đợi:**

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| Response | Thông báo lỗi `"Invalid username or password"` |

---

### TC-11: Đăng nhập thất bại — Body rỗng

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Kết quả mong đợi:**

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |

---

### TC-12: Đăng nhập trực tiếp tới Auth Service (bypass Gateway)

**Request:**
```bash
curl -X POST http://localhost:9001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Kết quả mong đợi:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "username": "admin",
  "message": "Login successful!"
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| Ý nghĩa | Auth Service hoạt động độc lập, không phụ thuộc Gateway |

---

## 4. Test API Gateway — Routing & JWT Filter

### TC-13: Truy cập endpoint được bảo vệ — KHÔNG có token

**Request:**
```bash
curl http://localhost:8080/api/users
```

**Postman setup:**
| Mục | Giá trị |
|-----|---------|
| Method | `GET` |
| URL | `http://localhost:8080/api/users` |
| Headers | Không có `Authorization` |

**Kết quả mong đợi:**
```json
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| `error` | `"Missing or invalid Authorization header"` |

---

### TC-14: Truy cập endpoint — Token KHÔNG có prefix "Bearer"

**Request:**
```bash
curl http://localhost:8080/api/users \
  -H "Authorization: eyJhbGciOiJIUzM4NCJ9..."
```

**Kết quả mong đợi:**
```json
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| Ý nghĩa | Gateway yêu cầu format `Bearer <token>` |

---

### TC-15: Truy cập endpoint — Token giả / không hợp lệ

**Request:**
```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer this.is.a.fake.token"
```

**Kết quả mong đợi:**
```json
{
  "error": "Invalid or expired JWT token",
  "status": 401
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| `error` | `"Invalid or expired JWT token"` |

---

### TC-16: Truy cập endpoint — Token hợp lệ ✅

> Đầu tiên, lấy token từ [TC-04](#tc-04-đăng-nhập-thành-công-với-tài-khoản-admin)

**Request:**
```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <PASTE_TOKEN_TỪ_TC04>"
```

**Postman setup:**
| Mục | Giá trị |
|-----|---------|
| Method | `GET` |
| URL | `http://localhost:8080/api/users` |
| Headers | `Authorization: Bearer <token>` |

**Kết quả mong đợi:**
```json
{
  "message": "User list fetched successfully from port 9002",
  "serverPort": "9002",
  "users": [
    {"id": "1", "name": "Nguyen Van A", "email": "nguyenvana@example.com"},
    {"id": "2", "name": "Tran Thi B", "email": "tranthib@example.com"},
    {"id": "3", "name": "Le Van C", "email": "levanc@example.com"}
  ]
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| `users` | Mảng gồm 3 user |
| `serverPort` | `"9002"` hoặc `"9003"` |

---

### TC-17: Endpoint Login không yêu cầu token (Open Endpoint)

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| Ý nghĩa | `/api/auth/login` nằm trong danh sách **Open Endpoints**, không cần Authorization header |

---

### TC-18: Gateway route tới đúng service

**Test 1 — Route tới Auth Service:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```
→ Gateway route `/api/auth/**` tới `lb://auth-service` ✅

**Test 2 — Route tới User Service:**
```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <TOKEN>"
```
→ Gateway route `/api/users/**` tới `lb://user-service` ✅

---

### TC-19: Truy cập endpoint không tồn tại

**Request:**
```bash
curl http://localhost:8080/api/nonexistent \
  -H "Authorization: Bearer <TOKEN>"
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `404 Not Found` |
| Ý nghĩa | Gateway không tìm thấy route phù hợp |

---

## 5. Test User Service — Lấy danh sách người dùng

### TC-20: Gọi trực tiếp User Service Instance 1

**Request:**
```bash
curl http://localhost:9002/api/users
```

**Kết quả mong đợi:**
```json
{
  "message": "User list fetched successfully from port 9002",
  "serverPort": "9002",
  "users": [
    {"id": "1", "name": "Nguyen Van A", "email": "nguyenvana@example.com"},
    {"id": "2", "name": "Tran Thi B", "email": "tranthib@example.com"},
    {"id": "3", "name": "Le Van C", "email": "levanc@example.com"}
  ]
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| `serverPort` | `"9002"` |
| `users` | 3 users |

---

### TC-21: Gọi trực tiếp User Service Instance 2

**Request:**
```bash
curl http://localhost:9003/api/users
```

**Kết quả mong đợi:**
```json
{
  "message": "User list fetched successfully from port 9003",
  "serverPort": "9003",
  "users": [...]
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `200 OK` |
| `serverPort` | `"9003"` |
| Ý nghĩa | Instance 2 hoạt động độc lập trên port 9003 |

---

## 6. Test Load Balancing

### TC-22: Kiểm tra Load Balancing hoạt động

> Gọi cùng một endpoint qua Gateway **ít nhất 6 lần** và quan sát `serverPort` thay đổi.

**Request (lặp lại nhiều lần):**
```bash
# Lần 1
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"

# Lần 2
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"

# Lần 3
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"

# Lần 4
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"

# Lần 5
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"

# Lần 6
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"
```

**Kết quả mong đợi:**

| Lần gọi | `serverPort` mong đợi |
|----------|----------------------|
| Lần 1 | `9002` hoặc `9003` |
| Lần 2 | Khác với lần 1 |
| Lần 3 | Khác với lần 2 |
| Lần 4 | Khác với lần 3 |
| Lần 5 | Khác với lần 4 |
| Lần 6 | Khác với lần 5 |

| Kiểm tra | Mong đợi |
|----------|----------|
| Pattern | `serverPort` **xen kẽ** giữa `9002` và `9003` |
| Ý nghĩa | Eureka + Spring Cloud LoadBalancer phân phối request đều giữa 2 instances |

> 💡 **Mẹo PowerShell:** Chạy nhanh script để test load balancing:
> ```powershell
> for ($i=1; $i -le 10; $i++) {
>     $response = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Headers @{"Authorization"="Bearer <TOKEN>"}
>     Write-Host "Lần $i → Port: $($response.serverPort)"
> }
> ```

---

### TC-23: Load Balancing khi tắt 1 instance

**Bước thực hiện:**

1. **Tắt** User Service Instance 2 (port 9003) → đóng terminal chạy instance 2
2. Đợi khoảng **30 giây** để Eureka cập nhật registry
3. Gọi lại endpoint:

```bash
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"
curl http://localhost:8080/api/users -H "Authorization: Bearer <TOKEN>"
```

**Kết quả mong đợi:**

| Kiểm tra | Mong đợi |
|----------|----------|
| `serverPort` | Luôn là `"9002"` |
| Ý nghĩa | Khi 1 instance bị tắt, tất cả request được chuyển sang instance còn lại |

---

## 7. Test Global Logging Filter

### TC-24: Kiểm tra log được ghi khi gửi request

**Bước thực hiện:**

1. Mở terminal đang chạy **API Gateway**
2. Gửi request bất kỳ:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```
3. Quan sát log trên terminal API Gateway

**Kết quả mong đợi trong log:**
```
>>> Incoming Request: POST /api/auth/login from /127.0.0.1:xxxxx
<<< Response: POST /api/auth/login | Status: 200 | Duration: xx ms
```

| Kiểm tra | Mong đợi |
|----------|----------|
| Log `>>>` | Ghi nhận method, path, và địa chỉ client |
| Log `<<<` | Ghi nhận status code và thời gian xử lý (ms) |

---

### TC-25: Kiểm tra log khi request bị từ chối (401)

**Bước thực hiện:**

1. Gửi request không có token:
```bash
curl http://localhost:8080/api/users
```
2. Quan sát log trên terminal API Gateway

**Kết quả mong đợi trong log:**
```
>>> Incoming Request: GET /api/users from /127.0.0.1:xxxxx
<<< Response: GET /api/users | Status: 401 | Duration: xx ms
```

| Kiểm tra | Mong đợi |
|----------|----------|
| `Status` trong log | `401` |
| Ý nghĩa | LoggingGlobalFilter vẫn ghi log ngay cả khi request bị JwtFilter từ chối |

---

## 8. Test Edge Cases & Error Handling

### TC-26: Gửi request với method không đúng

**Request:**
```bash
curl -X GET http://localhost:8080/api/auth/login
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `405 Method Not Allowed` |
| Ý nghĩa | Endpoint `/api/auth/login` chỉ chấp nhận `POST` |

---

### TC-27: Gửi request không có Content-Type header

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -d '{"username": "admin", "password": "admin123"}'
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `415 Unsupported Media Type` hoặc `400 Bad Request` |
| Ý nghĩa | Server yêu cầu `Content-Type: application/json` |

---

### TC-28: Gửi JSON không hợp lệ (malformed)

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d 'this is not json'
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `400 Bad Request` |
| Ý nghĩa | Server từ chối body không phải JSON hợp lệ |

---

### TC-29: Token hết hạn (Expired Token)

**Bước thực hiện:**

> JWT token có thời gian hết hạn là **1 giờ** (3600000ms) theo cấu hình. Để test nhanh, bạn có thể sửa `jwt.expiration` trong `auth-service/src/main/resources/application.yml` thành `5000` (5 giây), restart Auth Service, lấy token mới, đợi 6 giây rồi test.

1. Sửa `jwt.expiration: 5000` trong `application.yml` của Auth Service
2. Restart Auth Service
3. Login lấy token mới
4. Đợi **6 giây**
5. Gọi:

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <EXPIRED_TOKEN>"
```

**Kết quả mong đợi:**
```json
{
  "error": "Invalid or expired JWT token",
  "status": 401
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| Ý nghĩa | Gateway phát hiện token hết hạn và từ chối request |

> ⚠️ **Nhớ đổi lại** `jwt.expiration: 3600000` sau khi test xong.

---

### TC-30: Token bị chỉnh sửa (Tampered Token)

**Bước thực hiện:**

1. Lấy token hợp lệ từ TC-04
2. Thay đổi 1 ký tự bất kỳ trong phần giữa của token (phần payload)
3. Gửi request:

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.TAMPERED_PAYLOAD.signature"
```

**Kết quả mong đợi:**
```json
{
  "error": "Invalid or expired JWT token",
  "status": 401
}
```

| Kiểm tra | Mong đợi |
|----------|----------|
| HTTP Status | `401 Unauthorized` |
| Ý nghĩa | Signature verification thất bại, token bị từ chối |

---

## 9. Bảng tổng hợp Test Cases

| # | Test Case | Endpoint | Method | Token? | Expected Status | Danh mục |
|---|-----------|----------|--------|--------|----------------|----------|
| TC-01 | Eureka Dashboard | `http://localhost:8761` | Browser | — | 200 | Service Discovery |
| TC-02 | Service Registration | Eureka Dashboard | Browser | — | 4 instances | Service Discovery |
| TC-03 | Eureka REST API | `/eureka/apps` | GET | — | 200 | Service Discovery |
| TC-04 | Login admin | `/api/auth/login` | POST | — | 200 | Authentication |
| TC-05 | Login user | `/api/auth/login` | POST | — | 200 | Authentication |
| TC-06 | Login demo | `/api/auth/login` | POST | — | 200 | Authentication |
| TC-07 | Sai password | `/api/auth/login` | POST | — | 401 | Authentication |
| TC-08 | User không tồn tại | `/api/auth/login` | POST | — | 401 | Authentication |
| TC-09 | Thiếu username | `/api/auth/login` | POST | — | 401 | Authentication |
| TC-10 | Thiếu password | `/api/auth/login` | POST | — | 401 | Authentication |
| TC-11 | Body rỗng | `/api/auth/login` | POST | — | 401 | Authentication |
| TC-12 | Login bypass Gateway | `localhost:9001/api/auth/login` | POST | — | 200 | Authentication |
| TC-13 | Không có token | `/api/users` | GET | ❌ | 401 | JWT Filter |
| TC-14 | Thiếu prefix Bearer | `/api/users` | GET | ❌ | 401 | JWT Filter |
| TC-15 | Token giả | `/api/users` | GET | ❌ | 401 | JWT Filter |
| TC-16 | Token hợp lệ | `/api/users` | GET | ✅ | 200 | JWT Filter |
| TC-17 | Open endpoint | `/api/auth/login` | POST | — | 200 | JWT Filter |
| TC-18 | Routing đúng service | `/api/auth/**`, `/api/users/**` | — | — | 200 | Gateway Routing |
| TC-19 | Route không tồn tại | `/api/nonexistent` | GET | ✅ | 404 | Gateway Routing |
| TC-20 | Direct call Instance 1 | `localhost:9002/api/users` | GET | — | 200 | User Service |
| TC-21 | Direct call Instance 2 | `localhost:9003/api/users` | GET | — | 200 | User Service |
| TC-22 | Load Balancing | `/api/users` (x6) | GET | ✅ | 200, port xen kẽ | Load Balancing |
| TC-23 | LB khi tắt 1 instance | `/api/users` (x3) | GET | ✅ | 200, port cố định | Load Balancing |
| TC-24 | Log request thành công | Gateway terminal | — | — | Log hiển thị | Logging |
| TC-25 | Log request bị từ chối | Gateway terminal | — | — | Log hiển thị | Logging |
| TC-26 | Wrong HTTP method | `/api/auth/login` | GET | — | 405 | Error Handling |
| TC-27 | Missing Content-Type | `/api/auth/login` | POST | — | 415/400 | Error Handling |
| TC-28 | Malformed JSON | `/api/auth/login` | POST | — | 400 | Error Handling |
| TC-29 | Token hết hạn | `/api/users` | GET | ❌ | 401 | Error Handling |
| TC-30 | Token bị sửa đổi | `/api/users` | GET | ❌ | 401 | Error Handling |

---

## 10. Checklist Test

Sử dụng checklist bên dưới để đánh dấu từng test case đã hoàn thành:

### Service Discovery
- [ ] TC-01: Eureka Dashboard hoạt động
- [ ] TC-02: Tất cả service đăng ký thành công (4 instances)
- [ ] TC-03: Eureka REST API trả về danh sách services

### Authentication (Auth Service)
- [ ] TC-04: Login thành công — admin
- [ ] TC-05: Login thành công — user
- [ ] TC-06: Login thành công — demo
- [ ] TC-07: Login thất bại — sai password
- [ ] TC-08: Login thất bại — user không tồn tại
- [ ] TC-09: Login thất bại — thiếu username
- [ ] TC-10: Login thất bại — thiếu password
- [ ] TC-11: Login thất bại — body rỗng
- [ ] TC-12: Login trực tiếp Auth Service (bypass Gateway)

### JWT Filter (API Gateway)
- [ ] TC-13: Request không có token → 401
- [ ] TC-14: Token không có prefix "Bearer" → 401
- [ ] TC-15: Token giả/không hợp lệ → 401
- [ ] TC-16: Token hợp lệ → 200, trả về data
- [ ] TC-17: Open endpoint không cần token

### Gateway Routing
- [ ] TC-18: Route `/api/auth/**` → auth-service, `/api/users/**` → user-service
- [ ] TC-19: Route không tồn tại → 404

### User Service
- [ ] TC-20: Gọi trực tiếp Instance 1 (port 9002)
- [ ] TC-21: Gọi trực tiếp Instance 2 (port 9003)

### Load Balancing
- [ ] TC-22: Request xen kẽ giữa port 9002 và 9003
- [ ] TC-23: Tắt 1 instance → request chỉ đến instance còn lại

### Logging
- [ ] TC-24: Log hiển thị khi request thành công
- [ ] TC-25: Log hiển thị khi request bị từ chối (401)

### Edge Cases & Error Handling
- [ ] TC-26: Wrong HTTP method → 405
- [ ] TC-27: Missing Content-Type → 415/400
- [ ] TC-28: Malformed JSON body → 400
- [ ] TC-29: Token hết hạn → 401
- [ ] TC-30: Token bị chỉnh sửa → 401

---

> 📝 **Tổng cộng: 30 test cases** covering tất cả các chức năng chính của hệ thống microservices.
>
> Nếu tất cả test cases đều **PASS**, hệ thống hoạt động đúng theo thiết kế.
