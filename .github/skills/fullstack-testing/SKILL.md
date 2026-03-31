---
name: fullstack-testing
description: "Use when user asks to 搭建测试框架, add tests, 测试后端前端, run fullstack tests for this Todo project."
---

# Fullstack Testing Skill

## 1. 适用场景

当用户提出以下需求时使用：

1. 搭建测试框架
2. 给后端或前端补测试
3. 运行全栈测试并汇总结果

## 2. 本项目测试分层约定

1. 后端单元测试：JUnit 5（业务规则和状态流转）
2. 后端接口测试：Spring MockMvc（Controller 协议）
3. 前端组件测试：Vitest + Testing Library（渲染和交互）
4. E2E 测试：M2 后再引入（当前先不做）

## 3. 标准执行流程

### 3.1 安装/校验依赖

```bash
source ~/.zshrc
cd api && mvn -q -DskipTests compile
cd ../web && npm install
```

### 3.2 运行测试

```bash
source ~/.zshrc
cd api && mvn test
cd ../web && npm run test
```

## 4. 最小测试新增策略

每次只新增一个最小测试切片：

1. 后端：优先补 service/model 的单元测试
2. 前端：优先补当前变更组件的渲染或交互测试
3. 每轮至少保证一条可复现的验证证据

## 5. 输出格式（回复用户）

必须包含：

1. 新增测试文件列表
2. 后端测试结果（通过/失败）
3. 前端测试结果（通过/失败）
4. 若失败，列出首个关键错误与修复建议

## 6. 文档联动（强制）

1. 修改代码或测试代码后，更新 docs/DEV-CHECKLIST.md 的进度记录。
2. 若变更影响 API/数据模型/同步/鉴权规则，先更新 docs/SPEC-v0.1.md 再改代码。
3. 交付时同时列出“代码改动文件 + 文档改动文件”。
