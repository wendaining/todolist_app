import { useEffect, useState } from 'react';
import { api, Task, TaskPriority } from './api/client';
import { AddTask } from './components/AddTask';
import { TaskList } from './components/TaskList';

function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState<string | null>(null);

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

  const handleAdd = async (title: string, priority: TaskPriority) => {
    setAdding(true);
    try {
      const newTask = await api.createTask({ title, priority });
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

  return (
    <main className="page">
      <section className="panel">
        <h1>TodoList</h1>
        <AddTask onAdd={handleAdd} loading={adding} />
        {error && <p className="error">{error}</p>}
        {loading ? (
          <p className="loading">加载中...</p>
        ) : (
          <TaskList tasks={tasks} onToggle={handleToggle} />
        )}
      </section>
    </main>
  );
}

export default App;
