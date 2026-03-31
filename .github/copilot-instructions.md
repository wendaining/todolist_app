# Copilot 工作约定（本项目）

1. 每次开始前，先阅读 [docs/START_HERE.md](../docs/START_HERE.md) 和 [docs/SPEC-v0.1.md](../docs/SPEC-v0.1.md)
2. 优先保证 MVP 闭环，不主动扩大需求范围
3. 提供建议时使用简洁中文，避免术语堆叠
4. 代码变更后，优先更新相关文档中的验收项或里程碑
5. 若要变更核心规则（同步、鉴权、数据模型），必须先更新规格文档再改代码
6. 任意代码修改都必须在同一轮同步更新 [docs/DEV-CHECKLIST.md](../docs/DEV-CHECKLIST.md)（至少更新对应任务状态或验收证据）
7. 若修改 API 契约、数据模型字段、同步策略、鉴权规则，必须先更新 [docs/SPEC-v0.1.md](../docs/SPEC-v0.1.md) 再提交代码改动
8. 若本轮仅推进进度（无代码修改），也必须更新 [docs/DEV-CHECKLIST.md](../docs/DEV-CHECKLIST.md) 的里程碑状态或新增进度记录
9. 完成任务时需在回复中明确列出“本轮改动的代码文件 + 同步更新的文档文件”，缺一不可
