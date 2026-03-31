import { Task } from '../api/client';

interface TaskItemProps {
  task: Task;
  onToggle: (id: string, done: boolean) => void;
}

const priorityLabels = {
  high: '高',
  medium: '中',
  low: '低',
};

export function TaskItem({ task, onToggle }: TaskItemProps) {
  const isDone = task.status === 'done';

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return null;
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <li className={`task-item ${isDone ? 'done' : ''} priority-${task.priority}`}>
      <label className="task-checkbox" onClick={(e) => e.stopPropagation()}>
        <input
          type="checkbox"
          checked={isDone}
          onChange={() => onToggle(task.id, !isDone)}
        />
        <span className="checkmark" />
      </label>
      <div className="task-content">
        <span className="task-title">{task.title}</span>
        <div className="task-meta">
          <span className={`priority priority-${task.priority}`}>
            {priorityLabels[task.priority]}
          </span>
          {task.dueAt && (
            <span className="due-date">{formatDate(task.dueAt)}</span>
          )}
        </div>
      </div>
    </li>
  );
}
