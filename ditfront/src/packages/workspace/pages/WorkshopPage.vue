<template>
  <div class="workshop-container">
    <canvas id="webgl-canvas" ref="canvasRef"></canvas>
    <div class="ui-layer">
      <TaskPod />
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { WebGLEngine } from '@/shared/core/webgl/Engine';
import { disposeScene } from '@/shared/core/webgl/GC';
import TaskPod from '@/packages/workspace/components/TaskPod.vue';
const canvasRef = ref(null);
let engine = null;
onMounted(() => {
  if (canvasRef.value) {
    engine = new WebGLEngine(canvasRef.value);
    engine.start();
  }
});
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
