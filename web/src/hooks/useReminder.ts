import { useEffect, useRef, useCallback } from 'react';
import { Task } from '../api/client';

const REMINDER_WINDOW_MS = 15 * 60 * 1000; // 15 分钟
const SCAN_INTERVAL_MS = 60 * 1000; // 每分钟扫描一次

/**
 * 本地提醒 Hook
 * - 每分钟扫描未完成任务
 * - 检测 dueAt 是否在未来 15 分钟内
 * - 触发浏览器通知（需用户授权）
 * - 已提醒的任务不重复弹窗
 */
export function useReminder(tasks: Task[]) {
  const notifiedIds = useRef<Set<string>>(new Set());

  // 请求通知权限
  const requestPermission = useCallback(async () => {
    if (!('Notification' in window)) {
      console.warn('浏览器不支持通知');
      return false;
    }
    if (Notification.permission === 'granted') {
      return true;
    }
    if (Notification.permission !== 'denied') {
      const permission = await Notification.requestPermission();
      return permission === 'granted';
    }
    return false;
  }, []);

  // 发送通知
  const sendNotification = useCallback((task: Task) => {
    if (Notification.permission !== 'granted') return;
    
    const dueDate = new Date(task.dueAt!);
    const timeStr = dueDate.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    });

    new Notification('任务即将到期', {
      body: `「${task.title}」将于 ${timeStr} 到期`,
      tag: task.id, // 防止同一任务重复弹窗
    });
  }, []);

  // 扫描任务
  const scanTasks = useCallback(() => {
    const now = Date.now();

    tasks.forEach((task) => {
      // 跳过：已完成、无截止日期、已提醒
      if (task.status === 'done') return;
      if (!task.dueAt) return;
      if (notifiedIds.current.has(task.id)) return;

      const dueTime = new Date(task.dueAt).getTime();
      const timeUntilDue = dueTime - now;

      // 在提醒窗口内（0 ~ 15分钟）
      if (timeUntilDue > 0 && timeUntilDue <= REMINDER_WINDOW_MS) {
        sendNotification(task);
        notifiedIds.current.add(task.id);
      }
    });
  }, [tasks, sendNotification]);

  // 启动定时扫描
  useEffect(() => {
    // 首次加载时请求权限
    requestPermission();

    // 立即扫描一次
    scanTasks();

    // 每分钟扫描
    const interval = setInterval(scanTasks, SCAN_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [requestPermission, scanTasks]);

  // 清理已完成任务的提醒记录（避免内存泄漏）
  useEffect(() => {
    const activeIds = new Set(tasks.map((t) => t.id));
    notifiedIds.current.forEach((id) => {
      if (!activeIds.has(id)) {
        notifiedIds.current.delete(id);
      }
    });
  }, [tasks]);
}
