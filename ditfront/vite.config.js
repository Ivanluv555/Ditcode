import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';
import { copyFileSync, mkdirSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://localhost:3100';

function copyAssetsPlugin() {
  return {
    name: 'copy-assets',
    closeBundle() {
      const srcDir = 'assets';
      const destDir = 'dist/assets';

      function copyRecursive(src, dest) {
        mkdirSync(dest, { recursive: true });
        const entries = readdirSync(src);

        for (const entry of entries) {
          const srcPath = join(src, entry);
          const destPath = join(dest, entry);

          if (statSync(srcPath).isDirectory()) {
            copyRecursive(srcPath, destPath);
          } else {
            copyFileSync(srcPath, destPath);
          }
        }
      }

      copyRecursive(srcDir, destDir);
      console.log('✓ Assets copied to dist/assets');
    }
  };
}

export default defineConfig({
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  plugins: [vue(), copyAssetsPlugin()],
  server: {
    proxy: {
      '/api': apiProxyTarget
    }
  }
});
