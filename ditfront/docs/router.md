Router (src/app/router/index.js)

Purpose

Defines SPA routes mapped to page components. The router determines what component is rendered in the central router-view depending on the URL path.

Routes of interest

- / : Home page (workspace listing)
- /viewer : Viewer page (displays full-screen panorama in an iframe)
- /workshop/:id : Workshop page for a specific archive (keeps compatibility)
- /login and /register : authentication pages (standalone layout)

Developer notes

- Routes may include meta flags (e.g., hideComposer) used by App.vue to control layout. When adding/removing routes, ensure meta keys are respected by the shell.
- When navigating programmatically, always prefer router.push({ path, query }) for clarity.

Example

router.push({ path: '/viewer', query: { image: 'https://...' } });