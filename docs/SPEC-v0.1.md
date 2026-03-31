# Todo 项目规格 v0.1

## 1. 项目目标

1. 学习覆盖面最大化
2. 产出可写进简历的完整项目
3. 先 Web，后续扩展客户端

## 2. 功能范围（MVP）

1. 新建任务
2. 勾选完成
3. 完成历史保留
4. 设置优先级（高/中/低）
5. 设置 DDL
6. DDL 前提醒
7. 云同步（Token）

## 3. 非目标（MVP 不做）

1. 多人协作与团队权限
2. 复杂 AI 自动规划
3. 复杂微服务拆分

## 4. 数据模型（最小版）

## Task

1. id: string
2. title: string
3. status: todo | done
4. priority: high | medium | low
5. dueAt: datetime | null
6. createdAt: datetime
7. updatedAt: datetime
8. completedAt: datetime | null

## 5. 业务规则

1. title 不能为空
2. 任务完成后进入历史，不删除
3. 可以把 done 改回 todo
4. 同步冲突先用“最后写入优先”

## 6. 鉴权规则（Token）

1. 不使用账号密码
2. 每个用户空间由 Token 标识
3. 请求必须带 Token（通过请求头 X-Token 传递）
4. Token 必须覆盖所有任务与同步接口（/tasks/**, /sync/**）
5. 服务端仅保存 Token 哈希值，不保存明文
6. Token 支持过期时间与吊销状态
7. Token 支持轮换与吊销

## 6.2 限流规则（M2）

1. 维度：按 Token 做限流
2. 策略：固定时间窗口（每分钟）
3. 默认阈值：每个 Token 每分钟 120 次请求（后续可配置）
4. 超限响应：429 Too Many Requests

## 6.3 安全状态持久化（M2）

1. Token 元数据（哈希、创建时间、过期时间、吊销时间）必须持久化存储
2. 限流计数窗口必须持久化，服务重启后不能全部失效
3. MVP 阶段统一使用 SQLite 持久化安全状态

## 6.1 同步接口最小契约（M2）

### POST /sync/pull

1. Header: X-Token: <token>
2. Body: 可为空对象 {}
3. Response: 返回该 Token 空间下的全部任务列表与服务器时间

### POST /sync/push

1. Header: X-Token: <token>
2. Body: tasks 数组（字段与 Task 一致）
3. 处理规则: 使用 updatedAt 进行 LWW 合并
4. Response: 返回合并后的全部任务列表与服务器时间

## 7. API 最小清单

1. POST /tasks
2. PATCH /tasks/{id}
3. GET /tasks
4. POST /sync/pull
5. POST /sync/push

### 安全辅助接口（M2+）

1. POST /auth/token/rotate
2. POST /auth/token/revoke

## 8. 验收标准

1. 能创建任务并看到列表更新
2. 能勾选完成并在历史中看到
3. 能修改优先级并生效
4. 能设置 DDL 并在提醒窗口触发提醒
5. 两端使用同一 Token 可同步同一批任务

## 9. 里程碑

1. M1: 本地单端可用（无同步）
2. M2: 云同步可用（Token）
3. M3: 提醒机制可用
4. M4: 稳定性与文档补齐
