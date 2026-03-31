# 项目目录规划（Java 主线）

目标：先保证 Web MVP 闭环，同时为后续同步与客户端扩展留接口。

## 顶层目录

- api: Java 后端（Spring Boot）
- web: React 前端（Vite + TypeScript）
- docs: 规格、清单、Agent 协作文档

## 后端目录（api）

- src/main/java/com/todolist/api
- src/main/java/com/todolist/api/common: 通用响应、异常、时间工具
- src/main/java/com/todolist/api/task: 任务模块
- src/main/java/com/todolist/api/sync: 同步模块（M2）
- src/main/resources: 配置文件
- src/test/java: 单元测试与接口测试

建议模块分层：

1. controller: 接口层，只处理请求协议和参数校验
2. service: 业务层，处理任务状态流转与规则
3. repository: 持久化层（M1 可先内存实现，后续替换）
4. dto: 请求/响应对象（Request/Response）
5. model: 领域对象（Task）

## 这套分层为什么适合当前阶段

1. 你现在最需要的是“先跑通，再理解”，这套分层能把复杂度拆开。
2. controller 和 service 分开后，你能明显看出“接口协议”和“业务规则”的边界。
3. repository 先做内存实现，能先交付功能，不会被数据库细节卡住。
4. dto 和 model 分开，后续加同步与鉴权时不容易把内部字段直接暴露出去。

一句话：这是“学习型分层”，不是一开始就追求重架构。

## M1 最简落地（先用 3 层）

M1 只要求你先稳定使用这 3 层：

1. controller（接收请求 + 参数校验）
2. service（状态流转与规则）
3. repository（先内存，后替换 SQLite）

dto/model 可以先保持最小集合，不需要一次性建很多类。

等 M2 再扩展：

1. 增加 sync 模块
2. 增加 token 空间隔离
3. 把内存 repository 替换成数据库实现

## 前端目录（web）

- src/pages: 页面容器
- src/components: 复用组件
- src/features/tasks: 任务相关 UI 与状态管理
- src/services: API 请求封装
- src/types: 类型定义
- src/utils: 工具函数（时间、格式化）

## 本阶段创建范围（M1 开工骨架）

1. 创建 api 和 web 基础工程文件，可启动开发服务器
2. 不提前引入 M2 的同步细节实现
3. 先打通新增/列表/完成流程
