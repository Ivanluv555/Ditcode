<!--
  AssetCard.vue
  ---------------
  功能：展示单个封面卡片（通常在社区或我的内容页面中使用），包括封面图片、作者、标签和 remix 计数。

  备注给非前端人员：
  - 该组件把封面图显示为背景图片，并在卡片点击时导航到工作区并打开指定的 archive（通过路由 query）。
  - 使用了 useReducedMotion Hook 来尊重用户的动画偏好（减少运动）。
-->
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

/*
 * AssetCard styles
 * -----------------
 * 卡片组件的视觉样式：圆角、背景模糊、封面区域和信息区。
 * 关键点：
 * - cover-wrapper 采用 padding-bottom 技巧保持固定宽高比（16:9 或 56.25%）；
 * - hover 效果通过 transform 与 box-shadow 增强交互感；
 * - remix-badge 放置在封面右上角以提示 remix 次数。
 */
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
