import { useCallback, useRef } from 'react';
import { api, Task } from '../api/client';

/**
 * 云同步 Hook
 * - pull: 从服务器拉取最新任务列表
 * - push: 将本地任务推送到服务器（LWW 合并）
 * - 防抖：push 操作延迟执行，避免频繁请求
 */
export function useSync(
  setTasks: React.Dispatch<React.SetStateAction<Task[]>>,
  setError: (error: string | null) => void
) {
  const pushTimeoutRef = useRef<number | null>(null);
  const pendingTasksRef = useRef<Task[]>([]);

  // 从服务器拉取任务
  const pull = useCallback(async () => {
    try {
      const response = await api.syncPull();
      setTasks(response.tasks);
      setError(null);
      console.log(`[Sync] Pulled ${response.tasks.length} tasks from server`);
      return response.tasks;
    } catch (e) {
      console.error('[Sync] Pull failed:', e);
      setError('同步拉取失败');
      throw e;
    }
  }, [setTasks, setError]);

  // 推送任务到服务器（立即执行）
  const pushNow = useCallback(async (tasks: Task[]) => {
    try {
      const response = await api.syncPush(tasks);
      setTasks(response.tasks);
      setError(null);
      console.log(`[Sync] Pushed ${tasks.length} tasks, server returned ${response.tasks.length}`);
      return response.tasks;
    } catch (e) {
      console.error('[Sync] Push failed:', e);
      setError('同步推送失败');
      throw e;
    }
  }, [setTasks, setError]);

  // 防抖推送：延迟 1 秒执行，合并多次变更
  const pushDebounced = useCallback((tasks: Task[]) => {
    pendingTasksRef.current = tasks;

    if (pushTimeoutRef.current) {
      clearTimeout(pushTimeoutRef.current);
    }

    pushTimeoutRef.current = window.setTimeout(() => {
      pushNow(pendingTasksRef.current);
      pushTimeoutRef.current = null;
    }, 1000);
  }, [pushNow]);

  // 取消待执行的推送
  const cancelPendingPush = useCallback(() => {
    if (pushTimeoutRef.current) {
      clearTimeout(pushTimeoutRef.current);
      pushTimeoutRef.current = null;
    }
  }, []);

  return {
    pull,
    push: pushDebounced,
    pushNow,
    cancelPendingPush,
  };
}
