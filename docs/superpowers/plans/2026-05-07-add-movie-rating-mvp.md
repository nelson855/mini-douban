# 电影评分 MVP 实施计划

> **给 agentic workers：** 必须使用子技能 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans`，按任务逐项执行本计划。步骤使用复选框（`- [ ]`）语法跟踪进度。

**目标：** 构建 mini-douban v0.1 MVP：注册、登录、浏览预置电影、给电影评分、更新已有评分，并在本地持久化数据。

**架构：** 创建 `backend/`、`frontend/`、`docs/` 三个子目录组成的 monorepo。后端使用 Java 21 + Spring Boot 3，按领域分包（`user`、`auth`、`movie`、`rating`、`common`），通过 Flyway 管理 H2 file 模式数据库，使用 JWT 做无状态鉴权。前端使用 Vue 3 + TypeScript + Vite，配合 Pinia、Vue Router、Axios、Element Plus 完成基础交互。

**技术栈：** Java 21、Maven、Spring Boot 3.x、Spring Web MVC、Spring Data JPA、Spring Security 6、Flyway Core、H2、JJWT、Vue 3、TypeScript、Vite、Pinia、Axios、Vue Router 4、Element Plus。

---

## 规范来源

- OpenSpec change：`openspec/changes/add-movie-rating-mvp`
- 提案：`openspec/changes/add-movie-rating-mvp/proposal.md`
- 设计：`openspec/changes/add-movie-rating-mvp/design.md`
- 规格：
  - `openspec/changes/add-movie-rating-mvp/specs/user-auth/spec.md`
  - `openspec/changes/add-movie-rating-mvp/specs/movie-catalog/spec.md`
  - `openspec/changes/add-movie-rating-mvp/specs/movie-rating/spec.md`
- 任务列表：`openspec/changes/add-movie-rating-mvp/tasks.md`
- 已通过 `openspec.cmd instructions apply --change "add-movie-rating-mvp" --json` 确认：schema 为 `spec-driven`，共有 63 个待完成任务。

## 文件结构

需要创建或修改的主要区域：

- `backend/pom.xml`：Maven 项目与依赖；H2 使用 `flyway-core` 支持，不引入 `flyway-database-h2`。
- `backend/src/main/resources/application.yml`：H2 file 数据库、Flyway、JWT、H2 console 配置。
- `backend/src/main/resources/db/migration/*.sql`：Flyway schema 与种子数据。
- `backend/src/main/java/com/minidouban/MiniDoubanApplication.java`：Spring Boot 启动类。
- `backend/src/main/java/com/minidouban/common/*`：统一错误模型、业务异常、全局异常处理。
- `backend/src/main/java/com/minidouban/user/*`：用户实体、仓库、服务、DTO。
- `backend/src/main/java/com/minidouban/auth/*`：鉴权控制器、JWT 服务、JWT 过滤器、安全配置、CORS 配置。
- `backend/src/main/java/com/minidouban/movie/*`：电影实体、仓库、服务、控制器、DTO。
- `backend/src/main/java/com/minidouban/rating/*`：评分实体、仓库、服务、控制器、DTO。
- `backend/src/test/java/com/minidouban/*`：后端 MVP 闭环冒烟/契约测试。
- `frontend/package.json`、`frontend/vite.config.ts`、`frontend/src/*`：Vue 应用、路由、状态、API 客户端、页面、组件。
- `docs/local-setup.md`：本地启动与排障说明。
- `docs/learning-notes.md`：学习笔记与已知坑。
- `.gitignore`：忽略构建产物、本地 H2 数据、前端依赖等。

## 实施任务

### 任务 1：后端项目骨架

**文件：**
- 创建：`backend/pom.xml`
- 创建：`backend/src/main/java/com/minidouban/MiniDoubanApplication.java`
- 创建：`backend/src/main/resources/application.yml`
- 修改：`.gitignore`

- [ ] **步骤 1：创建 Maven 项目文件**

使用 Spring Boot parent `3.3.5`、Java `21`，依赖包括：

```xml
<dependencies>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
  <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>${jjwt.version}</version></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
  <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
  <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
</dependencies>
```

- [ ] **步骤 2：配置 `application.yml`**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/minidouban;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true

app:
  jwt:
    secret: ${JWT_SECRET:dev-secret-change-me-dev-secret-change-me}
    expiration-hours: 24
```

- [ ] **步骤 3：添加启动类**

```java
package com.minidouban;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MiniDoubanApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniDoubanApplication.class, args);
    }
}
```

- [ ] **步骤 4：运行编译**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：依赖解析通过，构建进入 Spring context 启动阶段，并因为 Flyway 迁移和实体尚未创建而失败。

- [ ] **步骤 5：提交**

```bash
git add .gitignore backend/pom.xml backend/src/main backend/src/main/resources
git commit -m "chore: scaffold backend application"
```

### 任务 2：后端 MVP 契约测试

**文件：**
- 创建：`backend/src/test/java/com/minidouban/MiniDoubanApplicationTests.java`

- [ ] **步骤 1：先写失败的冒烟测试**

用一个 Spring Boot 集成测试覆盖以下行为：

```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:minidouban-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "app.jwt.secret=test-secret-test-secret-test-secret-test-secret",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class MiniDoubanApplicationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void movieRatingMvpFlowWorks() throws Exception {
        // GET /api/movies 返回至少 10 部种子电影。
        // POST /api/auth/register 创建用户且不返回任何密码字段。
        // POST /api/auth/login 返回 token 和 user。
        // GET /api/me 携带 Bearer token 时成功。
        // PUT /api/movies/1/rating 不带 token 时返回 401。
        // PUT /api/movies/1/rating 首次打 4 分，ratingCount 为 1。
        // PUT /api/movies/1/rating 再次打 5 分，更新同一条记录，ratingCount 仍为 1。
        // GET /api/movies/1 登录用户可看到 myScore。
        // PUT score 6 返回 INVALID_SCORE。
        // GET /api/movies/9999 返回 MOVIE_NOT_FOUND。
    }
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：失败原因是控制器、实体、迁移和安全配置尚未实现。

- [ ] **步骤 3：提交**

```bash
git add backend/src/test/java/com/minidouban/MiniDoubanApplicationTests.java
git commit -m "test: define movie rating mvp flow"
```

### 任务 3：Flyway Schema 与种子数据

**文件：**
- 创建：`backend/src/main/resources/db/migration/V1__create_users_table.sql`
- 创建：`backend/src/main/resources/db/migration/V2__create_movies_table.sql`
- 创建：`backend/src/main/resources/db/migration/V3__create_ratings_table.sql`
- 创建：`backend/src/main/resources/db/migration/V4__seed_movies.sql`

- [ ] **步骤 1：创建 `users` 表**

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(32) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **步骤 2：创建 `movies` 表**

```sql
CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    director VARCHAR(255),
    release_year INTEGER,
    poster_url VARCHAR(1024),
    synopsis TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **步骤 3：创建 `ratings` 表**

```sql
CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    score SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ratings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ratings_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT uk_ratings_user_movie UNIQUE (user_id, movie_id),
    CONSTRAINT ck_ratings_score CHECK (score BETWEEN 1 AND 5)
);
```

- [ ] **步骤 4：写入 10 部电影种子数据**

插入 10 条 `title` 非空的记录，至少包括：`The Shawshank Redemption`、`Farewell My Concubine`、`Inception`、`Spirited Away`、`Parasite`、`Interstellar`、`The Godfather`、`Chungking Express`、`Coco`、`The Wandering Earth`。

- [ ] **步骤 5：通过测试触发迁移**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：仍然失败，因为 Java 实体和控制器尚未实现；但 Flyway 不应再是根因。

- [ ] **步骤 6：提交**

```bash
git add backend/src/main/resources/db/migration
git commit -m "feat: add database migrations and seed movies"
```

### 任务 4：领域实体与仓库

**文件：**
- 创建：`backend/src/main/java/com/minidouban/user/User.java`
- 创建：`backend/src/main/java/com/minidouban/user/UserRepository.java`
- 创建：`backend/src/main/java/com/minidouban/movie/Movie.java`
- 创建：`backend/src/main/java/com/minidouban/movie/MovieRepository.java`
- 创建：`backend/src/main/java/com/minidouban/rating/Rating.java`
- 创建：`backend/src/main/java/com/minidouban/rating/RatingRepository.java`

- [ ] **步骤 1：创建 JPA 实体**

使用 `@Entity`、`@Table`、字段级 `@Column`，时间字段使用 `LocalDateTime`，Java 字段使用 camelCase 并映射到 snake_case 数据库列。`Rating` 使用 `@ManyToOne(fetch = FetchType.LAZY)` 关联 `User` 与 `Movie`。

- [ ] **步骤 2：创建仓库接口**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

```java
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
```

```java
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndMovieId(Long userId, Long movieId);

    @Query("select new com.minidouban.rating.RatingStats(r.movie.id, avg(r.score), count(r)) from Rating r group by r.movie.id")
    List<RatingStats> findAllStats();

    @Query("select new com.minidouban.rating.RatingStats(r.movie.id, avg(r.score), count(r)) from Rating r where r.movie.id = :movieId group by r.movie.id")
    Optional<RatingStats> findStatsByMovieId(Long movieId);
}
```

- [ ] **步骤 3：运行测试**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：失败推进到缺少 API 或安全相关类。

- [ ] **步骤 4：提交**

```bash
git add backend/src/main/java/com/minidouban/user backend/src/main/java/com/minidouban/movie backend/src/main/java/com/minidouban/rating
git commit -m "feat: add domain entities and repositories"
```

### 任务 5：统一 API 错误处理

**文件：**
- 创建：`backend/src/main/java/com/minidouban/common/ApiError.java`
- 创建：`backend/src/main/java/com/minidouban/common/BusinessException.java`
- 创建：`backend/src/main/java/com/minidouban/common/GlobalExceptionHandler.java`
- 创建：`backend/src/main/java/com/minidouban/common/CurrentUser.java`

- [ ] **步骤 1：添加错误模型与业务异常基类**

```java
public record ApiError(String code, String message) {}
```

`BusinessException` 需要保存 `HttpStatus`、`code`、`message`。

- [ ] **步骤 2：添加全局异常处理器**

映射规则：
- `BusinessException` 使用自身状态码与 `{ code, message }`。
- `MethodArgumentNotValidException` 根据字段返回 `400 INVALID_CREDENTIALS_FORMAT` 或 `400 INVALID_SCORE`。
- `AuthenticationException` 与缺失 token 场景返回 `401 UNAUTHORIZED`。
- `AccessDeniedException` 返回 `403 FORBIDDEN`。

- [ ] **步骤 3：运行测试**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：仍失败，直到控制器和服务开始抛出这些异常。

- [ ] **步骤 4：提交**

```bash
git add backend/src/main/java/com/minidouban/common
git commit -m "feat: add common api error handling"
```

### 任务 6：鉴权与用户 API

**文件：**
- 创建：`backend/src/main/java/com/minidouban/auth/AuthController.java`
- 创建：`backend/src/main/java/com/minidouban/auth/JwtAuthenticationFilter.java`
- 创建：`backend/src/main/java/com/minidouban/auth/JwtService.java`
- 创建：`backend/src/main/java/com/minidouban/auth/SecurityConfig.java`
- 创建：`backend/src/main/java/com/minidouban/auth/CorsConfig.java`
- 创建：`backend/src/main/java/com/minidouban/user/UserService.java`
- 创建 DTO record：位于 `backend/src/main/java/com/minidouban/auth/` 与 `backend/src/main/java/com/minidouban/user/`

- [ ] **步骤 1：实现 DTO**

使用 records：
- `RegisterRequest(String username, String password)`
- `LoginRequest(String username, String password)`
- `LoginResponse(String token, UserResponse user)`
- `UserResponse(Long id, String username, LocalDateTime createdAt)`

校验规则：
- username：`@Pattern(regexp = "^[A-Za-z0-9_]{3,32}$")`
- password：`@Size(min = 6)`

- [ ] **步骤 2：实现用户服务**

注册时检查用户名唯一性，使用 BCrypt 哈希密码，返回 `UserResponse`。登录时校验用户名和密码，但错误响应不得泄露到底是用户名不存在还是密码错误。

- [ ] **步骤 3：实现 JWT 服务**

使用 HS256，`sub` 为用户 id 字符串，包含 `iat`、`exp`，过期时间来自配置，默认 24 小时。

- [ ] **步骤 4：实现 Spring Security 配置**

白名单：
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/movies`
- `GET /api/movies/{id}`
- 本地开发用 H2 console 路径

其他接口必须鉴权。Session 策略为 stateless，禁用 CSRF，并允许 H2 console frame。

- [ ] **步骤 5：实现控制器**

路由：
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/me`

- [ ] **步骤 6：运行测试**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：鉴权相关流程通过；电影和评分接口可能仍失败。

- [ ] **步骤 7：提交**

```bash
git add backend/src/main/java/com/minidouban/auth backend/src/main/java/com/minidouban/user
git commit -m "feat: add jwt authentication"
```

### 任务 7：电影与评分 API

**文件：**
- 创建：`backend/src/main/java/com/minidouban/movie/MovieController.java`
- 创建：`backend/src/main/java/com/minidouban/movie/MovieService.java`
- 创建 DTO records：位于 `backend/src/main/java/com/minidouban/movie/`
- 创建：`backend/src/main/java/com/minidouban/rating/RatingController.java`
- 创建：`backend/src/main/java/com/minidouban/rating/RatingService.java`
- 创建 DTO records：位于 `backend/src/main/java/com/minidouban/rating/`

- [ ] **步骤 1：实现电影 DTO**

列表与详情使用统一字段：

```java
public record MovieResponse(
    Long id,
    String title,
    String director,
    Integer releaseYear,
    String posterUrl,
    String synopsis,
    BigDecimal averageScore,
    long ratingCount,
    Integer myScore
) {}
```

列表接口中 `myScore` 设置为 `null`。

- [ ] **步骤 2：实现电影服务**

按 `id` 升序查询所有电影，从评分仓库聚合平均分和评分数，平均分保留 1 位小数；无评分时 `averageScore` 返回 `null`。

- [ ] **步骤 3：实现评分 DTO**

```java
public record RatingRequest(@Min(1) @Max(5) Integer score) {}
public record RatingResponse(Long movieId, Integer myScore, BigDecimal averageScore, long ratingCount) {}
```

- [ ] **步骤 4：实现评分服务**

校验电影存在；按 `(userId, movieId)` 查找现有评分；存在则更新，不存在则插入；更新 `updatedAt`；最后重新计算并返回评分统计。

- [ ] **步骤 5：实现控制器**

路由：
- `GET /api/movies`
- `GET /api/movies/{id}`
- `PUT /api/movies/{id}/rating`

- [ ] **步骤 6：运行后端测试**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：`MiniDoubanApplicationTests.movieRatingMvpFlowWorks` 通过。

- [ ] **步骤 7：提交**

```bash
git add backend/src/main/java/com/minidouban/movie backend/src/main/java/com/minidouban/rating backend/src/test
git commit -m "feat: add movie catalog and rating api"
```

### 任务 8：前端项目骨架

**文件：**
- 创建：`frontend/` 下的 Vite Vue 项目文件

- [ ] **步骤 1：生成前端项目**

运行：

```bash
npm.cmd create vite@latest frontend -- --template vue-ts
cd frontend
npm.cmd install
npm.cmd install vue-router@4 pinia axios element-plus
```

预期：`frontend/package.json` 包含 Vue、Vite、TypeScript、Vue Router、Pinia、Axios、Element Plus。

- [ ] **步骤 2：配置 Vite**

在 `frontend/vite.config.ts` 中将 dev server 端口设为 `5173`。

- [ ] **步骤 3：配置应用入口**

在 `frontend/src/main.ts` 中安装 Pinia、router、Element Plus，并引入 Element Plus CSS。

- [ ] **步骤 4：提交**

```bash
git add frontend
git commit -m "chore: scaffold frontend application"
```

### 任务 9：前端类型、API 客户端、状态与路由

**文件：**
- 创建：`frontend/src/types/domain.ts`
- 创建：`frontend/src/api/http.ts`
- 创建：`frontend/src/api/auth.ts`
- 创建：`frontend/src/api/movie.ts`
- 创建：`frontend/src/api/rating.ts`
- 创建：`frontend/src/stores/user.ts`
- 创建：`frontend/src/router/index.ts`

- [ ] **步骤 1：添加领域类型**

类型包括：`User`、`LoginResponse`、`Movie`、`MovieDetail`、`RatingResult`、`ApiError`。

- [ ] **步骤 2：添加 Axios 实例**

`baseURL` 固定为 `http://localhost:8080`。请求拦截器从 user store 读取 token 并注入 `Authorization: Bearer <token>`。响应拦截器遇到 `401` 时清空登录态并跳转 `/login`。

- [ ] **步骤 3：添加用户 store**

状态：`token`、`user`。Actions：`login`、`register`、`logout`、`fetchMe`。Token 持久化到 `localStorage`。

- [ ] **步骤 4：添加 API 客户端函数**

函数：
- `register(username, password)`
- `login(username, password)`
- `fetchMe()`
- `listMovies()`
- `getMovie(id)`
- `rateMovie(movieId, score)`

- [ ] **步骤 5：添加路由**

路由：
- `/` 重定向到 `/movies`
- `/login`
- `/movies`
- `/movies/:id`
- 兜底 404

- [ ] **步骤 6：运行类型检查**

运行：`npm.cmd run build`

预期：如果页面尚未创建，可能因为路由引用缺失而失败；完成任务 10 后必须通过。

- [ ] **步骤 7：提交**

```bash
git add frontend/src/types frontend/src/api frontend/src/stores frontend/src/router
git commit -m "feat: add frontend api and state foundation"
```

### 任务 10：前端页面与组件

**文件：**
- 创建：`frontend/src/components/AppHeader.vue`
- 创建：`frontend/src/components/MovieCard.vue`
- 创建：`frontend/src/pages/LoginPage.vue`
- 创建：`frontend/src/pages/MovieListPage.vue`
- 创建：`frontend/src/pages/MovieDetailPage.vue`
- 创建：`frontend/src/pages/NotFoundPage.vue`
- 修改：`frontend/src/App.vue`
- 修改：`frontend/src/style.css`

- [ ] **步骤 1：实现 `AppHeader`**

顶部栏显示应用名称；登录时显示用户名和登出按钮；未登录时显示登录入口。

- [ ] **步骤 2：实现 `LoginPage`**

使用 Element Plus tabs 在登录和注册之间切换。登录或注册成功后写入 store，并跳转 `/movies`。

- [ ] **步骤 3：实现 `MovieListPage` 与 `MovieCard`**

页面挂载时调用 `listMovies()`。用电影卡片展示海报、标题、导演、年份、平均分、评分数。点击卡片跳转 `/movies/:id`。

- [ ] **步骤 4：实现 `MovieDetailPage`**

根据路由 id 获取电影详情。展示详情和 `el-rate`。未登录用户尝试评分时跳转 `/login`；已登录用户调用 `rateMovie`，并刷新 `myScore`、`averageScore`、`ratingCount`。

- [ ] **步骤 5：实现 `NotFoundPage` 和应用外壳**

`App.vue` 渲染 `AppHeader` 与 `router-view`。

- [ ] **步骤 6：运行前端构建**

运行：`npm.cmd run build`

预期：构建通过，没有 TypeScript 错误。

- [ ] **步骤 7：提交**

```bash
git add frontend/src
git commit -m "feat: add movie rating frontend"
```

### 任务 11：文档

**文件：**
- 创建：`docs/local-setup.md`
- 创建：`docs/learning-notes.md`
- 创建：`backend/README.md`
- 创建：`frontend/README.md`

- [ ] **步骤 1：编写本地启动文档**

记录所需版本、后端启动命令、前端启动命令、H2 console URL、JDBC URL、用户名 `sa`、空密码，以及如何删除 `backend/data/` 重置本地数据。

- [ ] **步骤 2：编写后端 README**

说明包结构、核心接口、Flyway 迁移位置、测试命令。

- [ ] **步骤 3：编写前端 README**

说明 Vite 命令、路由表、store/API 目录布局、预期后端地址。

- [ ] **步骤 4：编写学习笔记**

记录：H2 与 SQLite 作为嵌入式数据库，在 Flyway 10 的模块拆分中仍由 `flyway-core` 支持，不需要 `flyway-database-h2`；PowerShell execution policy 拦截时使用 `npm.cmd`、`openspec.cmd`；项目 Maven 命令使用 `D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml` 与 `D:\software\apache\apache-maven-3.8.1\repository`；v0.1 的 JWT 登出只是前端清除 token。

- [ ] **步骤 5：提交**

```bash
git add docs backend/README.md frontend/README.md
git commit -m "docs: add local setup notes"
```

### 任务 12：端到端验证与 OpenSpec 任务更新

**文件：**
- 修改：`openspec/changes/add-movie-rating-mvp/tasks.md`

- [ ] **步骤 1：运行后端测试**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

预期：构建成功，所有后端测试通过。

- [ ] **步骤 2：运行前端构建**

运行：`npm.cmd run build`

预期：构建成功，没有 TypeScript 错误。

- [ ] **步骤 3：启动后端**

运行：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" spring-boot:run
```

预期：后端监听 `http://localhost:8080`，Flyway 迁移完成，H2 console 可通过 `http://localhost:8080/h2-console` 访问。

- [ ] **步骤 4：启动前端**

运行：`npm.cmd run dev -- --host 127.0.0.1`

预期：前端监听 `http://127.0.0.1:5173`。

- [ ] **步骤 5：手工验收**

验证：
- 注册新用户。
- 登录后跳转 `/movies`。
- 电影列表至少显示 10 部电影。
- 登出状态仍可访问电影详情页。
- 登录用户可以给电影 1 打分。
- 再次给电影 1 打分会更新同一条记录，`ratingCount` 仍为 1。
- 刷新页面后 token 仍有效，用户保持登录态。
- 登出后仍可查看详情，但不能评分。
- 重启后端后，前一次注册的用户和评分仍在，证明 H2 file 模式持久化生效。

- [ ] **步骤 6：更新 OpenSpec 任务**

仅在验证通过后，将 `openspec/changes/add-movie-rating-mvp/tasks.md` 中已完成的任务勾选。

- [ ] **步骤 7：提交**

```bash
git add openspec/changes/add-movie-rating-mvp/tasks.md
git commit -m "chore: mark movie rating mvp tasks complete"
```

## 自检

- 规格覆盖：本计划覆盖用户注册/登录、JWT 鉴权、电影列表与详情、评分提交与更新、Flyway Core 数据库迁移、H2 持久化、CORS、前端登录态、文档要求。
- 占位扫描：没有使用 TBD、TODO、fill later 等占位式表达；关键步骤包含明确文件路径、命令和预期结果。
- 类型一致性：DTO 名称、接口路径、包名、响应字段与 OpenSpec 的 camelCase 要求一致。
- 设计修正：`docs/local-setup.md` 应记录 H2，而不是 PostgreSQL，因为已接受的 v0.1 设计明确使用 H2 file 模式，PostgreSQL 留到后续版本；H2 不需要 `flyway-database-h2`，只保留 `flyway-core`。

## 执行交接

计划已保存到 `docs/superpowers/plans/2026-05-07-add-movie-rating-mvp.md`。后续有两种执行方式：

**1. Subagent-Driven（推荐）**：每个任务派发独立 subagent，任务间进行审查，迭代速度更快。

**2. Inline Execution**：在当前会话中使用 `executing-plans` 批量执行，并在检查点回看。

请选择后续执行方式。
