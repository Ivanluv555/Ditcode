import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:3100';

export default defineConfig({
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  plugins: [vue()],
  server: {
    proxy: {
      '/api': apiProxyTarget
    }
  }
});
