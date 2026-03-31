# Agent Quickstart（跨智能体）

目标：让任何 Agent 在 1 分钟内进入同一项目上下文。

## 1. 切换 Agent 后第一条消息（直接复制）

请先阅读以下文件，并先复述后执行：
1. AGENTS.md
2. docs/START_HERE.md
3. docs/SPEC-v0.1.md
4. docs/DEV-CHECKLIST.md

复述要求：
1. 当前里程碑
2. MVP 范围与非目标
3. 下一步最小动作（只做一件事）
4. 验证方式

## 2. 让 Agent 执行任务的模板

请按下面格式推进，不要扩需求：
1. 先给 3-5 步最小计划
2. 只实现当前最小动作
3. 完成后给出可验证步骤
4. 更新 docs/DEV-CHECKLIST.md 对应项
5. 若改动 API/数据模型/同步/鉴权，先更新 docs/SPEC-v0.1.md 再改代码
6. 在总结中同时列出：代码改动文件 + 文档改动文件

## 3. 如果 Agent 没读懂上下文

追加这一句：
请以 docs/SPEC-v0.1.md 为唯一需求基准，若与其他文档冲突，以 SPEC 为准。

## 4. 当前项目固定事实（2026-03-31）

1. 主线技术栈：Java 21 + Spring Boot 3 + React + TypeScript + SQLite
2. 当前目标：M1 本地单端可用（无同步）
3. 核心规则：同步/鉴权/数据模型变更需先更新 SPEC

## 5. 常用触发语（可复制）

1. "请使用 java-build-test skill，对当前项目执行构建并测试，并给出失败摘要。"