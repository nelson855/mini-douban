# Mini Douban 后端

后端使用 Java 21、Spring Boot 3.3.5、Spring Web MVC、Spring Data JPA、Spring Security、Flyway、H2 和 JJWT，实现电影评分 MVP 的 API。

## 包结构

- `com.minidouban.auth`：注册、登录、JWT 生成与校验、Spring Security 配置、CORS 配置。
- `com.minidouban.user`：用户实体、仓库、服务和响应 DTO。
- `com.minidouban.movie`：电影实体、仓库、服务、控制器和响应 DTO。
- `com.minidouban.rating`：评分实体、仓库、服务、控制器和评分统计 DTO。
- `com.minidouban.common`：统一错误响应、业务异常、当前登录用户模型和全局异常处理。

## 核心接口

- `POST /api/auth/register`：注册用户。
- `POST /api/auth/login`：登录并返回 JWT。
- `GET /api/me`：读取当前登录用户。
- `GET /api/movies`：读取电影列表。
- `GET /api/movies/{id}`：读取电影详情，登录用户会返回 `myScore`。
- `PUT /api/movies/{id}/rating`：创建或更新当前用户对电影的评分。

错误响应统一为：

```json
{
  "code": "ERROR_CODE",
  "message": "错误说明"
}
```

## 数据库

Flyway 迁移位于 `src/main/resources/db/migration/`：

- `V1__create_users_table.sql`
- `V2__create_movies_table.sql`
- `V3__create_ratings_table.sql`
- `V4__seed_movies.sql`

本地数据库使用 H2 file 模式，数据目录为 `backend/data/`。

## 常用命令

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" spring-boot:run
```
