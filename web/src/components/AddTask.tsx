import { useState } from 'react';
import { TaskPriority } from '../api/client';

interface AddTaskProps {
  onAdd: (title: string, priority: TaskPriority) => void;
  loading?: boolean;
}

export function AddTask({ onAdd, loading }: AddTaskProps) {
  const [title, setTitle] = useState('');
  const [priority, setPriority] = useState<TaskPriority>('medium');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = title.trim();
    if (!trimmed) return;
    onAdd(trimmed, priority);
    setTitle('');
  };

  return (
    <form className="add-task" onSubmit={handleSubmit}>
      <input
        type="text"
        className="task-input"
        placeholder="添加新任务..."
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        disabled={loading}
      />
      <select
        className="priority-select"
        value={priority}
        onChange={(e) => setPriority(e.target.value as TaskPriority)}
        disabled={loading}
      >
        <option value="high">高优先级</option>
        <option value="medium">中优先级</option>
        <option value="low">低优先级</option>
      </select>
      <button type="submit" className="add-btn" disabled={loading || !title.trim()}>
        {loading ? '添加中...' : '添加'}
      </button>
    </form>
  );
}
