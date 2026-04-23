<!--
  ViewerPage.vue
  ----------------
  功能：全景图查看器页面。该页面使用 Three.js 渲染 2:1 全景图球体，避免 iframe 嵌入 demo.html。
  - 通过 query 参数 image 获取平面全景图 URL 或 Base64 数据。
  - 工作完成后，前端应立即显示平面预览气泡，用户点击图片再跳转到此查看器。
-->
<template>
  <div class="viewer-root">
    <div class="viewer-toolbar">
      <button class="back-btn" @click="goBack">返回工作区</button>
      <span class="viewer-tip">拖拽 / 滚轮缩放查看全景</span>
    </div>

    <div class="canvas-wrapper">
      <canvas ref="canvasRef" class="viewer-canvas"></canvas>
      <div v-if="loading" class="viewer-loading">加载全景图，请稍候…</div>
      <div v-if="error" class="viewer-error">
        <p>无法加载全景图。</p>
        <p>{{ error }}</p>
      </div>
      <div v-if="!image && !loading" class="viewer-empty">
        未找到要展示的全景图。请在工作区中点击平面预览图片打开查看器。
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import * as TWEEN from '@tweenjs/tween.js';

const route = useRoute();
const router = useRouter();
const canvasRef = ref(null);
const loading = ref(false);
const error = ref('');
const image = computed(() => (route.query.image ? String(route.query.image) : ''));

let viewerEngine = null;

class PanoramaViewer {
  constructor(canvas) {
    this.canvas = canvas;
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0x111111);

    const width = window.innerWidth;
    const height = window.innerHeight;
    this.camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000);
    this.camera.position.set(0, 0, 0.1);

    this.renderer = new THREE.WebGLRenderer({
      canvas: this.canvas,
      antialias: true,
      powerPreference: 'high-performance'
    });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.setSize(width, height);

    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.dampingFactor = 0.05;
    this.controls.enablePan = false;
    this.controls.minDistance = 0;
    this.controls.maxDistance = 100;

    this.currentSphere = null;
    this.running = true;
    this.animate = this.animate.bind(this);
    this.resizeListener = this.onWindowResize.bind(this);
    window.addEventListener('resize', this.resizeListener);
    this.createPlaceholder();
    this.animationId = requestAnimationFrame(this.animate);
  }

  createPlaceholder() {
    const geometry = new THREE.SphereGeometry(500, 60, 40);
    const material = new THREE.MeshBasicMaterial({
      color: 0x222222,
      wireframe: true,
      side: THREE.BackSide
    });
    this.currentSphere = new THREE.Mesh(geometry, material);
    this.scene.add(this.currentSphere);
  }

  async loadPanorama(imageUrl) {
    loading.value = true;
    error.value = '';

    try {
      const textureLoader = new THREE.TextureLoader();
      const texture = await textureLoader.loadAsync(imageUrl);
      texture.colorSpace = THREE.SRGBColorSpace;
      texture.flipY = true;
      texture.minFilter = THREE.LinearFilter;
      texture.generateMipmaps = false;

      const geometry = new THREE.SphereGeometry(500, 60, 40);
      const material = new THREE.MeshBasicMaterial({
        map: texture,
        side: THREE.BackSide,
        transparent: true,
        opacity: 0,
        depthWrite: true,
        depthTest: true
      });
      const newSphere = new THREE.Mesh(geometry, material);
      this.scene.add(newSphere);

      new TWEEN.Tween(material)
        .to({ opacity: 1 }, 900)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onComplete(() => {
          if (this.currentSphere) {
            this.scene.remove(this.currentSphere);
            if (this.currentSphere.material.map) {
              this.currentSphere.material.map.dispose();
            }
            this.currentSphere.material.dispose();
            this.currentSphere.geometry.dispose();
          }
          this.currentSphere = newSphere;
        })
        .start();

      if (imageUrl.startsWith('blob:')) {
        setTimeout(() => {
          try { URL.revokeObjectURL(imageUrl); } catch (e) {}
        }, 2000);
      }
    } catch (err) {
      error.value = err?.message || '加载图片失败，请检查图片地址或网络状态。';
    } finally {
      loading.value = false;
    }
  }

  onWindowResize() {
    if (!this.renderer || !this.camera) return;
    const width = window.innerWidth;
    const height = window.innerHeight;
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(width, height);
  }

  animate(time) {
    if (!this.running) return;
    this.controls.update();
    TWEEN.update(time);
    this.renderer.render(this.scene, this.camera);
    this.animationId = requestAnimationFrame(this.animate);
  }

  dispose() {
    this.running = false;
    if (typeof this.animationId === 'number') {
      cancelAnimationFrame(this.animationId);
    }
    window.removeEventListener('resize', this.resizeListener);
    if (this.currentSphere) {
      if (this.currentSphere.material.map) {
        this.currentSphere.material.map.dispose();
      }
      this.currentSphere.material.dispose();
      this.currentSphere.geometry.dispose();
    }
    if (this.renderer) {
      this.renderer.dispose();
    }
  }
}

const initViewer = async () => {
  if (!canvasRef.value) return;

  try {
    viewerEngine = new PanoramaViewer(canvasRef.value);

    if (image.value) {
      await viewerEngine.loadPanorama(decodeURIComponent(image.value));
    }
  } catch (err) {
    error.value = err?.message || '无法初始化全景查看器。';
  }
};

const goBack = () => router.back();

onMounted(initViewer);

watch(image, async (newValue) => {
  if (viewerEngine && newValue) {
    await viewerEngine.loadPanorama(decodeURIComponent(newValue));
  }
});

onBeforeUnmount(() => {
  if (viewerEngine) viewerEngine.dispose();
});
</script>

<style scoped>
.viewer-root {
  width: 100%;
  height: 100%;
  position: relative;
  background: #090b10;
  color: #eef2f8;
  overflow: hidden;
}
.viewer-toolbar {
  position: absolute;
  top: 18px;
  left: 18px;
  z-index: 20;
  display: flex;
  gap: 12px;
  align-items: center;
}
.back-btn {
  border: none;
  background: rgba(14, 35, 80, 0.95);
  color: #fff;
  padding: 10px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
}
.viewer-tip {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}
.canvas-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}
.viewer-canvas {
  width: 100%;
  height: 100%;
  display: block;
}
.viewer-loading,
.viewer-error,
.viewer-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(4, 9, 18, 0.78);
  color: #f5f7ff;
  text-align: center;
  padding: 24px;
  z-index: 15;
  pointer-events: none;
}
.viewer-error p { margin: 6px 0; }
</style>
