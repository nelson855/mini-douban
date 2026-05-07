# 本地启动说明

## 环境要求

- JDK 21
- Maven 3.8.1
- Node.js 24.x
- npm 11.x

本项目在 Codex 环境中默认使用以下 Maven 配置：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" <goal>
```

## 启动后端

```powershell
cd backend
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" spring-boot:run
```

后端默认监听 `http://localhost:8080`。

H2 console 地址：`http://localhost:8080/h2-console`

连接信息：

- JDBC URL：`jdbc:h2:file:./data/minidouban;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`
- 用户名：`sa`
- 密码：空

## 启动前端

```powershell
cd frontend
npm.cmd install
npm.cmd run dev -- --host 127.0.0.1
```

前端默认监听 `http://127.0.0.1:5173`，并访问后端 `http://localhost:8080`。

## 测试与构建

后端测试：

```powershell
cd backend
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" test
```

前端构建：

```powershell
cd frontend
npm.cmd run build
```

## 重置本地数据

H2 file 模式会把数据写入 `backend/data/`。如果需要重置本地用户、评分和 Flyway 历史，先停止后端，再删除：

```powershell
Remove-Item -LiteralPath .\backend\data -Recurse -Force
```

重新启动后端后，Flyway 会重新建表并写入电影种子数据。
