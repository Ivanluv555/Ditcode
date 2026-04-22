<template>
  <div class="workshop-container">
    <canvas id="webgl-canvas" ref="canvasRef"></canvas>
    <div class="ui-layer">
      <TaskPod />
    </div>
  </div>
</template>
<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { WebGLEngine } from '@/shared/core/webgl/Engine';
import { disposeScene } from '@/shared/core/webgl/GC';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import TaskPod from '@/packages/workspace/components/TaskPod.vue';

const canvasRef = ref(null);
const taskStore = useTaskStore();
const currentImagePreview = computed(() => taskStore.currentArchive?.modelAsset?.imagePreview || '');
const TEST_PANORAMA_URL = '/testpicture.png';
let engine = null;

const applyPanoramaToEngine = (imagePreview) => {
  if (!engine || !imagePreview) return;
  void engine.onCrossFade({ detail: { imagePreview } });
};

onMounted(() => {
  if (canvasRef.value) {
    engine = new WebGLEngine(canvasRef.value);
    engine.start();
    applyPanoramaToEngine(currentImagePreview.value || TEST_PANORAMA_URL);
  }
});

watch(
  () => currentImagePreview.value,
  (nextImagePreview, prevImagePreview) => {
    if (!engine || !nextImagePreview || nextImagePreview === prevImagePreview) return;
    applyPanoramaToEngine(nextImagePreview);
  }
);

onBeforeUnmount(() => {
  if (engine) {
    engine.stop();
    disposeScene(engine.scene, engine.renderer);
    engine = null;
  }
});
</script>
<style scoped>
.workshop-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  background: var(--color-bg-page);
}
#webgl-canvas {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 1;
}
.ui-layer {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 10;
  pointer-events: none;
}

</style>
