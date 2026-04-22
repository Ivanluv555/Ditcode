<template>
  <div class="panorama-container">
    <canvas id="webgl-canvas" ref="canvasRef"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { WebGLEngine } from '@/shared/core/webgl/Engine';
import { disposeScene } from '@/shared/core/webgl/GC';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';

const canvasRef = ref(null);
let engine = null;
let objectUrl = null;
const taskStore = useTaskStore();
const asset = computed(() => taskStore.currentArchive?.modelAsset || null);

const loadFromCacheOrPreview = async () => {
  if (!asset.value) return;
  const assetId = asset.value.id;
  const cachedKey = asset.value.cachedLocal || localStorage.getItem('dit-panorama-cache:' + assetId) || null;
  if (cachedKey && 'caches' in window) {
    try {
      const cache = await caches.open('dit-panorama-cache');
      const resp = await cache.match(cachedKey);
      if (resp) {
        const blob = await resp.blob();
        objectUrl = URL.createObjectURL(blob);
        engine.onCrossFade({ detail: { imagePreview: objectUrl } });
        return;
      }
    } catch (e) {
      console.warn('panorama cache read failed', e);
    }
  }

  if (asset.value.imagePreview) {
    engine.onCrossFade({ detail: { imagePreview: asset.value.imagePreview } });
  }
};

onMounted(() => {
  if (canvasRef.value) {
    engine = new WebGLEngine(canvasRef.value);
    engine.start();
    void loadFromCacheOrPreview();
  }
});

onBeforeUnmount(() => {
  if (engine) {
    engine.stop();
    disposeScene(engine.scene, engine.renderer);
    engine = null;
  }
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl);
    objectUrl = null;
  }
});
</script>

<style scoped>
.panorama-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: var(--color-bg-page);
}
#webgl-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
}
</style>
