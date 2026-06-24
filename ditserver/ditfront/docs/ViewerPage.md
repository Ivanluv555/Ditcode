ViewerPage (src/packages/workspace/pages/ViewerPage.vue)

Purpose

This page displays a full-screen viewer for a panorama image. It currently embeds demo.html via an iframe and passes the image URL via query string (demo.html?image=...). demo.html contains the Three.js PanoramaEngine (independent page) which performs WebGL rendering.

What beginners must know

- ViewerPage simply builds an iframe src pointing to demo.html with an encoded image parameter. The heavy lifting (Three.js) happens inside demo.html and is sandboxed from the SPA.
- This architecture keeps the SPA free of Three.js dependencies while allowing a separate page to use it via CDN.

Limitations and notes

- Blob URL ownership: if the store creates a blob: URL in SPA and the iframe's document tries to load it, the iframe may not have access unless same-origin and the blob was created in a context accessible to the iframe. Prefer remote URLs or caching to avoid blob transfer issues.
- For better integration, future work could port PanoramaEngine into an SPA component instead of an iframe. That allows better control of assets and avoids cross-document blob issues.

Developer tip

- To debug the viewer, open demo.html directly with a query parameter (e.g., demo.html?image=https://example.com/my.jpg) to test Three.js rendering outside the SPA.