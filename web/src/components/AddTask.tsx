import { useState } from 'react';
import { TaskPriority } from '../api/client';

interface AddTaskProps {
  onAdd: (title: string, priority: TaskPriority, dueAt: string | null) => void;
  loading?: boolean;
}

export function AddTask({ onAdd, loading }: AddTaskProps) {
  const [title, setTitle] = useState('');
  const [priority, setPriority] = useState<TaskPriority>('medium');
  const [dueAt, setDueAt] = useState('');
  const [showDueAt, setShowDueAt] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = title.trim();
    if (!trimmed) return;
    const dueAtValue = dueAt ? new Date(dueAt).toISOString() : null;
    onAdd(trimmed, priority, dueAtValue);
    setTitle('');
    setDueAt('');
    setShowDueAt(false);
  };

  return (
    <form className="add-task-form" onSubmit={handleSubmit}>
      <div className="add-task-row">
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
        <button
          type="button"
          className={`due-toggle ${showDueAt || dueAt ? 'active' : ''}`}
          onClick={() => setShowDueAt(!showDueAt)}
          disabled={loading}
          title="设置截止时间"
        >
          📅
        </button>
        <button type="submit" className="add-btn" disabled={loading || !title.trim()}>
          {loading ? '添加中...' : '添加'}
        </button>
      </div>
      {showDueAt && (
        <div className="add-task-due">
          <label className="due-label">截止时间</label>
          <input
            type="datetime-local"
            className="due-input"
            value={dueAt}
            onChange={(e) => setDueAt(e.target.value)}
            disabled={loading}
          />
          {dueAt && (
            <button
              type="button"
              className="clear-due-btn"
              onClick={() => setDueAt('')}
            >
              清除
            </button>
          )}
        </div>
      )}
    </form>
  );
}
