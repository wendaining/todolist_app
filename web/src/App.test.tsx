import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import App from './App';

describe('App', () => {
  it('renders MVP heading', () => {
    render(<App />);

    const heading = screen.getByRole('heading', { name: 'TodoList Web MVP' });
    expect(heading).toBeTruthy();
  });
});
