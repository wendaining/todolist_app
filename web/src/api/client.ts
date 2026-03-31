const API_BASE = 'http://localhost:8080';

// MVP 阶段使用固定开发 Token，后续可改为用户输入
const DEV_TOKEN = 'dev-token-change-me';

export type TaskStatus = 'todo' | 'done';
export type TaskPriority = 'high' | 'medium' | 'low';

export interface Task {
  id: string;
  title: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueAt: string | null;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
}

export interface CreateTaskRequest {
  title: string;
  priority?: TaskPriority;
  dueAt?: string | null;
}

export interface UpdateTaskRequest {
  title?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  dueAt?: string | null;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'X-Token': DEV_TOKEN,
      ...options.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }

  return response.json();
}

export const api = {
  listTasks: () => request<Task[]>('/tasks'),

  createTask: (data: CreateTaskRequest) =>
    request<Task>('/tasks', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateTask: (id: string, data: UpdateTaskRequest) =>
    request<Task>(`/tasks/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),
};
