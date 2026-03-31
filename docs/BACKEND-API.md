# 后端 API 参考（Java）

本文件用于记录后端类职责与接口契约，作为开发与协作时的统一参考。

## 1. 当前范围

1. 当前已完成任务读取主链路：Repository -> Service -> Controller（GET /tasks）。
2. POST /tasks、PATCH /tasks/{id}、同步接口仍在待实现状态。

## 2. 包结构

1. com.todolist.api: 应用启动入口
2. com.todolist.api.task.model: Task 领域模型与枚举
3. com.todolist.api.task.repository: 仓储接口与内存实现
4. com.todolist.api.task.service: 任务业务服务
5. com.todolist.api.task.dto: API 响应对象
6. com.todolist.api.task.controller: 任务接口控制器

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
- 作用: 定义任务读取能力，并提供当前阶段内存实现。

### 3.6 TaskService

- 文件: api/src/main/java/com/todolist/api/task/service/TaskService.java
- 作用: 提供任务查询业务方法 listTasks()。

### 3.7 TaskResponse

- 文件: api/src/main/java/com/todolist/api/task/dto/TaskResponse.java
- 作用: API 响应 DTO，避免直接暴露领域对象。

### 3.8 TaskController

- 文件: api/src/main/java/com/todolist/api/task/controller/TaskController.java
- 作用: 暴露 GET /tasks 接口，返回任务响应数组。

## 4. 计划中的最小接口（来自 SPEC）

1. POST /tasks
2. PATCH /tasks/{id}
3. GET /tasks
4. POST /sync/pull
5. POST /sync/push

当前状态：已实现 GET /tasks，其余接口待实现。

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

## 5. 时间与时区约定（当前实现）

1. 后端内部生成时间统一使用 UTC。
2. dueAt 使用 OffsetDateTime，保留客户端传入偏移量。
3. 前端展示时按用户本地时区进行转换。

## 6. 维护规则

1. 新增/修改后端类时，更新本文件对应条目。
2. 新增/修改接口时，补充请求体、响应体、状态码与异常说明。
3. 若改动 API 契约、数据模型、同步或鉴权规则，先更新 docs/SPEC-v0.1.md。