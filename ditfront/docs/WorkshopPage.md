WorkshopPage (src/packages/workspace/pages/WorkshopPage.vue)

Purpose

This page renders the workspace where the user interacts with a chat-like UI of messages and generated assets. It displays messages as left/right bubbles and provides a TaskPod input layer for sending prompts.

Key concepts

- messages: sourced from taskStore.currentArchiveMessages — these contain both user prompts and assistant responses (including images).
- Left bubble (assistant): used to display returned images. The bubble width is limited to 50% of the main panel.
- Right bubble (user): displays prompts entered by the user.

Important template behaviors

- Each message is rendered in a row that is left- or right-aligned depending on role.
- If the assistant message contains imagePreview, the component shows the image and an "Open in viewer" button which navigates to /viewer with the image URL.

Styling notes

- The message list is vertically stacked with gaps; the container constrains width for readability.
- Bubbles have rounded corners and different background colors to distinguish roles.

Operational notes

- The component does not directly fetch images; it simply displays image URLs supplied by the store.
- If an image is blob: URL, it will render in the img tag. When handing blob: URLs between pages/iframes, be mindful that blob URL ownership is tied to the document that created them; using blob URLs in an iframe may fail if not created in the same origin/document, so the store prefers caching and remote URLs when possible.

Developer example

// open the viewer for a message's image
openViewer(message.imagePreview); // navigates to /viewer?image=...