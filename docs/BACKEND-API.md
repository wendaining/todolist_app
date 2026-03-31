# 后端 API 参考（Java）

本文件用于记录后端类职责与接口契约，作为开发与协作时的统一参考。

## 1. 当前范围

1. 当前已完成任务读取、创建、更新主链路：Repository -> Service -> Controller（GET /tasks、POST /tasks、PATCH /tasks/{id}）。
2. 当前已完成同步最小主链路（POST /sync/pull、POST /sync/push），支持 X-Token 空间隔离与 LWW 合并。

## 2. 包结构

1. com.todolist.api: 应用启动入口
2. com.todolist.api.task.model: Task 领域模型与枚举
3. com.todolist.api.task.repository: 仓储接口与内存实现
4. com.todolist.api.task.service: 任务业务服务
5. com.todolist.api.task.dto: API 请求/响应对象
6. com.todolist.api.task.controller: 任务接口控制器
7. com.todolist.api.sync.dto: 同步请求/响应对象
8. com.todolist.api.sync.service: 同步服务（Token 空间与 LWW）
9. com.todolist.api.sync.controller: 同步接口控制器

## 3. 类与枚举说明

### 3.1 TodoApiApplication

- 文件: api/src/main/java/com/todolist/api/TodoApiApplication.java
- 作用: Spring Boot 启动入口，负责应用启动。

### 3.2 Task

- 文件: api/src/main/java/com/todolist/api/task/model/Task.java
- 作用: Task 领域对象，字段与 SPEC 对齐。

字段说明：

1. id: 任务唯一标识
2. title: 任务标题（不能为空）
3. status: 任务状态（todo/done）
4. priority: 任务优先级（high/medium/low）
5. dueAt: 截止时间，可为空
6. createdAt: 创建时间
7. updatedAt: 更新时间
8. completedAt: 完成时间，可为空

核心方法：

1. createNew(...): 创建新任务，默认 status=todo、priority=medium（若未传）
2. markDone(): 标记完成并更新 completedAt/updatedAt
3. markTodo(): 恢复待办并清空 completedAt

### 3.3 TaskStatus

- 文件: api/src/main/java/com/todolist/api/task/model/TaskStatus.java
- 作用: 任务状态枚举（todo/done）。

说明：

1. @JsonValue: 序列化时输出小写值（todo/done）
2. @JsonCreator: 反序列化时支持从字符串解析枚举

### 3.4 TaskPriority

- 文件: api/src/main/java/com/todolist/api/task/model/TaskPriority.java
- 作用: 优先级枚举（high/medium/low）。

说明：

1. @JsonValue: 序列化时输出小写值（high/medium/low）
2. @JsonCreator: 反序列化时支持从字符串解析枚举

### 3.5 TaskRepository / InMemoryTaskRepository

- 文件: api/src/main/java/com/todolist/api/task/repository/TaskRepository.java
- 文件: api/src/main/java/com/todolist/api/task/repository/InMemoryTaskRepository.java
- 作用: 定义任务读写能力，并提供当前阶段内存实现。

### 3.6 TaskService

- 文件: api/src/main/java/com/todolist/api/task/service/TaskService.java
- 作用: 提供任务查询、创建、更新业务方法（listTasks()/createTask()/updateTask()）。

### 3.7 CreateTaskRequest

- 文件: api/src/main/java/com/todolist/api/task/dto/CreateTaskRequest.java
- 作用: POST /tasks 请求 DTO。

字段与约束：

1. title: 必填且非空白
2. priority: 可选，未传时默认 medium
3. dueAt: 可选

### 3.8 TaskResponse

- 文件: api/src/main/java/com/todolist/api/task/dto/TaskResponse.java
- 作用: API 响应 DTO，避免直接暴露领域对象。

### 3.9 UpdateTaskRequest

- 文件: api/src/main/java/com/todolist/api/task/dto/UpdateTaskRequest.java
- 作用: PATCH /tasks/{id} 请求 DTO。

字段与约束：

1. title: 可选；若提供则必须非空白
2. status: 可选（todo/done）
3. priority: 可选（high/medium/low）
4. dueAt: 可选；可显式传 null 清空 DDL

### 3.10 TaskController

- 文件: api/src/main/java/com/todolist/api/task/controller/TaskController.java
- 作用: 暴露 GET /tasks、POST /tasks、PATCH /tasks/{id} 接口。

### 3.11 SyncTaskPayload / SyncPushRequest / SyncResponse

- 文件: api/src/main/java/com/todolist/api/sync/dto/SyncTaskPayload.java
- 文件: api/src/main/java/com/todolist/api/sync/dto/SyncPushRequest.java
- 文件: api/src/main/java/com/todolist/api/sync/dto/SyncResponse.java
- 作用: 定义同步入参与出参。

关键点：

1. SyncPushRequest.tasks 必填
2. SyncTaskPayload 字段与 Task 对齐，含 createdAt/updatedAt
3. SyncResponse 返回 tasks 与 serverTime

### 3.12 SyncService

- 文件: api/src/main/java/com/todolist/api/sync/service/SyncService.java
- 作用: 管理 Token 空间下的任务集合，并在 push 时按 updatedAt 做 LWW 合并。

### 3.13 SyncController

- 文件: api/src/main/java/com/todolist/api/sync/controller/SyncController.java
- 作用: 暴露 POST /sync/pull、POST /sync/push。

关键点：

1. 使用请求头 X-Token 识别用户空间
2. X-Token 空白时返回 400

## 4. 计划中的最小接口（来自 SPEC）

1. POST /tasks
2. PATCH /tasks/{id}
3. GET /tasks
4. POST /sync/pull
5. POST /sync/push

当前状态：最小 API 已全部实现（POST /tasks、PATCH /tasks/{id}、GET /tasks、POST /sync/pull、POST /sync/push）。

### 4.1 GET /tasks

- 路径: /tasks
- 方法: GET
- 说明: 获取当前任务列表。

请求示例：

```http
GET /tasks HTTP/1.1
Host: localhost:8080
```

成功响应示例（200）：

```json
[
	{
		"id": "task-1",
		"title": "read spec",
		"status": "todo",
		"priority": "high",
		"dueAt": null,
		"createdAt": "2026-03-31T03:10:00Z",
		"updatedAt": "2026-03-31T03:10:00Z",
		"completedAt": null
	}
]
```

### 4.2 POST /tasks

- 路径: /tasks
- 方法: POST
- 说明: 创建新任务。

请求示例：

```http
POST /tasks HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
	"title": "write code",
	"priority": "medium",
	"dueAt": null
}
```

成功响应示例（201）：

```json
{
	"id": "6af558d4-66a5-4a4b-95f2-4ec314920385",
	"title": "write code",
	"status": "todo",
	"priority": "medium",
	"dueAt": null,
	"createdAt": "2026-03-31T03:20:00Z",
	"updatedAt": "2026-03-31T03:20:00Z",
	"completedAt": null
}
```

失败响应：

1. 400 Bad Request：title 为空或仅空白

### 4.3 PATCH /tasks/{id}

- 路径: /tasks/{id}
- 方法: PATCH
- 说明: 局部更新任务字段（title/status/priority/dueAt）。

请求示例：

```http
PATCH /tasks/task-3 HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
	"status": "done",
	"priority": "high",
	"dueAt": "2026-04-01T10:15:30+08:00"
}
```

成功响应示例（200）：

```json
{
	"id": "task-3",
	"title": "write tests",
	"status": "done",
	"priority": "high",
	"dueAt": "2026-04-01T10:15:30+08:00",
	"createdAt": "2026-03-31T03:30:00Z",
	"updatedAt": "2026-03-31T03:35:00Z",
	"completedAt": "2026-03-31T03:35:00Z"
}
```

失败响应：

1. 400 Bad Request：title 为空白或 status/priority 取值非法
2. 404 Not Found：任务不存在

### 4.4 POST /sync/pull

- 路径: /sync/pull
- 方法: POST
- Header: X-Token: <token>
- 说明: 拉取指定 Token 空间的全部任务。

请求示例：

```http
POST /sync/pull HTTP/1.1
Host: localhost:8080
X-Token: token-demo
Content-Type: application/json

{}
```

成功响应示例（200）：

```json
{
	"tasks": [
		{
			"id": "sync-1",
			"title": "from cloud",
			"status": "todo",
			"priority": "medium",
			"dueAt": null,
			"createdAt": "2026-03-31T03:00:00Z",
			"updatedAt": "2026-03-31T03:30:00Z",
			"completedAt": null
		}
	],
	"serverTime": "2026-03-31T04:00:00Z"
}
```

失败响应：

1. 400 Bad Request：缺少 X-Token 或值为空白

### 4.5 POST /sync/push

- 路径: /sync/push
- 方法: POST
- Header: X-Token: <token>
- 说明: 推送客户端任务并按 updatedAt 执行 LWW 合并。

请求示例：

```http
POST /sync/push HTTP/1.1
Host: localhost:8080
X-Token: token-demo
Content-Type: application/json

{
	"tasks": [
		{
			"id": "sync-2",
			"title": "merged",
			"status": "todo",
			"priority": "high",
			"dueAt": null,
			"createdAt": "2026-03-31T03:00:00Z",
			"updatedAt": "2026-03-31T03:30:00Z",
			"completedAt": null
		}
	]
}
```

成功响应示例（200）：

```json
{
	"tasks": [
		{
			"id": "sync-2",
			"title": "merged",
			"status": "todo",
			"priority": "high",
			"dueAt": null,
			"createdAt": "2026-03-31T03:00:00Z",
			"updatedAt": "2026-03-31T03:30:00Z",
			"completedAt": null
		}
	],
	"serverTime": "2026-03-31T04:00:00Z"
}
```

失败响应：

1. 400 Bad Request：X-Token 缺失/空白，或 tasks 缺失，或任务字段非法

## 5. 时间与时区约定（当前实现）

1. 后端内部生成时间统一使用 UTC。
2. dueAt 使用 OffsetDateTime，保留客户端传入偏移量。
3. 前端展示时按用户本地时区进行转换。

## 6. 维护规则

1. 新增/修改后端类时，更新本文件对应条目。
2. 新增/修改接口时，补充请求体、响应体、状态码与异常说明。
3. 若改动 API 契约、数据模型、同步或鉴权规则，先更新 docs/SPEC-v0.1.md。