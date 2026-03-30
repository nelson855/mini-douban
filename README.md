# mini-douban

一个用于学习前端开发的「迷你豆瓣」项目。

当前阶段已完成：
- React + Vite 基础工程搭建
- 豆瓣电影标签页（热门 / 最新 / 高分榜）
- 本地 mock 数据驱动页面渲染

## 快速启动

```bash
npm install
npm run dev
```

打开终端提示的本地地址即可查看页面。

## 目录结构

```text
src
├─ components
│  ├─ MovieCard.jsx      # 电影卡片
│  └─ MovieTabs.jsx      # 标签切换
├─ data
│  └─ movieData.js       # mock 数据
├─ App.jsx               # 页面容器
├─ main.jsx              # 入口
└─ styles.css            # 样式
```

## 学习建议

你可以先从这 3 个点入手：
1. 看 `useState` 如何控制当前标签
2. 看 `map` 如何把数据渲染成卡片列表
3. 尝试自己新增一个标签，例如「即将上映」

## 协作与执行说明

- 协作规则：`docs/COLLAB_RULES.md`
- 每轮命令执行清单：`docs/RUN_STEPS.md`
