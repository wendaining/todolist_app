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
4. Token 支持轮换和吊销（后续迭代可先占位）

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
