EditorPanel (src/packages/workspace/components/EditorPanel.vue)

Purpose

This small component offers a compact editor used inside the workspace for making generation requests (prompt text + optional reference image). It can operate in normal editing mode or previewMode (read-only, shows a preview image and text provided by props).

Inputs / Props

- previewMode (Boolean): when true, component is read-only and displays previewPrompt/previewImage.
- previewPrompt (String): text to show when in preview mode.
- previewImage (String): image URL to show when in preview mode.

Local state

- promptText: the current textual content to send to the model.
- imageFile / imagePreview: file selected by the user; imagePreview is a blob: URL for immediate display.

Key methods

- generateNew(): validates inputs, creates a task via taskStore.addTask/generateModelAsset, and then navigates to viewer when complete. Historically this triggered a Three.js cross-fade; it now opens viewer page.
- onPickFile(): handle a file input change and create an object URL for preview.

Important UX rules (why implemented)

- allowImageAttachment checks store.currentArchiveMessages to ensure images are only attached at the start of a work flow to prevent confusion.
- Blob URLs are revoked when no longer needed to avoid memory leaks.

Developer note

- generateNew includes a mock flow for testing: it simulates progress and then sets a success result. The real implementation goes through generateModelAsset which handles server communication and asset normalization.