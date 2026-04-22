demo.html (standalone Web page for PanoramaEngine)

Purpose

demo.html is a standalone HTML page that initializes a Three.js-based PanoramaEngine. It supports file upload (local blob) and direct loading via the query parameter ?image=URL.

Why it exists separately

- The SPA no longer depends on three.js. demo.html uses CDN imports so Three.js code is sandboxed in an isolated page that can be embedded in an iframe by ViewerPage.vue.

How PanoramaEngine works (brief)

- Creates a WebGLRenderer tied to a canvas.
- Builds a large inward-facing sphere (SphereGeometry) and maps an equirectangular texture onto its interior surface.
- Places the camera at the sphere center and lets users rotate via OrbitControls.
- Implements texture loading with fallback strategies and a fade-in using Tween for smooth transitions.

Usage

- Open demo.html directly and choose a panoramic image (2:1 recommended) or use demo.html?image=<encoded URL> to load remotely.

Notes for maintainers

- If you want to remove Three.js entirely, refactor ViewerPage to render a plain <img> and delete demo.html.
- To port the engine into SPA, extract PanoramaEngine into a Vue component and import three as an ESM dependency.