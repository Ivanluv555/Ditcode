import { spawn } from 'node:child_process';

const isWindows = process.platform === 'win32';
const mockPort = Number(process.env.MOCK_PORT || 3100);
const mockHealthUrl = `http://127.0.0.1:${mockPort}/api/health`;
const spawnOptions = {
  stdio: 'inherit',
  shell: isWindows,
  env: process.env
};

let shuttingDown = false;
let mockServer = null;
let viteServer = null;
let usingExternalMockServer = false;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const isMockHealthy = async () => {
  try {
    const response = await fetch(mockHealthUrl);
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

  for (const child of [mockServer, viteServer]) {
    if (child && !child.killed) {
      child.kill('SIGTERM');
    }
  }

  process.exit(code);
};

const handleChildExit = (label, code) => {
  console.log(`[dev] ${label} exited with code ${code ?? 0}`);

  if (label === 'mock-server' && usingExternalMockServer) {
    return;
  }

  shutdown(code ?? 0);
};

if (await isMockHealthy()) {
  usingExternalMockServer = true;
  console.log(`[dev] using existing mock-server on ${mockHealthUrl}`);
} else {
  mockServer = spawn('pnpm', ['mock:server'], spawnOptions);
  mockServer.on('exit', (code) => handleChildExit('mock-server', code));
  mockServer.on('error', (error) => {
    console.error('[dev] mock-server process error:', error.message);
    shutdown(1);
  });
}

try {
  await waitForHealth();
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

