## ADDED Requirements

### Requirement: 评分数据模型

系统 SHALL 在数据库中维护 `ratings` 表，包含以下字段：
- `id` BIGINT，主键，自增
- `user_id` BIGINT NOT NULL，外键指向 `users.id`
- `movie_id` BIGINT NOT NULL，外键指向 `movies.id`
- `score` SMALLINT NOT NULL，评分值（1-5）
- `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

表 MUST 对 `(user_id, movie_id)` 设置唯一约束，保证每位用户对每部电影至多一条评分记录。`score` MUST 通过 CHECK 约束或应用层校验限定在 1-5 整数范围。

#### Scenario: Flyway 迁移创建 ratings 表与约束

- **WHEN** 后端应用首次启动，Flyway 执行所有迁移
- **THEN** `ratings` 表被创建
- **AND** `(user_id, movie_id)` 唯一约束生效
- **AND** 插入 `score = 6` 的记录时数据库拒绝

### Requirement: 提交或更新评分

系统 SHALL 提供 `PUT /api/movies/{id}/rating` 接口让已登录用户对电影提交或更新评分。请求体 MUST 为 `{ "score": <1-5 整数> }`。若当前用户已对该电影评分过，接口 MUST 更新现有记录的 `score` 与 `updated_at`，而非新建记录。此接口 MUST 要求鉴权。

#### Scenario: 首次为电影打分

- **WHEN** 已登录用户向 `PUT /api/movies/1/rating` 提交 `{ "score": 4 }`，且该用户尚未对电影 1 评分
- **THEN** 系统在 `ratings` 表插入一条新记录
- **AND** 返回 HTTP 200 与 `{ "movieId": 1, "myScore": 4, "averageScore": <新平均分>, "ratingCount": <新评分数> }`

#### Scenario: 更新已有评分

- **WHEN** 已登录用户对电影 1 已有评分 3，再次提交 `{ "score": 5 }`
- **THEN** 系统更新原记录的 `score` 为 5，`updated_at` 刷新为当前时间
- **AND** `ratings` 表对 (user_id, movie_id=1) 仍只有一条记录
- **AND** 返回 HTTP 200 与最新的 `myScore`、`averageScore`、`ratingCount`

#### Scenario: 未登录用户尝试打分

- **WHEN** 请求未携带有效 JWT
- **THEN** 系统返回 HTTP 401 与 `{ "code": "UNAUTHORIZED", "message": "请先登录" }`
- **AND** `ratings` 表无任何写入

#### Scenario: 评分值越界

- **WHEN** 已登录用户提交 `{ "score": 0 }` 或 `{ "score": 6 }` 或非整数
- **THEN** 系统返回 HTTP 400 与 `{ "code": "INVALID_SCORE", "message": "评分必须是 1-5 的整数" }`
- **AND** 不写入任何记录

#### Scenario: 对不存在的电影打分

- **WHEN** 已登录用户请求 `PUT /api/movies/9999/rating`，但电影 9999 不存在
- **THEN** 系统返回 HTTP 404 与 `{ "code": "MOVIE_NOT_FOUND", "message": "电影不存在" }`

### Requirement: 平均分聚合计算

系统 SHALL 在电影列表与详情接口中返回 `averageScore`（全站平均分）与 `ratingCount`（评分总数）。`averageScore` MUST 保留 1 位小数（如 3.8），通过对 `ratings.score` 求算术平均得出。若某电影无评分记录，`averageScore` MUST 返回 `null`（而非 0）。

v0.1 允许实时聚合查询，不要求预计算或缓存。

#### Scenario: 有评分的电影返回准确平均分

- **WHEN** 电影 1 有评分 `[3, 4, 5]`
- **THEN** 列表与详情接口中 `averageScore` 为 `4.0`
- **AND** `ratingCount` 为 `3`

#### Scenario: 无评分的电影

- **WHEN** 电影 2 在 `ratings` 表无任何记录
- **THEN** 接口返回 `averageScore: null`
- **AND** `ratingCount: 0`
