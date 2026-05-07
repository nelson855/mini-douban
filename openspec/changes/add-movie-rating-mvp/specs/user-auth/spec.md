## ADDED Requirements

### Requirement: 用户注册

系统 SHALL 允许访客通过用户名和密码注册新账户。用户名 MUST 全局唯一，长度 3-32 字符，仅允许字母、数字、下划线。密码 MUST 至少 6 字符，后端 MUST 使用 BCrypt 进行哈希存储，严禁明文存储。

#### Scenario: 新用户使用合法凭证注册成功

- **WHEN** 访客向 `POST /api/auth/register` 提交 `{ "username": "alice", "password": "secret123" }`
- **THEN** 系统创建新用户，password 以 BCrypt 哈希存储
- **AND** 返回 HTTP 200 与用户基础信息 `{ "id", "username", "createdAt" }`（不返回密码字段）

#### Scenario: 用户名已存在时注册被拒绝

- **WHEN** 访客提交的 username 已在 `users` 表中存在
- **THEN** 系统返回 HTTP 409 与 `{ "code": "USERNAME_TAKEN", "message": "用户名已存在" }`
- **AND** 不创建任何记录

#### Scenario: 用户名或密码不符合格式要求

- **WHEN** 访客提交 username 长度超出 3-32 或包含非法字符，或 password 短于 6 字符
- **THEN** 系统返回 HTTP 400 与 `{ "code": "INVALID_CREDENTIALS_FORMAT", "message": "<具体字段>格式不正确" }`

### Requirement: 用户登录

系统 SHALL 允许已注册用户通过用户名和密码登录，成功时签发 JWT access token。Token 有效期 MUST 为 24 小时，负载 MUST 至少包含 `sub`（用户 ID）、`iat`、`exp`。

#### Scenario: 合法凭证登录成功

- **WHEN** 用户向 `POST /api/auth/login` 提交正确的 `{ "username", "password" }`
- **THEN** 系统验证 BCrypt 密码匹配后签发 JWT
- **AND** 返回 HTTP 200 与 `{ "token": "<jwt>", "user": { "id", "username" } }`

#### Scenario: 用户名不存在或密码错误

- **WHEN** 用户提交的 username 不存在，或 password 与哈希不匹配
- **THEN** 系统返回 HTTP 401 与 `{ "code": "INVALID_CREDENTIALS", "message": "用户名或密码错误" }`
- **AND** 错误响应对两种情况保持一致，不得泄露用户名是否存在

### Requirement: JWT 鉴权与请求识别

系统 SHALL 对所有非白名单接口要求请求头携带 `Authorization: Bearer <token>`。后端 MUST 在 Spring Security 过滤器链中解析 JWT，将用户身份注入 `SecurityContext`。白名单接口限定为 `/api/auth/register`、`/api/auth/login`、`GET /api/movies`、`GET /api/movies/{id}`。

#### Scenario: 已登录用户携带合法 token 访问受保护接口

- **WHEN** 用户携带 header `Authorization: Bearer <有效 token>` 访问 `PUT /api/movies/1/rating`
- **THEN** 系统解析 token，识别用户身份，并允许请求通过
- **AND** Controller 方法可通过 `@AuthenticationPrincipal` 或等价机制获取当前用户 ID

#### Scenario: 请求缺失 token 或 token 无效

- **WHEN** 未携带 `Authorization` 头、token 格式错误、签名无效或已过期的请求访问受保护接口
- **THEN** 系统返回 HTTP 401 与 `{ "code": "UNAUTHORIZED", "message": "请先登录" }`

#### Scenario: 获取当前用户信息

- **WHEN** 已登录用户请求 `GET /api/me`
- **THEN** 系统返回 HTTP 200 与 `{ "id", "username", "createdAt" }`
- **AND** 前端使用此接口在刷新后验证本地 token 是否仍然有效

### Requirement: 密码安全存储

系统 MUST 使用 BCrypt（工作因子 ≥ 10）对密码哈希；禁止使用 MD5、SHA-1、SHA-256 等未加盐哈希。用户实体中 MUST 仅保留 `password_hash` 字段，严禁持久化明文密码。任何 API 响应 MUST NOT 包含密码哈希字段。

#### Scenario: 注册时密码以 BCrypt 哈希写入数据库

- **WHEN** 用户完成注册
- **THEN** `users.password_hash` 列值以 BCrypt 字符串（`$2a$` / `$2b$` 开头）形式存储
- **AND** 原始密码在任何日志、返回体、数据库中均不出现

### Requirement: CORS 跨域支持

系统 SHALL 允许来自前端开发服务器（`http://localhost:5173`）的跨域请求。CORS 配置 MUST 允许 `GET/POST/PUT/DELETE/OPTIONS` 方法和 `Authorization`、`Content-Type` 请求头。

#### Scenario: 前端从 5173 端口访问后端 8080 端口

- **WHEN** 浏览器从 `http://localhost:5173` 向 `http://localhost:8080/api/movies` 发起请求
- **THEN** 响应头包含 `Access-Control-Allow-Origin: http://localhost:5173`
- **AND** 浏览器不会因 CORS 策略阻断响应
