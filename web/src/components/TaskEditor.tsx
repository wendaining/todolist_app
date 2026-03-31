import { useState } from 'react';
import { Task, TaskPriority } from '../api/client';

interface TaskEditorProps {
  task: Task;
  onSave: (id: string, priority: TaskPriority, dueAt: string | null) => void;
  onClose: () => void;
  saving?: boolean;
}

const priorityOptions: { value: TaskPriority; label: string }[] = [
  { value: 'high', label: '高' },
  { value: 'medium', label: '中' },
  { value: 'low', label: '低' },
];

export function TaskEditor({ task, onSave, onClose, saving }: TaskEditorProps) {
  const [priority, setPriority] = useState<TaskPriority>(task.priority);
  const [dueAt, setDueAt] = useState<string>(() => {
    if (!task.dueAt) return '';
    // 转换为本地 datetime-local 格式
    const date = new Date(task.dueAt);
    return date.toISOString().slice(0, 16);
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const dueAtValue = dueAt ? new Date(dueAt).toISOString() : null;
    onSave(task.id, priority, dueAtValue);
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="modal">
        <header className="modal-header">
          <h2 className="modal-title">编辑任务</h2>
          <button className="modal-close" onClick={onClose} aria-label="关闭">
            ×
          </button>
        </header>

        <form onSubmit={handleSubmit}>
          {/* 任务标题（只读） */}
          <div className="form-group">
            <label className="form-label">任务</label>
            <div className="task-title-display">{task.title}</div>
          </div>

          {/* 优先级选择 */}
          <div className="form-group">
            <label className="form-label">优先级</label>
            <div className="priority-options">
              {priorityOptions.map((opt) => (
                <button
                  key={opt.value}
                  type="button"
                  className={`priority-option ${opt.value} ${priority === opt.value ? 'selected' : ''}`}
                  onClick={() => setPriority(opt.value)}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {/* 截止日期 */}
          <div className="form-group">
            <label className="form-label">截止日期</label>
            <input
              type="datetime-local"
              className="form-input"
              value={dueAt}
              onChange={(e) => setDueAt(e.target.value)}
            />
            {dueAt && (
              <button
                type="button"
                className="clear-date-btn"
                onClick={() => setDueAt('')}
              >
                清除日期
              </button>
            )}
          </div>

          {/* 操作按钮 */}
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              取消
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? '保存中...' : '保存'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
