useTaskStore (src/packages/workspace/store/useTaskStore.js)

Purpose

This module is a Pinia store that manages the user's workspace: archives (projects), tasks (generation jobs), messages (chat-style entries), and model assets (generated images). It is the canonical source of truth for what the UI renders in the workspace.

High-level concepts (beginner-friendly)

- Archive: a project that contains messages, tasks, and optionally a modelAsset (the most recent generated image).
- Task: a single generation job. Contains id, status (queued, inferencing, success, failed), prompt, imagePreview, progress, timestamps.
- Message: chat-like entry with role (user/assistant), text, and optional imagePreview. Messages drive what appears in the "chat" UI.
- modelAsset: a canonical record for the most recent generated image for the archive.

Key API (what other code calls)

- init(): loads workspace from backend and populates archives/tasks/messages.
- createArchive(options): create a new archive and make it current.
- openArchive(id): set an existing archive to current (loads messages/tasks for UI).
- addTask(task): add or update a task, and append the task's prompt to messages (so the user message appears).
- updateTask(id, updates): update a task's status/progress.
- addAssetRecord(record): register a generated image (modelAsset) and cache it; this will also insert an assistant message referencing the image so the UI shows it as a bubble.
- generateModelAsset(payload): the high-level function that sends a POST to /api/model/generate, normalizes a variety of returned image formats, sets task state, and calls addAssetRecord.
- appendMessage(text, options): append a chat message (allows image-only assistant messages). Use this to push assistant/user messages.

Data flow (simple)

1. User initiates generation (UI calls generateModelAsset via EditorPanel or App composer).
2. generateModelAsset creates a task in the archive (status: inferencing) and sends an API request to the backend.
3. When backend returns an image (data:, URL, ArrayBuffer, Blob), generateModelAsset normalizes it to a displayable string (finalPreview) and—if possible—creates a local blob: URL (localPreview) for faster immediate rendering.
4. The store marks the task success and calls addAssetRecord, which writes modelAsset into the archive and appends an assistant message with imagePreview/localPreview.
5. UI components read store.getters: currentArchiveMessages and render chat bubbles accordingly.

Important implementation notes (why things are coded this way)

- Messages are the primary UI driver: even generated images are surfaced by inserting a message. This keeps the UI rendering simple (just iterate messages).
- Multiple backend formats are supported because different servers can return images as base64 strings, data URLs, raw buffers, or remote URLs.
- Blob URLs are used for immediate local rendering without persisting large base64 strings in state.
- Cache API + localStorage are used to reduce repeated downloads for the same asset.

Memory/cleanup responsibilities

- When storing blob URLs (localPreview), the code tries to revoke them when replaced or when components unmount, but blob URL lifecycle should be considered when integrating advanced features.

Examples

// append a user message and request a generation
useTaskStore.appendMessage('Make a cozy living room');
await useTaskStore.generateModelAsset({ prompt: 'Make a cozy living room' });

// open viewer for the latest image
const img = useTaskStore.currentArchive.modelAsset?.imagePreview || useTaskStore.currentArchive.modelAsset?.localPreview;


Notes for maintainers

- Keep normalizeModelAsset and normalizeTask robust; many other parts rely on these structures.
- If changing the message shape, update UI components that map messages to bubbles (WorkshopPage.vue).