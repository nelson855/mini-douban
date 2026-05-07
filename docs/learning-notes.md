# 学习笔记

## H2 与 Flyway

当前 OpenSpec 设计使用 Spring Boot 3.3.5 管理的 Flyway 版本配合 H2 file 模式。依赖只需要 `flyway-core` 和 `h2`，不再额外声明 `flyway-database-h2`。

H2 数据库 URL 使用：

```text
jdbc:h2:file:./data/minidouban;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
```

这样可以让本地数据跨后端重启保留，同时用 PostgreSQL 兼容模式降低后续迁移到 PostgreSQL 的摩擦。

## Maven 配置

本机 Maven settings 与 repository 不在项目目录内，运行 Maven 时应显式携带：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" <goal>
```

如果命令需要写入该 repository，Codex 需要按权限规则请求提升后执行。

## PowerShell 命令

在 Windows PowerShell 中，如果执行策略拦截 npm 或 OpenSpec 的 shim，优先使用：

- `npm.cmd`
- `openspec.cmd`

## JWT 登出

v0.1 的登出是前端清除 `localStorage` 中的 token 和用户信息。后端 JWT 仍会在过期前保持可验证；服务端黑名单、刷新 token、主动吊销属于后续版本能力。
