# 前端设计文档 v2.0

## 1. 概述

基于 React + TypeScript + Vite 的 TodoList Web 前端，采用**极简文艺风**设计，支持浅色/深色主题切换。

## 2. 技术栈

| 类别 | 选择 | 说明 |
|------|------|------|
| 框架 | React 18 | 函数组件 + Hooks |
| 语言 | TypeScript | 类型安全 |
| 构建 | Vite | 快速开发/构建 |
| 测试 | Vitest + Testing Library | 单元测试 + 组件测试 |
| 样式 | CSS Variables | 支持主题切换 |
| 状态管理 | useState/useEffect | MVP 阶段不引入 Redux/Zustand |

## 3. 设计风格：极简文艺（Refined Editorial）

### 3.1 调性
- 克制优雅、留白呼吸、精致细节
- 使用衬线字体作为标题增加文艺感
- 任务卡片用侧边色条标示优先级（而非背景色）

### 3.2 字体
```css
--font-heading: 'Playfair Display', 'Noto Serif SC', serif;  /* 标题 */
--font-body: 'Source Sans 3', 'Noto Sans SC', sans-serif;    /* 正文 */
```

### 3.3 色彩系统

**浅色主题（默认）**
| 变量 | 值 | 用途 |
|------|------|------|
| --bg-gradient | #f8f9fa → #e9ecef | 页面背景 |
| --card-bg | rgba(255,255,255,0.88) | 卡片背景 |
| --text-primary | #1a1a2e | 主要文字 |
| --accent | #4361ee | 强调色 |
| --priority-high | #ef476f | 高优先级 |
| --priority-medium | #f59f00 | 中优先级 |
| --priority-low | #06d6a0 | 低优先级 |

**深色主题**
| 变量 | 值 | 用途 |
|------|------|------|
| --bg-gradient | #0f0f1a → #1a1a2e | 页面背景 |
| --card-bg | rgba(30,30,50,0.9) | 卡片背景 |
| --text-primary | #f8f9fa | 主要文字 |
| --accent | #748ffc | 强调色 |

### 3.4 动效
- **页面加载**：fadeInUp 交错渐入
- **任务项**：hover 时右移 + 侧边条加宽
- **过渡**：所有颜色 0.3s ease 平滑切换

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
