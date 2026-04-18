<template>
  <div class="discovery-hall">
    <header ref="headerRef" class="community-header">
      <p class="title-eyebrow">Community</p>
      <h2>发现社区灵感</h2>
      <p>浏览他人的创作，点击任一卡片进入工作台继续 Remix。</p>
    </header>
    <div class="grid-container">
      <AssetCard
        v-for="(item, index) in communityCards"
        :key="item.id"
        :id="item.id"
        source="community"
        :cover="item.cover"
        :author="item.author"
        :remixCount="item.remixCount"
        :promptTags="item.promptTags"
        :delay="index * 70"
      />
    </div>
  </div>
</template>

<script setup>
import AssetCard from '@/shared/components/AssetCard.vue';
import { computed, nextTick, onMounted, ref } from 'vue';
import { gsap } from 'gsap';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';

const taskStore = useTaskStore();

const communityCards = computed(() =>
  taskStore.communityArchives.map((item) => ({
    id: item.id,
    cover: item.modelAsset?.imagePreview || '/assets/index.jpg',
    author: item.ownerName || '社区用户',
    remixCount: item.remixCount || 0,
    promptTags: item.messages.length
      ? [item.messages[item.messages.length - 1].text]
      : [item.title || 'remix']
  }))
);

const headerRef = ref(null);
const prefersReducedMotion = useReducedMotion();

onMounted(() => {
  void taskStore.fetchCommunityArchives();

  nextTick(() => {
    if (!headerRef.value || prefersReducedMotion.value) return;

    gsap.fromTo(
      headerRef.value,
      { opacity: 0, y: 18 },
      { opacity: 1, y: 0, duration: 0.6, ease: 'power3.out' }
    );
  });
});
</script>

<style scoped>
.discovery-hall {
  box-sizing: border-box;
  padding: 30px 24px 40px;
  background: transparent;
  min-height: 100vh;
  color: var(--color-text-primary);
}

.community-header {
  max-width: 1180px;
  margin: 0 auto 18px;
}

.title-eyebrow {
  display: inline-block;
  margin: 0;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  border-radius: 999px;
  font-size: 12px;
  padding: 4px 10px;
}

h2 {
  margin: 12px 0 6px;
}

.community-header p {
  margin: 0;
  color: var(--color-text-muted);
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  max-width: 1180px;
  margin: 0 auto;
}


@media (max-width: 768px) {
  .grid-container {
    display: block;
    columns: 2;
    column-gap: 12px;
  }
}
</style>

