## ADDED Requirements

### Requirement: 电影数据模型

系统 SHALL 在数据库中维护 `movies` 表，包含以下字段：
- `id` BIGINT，主键，自增
- `title` VARCHAR(255) NOT NULL，电影名称
- `director` VARCHAR(255)，导演
- `release_year` INTEGER，上映年份
- `poster_url` VARCHAR(1024)，海报图片 URL
- `synopsis` TEXT，剧情简介
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

电影数据 MUST 通过 Flyway 迁移脚本预置 10-20 条种子数据；v0.1 不提供创建、修改、删除电影的接口。

#### Scenario: Flyway 迁移创建 movies 表并写入种子数据

- **WHEN** 后端应用首次启动，Flyway 执行所有 `V*__*.sql` 迁移
- **THEN** `movies` 表被创建
- **AND** 表中至少包含 10 条种子电影记录，每条记录的 `title` 非空

### Requirement: 电影列表查询

系统 SHALL 提供 `GET /api/movies` 接口返回全部电影。响应 MUST 为 JSON 数组，每个元素包含电影基础字段与全站平均评分 `averageScore`（若无评分则为 `null`）。此接口不要求鉴权。

列表 MUST 按 `id` 升序返回，v0.1 不支持排序参数、分页、搜索、筛选。

#### Scenario: 任意用户获取电影列表

- **WHEN** 客户端请求 `GET /api/movies`（无论是否携带 token）
- **THEN** 返回 HTTP 200 与包含所有电影的 JSON 数组
- **AND** 每个元素形如 `{ "id", "title", "director", "releaseYear", "posterUrl", "synopsis", "averageScore", "ratingCount" }`

#### Scenario: 尚无评分的电影

- **WHEN** 某电影在 `ratings` 表中没有任何记录
- **THEN** 响应中该电影的 `averageScore` 为 `null`
- **AND** `ratingCount` 为 `0`

### Requirement: 电影详情查询

系统 SHALL 提供 `GET /api/movies/{id}` 接口返回单个电影的详情。响应 MUST 包含电影基础字段、全站平均分、评分总数；若请求携带合法 JWT，响应 MUST 额外包含 `myScore` 字段（当前用户对该电影的评分，未评分则为 `null`）。此接口对未登录用户可访问。

#### Scenario: 已登录用户查看已评分的电影详情

- **WHEN** 已登录用户请求 `GET /api/movies/1`，且该用户对电影 1 已有评分 4 星
- **THEN** 返回 HTTP 200 与 `{ "id": 1, "title": ..., "averageScore": 3.8, "ratingCount": 5, "myScore": 4 }`

#### Scenario: 未登录访客查看电影详情

- **WHEN** 未携带 token 的访客请求 `GET /api/movies/1`
- **THEN** 返回 HTTP 200
- **AND** 响应中 `myScore` 字段不存在或值为 `null`

#### Scenario: 电影不存在

- **WHEN** 请求 `GET /api/movies/{id}` 时 `id` 在数据库中不存在
- **THEN** 返回 HTTP 404 与 `{ "code": "MOVIE_NOT_FOUND", "message": "电影不存在" }`

### Requirement: 电影数据响应一致性

所有返回电影数据的接口（列表、详情）MUST 使用统一的字段命名（camelCase），且字段命名在列表与详情间保持一致。海报 URL 若数据库中为空，MUST 在响应中返回 `null`（不使用空字符串、不做默认图片填充）。

#### Scenario: 海报 URL 为空的电影

- **WHEN** 某电影 `poster_url` 在数据库中为 NULL
- **THEN** 响应中该电影 `posterUrl` 为 JSON `null`
