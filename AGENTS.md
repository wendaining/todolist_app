# AGENTS Handoff Guide

本文件用于让任意智能体（Copilot、Codex 等）快速理解项目并开始协作开发。

## 1. 项目一句话

一个跨平台私有 TodoList 项目，当前先做 Web MVP，核心是任务管理 + Token 云同步 + DDL 提醒。

## 2. 先读顺序（必须）

0. docs/AGENT-QUICKSTART.md（切换 Agent 时优先）
1. docs/START_HERE.md
2. docs/SPEC-v0.1.md
3. docs/DEV-CHECKLIST.md
4. .github/copilot-instructions.md

## 3. 当前技术决策（已确认）

1. 第一阶段主线：Java 21 + Spring Boot 3（后端）
2. 前端：React + TypeScript + Vite
3. 数据库：SQLite（MVP 阶段）
4. 同步冲突策略：LWW（最后写入优先）
5. 鉴权：Token（无账号密码）

## 4. 当前范围（MVP Only）

必须做：

1. 新建任务
2. 勾选完成与完成历史保留
3. 优先级（high/medium/low）
4. DDL 与到期前提醒
5. Token 云同步

暂不做：

1. 多人协作
2. 复杂 AI 自动规划
3. 复杂微服务拆分

## 5. 不可破坏的规则（Guardrails）

1. 不主动扩大需求范围，优先保证 MVP 闭环。
2. 若要修改核心规则（同步、鉴权、数据模型），先更新 docs/SPEC-v0.1.md 再改代码。
3. 每次代码变更后，优先更新 docs/DEV-CHECKLIST.md 的验收项或里程碑进度。
4. 建议每次只改一个小点，并给出可验证结果。

## 5.1 文档联动协议（强制）

1. 任意代码改动必须同步更新 docs/DEV-CHECKLIST.md（状态、验收项或证据）。
2. 若改动影响 API、数据模型、同步策略、鉴权规则，必须先更新 docs/SPEC-v0.1.md，再做代码变更。
3. 任意“make progress”行为（即使不改代码）也要在 docs/DEV-CHECKLIST.md 反映进度变化。
4. Agent 的交付说明必须同时包含：代码改动清单 + 文档改动清单。

## 6. 数据模型与最小 API（来源：SPEC-v0.1）

Task 字段：

1. id: string
2. title: string
3. status: todo | done
4. priority: high | medium | low
5. dueAt: datetime | null
6. createdAt: datetime
7. updatedAt: datetime
8. completedAt: datetime | null

最小 API：

1. POST /tasks
2. PATCH /tasks/{id}
3. GET /tasks
4. POST /sync/pull
5. POST /sync/push

## 7. 当前里程碑与下一步

1. 当前目标：M1 本地单端可用（无同步）
2. 下一步：建立 api + web 目录骨架
3. 然后：完成 Task 模型与最小 API，再做最小 Web 页面串通

## 8. Agent 协作建议（对任意 Agent 通用）

1. 开始前先输出 3-5 步最小计划。
2. 每完成一小步，给出验证方式（接口测试或页面操作路径）。
3. 若发现文档冲突：以 SPEC 为准，并在提交中注明差异修复。
4. 如果上下文不足，先读取本文件和 docs 下三份核心文档，不要猜测需求。

## 9. Agent 学习任务（本项目重点）

1. 使用一次子代理做只读审查：检查代码与 SPEC 一致性。
2. 增加一条文件级 instructions 并验证其生效。
3. 每个里程碑输出“规格对应项 + 验收证据”。

## 9.1 可直接复用的 Skill

1. Build/Test Skill: .github/skills/java-build-test/SKILL.md
2. 推荐触发词："构建项目并测试"、"build and test"、"运行构建验证"
3. Testing Skill: .github/skills/fullstack-testing/SKILL.md
4. 推荐触发词："搭建测试框架"、"运行全栈测试"、"补充测试用例"

## 10. 交接模板（给下一个 Agent）

可复制以下结构：

1. 当前里程碑：M1/M2/M3
2. 已完成项：
3. 进行中项：
4. 阻塞点：
5. 下一步最小动作：
6. 需要先阅读的文件：