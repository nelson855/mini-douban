## Why

mini-douban 是一个从零开始的个人学习项目，目标是让一名 Java 后端开发者通过构建"豆瓣式"的电影评分网站逐步成长为全栈工程师。项目时间预算紧（每周 5 小时），前端经验接近于零，因此 v0.1 必须严格控制在最小可运行闭环内，避免陷入半成品泥潭、失去反馈正循环。本提案定义这个 MVP 的范围、技术选型和交付目标。

## What Changes

- 从零搭建 monorepo 项目骨架：`backend/`（Spring Boot）+ `frontend/`（Vue 3）+ `docs/`
- 实现用户注册与登录能力（用户名 + 密码，JWT 无状态鉴权）
- 实现电影浏览能力（预置 10-20 部种子数据，列表页 + 详情页，无搜索、无分页、无筛选）
- 实现电影评分能力（1-5 星，每位用户对每部电影至多一条评分，可更新；聚合计算全站平均分）
- 建立后端按领域分包的代码组织约定（`user/ movie/ rating/ auth/ common/`），而非按技术层分层
- 使用 Flyway 管理所有数据库 schema 与种子数据，迁移文件纳入版本控制

**明确不做（v0.1 范围外，留待后续版本）**：短评/长评、收藏/想看/看过、用户主页、头像上传、关注关系、评论、搜索、分页、忘记密码、邮箱验证、管理后台、Docker 化、CI/CD、i18n、深色模式、生产部署。

## Capabilities

### New Capabilities

- `user-auth`: 用户注册、登录，基于 JWT 的无状态鉴权，前端拦截器自动注入 token
- `movie-catalog`: 电影条目的浏览能力（列表、详情）；数据通过 Flyway 种子预置，v0.1 不做电影 CRUD
- `movie-rating`: 用户对电影的 1-5 星评分，支持更新，聚合计算全站平均分

### Modified Capabilities

<!-- 无，首个版本 -->

## Impact

- **代码**：首次提交，新增 `backend/`（Maven 项目）与 `frontend/`（Vite 项目）两个子目录
- **数据库**：使用 H2 嵌入式数据库（file 模式，数据持久化到 `backend/data/`），开启 `MODE=PostgreSQL` 兼容模式以便未来平滑迁移到 PostgreSQL；包含 `users`、`movies`、`ratings` 三张表
- **依赖**：
  - 后端：Spring Boot 3.x、Spring Security 6、Spring Data JPA、Flyway Core、H2 数据库（嵌入式）、Lombok、jjwt（或 Spring Security 内置 JWT 支持）
  - 前端：Vue 3、TypeScript、Vite、Vue Router 4、Pinia、Axios、Element Plus
- **不引入**：Redis、消息队列、Elasticsearch、Docker、Nginx、SSR、SCSS 预处理器
- **本地运行要求**：JDK 21、Node.js 20+。数据库使用 H2 嵌入式（无需独立安装），通过 H2 浏览器控制台（`/h2-console`）查看数据
- **学习负债**：用户首次接触 Vue、TypeScript、Vite、JWT 实战；预估 Week 3（Spring Security）与 Week 5（前后端联调）为两个高风险周，可能超出 5h 预算
