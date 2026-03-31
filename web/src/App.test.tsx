import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import App from './App';

// Mock fetch API
beforeEach(() => {
  vi.spyOn(globalThis, 'fetch').mockResolvedValue({
    ok: true,
    json: () => Promise.resolve([]),
  } as Response);
});

describe('App', () => {
  it('renders heading and add task form', () => {
    render(<App />);

    const heading = screen.getByRole('heading', { name: 'TodoList' });
    expect(heading).toBeTruthy();

    const input = screen.getByPlaceholderText('添加新任务...');
    expect(input).toBeTruthy();

    const addButton = screen.getByRole('button', { name: '添加' });
    expect(addButton).toBeTruthy();
  });
});
