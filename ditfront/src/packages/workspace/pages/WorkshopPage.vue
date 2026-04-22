<template>
  <div class="workshop-container">
    <div class="messages-wrap">
      <div class="messages-list">
        <div v-for="msg in messages" :key="msg.id" :class="['message-row', msg.role === 'user' ? 'align-right' : 'align-left']">
          <!-- assistant (left) shows image bubble if available -->
          <div v-if="msg.role !== 'user'" class="bubble assistant">
            <div v-if="msg.imagePreview" class="image-wrap">
              <img :src="msg.imagePreview" alt="全景预览" />
              <div class="viewer-actions">
                <button class="open-viewer" @click="openViewer(msg.imagePreview)">在查看器中打开</button>
              </div>
            </div>
            <div v-else class="text-content">{{ msg.text }}</div>
          </div>

          <!-- user (right) shows text bubble -->
          <div v-if="msg.role === 'user'" class="bubble user">
            <div class="text-content">{{ msg.text }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="ui-layer">
      <TaskPod />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import TaskPod from '@/packages/workspace/components/TaskPod.vue';

const router = useRouter();
const taskStore = useTaskStore();
const messages = computed(() => taskStore.currentArchiveMessages || []);

const openViewer = (imageUrl) => {
  if (!imageUrl) return;
  router.push({ path: '/viewer', query: { image: imageUrl } });
};
</script>

<style scoped>
.workshop-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;
  background: var(--color-bg-page);
  padding: 24px 32px;
  box-sizing: border-box;
}
.messages-wrap {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
}
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.message-row {
  display: flex;
  align-items: flex-end;
}
.message-row.align-right { justify-content: flex-end; }
.message-row.align-left { justify-content: flex-start; }
.bubble {
  max-width: 50%;
  box-sizing: border-box;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f3f7fb;
  color: #102030;
}
.bubble.user {
  background: #2b6ef6;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.bubble.assistant {
  background: rgba(255,255,255,0.9);
  color: #102030;
}
.text-content { white-space: pre-wrap; word-break: break-word; }
.image-wrap img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 8px;
}
.viewer-actions { margin-top: 8px; display:flex; justify-content:flex-start; }
.open-viewer {
  border: none;
  background: #0f63d9;
  color: white;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
}
.ui-layer { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 10; pointer-events: none; }
.ui-layer > * { pointer-events: auto; }
</style>
