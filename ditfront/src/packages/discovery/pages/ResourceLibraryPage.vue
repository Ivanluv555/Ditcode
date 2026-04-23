<template>
  <div class="discovery-hall">
    <header ref="headerRef" class="community-header">
      <p class="title-eyebrow">灵感素材库</p>
      <h2>浏览资源灵感</h2>
      <p>按部件分类浏览，点击卡片查看并继续 Remix。</p>

      <div class="resource-filters">
        <button
          v-for="(item, idx) in filters"
          :key="item.value"
          :class="['filter-btn', { active: selected === item.value }]"
          @click="selectFilter(item.value)"
        >
          {{ item.label }}
        </button>
        <button class="filter-btn" :class="{ active: selected === '' }" @click="selectFilter('')">全部</button>
      </div>
    </header>

    <div class="grid-container">
      <AssetCard
        v-for="(item, index) in filteredCards"
        :key="item.id"
        :id="item.id"
        source="resource"
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
const filters = [
  { value: '卧具', label: '卧具' },
  { value: '坐具', label: '坐具' },
  { value: '实用部件', label: '实用部件' },
  { value: '其他', label: '其他' }
];

const selected = ref('');
const headerRef = ref(null);
const prefersReducedMotion = useReducedMotion();

const allCards = computed(() =>
  taskStore.resourceArchives.map((item) => ({
    id: item.id,
    cover: item.modelAsset?.imagePreview || '/assets/index.jpg',
    author: item.ownerName || '资源作者',
    remixCount: item.remixCount || 0,
    promptTags: item.messages.length ? [item.messages[item.messages.length - 1].text] : [item.title || '资源'],
    title: item.title || (item.messages && item.messages[item.messages.length - 1]?.text) || '资源',
    category: item.category || '',
    to: { path: '/', query: { archive: item.id } }
  }))
);

const filteredCards = computed(() => {
  if (!selected.value) return allCards.value;
  return allCards.value.filter((c) => c.category === selected.value);
});

const selectFilter = (value) => {
  selected.value = value;
};

onMounted(() => {
  void taskStore.fetchResourceArchives?.();
  nextTick(() => {
    if (!headerRef.value || prefersReducedMotion.value) return;
    gsap.fromTo(headerRef.value, { opacity: 0, y: 18 }, { opacity: 1, y: 0, duration: 0.6, ease: 'power3.out' });
  });
});
</script>

<style scoped>
.resource-filters { margin-top: 12px; display:flex; gap:8px; flex-wrap:wrap; }
.filter-btn { padding:6px 10px; border-radius:999px; border:1px solid var(--color-border); background:var(--color-bg-soft); cursor:pointer; }
.filter-btn.active { background:var(--color-primary); color:var(--color-primary-contrast); border-color:var(--color-primary); }

.discovery-hall { box-sizing:border-box; padding:30px 24px 40px; background:transparent; min-height:100vh; color:var(--color-text-primary); }
.community-header { max-width:1180px; margin:0 auto 18px; }
.title-eyebrow { display:inline-block; margin:0; color:var(--color-primary); background:var(--color-primary-soft); border-radius:999px; font-size:12px; padding:4px 10px; }
.grid-container { display:grid; grid-template-columns:repeat(auto-fill, minmax(280px, 1fr)); gap:20px; max-width:1180px; margin:0 auto; }

@media (max-width: 768px) {
  .grid-container { display:block; columns:2; column-gap:12px; }
}
</style>
