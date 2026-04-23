import { createServer } from 'node:http';
import { randomUUID } from 'node:crypto';
import { readFile, writeFile, mkdir, rename } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const PORT = Number(process.env.MOCK_PORT || 3100);
const __dirname = dirname(fileURLToPath(import.meta.url));
const DATA_DIR = join(__dirname, 'data');
const AUTH_FILE = join(DATA_DIR, 'auth.json');
const WORKSPACE_FILE = join(DATA_DIR, 'workspace.json');
const COMMUNITY_FILE = join(DATA_DIR, 'community.json');

const jsonHeaders = {
  'Content-Type': 'application/json; charset=utf-8',
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET,POST,PUT,OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization'
};

let writeQueue = Promise.resolve();

const enqueueWrite = (job) => {
  writeQueue = writeQueue.then(job, job);
  return writeQueue;
};

const ensureDataFiles = async () => {
  await mkdir(DATA_DIR, { recursive: true });
};

const readJson = async (filePath, fallback) => {
  try {
    const raw = await readFile(filePath, 'utf8');
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
};

const writeJson = async (filePath, value) => {
  const tempPath = `${filePath}.${process.pid}.${Date.now()}.tmp`;
  await writeFile(tempPath, JSON.stringify(value, null, 2), 'utf8');
  await rename(tempPath, filePath);
};

const loadAuthState = async () => {
  const state = await readJson(AUTH_FILE, { users: [], sessions: [] });
  state.users = Array.isArray(state.users) ? state.users : [];
  state.sessions = Array.isArray(state.sessions) ? state.sessions : [];
  return state;
};

const loadWorkspaceState = async () => {
  const state = await readJson(WORKSPACE_FILE, { workspaces: {} });
  state.workspaces = state.workspaces && typeof state.workspaces === 'object' ? state.workspaces : {};
  return state;
};

const loadCommunityState = async () => {
  const state = await readJson(COMMUNITY_FILE, { archives: [] });
  state.archives = Array.isArray(state.archives) ? state.archives : [];
  return state;
};

const persistAuthState = (state) => enqueueWrite(() => writeJson(AUTH_FILE, state));
const persistWorkspaceState = (state) => enqueueWrite(() => writeJson(WORKSPACE_FILE, state));
const persistCommunityState = (state) => enqueueWrite(() => writeJson(COMMUNITY_FILE, state));

const sendJson = (res, statusCode, payload) => {
  res.writeHead(statusCode, jsonHeaders);
  res.end(JSON.stringify(payload));
};

const parseBody = (req) =>
  new Promise((resolve) => {
    let raw = '';
    req.on('data', (chunk) => {
      raw += chunk;
      if (raw.length > 1e6) {
        raw = '';
        req.socket.destroy();
      }
    });
    req.on('end', () => {
      try {
        resolve(raw ? JSON.parse(raw) : {});
      } catch {
        resolve({});
      }
    });
  });

const toSafeUser = (user) => ({
  id: user.id,
  username: user.username,
  email: user.email
});

const findUserByAccount = (users, account = '') => {
  const normalized = String(account).trim().toLowerCase();
  return users.find(
    (item) => item.username.toLowerCase() === normalized || item.email.toLowerCase() === normalized
  );
};

const getTokenFromHeaders = (req) => {
  const auth = req.headers.authorization || '';
  if (!auth.startsWith('Bearer ')) return '';
  return auth.slice('Bearer '.length).trim();
};

const getSessionUser = (authState, token) => {
  const session = authState.sessions.find((item) => item.token === token);
  if (!session) return null;
  return authState.users.find((item) => item.id === session.userId) || null;
};

const createSession = (authState, userId) => {
  const token = `mock_${randomUUID()}`;
  authState.sessions = authState.sessions.filter((item) => item.userId !== userId);
  authState.sessions.push({ token, userId, createdAt: Date.now() });
  return token;
};

const normalizeTask = (task) => ({
  id: task.id || `task_${Date.now()}`,
  status: task.status || 'queued',
  progress: Number.isFinite(task.progress) ? task.progress : 0,
  prompt: task.prompt || '',
  imageName: task.imageName || '',
  imagePreview: task.imagePreview || '',
  createdAt: task.createdAt || Date.now(),
  updatedAt: task.updatedAt || task.createdAt || Date.now()
});

const normalizeModelAsset = (asset) => ({
  id: asset.id || `asset_${Date.now()}`,
  prompt: asset.prompt || '',
  imagePreview: asset.imagePreview || '',
  createdAt: asset.createdAt || Date.now(),
  updatedAt: asset.updatedAt || asset.createdAt || Date.now()
});

const normalizeMessage = (message) => ({
  id: message.id || `msg_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
  role: message.role || 'user',
  text: message.text || '',
  createdAt: message.createdAt || Date.now()
});

const normalizeArchive = (archive = {}, idFallback = '') => {
  const createdAt = archive.createdAt || Date.now();
  const tasks = Array.isArray(archive.tasks) ? archive.tasks.map(normalizeTask) : [];
  const messages = Array.isArray(archive.messages)
    ? archive.messages.map(normalizeMessage).filter((item) => item.text)
    : [];

  return {
    id: archive.id || idFallback || `archive_${Date.now()}`,
    title: archive.title || '新工作',
    ownerId: archive.ownerId || '',
    ownerName: archive.ownerName || '',
    tasks,
    messages,
    modelAsset: archive.modelAsset ? normalizeModelAsset(archive.modelAsset) : null,
    sourceCommunityId: archive.sourceCommunityId || '',
    isPrivate: archive.isPrivate !== false,
    remixCount: Number.isFinite(archive.remixCount) ? archive.remixCount : 0,
    status: archive.status || 'idle',
    createdAt,
    updatedAt: archive.updatedAt || createdAt
  };
};

const toArchiveFromLegacy = (workspace) => {
  const tasks = Array.isArray(workspace.tasks) ? workspace.tasks.map(normalizeTask) : [];
  const assets = Array.isArray(workspace.assetHistory)
    ? workspace.assetHistory.map(normalizeModelAsset)
    : [];
  const latestAsset = assets.sort((a, b) => b.createdAt - a.createdAt)[0] || null;

  return normalizeArchive({
    id: latestAsset?.id || tasks[0]?.id || `archive_${Date.now()}`,
    title: latestAsset?.prompt || tasks[0]?.prompt || '新工作',
    tasks,
    messages: tasks
      .filter((item) => item.prompt)
      .map((item) => ({ text: item.prompt, role: 'user', createdAt: item.createdAt })),
    modelAsset: latestAsset,
    createdAt: tasks[0]?.createdAt || latestAsset?.createdAt || Date.now(),
    updatedAt: Date.now()
  });
};

const hydrateWorkspace = (workspace = {}) => {
  const archiveSource = workspace.archives || {};
  const archiveEntries = Array.isArray(archiveSource)
    ? archiveSource.map((item) => [item.id, item])
    : Object.entries(archiveSource);

  let archives = archiveEntries
    .map(([id, archive]) => normalizeArchive(archive, id))
    .filter((archive) => archive.id);

  if (archives.length === 0 && (workspace.tasks?.length || workspace.assetHistory?.length)) {
    archives = [toArchiveFromLegacy(workspace)];
  }

  if (archives.length === 0) {
    archives = [normalizeArchive({ id: `archive_${Date.now()}`, title: '新工作', tasks: [], messages: [] })];
  }

  const archiveOrder = Array.isArray(workspace.archiveOrder) && workspace.archiveOrder.length
    ? workspace.archiveOrder.filter((id) => archives.some((archive) => archive.id === id))
    : archives.map((archive) => archive.id);

  const completeOrder = [...archiveOrder];
  for (const archive of archives) {
    if (!completeOrder.includes(archive.id)) {
      completeOrder.push(archive.id);
    }
  }

  const activeArchiveId = completeOrder.includes(workspace.activeArchiveId)
    ? workspace.activeArchiveId
    : completeOrder[0];

  const archiveMap = archives.reduce((acc, archive) => {
    acc[archive.id] = archive;
    return acc;
  }, {});

  return {
    version: 2,
    activeArchiveId,
    archiveOrder: completeOrder,
    archives: archiveMap
  };
};

const getActiveArchive = (workspace) =>
  workspace.archives[workspace.activeArchiveId] || workspace.archives[workspace.archiveOrder[0]];

const withLegacyProjection = (workspace) => {
  const activeArchive = getActiveArchive(workspace);
  return {
    ...workspace,
    tasks: activeArchive ? activeArchive.tasks.map((item) => ({ ...item })) : [],
    assetHistory:
      activeArchive && activeArchive.modelAsset
        ? [{ ...activeArchive.modelAsset }]
        : []
  };
};

const normalizeCommunityArchive = (archive = {}) => {
  const normalized = normalizeArchive(archive, archive.id || `community_${Date.now()}`);
  return {
    ...normalized,
    isPrivate: false,
    sourceArchiveId: archive.sourceArchiveId || normalized.id,
    communityId: archive.communityId || normalized.id
  };
};

const ensureCommunityDefaults = (communityState) => {
  if (communityState.archives.length > 0) return;

  const now = Date.now();
  communityState.archives = [
    normalizeCommunityArchive({
      id: 'community_demo_001',
      communityId: 'community_demo_001',
      sourceArchiveId: 'community_demo_001',
      title: 'Canvas 功能演示',
      ownerId: 'demo_user',
      ownerName: 'AI Creator',
      messages: [{ text: '展示canvas使用', role: 'user', createdAt: now - 120000 }],
      modelAsset: {
        id: 'community_demo_001',
        prompt: '展示canvas使用',
        imagePreview: '/index.jpg',
        createdAt: now - 120000,
        updatedAt: now - 120000
      },
      remixCount: 0,
      createdAt: now - 120000,
      updatedAt: now - 120000
    })
  ];
};

const ensureWorkspaceForUser = (workspaceState, userId) => {
  const hydrated = hydrateWorkspace(workspaceState.workspaces[userId] || {});
  workspaceState.workspaces[userId] = hydrated;
  return hydrated;
};

const getCurrentUser = async (req) => {
  const token = getTokenFromHeaders(req);
  if (!token) return null;

  const authState = await loadAuthState();
  return getSessionUser(authState, token);
};

const server = createServer(async (req, res) => {
  const { method, url } = req;

  if (method === 'OPTIONS') {
    res.writeHead(204, jsonHeaders);
    res.end();
    return;
  }

  if (method === 'GET' && url === '/api/health') {
    sendJson(res, 200, { ok: true, service: 'ditapp-mock-server' });
    return;
  }

  if (method === 'POST' && url === '/api/auth/default-login') {
    const authState = await loadAuthState();
    const workspaceState = await loadWorkspaceState();
    let defaultUser = authState.users[0];

    if (!defaultUser) {
      defaultUser = {
        id: 'user_default_001',
        username: 'demo_user',
        email: 'demo@ditapp.local',
        password: 'demo123456',
        createdAt: Date.now()
      };
      authState.users.push(defaultUser);
      ensureWorkspaceForUser(workspaceState, defaultUser.id);
      await Promise.all([persistAuthState(authState), persistWorkspaceState(workspaceState)]);
    } else {
      ensureWorkspaceForUser(workspaceState, defaultUser.id);
      await persistWorkspaceState(workspaceState);
    }

    const token = createSession(authState, defaultUser.id);
    await persistAuthState(authState);
    sendJson(res, 200, { ok: true, token, user: toSafeUser(defaultUser) });
    return;
  }

  if (method === 'GET' && url === '/api/auth/session') {
    const token = getTokenFromHeaders(req);
    const authState = await loadAuthState();
    const user = getSessionUser(authState, token);
    if (!token || !user) {
      sendJson(res, 401, { ok: false, message: '会话无效或已过期' });
      return;
    }
    sendJson(res, 200, { ok: true, user: toSafeUser(user) });
    return;
  }

  if (method === 'POST' && url === '/api/auth/login') {
    const body = await parseBody(req);
    const authState = await loadAuthState();
    const user = findUserByAccount(authState.users, body.account);

    if (!user) {
      sendJson(res, 404, { ok: false, message: '账号不存在' });
      return;
    }

    if (user.password !== body.password) {
      sendJson(res, 400, { ok: false, message: '密码不正确' });
      return;
    }

    const token = createSession(authState, user.id);
    await persistAuthState(authState);
    sendJson(res, 200, { ok: true, token, user: toSafeUser(user) });
    return;
  }

  if (method === 'POST' && url === '/api/auth/register') {
    const body = await parseBody(req);
    const username = String(body.username || '').trim();
    const email = String(body.email || '').trim().toLowerCase();
    const password = String(body.password || '');

    const authState = await loadAuthState();
    const workspaceState = await loadWorkspaceState();

    const usernameExists = authState.users.find((item) => item.username === username);
    if (usernameExists) {
      sendJson(res, 400, { ok: false, message: '用户名已存在' });
      return;
    }

    const emailExists = authState.users.find((item) => item.email === email);
    if (emailExists) {
      sendJson(res, 400, { ok: false, message: '邮箱已被注册' });
      return;
    }

    const user = {
      id: `user_${Date.now()}`,
      username,
      email,
      password,
      createdAt: Date.now()
    };

    authState.users.push(user);
    ensureWorkspaceForUser(workspaceState, user.id);
    const token = createSession(authState, user.id);

    await Promise.all([persistAuthState(authState), persistWorkspaceState(workspaceState)]);
    sendJson(res, 200, { ok: true, token, user: toSafeUser(user) });
    return;
  }

  if (method === 'POST' && url === '/api/auth/logout') {
    const token = getTokenFromHeaders(req);
    if (token) {
      const authState = await loadAuthState();
      authState.sessions = authState.sessions.filter((item) => item.token !== token);
      await persistAuthState(authState);
    }
    sendJson(res, 200, { ok: true });
    return;
  }

  if (method === 'GET' && url === '/api/community') {
    const communityState = await loadCommunityState();
    ensureCommunityDefaults(communityState);
    await persistCommunityState(communityState);

    sendJson(res, 200, {
      ok: true,
      archives: communityState.archives
        .filter((item) => item.isPrivate !== true)
        .map((item) => normalizeCommunityArchive(item))
    });
    return;
  }

  if (method === 'GET' && url === '/api/featured') {
    // Placeholder endpoint for featured examples. Currently returns an empty list.
    sendJson(res, 200, { ok: true, archives: [] });
    return;
  }

  if (method === 'GET' && url === '/api/resources') {
    // Return example resource archives with category field for frontend filtering
    const now = Date.now();
    const sample = [
      {
        id: 'res_bed_001',
        title: '极简床头设计',
        ownerId: 'res_author_1',
        ownerName: '资源作者A',
        messages: [{ text: '床头设计参考', role: 'user', createdAt: now - 60000 }],
        modelAsset: { id: 'res_bed_001', prompt: '床头设计参考', imagePreview: '/index.jpg', createdAt: now - 60000, updatedAt: now - 60000 },
        category: '卧具',
        remixCount: 3,
        createdAt: now - 60000,
        updatedAt: now - 60000
      },
      {
        id: 'res_seat_001',
        title: '人体工学椅子',
        ownerId: 'res_author_2',
        ownerName: '资源作者B',
        messages: [{ text: '椅子参考', role: 'user', createdAt: now - 120000 }],
        modelAsset: { id: 'res_seat_001', prompt: '椅子参考', imagePreview: '/index.jpg', createdAt: now - 120000, updatedAt: now - 120000 },
        category: '坐具',
        remixCount: 5,
        createdAt: now - 120000,
        updatedAt: now - 120000
      },
      {
        id: 'res_util_001',
        title: '延展功能部件',
        ownerId: 'res_author_3',
        ownerName: '资源作者C',
        messages: [{ text: '部件参考', role: 'user', createdAt: now - 180000 }],
        modelAsset: { id: 'res_util_001', prompt: '部件参考', imagePreview: '/index.jpg', createdAt: now - 180000, updatedAt: now - 180000 },
        category: '实用部件',
        remixCount: 2,
        createdAt: now - 180000,
        updatedAt: now - 180000
      }
    ];

    sendJson(res, 200, { ok: true, archives: sample });
    return;
  }

  if (method === 'POST' && url === '/api/community/remix') {
    const body = await parseBody(req);
    const communityId = String(body.communityId || '').trim();
    if (!communityId) {
      sendJson(res, 400, { ok: false, message: 'communityId is required' });
      return;
    }

    const communityState = await loadCommunityState();
    const target = communityState.archives.find((item) => item.id === communityId || item.communityId === communityId);
    if (!target) {
      sendJson(res, 404, { ok: false, message: '社区存档不存在' });
      return;
    }

    target.remixCount = (Number(target.remixCount) || 0) + 1;
    target.updatedAt = Date.now();
    await persistCommunityState(communityState);
    sendJson(res, 200, { ok: true, remixCount: target.remixCount });
    return;
  }

  if (method === 'POST' && (url === '/api/community/publish' || url === '/api/community/unpublish')) {
    const user = await getCurrentUser(req);
    if (!user) {
      sendJson(res, 401, { ok: false, message: '请先登录' });
      return;
    }

    const body = await parseBody(req);
    const archiveId = String(body.archiveId || '').trim();
    if (!archiveId) {
      sendJson(res, 400, { ok: false, message: 'archiveId is required' });
      return;
    }

    const workspaceState = await loadWorkspaceState();
    const workspace = ensureWorkspaceForUser(workspaceState, user.id);
    const archive = workspace.archives[archiveId];

    if (!archive || archive.ownerId !== user.id) {
      sendJson(res, 404, { ok: false, message: '存档不存在或无权限' });
      return;
    }

    const shouldPrivate = url === '/api/community/unpublish';
    archive.isPrivate = shouldPrivate;
    archive.updatedAt = Date.now();

    const communityState = await loadCommunityState();
    const communityId = `community_${archive.id}`;
    const existing = communityState.archives.find((item) => item.communityId === communityId || item.id === communityId);

    if (!shouldPrivate) {
      const communityArchive = normalizeCommunityArchive({
        ...archive,
        id: communityId,
        communityId,
        sourceArchiveId: archive.id,
        isPrivate: false,
        ownerId: archive.ownerId || user.id,
        ownerName: archive.ownerName || user.username
      });

      if (existing) {
        Object.assign(existing, communityArchive, {
          remixCount: Number(existing.remixCount) || Number(communityArchive.remixCount) || 0,
          updatedAt: Date.now()
        });
      } else {
        communityState.archives.push({ ...communityArchive, remixCount: 0 });
      }
    } else if (existing) {
      communityState.archives = communityState.archives.filter(
        (item) => item.id !== existing.id && item.communityId !== existing.communityId
      );
    }

    await Promise.all([persistWorkspaceState(workspaceState), persistCommunityState(communityState)]);
    sendJson(res, 200, { ok: true, isPrivate: archive.isPrivate });
    return;
  }

  if (url === '/api/workspace') {
    const user = await getCurrentUser(req);
    if (!user) {
      sendJson(res, 401, { ok: false, message: '请先登录' });
      return;
    }

    const workspaceState = await loadWorkspaceState();
    const workspace = ensureWorkspaceForUser(workspaceState, user.id);

    if (method === 'GET') {
      sendJson(res, 200, { ok: true, ...withLegacyProjection(workspace) });
      return;
    }

    if (method === 'PUT') {
      const body = await parseBody(req);

      const nextWorkspace =
        body && (body.version === 2 || body.archives)
          ? hydrateWorkspace(body)
          : hydrateWorkspace({
              ...workspace,
              tasks: Array.isArray(body.tasks) ? body.tasks : [],
              assetHistory: Array.isArray(body.assetHistory) ? body.assetHistory : []
            });

      workspaceState.workspaces[user.id] = nextWorkspace;
      await persistWorkspaceState(workspaceState);
      sendJson(res, 200, { ok: true, ...withLegacyProjection(nextWorkspace) });
      return;
    }
  }

  if (method === 'POST' && url === '/api/workspace/reset') {
    const user = await getCurrentUser(req);
    if (!user) {
      sendJson(res, 401, { ok: false, message: '请先登录' });
      return;
    }

    const workspaceState = await loadWorkspaceState();
    const workspace = normalizeArchive({ id: `archive_${Date.now()}`, title: '新工作', tasks: [], messages: [] });
    workspaceState.workspaces[user.id] = {
      version: 2,
      activeArchiveId: workspace.id,
      archiveOrder: [workspace.id],
      archives: {
        [workspace.id]: workspace
      }
    };
    await persistWorkspaceState(workspaceState);
    sendJson(res, 200, { ok: true, ...withLegacyProjection(workspaceState.workspaces[user.id]) });
    return;
  }

  sendJson(res, 404, { ok: false, message: 'Not Found' });
});

await ensureDataFiles();

server.listen(PORT, () => {
  console.log(`[mock-server] listening on http://127.0.0.1:${PORT}`);
  console.log('[mock-server] data files: mock-server/data/auth.json, mock-server/data/workspace.json');
});
