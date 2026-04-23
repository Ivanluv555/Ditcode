import { defineStore } from 'pinia';
import { apiRequest } from '@/shared/utils/apiClient';

const AUTH_TOKEN_KEY = 'ditapp_auth_token';

const readToken = () => localStorage.getItem(AUTH_TOKEN_KEY) || '';
const saveToken = (token) => {
  if (!token) {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    return;
  }
  localStorage.setItem(AUTH_TOKEN_KEY, token);
};

const requestAuthApi = (path, options = {}) =>
  apiRequest(path, {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...(readToken() ? { Authorization: `Bearer ${readToken()}` } : {})
    }
  });

export const useAuthStore = defineStore('auth', {
  state: () => ({
    currentUser: null,
    initialized: false
  }),
  actions: {
    async init() {
      if (this.initialized) return;

      try {
        const token = readToken();
        if (token) {
          const session = await requestAuthApi('/api/auth/session', { method: 'GET' });
          this.currentUser = session.user || null;
        } else {
          this.currentUser = null;
        }
      } catch (error) {
        // Clear any invalid token and treat as not logged in
        saveToken('');
        this.currentUser = null;
      }

      this.initialized = true;
    },
    async register({ username, email, password }) {
      await this.init();
      const normalizedEmail = email.toLowerCase();

      try {
        const result = await requestAuthApi('/api/auth/register', {
          method: 'POST',
          body: JSON.stringify({ username, email: normalizedEmail, password })
        });
        this.currentUser = result.user || null;
        saveToken(result.token || '');
        return { ok: true };
      } catch (error) {
        return { ok: false, message: error.message || '注册失败' };
      }
    },
    async login({ account, password }) {
      await this.init();

      try {
        const result = await requestAuthApi('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify({ account, password })
        });
        this.currentUser = result.user || null;
        saveToken(result.token || '');
        return { ok: true };
      } catch (error) {
        return { ok: false, message: error.message || '登录失败' };
      }
    },
    async logout() {
      try {
        await requestAuthApi('/api/auth/logout', { method: 'POST' });
      } catch {
        // Ignore network failures on logout and clear local data anyway.
      }
      this.currentUser = null;
      saveToken('');
    }
  }
});
