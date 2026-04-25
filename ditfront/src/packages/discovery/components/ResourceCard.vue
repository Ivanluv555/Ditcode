<!--
  ResourceCard.vue
  ----------------
  功能：灵感素材库的语义化资产卡片，展示设计单品的高质量渲染效果、专业术语、物理属性关键词和风格简介。
  提供快捷复制按钮，一键将配套提示词复制到系统剪贴板。

  设计要点：
  - 图文对齐排版，建立视觉与提示词参数的直觉关联
  - 悬浮时显示复制按钮和完整描述
  - 使用 GSAP 动画保持全局风格统一
-->
<template>
  <div ref="cardRef" class="resource-card" :style="cardStyle">
    <!-- 封面区域 -->
    <div class="cover-wrapper">
      <img :src="item.cover" class="cover" alt="设计资产封面" />

      <!-- 悬浮时显示的操作层 -->
      <div class="hover-overlay">
        <button class="copy-btn" @click.stop="handleCopy">
          <Icon icon="fa6-solid:copy" />
          <span>复制提示词</span>
        </button>
      </div>
    </div>

    <!-- 元数据区 -->
    <div class="metadata">
      <h3 class="title">{{ item.title }}</h3>

      <!-- 专业术语与物理属性关键词 -->
      <div class="keywords">
        <span v-for="keyword in item.keywords" :key="keyword" class="keyword-tag">
          {{ keyword }}
        </span>
      </div>

      <!-- 风格简介 -->
      <p class="description">{{ item.description }}</p>

      <!-- 提示词预览（部分显示） -->
      <div class="prompt-preview">
        <Icon icon="fa6-solid:wand-magic-sparkles" class="prompt-icon" />
        <span class="prompt-text">{{ truncatedPrompt }}</span>
      </div>

      <!-- Remix 计数 -->
      <div class="stats">
        <Icon icon="fa6-solid:arrows-rotate" />
        <span>{{ item.remixCount }} 次使用</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue';
import { computed, onMounted, ref } from 'vue';
import { gsap } from 'gsap';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';
import { MOTION_DURATION, MOTION_EASE } from '@/shared/motion/preset';

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  delay: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['copy-prompt']);

const cardRef = ref(null);
const prefersReducedMotion = useReducedMotion();

const cardStyle = {
  '--card-delay': `${props.delay}ms`
};

// 截断提示词用于预览
const truncatedPrompt = computed(() => {
  const text = props.item.promptText || '';
  return text.length > 60 ? text.slice(0, 60) + '...' : text;
});

const handleCopy = () => {
  emit('copy-prompt', props.item.promptText);

  // 复制按钮反馈动画
  if (!cardRef.value || prefersReducedMotion.value) return;

  const btn = cardRef.value.querySelector('.copy-btn');
  if (btn) {
    gsap.to(btn, {
      scale: 0.92,
      duration: 0.1,
      ease: 'power2.out',
      yoyo: true,
      repeat: 1
    });
  }
};

onMounted(() => {
  if (!cardRef.value || prefersReducedMotion.value) return;

  // 入场动画：与 AssetCard 保持一致的风格
  gsap.fromTo(
    cardRef.value,
    { opacity: 0, y: 36, scale: 0.98 },
    {
      opacity: 1,
      y: 0,
      scale: 1,
      duration: MOTION_DURATION.intro,
      delay: props.delay / 1000,
      ease: MOTION_EASE.enter
    }
  );
});
</script>

<style scoped>
.resource-card {
  backdrop-filter: blur(10px);
  background: var(--color-bg-card);
  border-radius: 16px;
  border: 1px solid var(--color-border);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  color: var(--color-text-primary);
  position: relative;
}

.resource-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
  border-color: var(--color-primary);
}

/* 封面区域 */
.cover-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 66.67%; /* 3:2 比例 */
  overflow: hidden;
  background: var(--color-bg-soft);
}

.cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.resource-card:hover .cover {
  transform: scale(1.08);
}

/* 悬浮操作层 */
.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.32s cubic-bezier(0.16, 1, 0.3, 1);
}

.resource-card:hover .hover-overlay {
  opacity: 1;
}

.copy-btn {
  padding: 12px 24px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
  transform: translateY(10px);
}

.resource-card:hover .copy-btn {
  transform: translateY(0);
}

.copy-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.05);
}

.copy-btn svg {
  font-size: 16px;
}

/* 元数据区 */
.metadata {
  padding: 16px;
}

.title {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--color-text-primary);
}

/* 关键词标签 */
.keywords {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.keyword-tag {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

/* 风格简介 */
.description {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 提示词预览 */
.prompt-preview {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px;
  background: var(--color-bg-soft);
  border-radius: 8px;
  margin-bottom: 10px;
  border: 1px solid var(--color-border);
}

.prompt-icon {
  color: var(--color-primary);
  font-size: 14px;
  margin-top: 2px;
  flex-shrink: 0;
}

.prompt-text {
  font-size: 12px;
  line-height: 1.5;
  color: var(--color-text-secondary);
  font-family: 'Courier New', monospace;
  word-break: break-word;
}

/* 统计信息 */
.stats {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.stats svg {
  font-size: 14px;
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .metadata {
    padding: 12px;
  }

  .title {
    font-size: 15px;
  }

  .description {
    font-size: 12px;
  }

  .copy-btn {
    padding: 10px 20px;
    font-size: 13px;
  }
}
</style>
