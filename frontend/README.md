# Mini Douban 前端

前端使用 Vue 3、TypeScript、Vite、Vue Router、Pinia、Axios 和 Element Plus，实现登录、注册、电影列表、电影详情和评分交互。

## 命令

```powershell
npm.cmd install
```

```powershell
npm.cmd run dev -- --host 127.0.0.1
```

```powershell
npm.cmd run build
```

## 路由

- `/`：重定向到 `/movies`。
- `/login`：登录与注册页。
- `/movies`：电影列表页。
- `/movies/:id`：电影详情与评分页。
- `/:pathMatch(.*)*`：404 页面。

## 目录说明

- `src/api/`：Axios 实例和接口函数。
- `src/stores/user.ts`：登录态、token 持久化、用户信息。
- `src/router/index.ts`：路由配置。
- `src/pages/`：页面组件。
- `src/components/`：可复用组件。
- `src/types/domain.ts`：后端响应对应的 TypeScript 类型。

## 后端地址

前端 Axios `baseURL` 固定为 `http://localhost:8080`。本地开发时请先启动后端，再启动前端。
