# 项目协作规则

## 文档语言

- 本项目后续所有项目文档、计划、README、学习笔记、OpenSpec 辅助说明、执行总结默认使用中文。
- 代码标识符、包名、类名、函数名、配置 key、命令、HTTP 路径、commit message 可按工程惯例使用英文。
- 如果用户没有特别要求，不要把中文文档改写成英文，也不要新增英文版文档。

## OpenSpec 工作流

- 涉及 OpenSpec change 时，先读取对应 change 下的 `proposal.md`、`design.md`、`specs/**/spec.md`、`tasks.md`。
- 如果用户点名 `writing-plans`，只先产出执行计划并等待确认，不直接开始代码实现。
- 只有在实现和验证完成后，才更新 `openspec/changes/*/tasks.md` 的勾选状态。

## Maven 配置

- 本机 Maven settings 文件使用 `D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml`。
- 本机 Maven repository 使用 `D:\software\apache\apache-maven-3.8.1\repository`。
- 在本项目运行 Maven 命令时，默认显式携带：

```powershell
mvn -s "D:\software\apache\apache-maven-3.8.1\conf\settings-codex.xml" "-Dmaven.repo.local=D:\software\apache\apache-maven-3.8.1\repository" <goal>
```

- 如果 Maven 命令需要写入上述本地仓库路径，按 Codex 权限规则请求提升权限后再执行。
