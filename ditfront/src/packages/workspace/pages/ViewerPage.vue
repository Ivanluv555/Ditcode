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