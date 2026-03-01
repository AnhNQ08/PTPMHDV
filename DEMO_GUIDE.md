# 🎤 HƯỚNG DẪN THUYẾT TRÌNH DEMO

> Kịch bản demo chi tiết cho buổi thuyết trình môn **Phát Triển Phần Mềm Hướng Dịch Vụ**.
> Thời lượng demo: **10–15 phút**

---

## Chuẩn bị trước khi Demo

### Phần mềm cần mở sẵn
- [ ] **IntelliJ IDEA** — mở project `PTPMHDV`
- [ ] **Postman** — tạo sẵn Collection (hướng dẫn bên dưới)
- [ ] **Trình duyệt** — mở sẵn tab `http://localhost:8761`
- [ ] **5 Terminal** trong IntelliJ — mỗi terminal chạy 1 service

### Chạy hệ thống trước khi lên thuyết trình
Bật theo thứ tự, đợi mỗi service hiện `Started ...Application`:
1. `cd eureka-server; mvn spring-boot:run`
2. `cd auth-service; mvn spring-boot:run`
3. `cd user-service; mvn spring-boot:run`
4. `cd user-service; mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9003"`
5. `cd api-gateway; mvn spring-boot:run`

### Tạo Postman Collection sẵn

```
📁 PTPMHDV Demo
  ├── 🟢 1. Login (admin)                POST  http://localhost:8080/api/auth/login
  ├── 🟢 2. Login (sai password)          POST  http://localhost:8080/api/auth/login
  ├── 🔴 3. Users (không token)           GET   http://localhost:8080/api/users
  ├── 🟢 4. Users (có token)              GET   http://localhost:8080/api/users
  └── 🔁 5. Users (test Load Balancing)   GET   http://localhost:8080/api/users
```

**Chi tiết từng request Postman:**

| # | Method | URL | Headers | Body |
|---|--------|-----|---------|------|
| 1 | POST | `http://localhost:8080/api/auth/login` | `Content-Type: application/json` | `{"username":"admin","password":"admin123"}` |
| 2 | POST | `http://localhost:8080/api/auth/login` | `Content-Type: application/json` | `{"username":"admin","password":"saimatkhau"}` |
| 3 | GET | `http://localhost:8080/api/users` | *(không có Authorization)* | — |
| 4 | GET | `http://localhost:8080/api/users` | `Authorization: Bearer <token>` | — |
| 5 | GET | `http://localhost:8080/api/users` | `Authorization: Bearer <token>` | — |

---

## Kịch bản Demo

---

### 🔶 PHẦN 1: Giới thiệu kiến trúc (2 phút)

**Mở:** IntelliJ — show cấu trúc thư mục project

**Nói:**
> "Dự án gồm 4 service độc lập, mỗi service là một ứng dụng Spring Boot riêng:
> - **Eureka Server** — đóng vai trò Service Registry, nơi các service đăng ký
> - **Auth Service** — xử lý đăng nhập, tạo JWT token
> - **User Service** — cung cấp API lấy danh sách user, chạy 2 instance để demo load balancing
> - **API Gateway** — điểm vào duy nhất, xác thực JWT, ghi log, điều hướng request"

**Show:** Mở nhanh từng file chính
- `EurekaServerApplication.java` → chỉ annotation `@EnableEurekaServer`
- `AuthController.java` → chỉ hàm `login()` và mock users
- `UserController.java` → chỉ hàm `getUsers()` có trả `serverPort`
- `JwtAuthenticationFilter.java` → chỉ logic kiểm tra Authorization header

---

### 🔶 PHẦN 2: Service Discovery với Eureka (2 phút)

**Mở:** Trình duyệt → `http://localhost:8761`

**Nói:**
> "Đây là Eureka Dashboard. Tất cả service khi khởi động sẽ tự động đăng ký vào đây.
> Hiện tại có 4 instances đang chạy:
> - API-GATEWAY trên port 8080
> - AUTH-SERVICE trên port 9001
> - USER-SERVICE có 2 instances trên port 9002 và 9003
>
> Nhờ Eureka, Gateway không cần biết địa chỉ cụ thể của từng service.
> Nó chỉ cần gọi theo tên, ví dụ `lb://auth-service`, và Eureka sẽ trả về địa chỉ thực."

**Chỉ trên màn hình:**
- Bảng "Instances currently registered with Eureka"
- Highlight USER-SERVICE có 2 instances

---

### 🔶 PHẦN 3: Xác thực JWT — Login thành công (2 phút)

**Mở:** Postman → Request **"1. Login (admin)"**

**Nói:**
> "Bây giờ em sẽ demo chức năng xác thực. Client gửi POST request tới `/api/auth/login` với username và password."

**Thao tác:** Nhấn **Send**

**Nói (khi thấy kết quả):**
> "Server trả về 200 OK, gồm 3 trường:
> - `token` — đây là JWT token, được mã hóa bằng thuật toán HS384
> - `username` — tên user đã đăng nhập
> - `message` — Login successful
>
> Token này sẽ được dùng để truy cập các API được bảo vệ."

**Mẹo:** Copy token → lưu vào biến Postman hoặc note lại

---

### 🔶 PHẦN 4: Xác thực JWT — Login thất bại (1 phút)

**Mở:** Postman → Request **"2. Login (sai password)"**

**Nói:**
> "Nếu nhập sai mật khẩu, server sẽ từ chối."

**Thao tác:** Nhấn **Send**

**Nói:**
> "Server trả về 401 Unauthorized với message 'Invalid username or password'. Hệ thống không cho biết cụ thể là sai username hay password — đây là best practice về bảo mật."

---

### 🔶 PHẦN 5: API Gateway — Chặn request không có token (2 phút)

**Mở:** Postman → Request **"3. Users (không token)"**

**Nói:**
> "Bây giờ em sẽ thử truy cập API lấy danh sách user mà KHÔNG kèm JWT token."

**Thao tác:** Nhấn **Send**

**Nói:**
> "Gateway trả về 401 với message 'Missing or invalid Authorization header'.
> Đây chính là vai trò của **JwtAuthenticationFilter** — nó kiểm tra mọi request trước khi chuyển tiếp.
> Endpoint `/api/auth/login` được cấu hình là Open Endpoint nên không cần token, nhưng tất cả endpoint khác đều phải có."

**Mở nhanh:** `JwtAuthenticationFilter.java` → chỉ dòng `OPEN_ENDPOINTS` và logic kiểm tra

---

### 🔶 PHẦN 6: API Gateway — Truy cập với token hợp lệ (2 phút)

**Mở:** Postman → Request **"4. Users (có token)"**

**Thao tác:**
1. Trong tab Headers, thêm: `Authorization: Bearer <paste_token>`
2. Nhấn **Send**

**Nói:**
> "Lần này em gửi kèm JWT token trong header Authorization. Gateway xác thực token thành công và chuyển tiếp request tới User Service.
> Response trả về 200 OK gồm:
> - Danh sách 3 users
> - `serverPort: 9002` — cho biết instance nào đã xử lý request này
>
> Lưu ý: Client chỉ gọi tới Gateway port 8080, không cần biết User Service chạy ở port nào. Gateway tự động điều hướng nhờ Eureka."

---

### 🔶 PHẦN 7: Load Balancing (2 phút) ⭐ Điểm nhấn

**Mở:** Postman → Request **"5. Users (test Load Balancing)"**

**Nói:**
> "Phần quan trọng nhất — em sẽ demo cân bằng tải. User Service có 2 instances chạy trên port 9002 và 9003. Em sẽ gửi cùng một request nhiều lần."

**Thao tác:** Nhấn **Send** liên tục **6 lần**, mỗi lần chỉ vào `serverPort`

```
Lần 1: serverPort = 9002
Lần 2: serverPort = 9003  ← đổi rồi!
Lần 3: serverPort = 9002
Lần 4: serverPort = 9003
Lần 5: serverPort = 9002
Lần 6: serverPort = 9003
```

**Nói:**
> "Có thể thấy `serverPort` xen kẽ giữa 9002 và 9003. Điều này chứng minh Gateway đang phân phối request đều giữa 2 instances theo thuật toán **Round Robin**.
> Client hoàn toàn không biết có bao nhiêu instance đang chạy — tất cả được xử lý tự động bởi Eureka và Spring Cloud LoadBalancer."

---

### 🔶 PHẦN 8: Logging tập trung (1 phút)

**Mở:** Terminal IntelliJ đang chạy **API Gateway**

**Nói:**
> "Cuối cùng, em show phần logging. Mọi request đi qua Gateway đều được ghi log tự động bởi LoggingGlobalFilter."

**Chỉ trên log:**
```
>>> Incoming Request: POST /api/auth/login from /127.0.0.1:xxxxx
<<< Response: POST /api/auth/login | Status: 200 | Duration: 45 ms

>>> Incoming Request: GET /api/users from /127.0.0.1:xxxxx
<<< Response: GET /api/users | Status: 401 | Duration: 3 ms

>>> Incoming Request: GET /api/users from /127.0.0.1:xxxxx
<<< Response: GET /api/users | Status: 200 | Duration: 28 ms
```

**Nói:**
> "Log ghi nhận HTTP method, path, status code, và thời gian xử lý tính bằng millisecond. Cả request thành công lẫn bị từ chối đều được log — rất hữu ích cho việc monitoring và debug trong hệ thống microservices."

---

### 🔶 KẾT THÚC (30 giây)

**Nói:**
> "Tóm lại, dự án demo đã minh họa 5 khái niệm quan trọng trong kiến trúc microservices:
> 1. **Service Discovery** — Eureka tự động quản lý danh sách service
> 2. **API Gateway** — một điểm vào duy nhất, điều hướng request
> 3. **JWT Authentication** — xác thực bảo mật không cần session
> 4. **Load Balancing** — phân phối request tự động giữa các instance
> 5. **Centralized Logging** — ghi log tập trung tại Gateway
>
> Cảm ơn thầy/cô và các bạn đã lắng nghe. Em xin phép nhận câu hỏi."

---

## Xử lý câu hỏi thường gặp

| Câu hỏi | Gợi ý trả lời |
|---------|----------------|
| **Tại sao không dùng database?** | "Đây là demo tập trung vào kiến trúc microservices, nên dùng mock data để đơn giản hóa. Trong thực tế, mỗi service sẽ có database riêng (Database per Service pattern)." |
| **JWT có ưu điểm gì so với Session?** | "JWT là stateless — server không lưu session. Phù hợp với microservices vì mỗi service có thể xác thực độc lập chỉ cần biết secret key." |
| **Nếu Eureka chết thì sao?** | "Các service đã lưu cache danh sách trong bộ nhớ, nên vẫn hoạt động một thời gian. Trong production thường chạy Eureka cluster (2–3 nodes)." |
| **Load Balancing dùng thuật toán gì?** | "Spring Cloud LoadBalancer mặc định dùng Round Robin. Có thể tuỳ chỉnh sang Random hoặc Weighted." |
| **Gateway có phải single point of failure?** | "Đúng, trong demo chỉ có 1 instance. Production sẽ chạy nhiều Gateway instances phía sau một Load Balancer (ví dụ Nginx, AWS ALB)." |
| **Tại sao có 2 file JwtUtil?** | "Auth Service cần JwtUtil để TẠO token, Gateway cần JwtUtil để XÁC THỰC token. Hai class khác nhau nhưng dùng cùng secret key." |

---

## Mẹo thuyết trình

> 💡 **Focus vào live demo**, không nên chỉ đọc slide. Giám khảo thích thấy hệ thống chạy thật.

> 💡 **Nhấn mạnh `serverPort`** khi demo Load Balancing — đây là phần ấn tượng nhất, dễ thấy kết quả rõ ràng.

> 💡 **Mở code khi giải thích** — ví dụ khi nói về JWT Filter, mở file `JwtAuthenticationFilter.java` cho giám khảo thấy logic.

> 💡 **Giọng nói tự tin, chậm rãi**. Đừng vội — mỗi phần demo chỉ cần 1–2 phút.

> 💡 **Nếu bị lỗi khi demo**: bình tĩnh nói "Để em kiểm tra lại service", mở Eureka Dashboard xem service nào chưa UP.
