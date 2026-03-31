# 后端 API 参考（Java）

本文件用于记录后端类职责与接口契约，作为开发与协作时的统一参考。

## 1. 当前范围

1. 当前仅完成领域模型层（task model）与应用启动类。
2. Controller/Service/Repository 接口尚未实现，计划在 M1 下一步补齐。

## 2. 包结构

1. com.todolist.api: 应用启动入口
2. com.todolist.api.task.model: Task 领域模型与枚举

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

## 4. 计划中的最小接口（来自 SPEC）

1. POST /tasks
2. PATCH /tasks/{id}
3. GET /tasks
4. POST /sync/pull
5. POST /sync/push

当前状态：上述接口尚未实现，完成后需在本文件补充每个接口的请求/响应示例。

## 5. 时间与时区约定（当前实现）

1. 后端内部生成时间统一使用 UTC。
2. dueAt 使用 OffsetDateTime，保留客户端传入偏移量。
3. 前端展示时按用户本地时区进行转换。

## 6. 维护规则

1. 新增/修改后端类时，更新本文件对应条目。
2. 新增/修改接口时，补充请求体、响应体、状态码与异常说明。
3. 若改动 API 契约、数据模型、同步或鉴权规则，先更新 docs/SPEC-v0.1.md。