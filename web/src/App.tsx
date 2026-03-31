import { useEffect, useState } from 'react';
import { api, Task, TaskPriority } from './api/client';
import { AddTask } from './components/AddTask';
import { TaskList } from './components/TaskList';
import { TaskEditor } from './components/TaskEditor';
import { useReminder } from './hooks/useReminder';

type Theme = 'light' | 'dark';

function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [saving, setSaving] = useState(false);
  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem('theme') as Theme | null;
    if (saved) return saved;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

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

  const loadTasks = async () => {
    try {
      setError(null);
      const data = await api.listTasks();
      setTasks(data);
    } catch (e) {
      setError('加载任务失败，请确保后端服务已启动');
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  const handleAdd = async (title: string, priority: TaskPriority, dueAt: string | null) => {
    setAdding(true);
    try {
      const newTask = await api.createTask({ title, priority, dueAt });
      setTasks((prev) => [newTask, ...prev]);
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
      setTasks((prev) => prev.map((t) => (t.id === id ? updated : t)));
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
      setTasks((prev) => prev.map((t) => (t.id === id ? updated : t)));
      setEditingTask(null);
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
          <button
            className="theme-toggle"
            onClick={toggleTheme}
            aria-label={theme === 'light' ? '切换到深色模式' : '切换到浅色模式'}
          >
            {theme === 'light' ? 'Dark' : 'Light'}
          </button>
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
