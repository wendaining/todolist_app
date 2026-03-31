# 前端设计文档 v3.0

## 1. 概述

基于 React + TypeScript + Vite 的 TodoList Web 前端，采用 **Editorial / 报刊杂志风格** 设计，支持浅色/深色主题切换。

## 2. 技术栈

| 类别 | 选择 | 说明 |
|------|------|------|
| 框架 | React 18 | 函数组件 + Hooks |
| 语言 | TypeScript | 类型安全 |
| 构建 | Vite | 快速开发/构建 |
| 测试 | Vitest + Testing Library | 单元测试 + 组件测试 |
| 样式 | CSS Variables | 支持主题切换，无 Tailwind |
| 状态管理 | useState/useEffect | MVP 阶段不引入 Redux/Zustand |

## 3. 设计风格：Editorial（报刊杂志）

### 3.1 设计理念
- **克制、利落、高对比**：黑白为主，彩色仅用于功能色（优先级）
- **直角锐利**：所有元素无圆角（border-radius: 0）
- **衬线字体**：Fraunces（标题）+ Newsreader（正文），呈现印刷品质感
- **实线边框**：用 1px 实线边框替代柔和阴影
- **优先级侧边条**：3px 纯色竖条标示优先级，不使用渐变或背景色

### 3.2 字体
```css
--font-display: 'Fraunces', Georgia, serif;      /* 标题/按钮 */
--font-body: 'Newsreader', 'Times New Roman', serif;  /* 正文 */
```
字体来源：Google Fonts

### 3.3 色彩系统

**浅色主题（默认）**
| 变量 | 值 | 用途 |
|------|------|------|
| --bg | #faf9f7 | 页面背景（暖白） |
| --bg-paper | #ffffff | 卡片/面板背景 |
| --bg-muted | #f5f4f2 | 次要背景 |
| --ink | #1a1a1a | 主要文字（纯黑） |
| --ink-secondary | #666666 | 次要文字 |
| --ink-muted | #999999 | 提示文字 |
| --border | #e0e0e0 | 默认边框 |
| --border-strong | #1a1a1a | 强调边框 |
| --urgent | #d62828 | 高优先级 |
| --warning | #f77f00 | 中优先级 |
| --calm | #2d6a4f | 低优先级 |

**深色主题**
| 变量 | 值 | 用途 |
|------|------|------|
| --bg | #121212 | 页面背景 |
| --bg-paper | #1e1e1e | 卡片/面板背景 |
| --bg-muted | #2a2a2a | 次要背景 |
| --ink | #e8e6e3 | 主要文字（暖白） |
| --ink-secondary | #a0a0a0 | 次要文字 |
| --ink-muted | #666666 | 提示文字 |
| --border | #333333 | 默认边框 |
| --border-strong | #e8e6e3 | 强调边框 |

### 3.4 交互与动效
- **过渡**：所有交互 0.15s ease，快速干脆
- **hover**：边框加深、颜色反转（而非阴影）
- **任务项**：hover 时背景变为 muted，侧边条保持位置
- **主题切换按钮**：文字 "Dark" / "Light"，无 emoji

## 4. 目录结构

```
web/src/
├── api/
│   └── client.ts          # API 调用封装
├── components/
│   ├── AddTask.tsx        # 新建任务表单
│   ├── TaskList.tsx       # 任务列表容器
│   ├── TaskItem.tsx       # 单个任务项
│   └── TaskEditor.tsx     # 任务编辑弹窗（待开发）
├── hooks/
│   └── useTasks.ts        # 任务数据 Hook（待开发）
├── types/
│   └── index.ts           # 共享类型定义（待开发）
├── App.tsx                # 主组件（含主题切换）
├── App.test.tsx           # App 测试
├── main.tsx               # 入口
└── styles.css             # 全局样式（CSS Variables）
```

## 5. 组件设计

### 5.1 已完成组件

| 组件 | 职责 | 状态 |
|------|------|------|
| `App` | 状态管理、主题切换、API 调用 | ✅ 完成 |
| `AddTask` | 新建任务表单 | ✅ 完成 |
| `TaskList` | 任务列表（待办/已完成分区） | ✅ 完成 |
| `TaskItem` | 单个任务展示（含优先级侧边条） | ✅ 完成 |
| `TaskEditor` | 编辑任务弹窗（优先级、DDL） | ✅ 完成 |

### 5.2 待开发组件

| 组件 | 职责 | Props |
|------|------|-------|
| `ReminderBanner` | 即将到期任务提醒 | `tasks` |

## 6. 主题切换功能

### 6.1 实现方式
- 使用 `data-theme` 属性切换主题
- CSS Variables 定义所有颜色
- localStorage 持久化用户选择
- 首次访问自动检测系统偏好

### 6.2 代码示例
```tsx
// App.tsx
const [theme, setTheme] = useState<'light' | 'dark'>(() => {
  const saved = localStorage.getItem('theme');
  if (saved) return saved;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
});

useEffect(() => {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('theme', theme);
}, [theme]);
```

## 7. 待开发功能清单

### 7.1 M1.2 剩余：编辑优先级与 DDL

**用户故事：** 用户点击任务，弹出编辑面板，可修改优先级和截止日期。

**界面草图：**
```
┌─────────────────────────────────────┐
│ 编辑任务                        [×] │
├─────────────────────────────────────┤
│ 标题：学习 React Hooks              │
│                                     │
│ 优先级                              │
│ ┌─────┐ ┌─────┐ ┌─────┐            │
│ │ 高  │ │ 中  │ │ 低  │            │
│ └─────┘ └─────┘ └─────┘            │
│                                     │
│ 截止日期                            │
│ ┌───────────────────────────────┐   │
│ │ 2026-04-05 18:00              │   │
│ └───────────────────────────────┘   │
│                                     │
│           [取消]    [保存]          │
└─────────────────────────────────────┘
```

### 7.2 M1.3：本地提醒

**实现方案：**
1. 使用 `setInterval` 每分钟扫描任务列表
2. 检查 `dueAt` 是否在未来 15 分钟内
3. 使用 `Notification API` 弹出浏览器通知
4. 记录已提醒的任务 ID，避免重复提醒

## 8. 开发顺序

| 步骤 | 任务 | 状态 |
|------|------|------|
| 1 | 样式升级：字体/色彩/动效 + 深色模式 | ✅ 完成 |
| 2 | TaskItem 添加点击编辑入口 | ✅ 完成 |
| 3 | 实现 TaskEditor 弹窗 | ✅ 完成 |
| 4 | 实现本地提醒机制 | ⏳ 待开发 |
| 5 | 补充组件测试 | ⏳ 待开发 |

## 9. 测试计划

| 测试文件 | 测试内容 | 优先级 |
|---------|---------|--------|
| `App.test.tsx` | 主组件渲染、主题切换 | ✅ 已有 |
| `AddTask.test.tsx` | 表单输入、提交、禁用状态 | 高 |
| `TaskItem.test.tsx` | 任务展示、勾选交互 | 高 |
| `TaskList.test.tsx` | 分区展示、空状态 | 中 |
| `TaskEditor.test.tsx` | 编辑表单、保存取消 | 中 |

---

**最后更新：2026-03-31**
