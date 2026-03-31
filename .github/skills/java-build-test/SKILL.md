---
name: java-build-test
description: "Use when user asks to 构建项目, build project, 进行测试, run tests, 构建并测试 for this Todo project."
---

# Java Build And Test Skill

## 1. 适用场景

当用户提出以下意图时使用本 Skill：

1. 构建项目
2. 运行测试
3. 构建并测试
4. 验证当前改动是否可运行

## 2. 执行前检查

1. 先读取 AGENTS.md、docs/DEV-CHECKLIST.md、docs/SPEC-v0.1.md。
2. 确保当前目录是仓库根目录。
3. 先执行 `source ~/.zshrc`，保证 JDK/Maven 环境变量已生效。

## 3. 标准执行流程

### 3.1 后端（api）

按顺序执行：

```bash
source ~/.zshrc
cd api
mvn clean test
```

若用户只要求编译不跑测试，可使用：

```bash
source ~/.zshrc
cd api
mvn clean package -DskipTests
```

### 3.2 前端（web）

按顺序执行：

```bash
cd web
npm install
npm run build
npm run test --if-present
```

## 4. 失败处理策略

1. 若是依赖下载失败，先重试一次。
2. 若是编译错误，优先输出首个关键报错位置和原因。
3. 若是测试失败，按失败用例逐条汇总，不要只给整段日志。

## 5. 输出格式（回复用户）

必须包含：

1. 后端构建结果（成功/失败）
2. 后端测试结果（通过数/失败数）
3. 前端构建结果（成功/失败）
4. 关键错误摘要（若失败）
5. 下一步最小修复建议（1-2 条）

## 6. 文档联动（强制）

1. 若本轮有代码修改，必须同步更新 docs/DEV-CHECKLIST.md。
2. 若本轮仅执行构建/测试，也要在 docs/DEV-CHECKLIST.md 的进度记录添加一条验证证据。
3. 若修复涉及 API、数据模型、同步、鉴权规则，先更新 docs/SPEC-v0.1.md 再改代码。
