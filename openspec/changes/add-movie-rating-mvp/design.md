## Context

这是 mini-douban 项目的首个 change，用户是一名有多年 Java 经验、但几乎没有前端经验的开发者，希望通过构建一个"豆瓣式"电影评分网站成长为全栈工程师。核心约束如下：

- **时间预算**：每周 5 小时，v0.1 目标 4-6 周交付
- **运行环境**：仅本地运行，不考虑生产部署、性能优化、高可用
- **已决定的技术边界**：后端 Java 21 + Maven；前端 Vue 3；前后端分离

本设计文档聚焦在 v0.1 的架构取舍与关键技术决策上，记录"为什么这么选"以便后续演进时有据可查。

## Goals / Non-Goals

**Goals:**
- 在有限时间预算内跑通一个前后端分离的完整闭环（注册 → 登录 → 浏览 → 打分 → 持久化）
- 建立可演进的代码组织方式（按领域分包），让 v0.2+ 加新功能时不用大改骨架
- 让用户通过这个项目真正学会 JWT 鉴权、前后端联调、Vue 基础等关键全栈技能
- 数据库 schema 与种子数据通过 Flyway 管理，保证环境可复现

**Non-Goals:**
- 不做生产级鉴权（OAuth2、多因素认证、会话管理）
- 不做性能优化（缓存、索引调优、查询优化）
- 不做自动化测试覆盖率要求（SpringBootTest 冒烟测试够用，不写 Controller / Service 单测）
- 不做 UI 精细化设计（Element Plus 默认样式即可，不写 SCSS，不做响应式适配）
- 不做 Docker 化、CI/CD、容器编排、反向代理

## Decisions

### 后端框架与分层

**决定**：Spring Boot 3.x + Spring Web MVC（同步）+ Spring Data JPA + Spring Security 6 + Flyway。

**理由**：
- Spring Boot 是 Java 生态最成熟的选择，学习资料最多，对求职最有价值
- 选 Web MVC 而非 WebFlux：响应式编程的心智负担对零前端经验的用户是额外负担，同步模型与用户后面学的 Vue 前端并无叠加收益
- JPA 对象建模思维比 MyBatis 的 SQL-first 更符合本项目的产品迭代节奏；后期真的遇到瓶颈再引入 JOOQ 或 MyBatis
- Spring Security 6 是鉴权必修课，踩一次坑收益极高

**替代方案**：Quarkus / Micronaut（生态小，资料少，不推荐初学）；MyBatis Plus（v0.2+ 可考虑）。

### 代码组织：按领域分包 vs 按技术层分包

**决定**：后端采用按领域分包（`user/`、`movie/`、`rating/`、`auth/`、`common/`），每个领域包内含自己的 Controller / Service / Repository / Entity。

**理由**：
- 项目规模增长后，按领域分包的代码更易读、易迁移（未来任何一块都可独立成模块或微服务）
- 避免 `controller/` 目录膨胀成几十个文件的常见反模式
- 学习目的上，按领域分包能帮用户建立"业务域"而非"技术层"的建模思维

**替代方案**：传统 `controller/service/repository/entity` 四层包结构——适合教程，不适合真实项目演进。

### 前端框架与 TypeScript

**决定**：Vue 3 + Composition API + `<script setup>` + TypeScript 强制启用。

**理由**：
- Vue 3 对 Java 背景的用户上手曲线更平缓，模板语法接近 Thymeleaf
- Composition API 是 Vue 3 的主流写法，Options API 已逐渐边缘化，直接学新的
- TypeScript：用户已有 Java 强类型背景，TS 是他的舒适区；纯 JavaScript 反而会让他在 "undefined" 类调试上浪费大量时间
- Element Plus 作为组件库：中文文档好、生态成熟，v0.1 不用自己写按钮和表格

**替代方案**：React（就业面更广，但学习曲线陡，和用户当前诉求不匹配）；Naive UI（设计更现代，但中文搜索结果少）。

### 鉴权：JWT 无状态 vs Session

**决定**：JWT 无状态鉴权。前端用 Axios 拦截器自动注入 `Authorization: Bearer <token>` 头；后端用 Spring Security 的 `OncePerRequestFilter` 解析 token 并设置 `SecurityContext`。

**理由**：
- 前后端分离架构下 JWT 是业界标配，用户必须学
- 无状态鉴权避免了会话粘性、跨域 Cookie 等复杂问题，对本地开发友好
- v0.1 不实现 token 黑名单、刷新 token 等高阶机制——简单签发、短过期（如 24 小时）即可

**替代方案**：Session + Cookie（需处理 CORS 的 `credentials: include`、SameSite 等细节，坑更多，且不符合前后端分离标配）。

**妥协点**：登出只能由前端清除本地 token，后端无法主动失效 token。对学习项目可接受，v0.2+ 若要改进可引入 Redis 黑名单或刷新 token 机制。

### 数据库：H2 嵌入式 vs PostgreSQL

**决定**：v0.1 使用 H2 数据库的 **file 模式**（数据持久化到本地文件 `backend/data/`），并启用 `MODE=PostgreSQL` 兼容模式。JDBC URL 形如 `jdbc:h2:file:./data/minidouban;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`。

**理由**：
- 5h/week 预算下，安装维护 PostgreSQL 是显著的入门摩擦，与"v0.1 跑通闭环"的核心学习目标无关
- file 模式而非 in-memory：用户重启项目后注册用户、打分数据仍在，反馈循环更贴近真实产品体验
- `MODE=PostgreSQL` 让 H2 模仿 PostgreSQL SQL 方言（`BIGSERIAL`、`TIMESTAMP DEFAULT CURRENT_TIMESTAMP` 等），未来切换到 PostgreSQL 时 Flyway 脚本基本无需修改
- H2 自带浏览器控制台（`/h2-console`），用户可以直接在浏览器看表、跑 SQL，对学习友好
- 切换到真实 PostgreSQL 留到 v0.2+ 作为独立学习专题——届时用户已经熟悉 Flyway，切换数据库会是一次高信息密度的学习体验

**替代方案**：
- PostgreSQL 16：更真实，但安装与运维负担在 v0.1 不值得付
- H2 in-memory：每次启动清空，会让"我刚打的分还在不在"这种学习反馈消失
- SQLite：JPA + SQLite 配合需要第三方方言库，社区资料少，不利于复用学习资源

**已知风险**：H2 即便在 PostgreSQL 兼容模式下，对部分高级特性（窗口函数细节、JSONB、全文检索）支持不完整。v0.1 的 SQL 都不会触及这些；但要求自己写 SQL 时保持 PostgreSQL 标准语法，避免依赖 H2 特有写法。

### 数据库迁移：Flyway

**决定**：保留 Flyway 并在 v0.1 第一天启用，运行在 H2 之上。所有表结构与种子数据（电影列表）通过 `V*__*.sql` 脚本管理；SQL 语法保持 PostgreSQL 兼容（借助 H2 的 `MODE=PostgreSQL`）。

**理由**：
- 用户主动要求把 Flyway 作为本次的学习内容——在熟悉的小项目里把工具用一遍，远比将来在压力下临时学要划算
- "schema 即代码"是 Java 后端工程师的基础能力，即使单人项目也值得养成习惯
- Flyway 本身与数据库无关，运行在 H2 上和 PostgreSQL 上几乎一致；将来切换数据库时迁移脚本可直接复用
- 纯 SQL 语法，比 Liquibase 的 XML/YAML 直观

**与 H2 配合的注意点**：
- H2 与 SQLite 作为嵌入式数据库，在 Flyway 10 的模块拆分中**保留在 `flyway-core` 内**，不需要额外的 `flyway-database-*` 模块；只有 PostgreSQL、MySQL、Oracle 等非嵌入式数据库才需要引入独立模块
- 写 SQL 尽量用 PostgreSQL 标准语法（如 `BIGSERIAL`、`TIMESTAMP DEFAULT CURRENT_TIMESTAMP`），避免 H2 特有写法，保证未来切换无痛
- Flyway 默认禁止修改已执行过的迁移（校验哈希）；v0.1 调试期如果想重做 schema，删掉 `backend/data/` 整个目录后重启即可全新开始——在单人学习阶段这是合法操作

**替代方案**：Spring Boot 内置的 `schema.sql` + `data.sql`——更简单但学不到 Flyway，且缺乏版本管理能力，无法支持后续迭代。

### 电影种子数据来源

**决定**：v0.1 通过 Flyway 迁移脚本手动插入 10-20 条数据，不对接任何外部 API。

**理由**：
- 对接 TMDB 等 API 会消耗 1-2 周预算在网络请求、数据清洗、key 管理上，挤占前后端联调的核心学习时间
- 手动数据已足够让列表页、详情页看起来不空
- v0.2+ 可将对接 TMDB 作为独立学习专题

### 前端状态管理：Pinia

**决定**：Pinia（Vue 官方推荐）管理登录态与用户信息；组件内部状态用 `ref`/`reactive`。

**理由**：
- Pinia 是 Vuex 的继任者，API 更简单，TS 支持更好
- v0.1 只需一个 `userStore`（token、当前用户），不过度设计

### API 响应格式与错误处理

**决定**：
- 成功响应直接返回业务数据（DTO），HTTP 状态码 2xx
- 错误响应统一结构：`{ "code": "USER_NOT_FOUND", "message": "用户不存在" }`，HTTP 状态码 4xx/5xx
- 后端使用 `@RestControllerAdvice` 全局异常处理器统一返回错误响应

**理由**：贴近 RESTful 语义，避免"万物皆 200 + body 里 code 字段"的反模式；同时保留错误码字段便于前端国际化与区分错误类型。

### CORS

**决定**：后端 `application.yml` 配置允许来源 `http://localhost:5173`（Vite 默认端口）；Vite dev server 同时也可配置 `proxy` 反代后端接口作为备选方案。

**理由**：两种方式都要让用户遇到一次 CORS 报错、理解其本质，选一种落地即可。推荐先用后端 CORS 配置，因为更贴近生产环境的做法。

## Risks / Trade-offs

- **[风险] 用户前端基础为零，Week 4 可能在 CSS / Vue 模板语法上卡住** → 缓解：v0.1 明确依赖 Element Plus 组件库，不手写任何基础 UI；卡住时先用默认样式，不追求美观
- **[风险] Week 3（Spring Security + JWT）与 Week 5（前后端联调）超预算概率高** → 缓解：路线图预留 5-10h buffer；若 Week 3 超时，允许 Week 4-5 顺延，不压缩范围
- **[风险] JPA 与 Hibernate 的延迟加载、N+1 问题在列表页可能引起困惑** → 缓解：v0.1 数据量极小（20 条），不优先处理；在 design.md 记录此坑，v0.2+ 引入 `@EntityGraph` 或 DTO 投影
- **[权衡] 不做自动化测试 vs 学习最佳实践** → 承认这是权衡：5h/week 预算下写测试会挤占功能进度；v0.2+ 可专门开一个 change 补测试，当作独立学习模块
- **[权衡] JWT 无登出失效机制** → 接受，v0.1 不解；在 design.md 记录，未来通过 Redis 黑名单或短 token + refresh token 机制补齐
- **[风险] 用户习惯 Java 的 `@Autowired` 与接口驱动，可能对 Vue 的响应式数据流感到不适** → 缓解：鼓励用户在前端也建立"store → component → api"的清晰分层，类比后端 Service→Controller→Repository
