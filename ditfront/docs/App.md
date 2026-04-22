App Shell (src/App.vue)

Purpose

This file implements the overall application shell: left sidebar (archives/history), topbar (brand, controls), center content area (router-view), bottom composer (prompt input), and the model drawer (right-side panel). It wires together global stores, routing, and theme behavior.

What a beginner must understand

- Vue components are single-file components: template (HTML), script (JS), style (CSS). App.vue is the root component.
- router-view is where page components are rendered based on the URL.
- Reactive values (ref/computed) track state such as whether the sidebar is collapsed or whether a model drawer is open.

Key reactive variables and their roles

- isSidebarCollapsed: controls left sidebar collapsing.
- isModelDrawerOpen: whether the right model drawer (historical behavior) is visible.
- promptText / imageFile: inputs for the bottom composer used to request model generation.
- currentUser/history: derived from auth and task stores; used to populate sidebar and controls.

Important methods (what other code expects)

- generateFromComposer(): reads composer inputs and calls taskStore.generateModelAsset(). On success it now navigates to /viewer with the returned imagePreview.
- startNewWork(), openModelFromHistory(id): helpers to manage archive selection.

UI flow overview for novices

1. User types a prompt in the bottom composer.
2. generateFromComposer triggers taskStore.generateModelAsset.
3. After success, the app navigates to the viewer page, which displays the generated image.

Styles

- CSS variables (e.g., --left-panel-width) are used to compute layout values used by the shell. The shell applies different theme backgrounds based on data-theme attribute.

Notes for maintainers

- App.vue contains many animation helpers (GSAP) and user dropdown logic; these are presentation concerns and can be simplified if needed.
- If the viewer route path changes, update generateFromComposer and EditorPanel where router.push is used.