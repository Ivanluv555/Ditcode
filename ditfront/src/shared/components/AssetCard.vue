<template>
  <div ref="cardRef" class="asset-card" :style="cardStyle" @click="onClick">
    <div class="cover-wrapper">
      <img :src="resolvedCover" class="cover" alt="cover" />
      <span class="remix-badge">{{ remixCount }} Remix</span>
    </div>
    <div class="info">
      <div class="author">{{ author }}</div>
      <div class="tags">
        <span class="tag" v-for="tag in promptTags" :key="tag">#{{ tag }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { gsap } from 'gsap';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';

const props = defineProps({
  id: String,
  source: {
    type: String,
    default: 'archive'
  },
  cover: String,
  author: String,
  remixCount: Number,
  promptTags: Array,
  delay: {
    type: Number,
    default: 0
  }
});

const router = useRouter();
const cardRef = ref(null);
const prefersReducedMotion = useReducedMotion();
const resolvedCover = props.cover || '/assets/index.jpg';

const cardStyle = {
  '--card-delay': `${props.delay}ms`
};

const onClick = () => {
  router.push({
    path: '/',
    query: { archive: props.id, source: props.source }
  });
};

onMounted(() => {
  if (!cardRef.value || prefersReducedMotion.value) return;

  gsap.fromTo(
    cardRef.value,
    { opacity: 0, y: 36, scale: 0.98 },
    {
      opacity: 1,
      y: 0,
      scale: 1,
      duration: 0.7,
      delay: props.delay / 1000,
      ease: 'power3.out'
    }
  );
});
</script>

<style scoped>
.asset-card {
  backdrop-filter: blur(10px);
  background: var(--color-bg-card);
  border-radius: 12px;
  border: 1px solid var(--color-border);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.35s ease;
  color: var(--color-text-primary);
  margin-bottom: 20px;
}
.asset-card:hover {
  transform: translateY(-2px) scale(1.015);
  box-shadow: var(--shadow-card);
}
.cover-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 56.25%;
}
.cover {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  object-fit: cover;
}
.remix-badge {
  position: absolute;
  top: 8px; right: 8px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}
.info {
  padding: 12px;
}
.author {
  font-weight: bold;
  margin-bottom: 8px;
}
.tags {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  scrollbar-width: none;
}
.tag {
  background: var(--color-bg-soft-strong);
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  white-space: nowrap;
}
</style>
