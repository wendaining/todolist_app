# Todo 项目分步开发清单（含 Agent 学习）

## 文档同步规则（强制）

1. 任何代码改动都必须同步更新本清单（至少更新一个任务状态或一条验收证据）。
2. 任何仅推进进度（make progress）但未改代码的回合，也必须更新本清单的里程碑状态或进度记录。
3. 若改动 API、数据模型、同步策略、鉴权规则，必须先更新 docs/SPEC-v0.1.md，再进行代码修改。
4. 每次交付说明必须同时列出“代码改动文件”和“文档改动文件”。

## 进度记录

1. 2026-03-31：已完成项目目录骨架（api + web）与基础启动文件创建。
2. 2026-03-31：已安装并配置 Java 工具链（JDK 21 + Maven 3.9.14），并补充分层设计说明文档。
3. 2026-03-31：已新增 Build/Test Skill，可通过“构建项目并测试”等触发词复用构建测试流程。
4. 2026-03-31：已将 README 中个人思考迁移至独立背景文档，并完成 README 简化改写。
5. 2026-03-31：已完成后端 Task 数据结构定义（Task + TaskStatus + TaskPriority）。
6. 2026-03-31：执行 `cd api && mvn test` 构建成功（当前暂无测试用例）。
7. 2026-03-31：已搭建后端 JUnit + 前端 Vitest 测试框架，并新增 Testing Skill。
8. 2026-03-31：执行后端 `mvn test`（3/3 通过）与前端 `npm run test`（1/1 通过）。
9. 2026-03-31：已新增后端 API 参考文档（类职责 + 接口清单 + 时间约定）。
10. 2026-03-31：已完成 GET /tasks 主链路（Repository/Service/Controller）并新增 MockMvc 接口测试。
11. 2026-03-31：已完成 POST /tasks（请求校验 + service/repository 写入 + MockMvc 用例），当前静态诊断无报错；mvn test 输出通道异常，待终端恢复后补一次命令行验证证据。
12. 2026-03-31：已完成 PATCH /tasks/{id}（状态/优先级/DDL 更新 + 404/400 场景），新增 service 单测与 controller MockMvc 用例；当前静态诊断无报错，mvn test 输出通道仍异常。
13. 2026-03-31：已完成同步接口 POST /sync/pull、POST /sync/push，接入 X-Token 空间隔离与 LWW（updatedAt）合并；新增 sync service/controller 测试，当前静态诊断无报错，mvn test 输出通道仍异常。
14. 2026-03-31：已完成鉴权/限流加固：tasks+sync 全接口统一 Token 校验，服务端仅存 Token 哈希，支持过期/吊销/轮换，新增按 Token 每分钟固定窗口限流（超限 429），并补充 auth/security 测试。
15. 2026-03-31：已完成安全状态 SQLite 持久化：Token 元数据和限流窗口改由 JDBC 持久化（schema.sql 自动建表），服务重启后安全状态不再全丢。
16. 2026-03-31：已新增离线可验证集成测试（SpringBootTest + SQLite 临时库）：覆盖“重建服务后 Token 元数据仍可鉴权”与“重建存储后限流窗口计数仍连续”，用于在终端输出异常时提供断言级验收证据。
17. 2026-03-31：修复 TokenSecurityService 多构造器引发的 Spring Bean 实例化歧义（No default constructor found）：为主构造器显式添加 @Autowired，恢复 SpringBootTest 上下文加载。
18. 2026-03-31：已完成 Task 数据 SQLite 持久化：新增 JdbcTaskRepository 替换内存实现，扩展 schema.sql 增加 tasks 表，任务数据服务重启后不再丢失；新增 TaskPersistenceIntegrationTest（3 用例），全部测试 30/30 通过。

## 0. 启动前检查

- [x] 已阅读 START_HERE 和 SPEC
- [x] 已锁定 MVP，不扩需求
- [x] 选定第一版技术栈（Java 主线）

### 已确定技术栈（第一阶段）

- 后端：Java 21 + Spring Boot 3 + Spring Web + Spring Validation + Spring Data JPA
- 数据库：SQLite（MVP 低成本起步）
- 前端：React + TypeScript + Vite
- 同步策略：LWW（最后写入优先），与 SPEC 保持一致
- 鉴权方式：Token（无账号密码）

### 第二阶段增强（简历加分）

- 可选增加 Go 小服务（提醒扫描或同步任务处理）
- 目标是展示异构后端与并发处理能力，不影响主线 MVP 交付

## 1. 里程碑 M1：本地单端可用（无同步）

### 1.1 数据与接口

- [x] 定义 Task 数据结构（与 SPEC 一致）
- [x] 完成最小 API：POST /tasks、PATCH /tasks/{id}、GET /tasks
- [x] 增加最小参数校验：title 非空，priority 枚举（POST/PATCH 已接入）

### 1.2 Web 最小页面

- [ ] 新建任务输入框
- [ ] 任务列表展示（todo 和 done 分区）
- [ ] 勾选完成与取消完成
- [ ] 编辑优先级与 DDL

### 1.3 本地提醒

- [ ] 前端定时扫描即将到期任务
- [ ] 触发浏览器通知（先做最小可用）

### 1.4 M1 验收

- [ ] 新建任务后立即可见
- [ ] 完成任务会进入历史
- [ ] 可修改优先级并生效
- [ ] 设置 DDL 后可收到提醒

## 2. 里程碑 M2：云同步（Token）

### 2.1 同步接口

- [x] POST /sync/pull
- [x] POST /sync/push
- [x] 最后写入优先（LWW）冲突策略

### 2.2 Token 机制

- [x] 请求头携带 Token（X-Token）
- [x] 后端按 Token 进行空间隔离
- [x] Token 轮换/吊销接口（POST /auth/token/rotate, POST /auth/token/revoke）
- [x] 按 Token 限流（固定窗口：每分钟）
- [x] Token/限流状态 SQLite 持久化（重启保留）

### 2.3 M2 验收

- [ ] 两个端使用同一 Token 能同步同一批任务

## 3. 里程碑 M3：提醒机制增强

- [ ] 支持提醒窗口可配置（例如 5/15/30 分钟前）
- [ ] 重启后仍能恢复提醒状态

## 4. Agent 学习清单（边做边学）

### 4.1 Sub-agent（Explore）

- [ ] 使用一次 Explore 子代理做只读调研（例如：检查 API 与 SPEC 是否一致）
- [ ] 让 Explore 汇总“缺失接口/字段”清单

可直接使用的提示词：

1. "请用 Explore 子代理做 thorough 搜索，检查 API 是否覆盖 SPEC-v0.1 的最小清单，并给出缺失项和文件位置。"
2. "请用 Explore 子代理做 medium 搜索，列出 Task 模型字段与 SPEC 不一致的地方。"

### 4.2 Skills / 指令体系

- [ ] 理解工作区级指令：.github/copilot-instructions.md
- [x] 新增一条文件级 instructions（例如 API 命名规范）
- [ ] 验证 instructions 是否生效（让 Copilot 按规则改一次代码）

### 4.3 Agent 工作流习惯

- [ ] 每次改动前先让 Agent 列出 3-5 步最小计划
- [ ] 每完成一个里程碑，先跑验证再写总结
- [ ] 在 PR 描述里写明“规格对应项 + 验收证据”
- [ ] 做一次跨 Agent 交接演练：让新 Agent 仅根据 AGENTS.md 复述项目目标、边界和下一步

## 5. 今日开工顺序（建议）

1. 已确认技术栈（Java 主线）
2. 已完成：建立项目目录（api + web）
3. 完成 Task 模型与最小 API
4. 做最小 Web 页面串通新增/完成流程
5. 录一段 1 分钟演示作为 M1 证据