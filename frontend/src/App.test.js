import { render, screen } from '@testing-library/react';

jest.mock('./auth', () => ({
  isLoggedIn: () => false,
  getCurrentUserDetail: () => null,
}));

import App from './App';

test('redirects unauthenticated users to login screen', async () => {
  render(<App />);
  expect(await screen.findByText(/Inloggen/i)).toBeInTheDocument();
});
