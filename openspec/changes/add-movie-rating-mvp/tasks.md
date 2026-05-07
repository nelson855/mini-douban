## 1. 项目骨架与基础设施（Week 1）

- [x] 1.1 在仓库根创建 `backend/`、`frontend/`、`docs/` 三个子目录
- [x] 1.2 在 `backend/` 通过 `spring initializr` 生成 Maven 项目，groupId `com.minidouban`，artifactId `backend`，Java 21，Spring Boot 3.x
- [x] 1.3 在 `backend/pom.xml` 中添加依赖：Spring Web、Spring Data JPA、Spring Security、Spring Validation、Flyway Core、H2 数据库（`com.h2database:h2`）、Lombok、jjwt（或同类 JWT 库）
- [x] 1.4 在 `backend/src/main/resources/application.yml` 配置：数据源 URL `jdbc:h2:file:./data/minidouban;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`、用户名 `sa`、空密码；启用 H2 console（`spring.h2.console.enabled: true`）；JPA `ddl-auto: validate`（让 Flyway 管 schema）；Flyway 配置；JWT 密钥占位
- [x] 1.5 创建 `backend/src/main/java/com/minidouban/MiniDoubanApplication.java` 主类，`mvn spring-boot:run` 启动后访问 `http://localhost:8080/h2-console` 能看到空数据库（此时还没跑迁移）
- [x] 1.6 提交 `.gitignore` 更新，忽略 `backend/target/`、`backend/data/`（H2 数据文件）、`frontend/node_modules/`、`frontend/dist/`、`.idea/` 等

## 2. 数据库 schema 与种子数据（Week 1 尾 / Week 2 初）

- [x] 2.1 创建 `V1__create_users_table.sql`：`users(id BIGSERIAL PK, username VARCHAR(32) UNIQUE NOT NULL, password_hash VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)`
- [x] 2.2 创建 `V2__create_movies_table.sql`：按 `movie-catalog` spec 定义的字段
- [x] 2.3 创建 `V3__create_ratings_table.sql`：按 `movie-rating` spec 定义字段，含 `(user_id, movie_id)` 唯一约束、`score` CHECK 约束 (1-5)、外键
- [x] 2.4 创建 `V4__seed_movies.sql`：INSERT 至少 10 部电影的种子数据（`title`、`director`、`release_year`、`poster_url`、`synopsis`）
- [x] 2.5 启动应用验证所有 Flyway 迁移成功执行，表结构与约束正确

## 3. 后端领域实体与基础仓库（Week 2）

- [x] 3.1 在 `com.minidouban.user` 包创建 `User` JPA 实体（字段与数据库对齐，password_hash 字段映射）
- [x] 3.2 在 `com.minidouban.movie` 包创建 `Movie` JPA 实体
- [x] 3.3 在 `com.minidouban.rating` 包创建 `Rating` JPA 实体，含 `@ManyToOne` 关联到 User 与 Movie（或仅存 id 字段）
- [x] 3.4 为三个实体分别创建 `UserRepository`、`MovieRepository`、`RatingRepository`（`extends JpaRepository`）
- [x] 3.5 在 `RatingRepository` 中定义 `findByUserIdAndMovieId(userId, movieId)` 与统计聚合的查询方法（用于计算平均分、评分数）

## 4. 鉴权：Spring Security + JWT（Week 3，高风险周）

- [x] 4.1 创建 `com.minidouban.auth.JwtService`：封装签发、解析、校验 token 的逻辑，使用 HS256 + application.yml 中的密钥
- [x] 4.2 创建 `JwtAuthenticationFilter extends OncePerRequestFilter`：从 `Authorization: Bearer` 解析 token，成功则设置 `SecurityContextHolder`
- [x] 4.3 创建 `SecurityConfig`：禁用 CSRF、设置 session stateless、配置白名单（`/api/auth/**`、`GET /api/movies`、`GET /api/movies/{id}`）、挂载 JwtAuthenticationFilter、暴露 `AuthenticationManager` bean、配置 `BCryptPasswordEncoder`
- [x] 4.4 创建 `CorsConfig`：允许来源 `http://localhost:5173`，允许方法 GET/POST/PUT/DELETE/OPTIONS，允许头 Authorization、Content-Type
- [x] 4.5 在 `com.minidouban.auth` 创建 `AuthController`：实现 `POST /api/auth/register` 与 `POST /api/auth/login`
- [x] 4.6 实现 `UserService.register(username, password)`：校验用户名格式与唯一性、BCrypt 哈希密码、写库
- [x] 4.7 实现 `UserService.login(username, password)`：用 `AuthenticationManager` 认证、签发 JWT 返回
- [x] 4.8 实现 `GET /api/me` 接口返回当前登录用户信息，需鉴权
- [x] 4.9 使用 Postman / curl 手工验证：注册 → 登录拿 token → 带 token 访问 `/api/me` 成功；不带 token 返回 401

## 5. 后端业务接口：电影与评分（Week 3 尾 / Week 2 可提前做）

- [x] 5.1 在 `com.minidouban.common` 创建 `GlobalExceptionHandler`（`@RestControllerAdvice`）：统一处理 `BusinessException`、`MethodArgumentNotValidException`、`AccessDeniedException` 等，返回 `{ code, message }` 格式
- [x] 5.2 创建 `BusinessException` 基类与 `MovieNotFoundException`、`UsernameTakenException`、`InvalidScoreException` 等子类
- [x] 5.3 在 `com.minidouban.movie` 实现 `MovieController.list()` 对应 `GET /api/movies`，返回包含 `averageScore`、`ratingCount` 的 DTO 列表
- [x] 5.4 实现 `MovieController.detail(id, currentUser)` 对应 `GET /api/movies/{id}`，已登录用户额外返回 `myScore`
- [x] 5.5 在 `com.minidouban.rating` 实现 `RatingController.upsert(movieId, body, currentUser)` 对应 `PUT /api/movies/{id}/rating`
- [x] 5.6 实现 `RatingService.upsert(userId, movieId, score)`：校验 score 范围、电影存在性、存在则更新否则插入
- [x] 5.7 用 Postman 验证全部接口：列表、详情、打分、重复打分更新、未登录打分被拒

## 6. 前端项目骨架（Week 4）

- [x] 6.1 在 `frontend/` 运行 `npm create vite@latest . -- --template vue-ts` 生成 Vue 3 + TS 项目
- [x] 6.2 安装依赖：`vue-router@4`、`pinia`、`axios`、`element-plus`
- [x] 6.3 配置 `main.ts` 全局引入 Element Plus 与其样式
- [x] 6.4 配置 `vite.config.ts` 的 dev server 端口为 5173，并可选配置 `proxy` 备用
- [x] 6.5 创建目录结构：`src/pages/`、`src/components/`、`src/api/`、`src/stores/`、`src/router/`、`src/types/`
- [x] 6.6 在 `src/router/index.ts` 配置路由表：`/login`、`/movies`、`/movies/:id`、`*` 404
- [x] 6.7 创建 `src/types/` 下的领域类型定义：`User`、`Movie`、`MovieDetail`、`RatingResult`

## 7. 前端 API 与状态管理（Week 4-5）

- [x] 7.1 在 `src/api/http.ts` 创建 axios 实例，baseURL 指向 `http://localhost:8080`
- [x] 7.2 配置 axios 请求拦截器：从 Pinia `userStore` 读取 token，若存在则注入 `Authorization` 头
- [x] 7.3 配置 axios 响应拦截器：遇到 401 清空 `userStore` 并跳转 `/login`
- [x] 7.4 在 `src/stores/user.ts` 创建 `userStore`：state 含 `token`、`user`；actions 含 `login`、`logout`、`fetchMe`；token 持久化到 `localStorage`
- [x] 7.5 在 `src/api/auth.ts` 封装 `register`、`login`、`fetchMe`
- [x] 7.6 在 `src/api/movie.ts` 封装 `listMovies`、`getMovie`
- [x] 7.7 在 `src/api/rating.ts` 封装 `rateMovie(movieId, score)`

## 8. 前端页面（Week 5-6）

- [x] 8.1 `src/pages/LoginPage.vue`：Element Plus 表单，含登录/注册切换 tab；提交成功后写入 store 并跳转 `/movies`
- [x] 8.2 `src/pages/MovieListPage.vue`：页面加载时调用 `listMovies`，用 `el-card` 或 `el-table` 网格展示，点击跳转详情
- [x] 8.3 `src/components/MovieCard.vue`：展示海报、标题、导演、年份、平均分
- [x] 8.4 `src/pages/MovieDetailPage.vue`：展示电影详情 + `el-rate` 打分组件；未登录时打分按钮提示跳转登录；已登录时调用 `rateMovie` 并本地刷新 `myScore`、`averageScore`
- [x] 8.5 `src/pages/NotFoundPage.vue`：简单 404 提示
- [x] 8.6 创建全局布局组件 `src/components/AppHeader.vue`：显示登录用户名或登录入口，含登出按钮

## 9. 前后端联调与验收（Week 5-6）

- [x] 9.1 同时启动后端（8080）与前端（5173），验证 CORS 无错误
- [x] 9.2 端到端跑通：注册新用户 → 登录 → 跳转列表 → 进入详情 → 打分 → 刷新页面评分仍在
- [x] 9.3 端到端跑通：登出后访问详情页可查看但无法打分
- [x] 9.4 端到端跑通：刷新浏览器后 token 仍有效，用户保持登录态
- [x] 9.5 检查浏览器 Network 面板：所有受保护请求都带 `Authorization` 头
- [x] 9.6 检查数据库：`ratings` 表对同一用户同一电影始终只有一条记录
- [x] 9.7 停止后端进程后重新启动，前一次注册的用户与打分仍在（验证 H2 file 模式持久化生效）

## 10. 收尾与文档

- [x] 10.1 编写 `docs/local-setup.md`：记录 H2 本地数据库、后端启动、前端启动、H2 console 与数据重置的完整步骤
- [x] 10.2 编写 `backend/README.md` 与 `frontend/README.md`：简单说明目录结构与启动命令
- [x] 10.3 在 `docs/learning-notes.md` 记录 v0.1 过程中遇到的坑与解决方案，为 v0.2 积累
- [x] 10.4 将 JWT 密钥从 `application.yml` 改为读取环境变量（如 `JWT_SECRET`），避免提交到仓库
