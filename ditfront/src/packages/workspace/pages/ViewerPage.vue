<!--
  ViewerPage.vue
  ----------------
  功能：全屏查看器页面。当前实现通过 iframe 加载 demo.html 并将要查看的图片地址通过查询参数传入。

  重要说明（非前端人员）：
  - 本页本身并不执行 Three.js 渲染，渲染在 demo.html（独立页面）中完成。
  - 传递图片给 iframe 的方法是通过 URL 的 query 参数（例如 /viewer?image=<encoded-url>）。
  - 注意：如果传入的是 blob: URL（本地对象 URL），iframe 可能无法访问该资源；首选使用远程可访问的 http(s) URL 或事先缓存。
-->
<template>
  <div class="viewer-root">
    <iframe v-if="iframeSrc" :src="iframeSrc" frameborder="0" class="viewer-frame"></iframe>
    <div v-else class="viewer-empty">无预览图</div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const image = computed(() => route.query.image || '');
const iframeSrc = computed(() => {
  if (!image.value) return '';
  try {
    return `/demo.html?image=${encodeURIComponent(String(image.value))}`;
  } catch (e) {
    return `/demo.html`;
  }
});
</script>

<style scoped>
.viewer-root { width: 100%; height: 100%; background: #000; }
.viewer-frame { width: 100%; height: 100%; border: none; }
.viewer-empty { color: #999; padding: 20px; text-align: center; }
</style>