# 🎓 Demo Kiến Trúc Microservices với Spring Boot

> Dự án demo gồm 4 service minh họa các khái niệm: **Service Discovery**, **API Gateway**, **Xác thực JWT**, và **Cân bằng tải (Load Balancing)**.

## Kiến trúc hệ thống

```
                        ┌──────────────────┐
                        │   Client (curl/  │
                        │    Postman)       │
                        └────────┬─────────┘
                                 │
                        ┌────────▼─────────┐
                        │   API Gateway    │
                        │   (port 8080)    │
                        │  • JWT Filter    │
                        │  • Logging       │
                        │  • Routing       │
                        └───┬─────────┬────┘
                            │         │
              ┌─────────────▼──┐  ┌───▼──────────────┐
              │  Auth Service  │  │  User Service     │
              │  (port 9001)   │  │  (port 9002,9003) │
              │  POST /login   │  │  GET /api/users   │
              └───────┬────────┘  └────┬──────┬───────┘
                      │                │      │
                ┌─────▼────────────────▼──────▼──────┐
                │       Eureka Server (port 8761)    │
                │         Service Registry           │
                └────────────────────────────────────┘
```

## Công nghệ sử dụng

| Công nghệ | Mục đích |
|---|---|
| Java 17 | Ngôn ngữ lập trình |
| Spring Boot 3.2.5 | Framework chính |
| Spring Cloud 2023.0.1 | Hỗ trợ microservices |
| Spring Cloud Gateway | API Gateway (reactive) |
| Netflix Eureka | Đăng ký & phát hiện service |
| JJWT 0.12.5 | Tạo & xác thực JWT token |
| Maven | Build tool |

## Cấu trúc dự án

```
PTPMHDV/
├── eureka-server/       ← Service Discovery (port 8761)
├── api-gateway/         ← Gateway + JWT Filter + Logging (port 8080)
├── auth-service/        ← Xác thực & tạo JWT token (port 9001)
├── user-service/        ← Quản lý user, chạy 2 instance (port 9002, 9003)
├── TESTING_GUIDE.md     ← Hướng dẫn test đầy đủ (30 test cases)
└── README.md
```

## Các chức năng chính

| # | Chức năng | Mô tả |
|---|-----------|-------|
| 1 | **Service Discovery** | Tất cả service tự đăng ký với Eureka, không cần cấu hình URL cứng |
| 2 | **API Gateway** | Một điểm vào duy nhất (port 8080), tự động định tuyến tới service phù hợp |
| 3 | **Xác thực JWT** | Auth Service tạo token, Gateway xác thực token trước khi chuyển tiếp request |
| 4 | **Cân bằng tải** | 2 instance User Service, Gateway tự động phân phối request đều giữa chúng |
| 5 | **Ghi log tập trung** | Mọi request qua Gateway đều được log: method, path, status, thời gian xử lý |

## Hướng dẫn chạy

### Yêu cầu
- Java 17+ → `java -version`
- Maven → `mvn -version`

### Khởi động (theo đúng thứ tự)

> ⚠️ Mỗi service chạy trên **một terminal riêng**.

```powershell
# 1. Eureka Server
cd eureka-server; mvn spring-boot:run

# 2. Auth Service
cd auth-service; mvn spring-boot:run

# 3. User Service — Instance 1
cd user-service; mvn spring-boot:run

# 4. User Service — Instance 2
cd user-service; mvn spring-boot:run '-Dspring-boot.run.arguments=--server.port=9003'

# 5. API Gateway
cd api-gateway; mvn spring-boot:run
```

Kiểm tra Eureka Dashboard: **http://localhost:8761** — đảm bảo có đủ 4 instances.

## Test nhanh

### 1. Đăng nhập lấy JWT token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Tài khoản có sẵn: `admin/admin123`, `user/user123`, `demo/demo123`

### 2. Truy cập API (có token)

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <TOKEN>"
```

### 3. Truy cập API (không token → bị chặn)

```bash
curl http://localhost:8080/api/users
# → 401: "Missing or invalid Authorization header"
```

### 4. Kiểm tra cân bằng tải

Gọi nhiều lần, quan sát `serverPort` xen kẽ giữa `9002` và `9003`:

```powershell
for ($i=1; $i -le 6; $i++) {
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Headers @{"Authorization"="Bearer <TOKEN>"}
    Write-Host "Lần $i → Port: $($r.serverPort)"
}
```

> 📋 Xem hướng dẫn test chi tiết **30 test cases** tại [TESTING_GUIDE.md](TESTING_GUIDE.md)

## Bảng port

| Service | Port |
|---|---|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Auth Service | 9001 |
| User Service #1 | 9002 |
| User Service #2 | 9003 |
