<template>
  <div class="workshop-container">
    <div class="panorama-preview">
      <img :src="currentImagePreview || TEST_PANORAMA_URL" alt="全景预览" />
    </div>

    <div class="ui-layer">
      <TaskPod />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import TaskPod from '@/packages/workspace/components/TaskPod.vue';

const taskStore = useTaskStore();
const currentImagePreview = computed(() => taskStore.currentArchive?.modelAsset?.imagePreview || '');
const TEST_PANORAMA_URL = '/testpicture.png';
</script>

<style scoped>
.workshop-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  background: var(--color-bg-page);
}
.panorama-preview {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--color-bg-page);
}
.panorama-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.ui-layer {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 10;
  pointer-events: none;
}

</style>
