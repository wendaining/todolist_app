import { useEffect, useState, useCallback } from 'react';
import { api, Task, TaskPriority } from './api/client';
import { AddTask } from './components/AddTask';
import { TaskList } from './components/TaskList';
import { TaskEditor } from './components/TaskEditor';
import { useReminder } from './hooks/useReminder';
import { useSync } from './hooks/useSync';

type Theme = 'light' | 'dark';

function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [saving, setSaving] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem('theme') as Theme | null;
    if (saved) return saved;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

  // 云同步 hook
  const { pull, push } = useSync(setTasks, setError);

  // 主题切换
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
  };

  // 本地提醒：扫描即将到期任务并触发浏览器通知
  useReminder(tasks);

  // 初始加载：从服务器拉取任务
  const loadTasks = useCallback(async () => {
    try {
      setError(null);
      await pull();
    } catch (e) {
      // 如果 pull 失败，尝试用普通 listTasks
      try {
        const data = await api.listTasks();
        setTasks(data);
      } catch {
        setError('加载任务失败，请确保后端服务已启动');
      }
    } finally {
      setLoading(false);
    }
  }, [pull]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  // 手动同步按钮
  const handleSync = async () => {
    setSyncing(true);
    try {
      await push(tasks);
    } finally {
      setSyncing(false);
    }
  };

  const handleAdd = async (title: string, priority: TaskPriority, dueAt: string | null) => {
    setAdding(true);
    try {
      const newTask = await api.createTask({ title, priority, dueAt });
      const newTasks = [newTask, ...tasks];
      setTasks(newTasks);
      // 新增后自动推送同步
      push(newTasks);
    } catch (e) {
      setError('添加任务失败');
      console.error(e);
    } finally {
      setAdding(false);
    }
  };

  const handleToggle = async (id: string, done: boolean) => {
    try {
      const updated = await api.updateTask(id, {
        status: done ? 'done' : 'todo',
      });
      const newTasks = tasks.map((t) => (t.id === id ? updated : t));
      setTasks(newTasks);
      // 状态变更后自动推送同步
      push(newTasks);
    } catch (e) {
      setError('更新任务失败');
      console.error(e);
    }
  };

  const handleEdit = (task: Task) => {
    setEditingTask(task);
  };

  const handleSaveEdit = async (id: string, priority: TaskPriority, dueAt: string | null) => {
    setSaving(true);
    try {
      const updated = await api.updateTask(id, { priority, dueAt });
      const newTasks = tasks.map((t) => (t.id === id ? updated : t));
      setTasks(newTasks);
      setEditingTask(null);
      // 编辑后自动推送同步
      push(newTasks);
    } catch (e) {
      setError('保存失败');
      console.error(e);
    } finally {
      setSaving(false);
    }
  };

  const handleCloseEditor = () => {
    setEditingTask(null);
  };

  return (
    <main className="page">
      <section className="panel">
        <header className="header">
          <h1>TodoList</h1>
          <div className="header-actions">
            <button
              className="sync-btn"
              onClick={handleSync}
              disabled={syncing || loading}
              aria-label="同步"
            >
              {syncing ? '同步中...' : 'Sync'}
            </button>
            <button
              className="theme-toggle"
              onClick={toggleTheme}
              aria-label={theme === 'light' ? '切换到深色模式' : '切换到浅色模式'}
            >
              {theme === 'light' ? 'Dark' : 'Light'}
            </button>
          </div>
        </header>
        <AddTask onAdd={handleAdd} loading={adding} />
        {error && <p className="error">{error}</p>}
        {loading ? (
          <p className="loading">加载中...</p>
        ) : (
          <TaskList tasks={tasks} onToggle={handleToggle} onEdit={handleEdit} />
        )}
      </section>

      {/* 编辑弹窗 */}
      {editingTask && (
        <TaskEditor
          task={editingTask}
          onSave={handleSaveEdit}
          onClose={handleCloseEditor}
          saving={saving}
        />
      )}
    </main>
  );
}

export default App;
