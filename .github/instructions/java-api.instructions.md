---
applyTo: "api/**/*.java"
description: "Use when creating or modifying Java backend API code for this Todo project."
---

# Java API 开发约束（MVP）

## 1. 范围控制

1. 只实现 MVP 范围：任务管理、Token 同步、DDL 提醒相关最小后端能力。
2. 不主动新增多人协作、复杂 AI、微服务拆分。

## 2. 与 SPEC 一致性

1. Task 字段必须与 docs/SPEC-v0.1.md 一致：
   id, title, status, priority, dueAt, createdAt, updatedAt, completedAt。
2. 最小 API 必须保持一致：
   POST /tasks, PATCH /tasks/{id}, GET /tasks, POST /sync/pull, POST /sync/push。

## 3. 命名和结构

1. 请求体类统一后缀 Request，响应体类统一后缀 Response。
2. Controller 仅处理协议与校验；业务逻辑放在 Service。
3. 持久化模型与 API DTO 分离，避免直接暴露实体。

## 4. 输入校验

1. title 不能为空。
2. priority 仅允许 high/medium/low。
3. status 仅允许 todo/done。

## 5. 文档联动

1. 每次代码改动后，优先更新 docs/DEV-CHECKLIST.md 的对应验收项。
2. 若改动同步、鉴权、数据模型等核心规则，必须先更新 docs/SPEC-v0.1.md 再改代码。