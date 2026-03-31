import { Task } from '../api/client';
import { TaskItem } from './TaskItem';

interface TaskListProps {
  tasks: Task[];
  onToggle: (id: string, done: boolean) => void;
}

export function TaskList({ tasks, onToggle }: TaskListProps) {
  const todoTasks = tasks.filter((t) => t.status === 'todo');
  const doneTasks = tasks.filter((t) => t.status === 'done');

  return (
    <div className="task-list">
      <section className="task-section">
        <h2>待办 ({todoTasks.length})</h2>
        {todoTasks.length === 0 ? (
          <p className="empty-hint">暂无待办任务</p>
        ) : (
          <ul className="tasks">
            {todoTasks.map((task) => (
              <TaskItem key={task.id} task={task} onToggle={onToggle} />
            ))}
          </ul>
        )}
      </section>

      <section className="task-section">
        <h2>已完成 ({doneTasks.length})</h2>
        {doneTasks.length === 0 ? (
          <p className="empty-hint">暂无已完成任务</p>
        ) : (
          <ul className="tasks">
            {doneTasks.map((task) => (
              <TaskItem key={task.id} task={task} onToggle={onToggle} />
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
