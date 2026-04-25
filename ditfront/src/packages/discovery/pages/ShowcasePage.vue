<template>
  <div class="showcase-gallery">
    <header ref="headerRef" class="gallery-header">
      <p class="title-eyebrow">优秀案例</p>
      <h2>高质量生成作品画廊</h2>
      <p>沉浸式展示系统精选的360°全景设计案例，一键复用提示词生成同款作品。</p>
    </header>

    <!-- 调试信息 -->
    <div v-if="showcaseItems.length === 0" style="color: var(--color-text-primary); text-align: center; padding: 40px; background: var(--color-bg-card); border-radius: 12px; margin: 20px auto; max-width: 600px;">
      <p style="font-size: 18px; margin-bottom: 10px;">加载案例数据中...</p>
      <p style="font-size: 14px; color: var(--color-text-secondary);">当前项目数: {{ showcaseItems.length }}</p>
    </div>

    <div class="gallery-grid">
      <div
        v-for="(item, index) in showcaseItems"
        :key="item.id"
        class="gallery-card"
        :style="{ '--delay': index * 80 + 'ms' }"
      >
        <div class="card-image-wrapper">
          <img :src="item.image" :alt="item.title" class="card-image" />

          <!-- 信息浮层 -->
          <div class="info-overlay">
            <div class="overlay-content">
              <h3 class="case-title">{{ item.title }}</h3>
              <div class="style-tags">
                <span v-for="tag in item.tags" :key="tag" class="style-tag">{{ tag }}</span>
              </div>
              <p class="prompt-preview">{{ item.promptPreview }}</p>
            </div>

            <!-- 功能入口 -->
            <div class="action-buttons">
              <button class="view-360-btn" @click="open360Viewer(item)">
                <Icon icon="fa6-solid:cube" />
                <span>360°全景</span>
              </button>
              <button class="clone-btn" @click="cloneCase(item)">
                <Icon icon="fa6-solid:wand-magic-sparkles" />
                <span>一键同款</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 复制成功提示 -->
    <Transition name="toast">
      <div v-if="showCloneToast" class="clone-toast">
        <Icon icon="fa6-solid:check-circle" />
        <span>提示词已应用，返回工作区开始生成</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue';
import { computed, nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { gsap } from 'gsap';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';
import { MOTION_DURATION, MOTION_EASE } from '@/shared/motion/preset';
import { getMockShowcaseData } from '@/packages/discovery/data/mockShowcaseData';

const router = useRouter();
const headerRef = ref(null);
const prefersReducedMotion = useReducedMotion();
const showCloneToast = ref(false);
const showcaseItems = ref([]);

const open360Viewer = (item) => {
  // 如果是多图层查看器的HTML路径，直接打开新窗口
  if (item.image360 && item.image360.includes('.html')) {
    window.open(item.image360, '_blank');
  } else {
    router.push({
      path: '/viewer',
      query: { image: item.image360 || item.image }
    });
  }
};

const cloneCase = async (item) => {
  try {
    await navigator.clipboard.writeText(item.fullPrompt);
    showCloneToast.value = true;

    setTimeout(() => {
      showCloneToast.value = false;
      router.push('/');
    }, 2000);
  } catch (err) {
    console.error('复制失败:', err);
  }
};

onMounted(async () => {
  try {
    const response = await getMockShowcaseData();
    console.log('Showcase data loaded:', response);
    if (response.success) {
      showcaseItems.value = response.data;
      console.log('showcaseItems set to:', showcaseItems.value);
    }
  } catch (error) {
    console.error('加载案例数据失败:', error);
  }

  nextTick(() => {
    if (!headerRef.value || prefersReducedMotion.value) return;
    gsap.fromTo(
      headerRef.value,
      { opacity: 0, y: 18 },
      {
        opacity: 1,
        y: 0,
        duration: MOTION_DURATION.intro,
        ease: MOTION_EASE.enter
      }
    );
  });
});
</script>

<style scoped>
.showcase-gallery {
  box-sizing: border-box;
  padding: 30px 24px 40px;
  background: transparent;
  min-height: 100vh;
  color: var(--color-text-primary);
}

.gallery-header {
  max-width: 1400px;
  margin: 0 auto 32px;
}

.title-eyebrow {
  display: inline-block;
  margin: 0 0 12px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  border-radius: 999px;
  font-size: 12px;
  padding: 4px 10px;
  font-weight: 600;
}

.gallery-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
}

.gallery-header > p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

/* 画廊网格 - 大尺寸宽比例 */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 32px;
  max-width: 1400px;
  margin: 0 auto;
}

.gallery-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: var(--color-bg-card);
  animation: cardFadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  animation-delay: var(--delay);
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-image-wrapper {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.gallery-card:hover .card-image {
  transform: scale(1.05);
}

/* 信息浮层 - 默认隐藏 */
.info-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.75) 0%,
    rgba(0, 0, 0, 0.4) 50%,
    rgba(0, 0, 0, 0.85) 100%
  );
  backdrop-filter: blur(8px);
  opacity: 0;
  transition: opacity 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 24px;
}

.gallery-card:hover .info-overlay {
  opacity: 1;
}

.overlay-content {
  flex: 1;
}

.case-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.style-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.style-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 12px;
  font-weight: 500;
}

.prompt-preview {
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 功能入口 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.view-360-btn,
.clone-btn {
  flex: 1;
  padding: 12px 16px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.view-360-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: #ffffff;
}

.view-360-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.clone-btn {
  background: linear-gradient(135deg, var(--color-primary) 0%, #9333ea 100%);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 16px rgba(var(--color-primary-rgb), 0.4);
}

.clone-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(var(--color-primary-rgb), 0.6);
}

.clone-btn svg,
.view-360-btn svg {
  font-size: 16px;
}

/* 复制成功提示 */
.clone-toast {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  z-index: 1000;
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 500;
}

.clone-toast svg {
  color: var(--color-primary);
  font-size: 18px;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(20px) scale(0.95);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px) scale(0.95);
}

@media (max-width: 1200px) {
  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .gallery-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .view-360-btn,
  .clone-btn {
    width: 100%;
  }
}
</style>
