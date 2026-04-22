<template>
  <div class="panorama-container">
    <div class="panorama-inner">
      <img v-if="imageUrl" :src="imageUrl" alt="全景预览" />
      <div v-else class="panorama-empty">暂无预览</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';

const taskStore = useTaskStore();
const asset = computed(() => taskStore.currentArchive?.modelAsset || null);
const imageUrl = ref('');
let objectUrl = null;

const setImageFromAsset = async (targetAsset) => {
  const useAsset = targetAsset || asset.value;
  if (!useAsset) {
    if (imageUrl.value && imageUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(imageUrl.value);
    }
    imageUrl.value = '';
    return;
  }

  // Prefer ephemeral local preview first
  if (useAsset.localPreview) {
    imageUrl.value = useAsset.localPreview;
    return;
  }

  const assetId = useAsset.id;
  const cachedKey = useAsset.cachedLocal || localStorage.getItem('dit-panorama-cache:' + assetId) || null;
  if (cachedKey && 'caches' in window) {
    try {
      const cache = await caches.open('dit-panorama-cache');
      const resp = await cache.match(cachedKey);
      if (resp) {
        const blob = await resp.blob();
        if (objectUrl) {
          URL.revokeObjectURL(objectUrl);
          objectUrl = null;
        }
        objectUrl = URL.createObjectURL(blob);
        imageUrl.value = objectUrl;
        return;
      }
    } catch (e) {
      console.warn('panorama cache read failed', e);
    }
  }

  if (useAsset.imagePreview) {
    imageUrl.value = useAsset.imagePreview;
  } else {
    imageUrl.value = '';
  }
};

onMounted(() => {
  void setImageFromAsset();
});

watch(asset, (newVal) => {
  void setImageFromAsset(newVal);
});

onBeforeUnmount(() => {
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
  display: flex;
  align-items: center;
  justify-content: center;
}
.panorama-inner {
  width: 100%;
  height: 100%;
  position: relative;
}
.panorama-inner img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
.panorama-empty {
  color: var(--color-text-muted);
  text-align: center;
}
</style>
