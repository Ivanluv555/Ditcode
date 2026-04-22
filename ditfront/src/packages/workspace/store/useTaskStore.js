import { defineStore } from 'pinia';
import { apiRequest } from '@/shared/utils/apiClient';
import { useAuthStore } from '@/packages/auth/store/useAuthStore';

const AUTH_TOKEN_KEY = 'ditapp_auth_token';
let workspaceWriteQueue = Promise.resolve();

const readToken = () => localStorage.getItem(AUTH_TOKEN_KEY) || '';

const requestWorkspaceApi = (path, options = {}) =>
  apiRequest(path, {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...(readToken() ? { Authorization: `Bearer ${readToken()}` } : {})
    }
  });

const requestCommunityApi = (path, options = {}) =>
  apiRequest(path, {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...(readToken() ? { Authorization: `Bearer ${readToken()}` } : {})
    }
  });

const requestModelApi = (path, options = {}) =>
  apiRequest(path, {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...(readToken() ? { Authorization: `Bearer ${readToken()}` } : {})
    }
  });

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

const normalizeModelAsset = (record) => ({
  id: record.id || `asset_${Date.now()}`,
  prompt: record.prompt || '',
  imagePreview: record.imagePreview || '',
  createdAt: record.createdAt || Date.now(),
  updatedAt: record.updatedAt || record.createdAt || Date.now()
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

  const modelAsset = archive.modelAsset ? normalizeModelAsset(archive.modelAsset) : null;

  return {
    id: archive.id || idFallback || `archive_${Date.now()}`,
    title: archive.title || '新工作',
    ownerId: archive.ownerId || '',
    ownerName: archive.ownerName || '',
    tasks,
    messages,
    modelAsset,
    sourceCommunityId: archive.sourceCommunityId || '',
    isPrivate: archive.isPrivate !== false,
    remixCount: Number.isFinite(archive.remixCount) ? archive.remixCount : 0,
    status: archive.status || 'idle',
    createdAt,
    updatedAt: archive.updatedAt || createdAt
  };
};

const toArchiveFromLegacy = (tasks = [], assetHistory = []) => {
  const normalizedTasks = tasks.map(normalizeTask);
  const normalizedAssets = assetHistory.map(normalizeModelAsset);
  const latestAsset = normalizedAssets.sort((a, b) => b.createdAt - a.createdAt)[0] || null;

  return normalizeArchive({
    id: latestAsset?.id || normalizedTasks[0]?.id || `archive_${Date.now()}`,
    title: latestAsset?.prompt || normalizedTasks[0]?.prompt || '新工作',
    tasks: normalizedTasks,
    messages: normalizedTasks
      .filter((item) => item.prompt)
      .map((item) => ({ text: item.prompt, role: 'user', createdAt: item.createdAt })),
    modelAsset: latestAsset,
    createdAt: normalizedTasks[0]?.createdAt || latestAsset?.createdAt || Date.now(),
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
    archives = [toArchiveFromLegacy(workspace.tasks || [], workspace.assetHistory || [])];
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
    : completeOrder[0] || '';

  return {
    version: 2,
    archives,
    archiveOrder: completeOrder,
    activeArchiveId
  };
};

const cloneArchive = (archive) => ({
  ...archive,
  tasks: archive.tasks.map((item) => ({ ...item })),
  messages: archive.messages.map((item) => ({ ...item })),
  modelAsset: archive.modelAsset ? { ...archive.modelAsset } : null
});

const canMutateArchive = (archive, currentUser) => {
  if (!archive || !currentUser) return false;
  if (archive.ownerId === currentUser.id) return true;

  // Legacy archives may not have owner fields; treat them as current user's local records.
  return !archive.ownerId && !archive.ownerName;
};

const enqueueWorkspaceWrite = (payload) => {
  workspaceWriteQueue = workspaceWriteQueue
    .then(() =>
      requestWorkspaceApi('/api/workspace', {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
    )
    .catch((error) => {
      console.warn('Workspace sync failed:', error);
    });

  return workspaceWriteQueue;
};

export const useTaskStore = defineStore('task', {
  state: () => ({
    archives: [],
    archiveOrder: [],
    currentArchiveId: '',
    communityArchives: [],
    tasks: [],
    assetHistory: [],
    initialized: false
  }),
  getters: {
    currentArchive(state) {
      return state.archives.find((item) => item.id === state.currentArchiveId) || null;
    },
    currentArchiveMessages(state) {
      const archive = state.archives.find((item) => item.id === state.currentArchiveId);
      return archive ? archive.messages : [];
    },
    archiveSummaries(state) {
      return state.archiveOrder
        .map((id) => state.archives.find((archive) => archive.id === id))
        .filter(Boolean)
        .map((archive) => {
          const lastMessage = archive.messages[archive.messages.length - 1] || null;
          return {
            id: archive.id,
            title: archive.title,
            ownerId: archive.ownerId,
            ownerName: archive.ownerName,
            isPrivate: archive.isPrivate,
            sourceCommunityId: archive.sourceCommunityId,
            remixCount: archive.remixCount,
            lastMessage: lastMessage ? lastMessage.text : '空白存档',
            createdAt: archive.createdAt,
            updatedAt: archive.updatedAt,
            modelAsset: archive.modelAsset
          };
        });
    }
  },
  actions: {
    resetForAuthChange() {
      this.archives = [];
      this.archiveOrder = [];
      this.currentArchiveId = '';
      this.communityArchives = [];
      this.tasks = [];
      this.assetHistory = [];
      this.initialized = false;
      workspaceWriteQueue = Promise.resolve();
    },
    async fetchCommunityArchives() {
      try {
        const result = await requestCommunityApi('/api/community', { method: 'GET' });
        this.communityArchives = Array.isArray(result.archives)
          ? result.archives.map((archive) => normalizeArchive(archive, archive.id))
          : [];
      } catch (error) {
        console.warn('Community archives load failed:', error);
        this.communityArchives = [];
      }
    },
    setWorkspace(workspace) {
      const hydrated = hydrateWorkspace(workspace);
      this.archives = hydrated.archives;
      this.archiveOrder = hydrated.archiveOrder;
      this.currentArchiveId = hydrated.activeArchiveId;
      this.refreshLegacyProjection();
    },
    refreshLegacyProjection() {
      const archive = this.currentArchive;
      if (!archive) {
        this.tasks = [];
        this.assetHistory = [];
        return;
      }

      // Reconcile modelAsset -> tasks to avoid stale "processing" UI states.
      if (archive.modelAsset) {
        try {
          const asset = archive.modelAsset;
          const assetTimestamp = Number(asset.updatedAt || asset.createdAt || Date.now());
          const processing = new Set(['queued', 'inferencing', 'compositing']);

          // Mark exact id matches first
          let matchedAny = false;
          for (const t of archive.tasks) {
            if (t.id === asset.id) {
              t.status = 'success';
              t.progress = 100;
              t.updatedAt = assetTimestamp;
              matchedAny = true;
            }
          }

          // Match by prompt/imagePreview or time window
          if (!matchedAny) {
            for (const t of archive.tasks) {
              if (!processing.has(t.status)) continue;
              const tCreated = Number(t.createdAt || 0);
              const promptMatch =
                asset.prompt && t.prompt && asset.prompt.trim() && t.prompt.trim() && asset.prompt.trim() === t.prompt.trim();
              const imageMatch = asset.imagePreview && t.imagePreview && asset.imagePreview === t.imagePreview;
              if (promptMatch || imageMatch || tCreated <= assetTimestamp) {
                t.status = 'success';
                t.progress = 100;
                t.updatedAt = assetTimestamp;
                matchedAny = true;
              }
            }
          }

          // If still no match, append a synthetic successful task
          if (!matchedAny) {
            archive.tasks.push(
              normalizeTask({
                id: asset.id,
                status: 'success',
                progress: 100,
                prompt: asset.prompt || '',
                imagePreview: asset.imagePreview || '',
                createdAt: asset.createdAt || Date.now(),
                updatedAt: asset.updatedAt || Date.now()
              })
            );
          }

          // Aggressively mark any older processing tasks as success to avoid stuck UI
          for (const t of archive.tasks) {
            const tCreated = Number(t.createdAt || 0);
            if (processing.has(t.status) && tCreated <= assetTimestamp) {
              t.status = 'success';
              t.progress = 100;
              t.updatedAt = assetTimestamp;
            }
          }
        } catch (e) {
          console.warn('Error normalizing modelAsset -> task mapping', e);
        }
      }

      this.tasks = archive.tasks.map((item) => ({ ...item }));
      this.assetHistory = archive.modelAsset ? [{ ...archive.modelAsset }] : [];
    },

    buildWorkspacePayload() {
      const archives = this.archives.reduce((acc, archive) => {
        acc[archive.id] = cloneArchive(archive);
        return acc;
      }, {});

      return {
        version: 2,
        activeArchiveId: this.currentArchiveId,
        archiveOrder: [...this.archiveOrder],
        archives,
        tasks: this.tasks.map((item) => ({ ...item })),
        assetHistory: this.assetHistory.map((item) => ({ ...item }))
      };
    },
    touchCurrentArchive() {
      const archive = this.currentArchive;
      if (!archive) return;
      archive.updatedAt = Date.now();
    },
    clearCurrentArchiveSelection() {
      this.currentArchiveId = '';
      this.refreshLegacyProjection();
    },
    async init() {
      if (this.initialized) return;

      const authStore = useAuthStore();
      await authStore.init();

      if (!authStore.currentUser) {
        this.resetForAuthChange();
        this.initialized = true;
        return;
      }

      try {
        const workspace = await requestWorkspaceApi('/api/workspace', { method: 'GET' });
        this.setWorkspace(workspace);
        await this.fetchCommunityArchives();
      } catch (error) {
        console.warn('Workspace init failed:', error);
        this.setWorkspace({});
        this.communityArchives = [];
      }

      this.initialized = true;
    },
    async syncWorkspace() {
      const authStore = useAuthStore();
      await authStore.init();

      if (!authStore.currentUser || !this.initialized) {
        return;
      }

      return enqueueWorkspaceWrite(this.buildWorkspacePayload());
    },
    async resetWorkspace() {
      this.resetForAuthChange();
      this.initialized = true;

      try {
        await requestWorkspaceApi('/api/workspace/reset', { method: 'POST' });
      } catch (error) {
        console.warn('Workspace reset failed:', error);
      }
    },
    createArchive(options = {}) {
      const now = Date.now();
      const archiveId = options.id || `archive_${now}`;
      const authStore = useAuthStore();
      const ownerId = authStore.currentUser?.id || '';
      const ownerName = authStore.currentUser?.username || '';

      const exists = this.archives.find((item) => item.id === archiveId);
      if (exists) {
        this.currentArchiveId = archiveId;
        this.refreshLegacyProjection();
        return archiveId;
      }

      const archive = normalizeArchive({
        id: archiveId,
        title: options.title || '新工作',
        ownerId,
        ownerName,
        isPrivate: options.isPrivate !== false,
        sourceCommunityId: options.sourceCommunityId || '',
        tasks: [],
        messages: options.seedMessage
          ? [{ text: options.seedMessage, role: 'user', createdAt: now }]
          : [],
        modelAsset: options.modelAsset || null,
        createdAt: now,
        updatedAt: now
      });

      this.archives.unshift(archive);
      this.archiveOrder = [archive.id, ...this.archiveOrder.filter((id) => id !== archive.id)];
      this.currentArchiveId = archive.id;
      this.refreshLegacyProjection();
      void this.syncWorkspace();
      return archive.id;
    },
    async openArchiveFromCommunity(communityId) {
      if (!communityId) return '';

      if (this.communityArchives.length === 0) {
        await this.fetchCommunityArchives();
      }

      const source = this.communityArchives.find((item) => item.id === communityId);
      if (!source) return '';

      const archiveId = `archive_${Date.now()}`;
      const id = this.createArchive({
        id: archiveId,
        title: `${source.title} Remix`,
        sourceCommunityId: source.id,
        isPrivate: true
      });

      const archive = this.archives.find((item) => item.id === id);
      if (!archive) return '';

      archive.messages = source.messages.map((msg) => ({ ...msg, id: `${msg.id}_fork_${Date.now()}` }));
      archive.tasks = source.tasks.map((task) => ({ ...task, id: `${task.id}_fork_${Date.now()}` }));
      archive.modelAsset = source.modelAsset ? { ...source.modelAsset } : null;
      archive.updatedAt = Date.now();

      this.currentArchiveId = archive.id;
      this.archiveOrder = [archive.id, ...this.archiveOrder.filter((item) => item !== archive.id)];
      this.refreshLegacyProjection();
      await this.syncWorkspace();
      return archive.id;
    },
    openArchive(id, options = {}) {
      if (!id) return;

      const archive = this.archives.find((item) => item.id === id);
      if (!archive && options.createIfMissing) {
        this.createArchive({
          id,
          title: options.title || `项目 ${id}`,
          seedMessage: options.seedMessage || ''
        });
        return;
      }

      if (!archive) return;

      this.currentArchiveId = id;
      this.archiveOrder = [id, ...this.archiveOrder.filter((item) => item !== id)];
      archive.updatedAt = Date.now();
      this.refreshLegacyProjection();
      void this.syncWorkspace();
    },
    async setArchivePrivacy(id, isPrivate) {
      const archive = this.archives.find((item) => item.id === id);
      const authStore = useAuthStore();
      if (!canMutateArchive(archive, authStore.currentUser)) return;

      archive.isPrivate = Boolean(isPrivate);
      archive.updatedAt = Date.now();

      try {
        if (archive.isPrivate) {
          await requestCommunityApi('/api/community/unpublish', {
            method: 'POST',
            body: JSON.stringify({ archiveId: archive.id })
          });
        } else {
          await requestCommunityApi('/api/community/publish', {
            method: 'POST',
            body: JSON.stringify({ archiveId: archive.id })
          });
        }
      } catch (error) {
        console.warn('Archive visibility update failed:', error);
      }

      await this.syncWorkspace();
      await this.fetchCommunityArchives();
    },
    async deleteArchive(id) {
      const authStore = useAuthStore();
      const archive = this.archives.find((item) => item.id === id);
      if (!canMutateArchive(archive, authStore.currentUser)) return;

      this.archives = this.archives.filter((item) => item.id !== id);
      this.archiveOrder = this.archiveOrder.filter((item) => item !== id);


      if (!this.archives.find((item) => item.id === this.currentArchiveId)) {
        this.currentArchiveId = this.archiveOrder[0] || this.archives[0]?.id || '';
      }

      this.refreshLegacyProjection();
      await this.syncWorkspace();
    },
    appendMessage(text, options = {}) {
      if (!text) return;

      if (!this.currentArchive) {
        this.createArchive({ title: '新工作' });
      }

      const archive = this.currentArchive;
      if (!archive) return;

      archive.messages.push(
        normalizeMessage({
          role: options.role || 'user',
          text,
          createdAt: options.createdAt || Date.now()
        })
      );

      archive.updatedAt = Date.now();
      if (!archive.title || archive.title === '新工作') {
        archive.title = text.slice(0, 24) || archive.title;
      }

      if (!options.silentSync) {
        this.refreshLegacyProjection();
        void this.syncWorkspace();
      }
    },
    addTask(task) {
      if (!task || !task.id) return;
      if (!this.currentArchive) {
        this.createArchive({ title: '新工作' });
      }

      const archive = this.currentArchive;
      if (!archive) return;

      const nextTask = normalizeTask(task);
      const existing = archive.tasks.find((item) => item.id === nextTask.id);
      if (!existing) {
        archive.tasks.push(nextTask);
      } else {
        Object.assign(existing, nextTask);
      }

      if (nextTask.prompt) {
        this.appendMessage(nextTask.prompt, { createdAt: nextTask.createdAt, silentSync: true });
      }

      archive.updatedAt = Date.now();
      if (!archive.title || archive.title === '新工作') {
        archive.title = nextTask.prompt ? nextTask.prompt.slice(0, 24) : archive.title;
      }

      this.refreshLegacyProjection();
      void this.syncWorkspace();
    },
    updateTask(id, updates) {
      const archive = this.currentArchive;
      if (!archive) return;

      const task = archive.tasks.find((item) => item.id === id);
      if (!task) return;

      Object.assign(task, updates, { updatedAt: Date.now() });
      archive.updatedAt = Date.now();
      this.refreshLegacyProjection();
      void this.syncWorkspace();
    },
    removeTask(id) {
      const archive = this.currentArchive;
      if (!archive) return;

      archive.tasks = archive.tasks.filter((item) => item.id !== id);
      archive.updatedAt = Date.now();
      this.refreshLegacyProjection();
      void this.syncWorkspace();
    },
    addAssetRecord(record) {
      if (!record || !record.id) return;
      if (!this.currentArchive) {
        this.createArchive({ title: '新工作' });
      }

      const archive = this.currentArchive;
      if (!archive) return;

      archive.modelAsset = normalizeModelAsset(record);
      archive.updatedAt = Date.now();
      if (!archive.title || archive.title === '新工作') {
        archive.title = archive.modelAsset.prompt ? archive.modelAsset.prompt.slice(0, 24) : archive.title;
      }

      this.refreshLegacyProjection();
      void this.syncWorkspace();

      // Try caching the panorama image locally (Cache API) for faster local rendering
      (async () => {
        try {
          const imagePreview = archive.modelAsset?.imagePreview || '';
          if (!imagePreview || typeof window === 'undefined' || !('caches' in window)) return;
          const assetId = archive.modelAsset.id;
          const cacheName = 'dit-panorama-cache';
          const cacheKey = `/__local_panorama_cache__/${assetId}`;
          const cache = await caches.open(cacheName);
          const existing = await cache.match(cacheKey);
          if (existing) {
            archive.modelAsset.cachedLocal = cacheKey;
            try { localStorage.setItem('dit-panorama-cache:' + assetId, cacheKey); } catch(e) {}
            return;
          }
          const dataUrlToBlob = (dataUrl) => {
            const parts = dataUrl.split(',');
            const header = parts[0] || '';
            const matches = header.match(/:(.*?);/);
            const mime = matches ? matches[1] : 'image/png';
            const bstr = atob(parts[1] || '');
            let n = bstr.length;
            const u8arr = new Uint8Array(n);
            while (n--) u8arr[n] = bstr.charCodeAt(n);
            return new Blob([u8arr], { type: mime });
          };
          let response = null;
          if (imagePreview.startsWith('data:')) {
            const blob = dataUrlToBlob(imagePreview);
            response = new Response(blob, { headers: { 'Content-Type': blob.type || 'image/png' }});
          } else {
            const resp = await fetch(imagePreview, { mode: 'cors' });
            if (!resp.ok) throw new Error(`fetch failed: ${resp.status}`);
            const blob = await resp.blob();
            response = new Response(blob, { headers: { 'Content-Type': blob.type || 'image/png' }});
          }
          await cache.put(cacheKey, response.clone());
          archive.modelAsset.cachedLocal = cacheKey;
          try { localStorage.setItem('dit-panorama-cache:' + assetId, cacheKey); } catch(e) {}
          // update persisted workspace if desired
          void this.syncWorkspace();
        } catch (error) {
          console.warn('Panorama caching failed:', error);
        }
      })();

      if (archive.sourceCommunityId) {
        void requestCommunityApi('/api/community/remix', {
          method: 'POST',
          body: JSON.stringify({ communityId: archive.sourceCommunityId })
        })
          .then(() => this.fetchCommunityArchives())
          .catch((error) => {
            console.warn('Community remix count update failed:', error);
          });
      }
    },
    async generateModelAsset({ prompt, imageName = '', imageBase64 = '' }) {
      const normalizedPrompt = (prompt || '').trim();
      if (!normalizedPrompt) {
        return { ok: false, message: 'prompt is required' };
      }

      if (!this.currentArchive) {
        this.createArchive({ title: '新工作' });
      }

      const archiveId = this.currentArchive?.id || '';
      const taskId = `task_${Date.now()}`;
        this.addTask({
          id: taskId,
          status: 'inferencing',
          progress: 5,
          prompt: normalizedPrompt,
          imageName,
          imagePreview: '',
          createdAt: Date.now()
        });

      try {
        const result = await requestModelApi('/api/model/generate', {
          method: 'POST',
            body: JSON.stringify({
              taskId,
              archiveId,
              prompt: normalizedPrompt,
              imageName,
              imageBase64
            })
          });

        // Prefer server-provided task id when available
        const returnedTaskId = result?.taskId || result?.id || taskId;

        // Support multiple possible response fields for the image (imagePreview, imageBase64, url...)
        const rawPreview = result?.imagePreview || result?.imageBase64 || result?.image || result?.image_url || result?.url || result?.data || result?.base64 || '';
        const mime = result?.imageMime || result?.mime || result?.contentType || 'image/png';
        let finalPreview = '';

        if (rawPreview) {
          if (typeof rawPreview === 'string') {
            const s = rawPreview.trim();
            if (s.startsWith('data:')) {
              finalPreview = s;
            } else if (s.startsWith('http') || s.startsWith('/')) {
              finalPreview = s;
            } else {
              // treat as raw base64 payload (no data: prefix)
              const b64 = s.replace(/\s+/g, '');
              finalPreview = `data:${mime};base64,${b64}`;
            }
          } else if (rawPreview instanceof Blob) {
            finalPreview = URL.createObjectURL(rawPreview);
          } else if (rawPreview instanceof ArrayBuffer || ArrayBuffer.isView(rawPreview)) {
            const blob = new Blob([rawPreview], { type: mime });
            finalPreview = URL.createObjectURL(blob);
          }
        }

        // If backend returned a different id, update archive task id to keep consistency
        const archive = this.currentArchive;
        if (archive && returnedTaskId !== taskId) {
          const t = archive.tasks.find((x) => x.id === taskId);
          if (t) t.id = returnedTaskId;
        }

        // Mark task as completed
        this.updateTask(returnedTaskId, { status: 'success', progress: 100, updatedAt: Date.now() });

        // Add asset record using the returned id
        this.addAssetRecord({
          id: returnedTaskId,
          prompt: normalizedPrompt,
          imagePreview: finalPreview,
          createdAt: result?.finishedAt || Date.now(),
          updatedAt: result?.finishedAt || Date.now()
        });

        return { ok: true, taskId: returnedTaskId, imagePreview: finalPreview };
      } catch (error) {
        this.updateTask(taskId, { status: 'failed', progress: 100, updatedAt: Date.now() });
        return { ok: false, message: error.message || '模型生成失败', taskId };
      }
    }
  }
});
