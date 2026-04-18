import { spawn } from 'node:child_process';

const isWindows = process.platform === 'win32';
const backendPort = Number(process.env.BACKEND_PORT || 3100);
const healthUrl = `http://127.0.0.1:${backendPort}/api/health`;
const useMockServer = String(process.env.USE_MOCK_SERVER || 'false').toLowerCase() === 'true';
const spawnOptions = {
  stdio: 'inherit',
  shell: isWindows,
  env: process.env
};

let shuttingDown = false;
let backendLikeServer = null;
let viteServer = null;
let usingExternalServer = false;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const isMockHealthy = async () => {
  try {
    const response = await fetch(healthUrl);
    return response.ok;
  } catch {
    return false;
  }
};

const waitForHealth = async () => {
  for (let attempt = 0; attempt < 40; attempt += 1) {
    if (await isMockHealthy()) {
      return;
    }

    await sleep(250);
  }

  throw new Error('mock-server did not become healthy in time');
};

const shutdown = (code = 0) => {
  if (shuttingDown) return;
  shuttingDown = true;

  for (const child of [backendLikeServer, viteServer]) {
    if (child && !child.killed) {
      child.kill('SIGTERM');
    }
  }

  process.exit(code);
};

const handleChildExit = (label, code) => {
  console.log(`[dev] ${label} exited with code ${code ?? 0}`);

  if (label === 'backend-like-server' && usingExternalServer) {
    return;
  }

  shutdown(code ?? 0);
};

if (await isMockHealthy()) {
  usingExternalServer = true;
  console.log(`[dev] using existing backend on ${healthUrl}`);
} else if (useMockServer) {
  backendLikeServer = spawn('pnpm', ['mock:server'], spawnOptions);
  backendLikeServer.on('exit', (code) => handleChildExit('backend-like-server', code));
  backendLikeServer.on('error', (error) => {
    console.error('[dev] mock-server process error:', error.message);
    shutdown(1);
  });
  console.log(`[dev] backend not found, started mock-server on ${healthUrl}`);
} else {
  console.log(`[dev] backend not found on ${healthUrl}. Start ditserver first, or run with USE_MOCK_SERVER=true.`);
}

try {
  if (useMockServer || usingExternalServer) {
    await waitForHealth();
  }
  viteServer = spawn('pnpm', ['exec', 'vite', '--host'], spawnOptions);
  viteServer.on('exit', (code) => handleChildExit('vite', code));
  viteServer.on('error', (error) => {
    console.error('[dev] vite process error:', error.message);
    shutdown(1);
  });
} catch (error) {
  console.error('[dev] failed to start:', error.message);
  shutdown(1);
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));

