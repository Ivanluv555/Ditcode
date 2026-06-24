<!--
  HomePage.vue
  --------------
  功能：应用的主页，通常显示欢迎信息、最近工作或快速入口，帮助用户快速开始新创建的工作流。

  初学者理解要点：
  - Home 页面通常从 store 读取最近的 archive 列表并展示简单的入口（点击可以打开工作区或加载存档）。
  - 这是用户第一次进入应用时看到的页面，很多引导逻辑会在这里触发（例如欢迎提示）。
-->
<template>
  <section ref="homeRef" class="home-wrap" :class="{ 'has-chat': !showWelcome }">
    <template v-if="showWelcome">
      <div class="welcome-card">
        <img class="welcome-logo js-welcome-item" :src="welcomeLogoSrc" alt="见筑 Logo" />
        <h2 class="js-welcome-item">今天想创作什么？</h2>
        <p class="desc js-welcome-item">上传图片以获得效果</p>
      </div>

      <div class="shortcut-row js-welcome-item">
        <RouterLink class="shortcut" to="/community">没灵感？去社区看看</RouterLink>
      </div>
    </template>

    <template v-else>
      <div class="chat-wrap">
        <p class="chat-title">最近对话</p>
        <div class="bubble-list">
          <template v-for="bubble in userBubbles" :key="bubble.key">
            <!-- 用户输入气泡（右侧） -->
            <div
              v-if="bubble.text"
              class="bubble-item user-item"
              :class="{ 'bubble-item-new': !animatedBubbleKeys.includes(bubble.key) }"
              :data-bubble-key="bubble.key"
            >
              <div class="bubble-content">
                <div class="bubble-header">
                  <span class="bubble-author">{{ showPublishedAuthor ? publishedAuthorName : '我' }}</span>
                  <span class="bubble-time">{{ formatDate(bubble.createdAt) }}</span>
                </div>
                <div class="bubble-row">
                  <div class="bubble user">
                    <div class="bubble-text">{{ bubble.text }}</div>
                  </div>
                  <div class="avatar user-avatar">
                    <Icon icon="fa6-solid:user" />
                  </div>
                </div>
              </div>
            </div>

            <!-- 助手回复气泡（左侧，包含图片和按钮） -->
            <div
              v-if="bubble.imagePreview"
              class="bubble-item assistant-item"
              :class="{ 'bubble-item-new': !animatedBubbleKeys.includes(bubble.key) }"
              :data-bubble-key="`${bubble.key}-assistant`"
            >
              <div class="bubble-content">
                <div class="bubble-header">
                  <span class="bubble-author">见筑助手</span>
                  <span class="bubble-time">{{ formatDate(bubble.createdAt) }}</span>
                </div>
                <div class="bubble-row">
                  <div class="avatar assistant-avatar">
                    <img :src="welcomeLogoBlack" alt="见筑" class="avatar-logo" />
                  </div>
                  <div class="bubble assistant">
                    <img
                      :src="bubble.imagePreview"
                      class="bubble-image"
                      alt="生成的图像"
                    />
                    <button
                      class="panorama-btn"
                      @click="goToViewer(bubble)"
                    >
                      Enjoy the panoramic view
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <p v-if="userBubbles.length === 0 && !loadingTaskExists" class="chat-empty">这个存档还没有对话，先输入你的想法吧。</p>
        </div>
      </div>
    </template>

    <!-- 屏幕中央加载动画 -->
    <div v-if="loadingTaskExists" class="loading-overlay">
      <LoadingAnimation :size="576" />
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { gsap } from 'gsap';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';
import { MOTION_DURATION, MOTION_EASE } from '@/shared/motion/preset';
import LoadingAnimation from '@/shared/components/LoadingAnimation.vue';
import welcomeLogoBlack from '../../../../ditlogos/Blank_transparent_black_logo.svg';
import welcomeLogoWhite from '../../../../ditlogos/Blank_transparent_white_logo.svg';

const FORCE_WELCOME_ONCE_KEY = 'ditapp_force_welcome_once';
const FORCE_WELCOME_BASELINE_KEY = 'ditapp_force_welcome_baseline';

const taskStore = useTaskStore();
const route = useRoute();
const router = useRouter();
const forceWelcome = ref(false);
const forceWelcomeBaseline = ref(0);
const homeRef = ref(null);
const prefersReducedMotion = useReducedMotion();
const isDarkTheme = ref(false);
let welcomeCtx = null;
let shellObserver = null;
const currentArchive = computed(() => taskStore.currentArchive);
const animatedBubbleKeys = ref([]);
const welcomeLogoSrc = computed(() => (isDarkTheme.value ? welcomeLogoWhite : welcomeLogoBlack));

const formatDate = (value) => {
  if (!value) return '';
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const taskEntries = computed(() =>
  taskStore.tasks
    .filter((item) => item.prompt)
    .map((item) => ({
      key: `task-${item.id}`,
      id: item.id,
      text: item.prompt,
      imagePreview: item.imagePreview,
      imageName: item.imageName,
      createdAt: Number(item.createdAt) || Date.now()
    }))
);

const historyEntries = computed(() => {
  const taskIds = new Set(taskEntries.value.map((item) => item.id));

  return taskStore.assetHistory
    .filter((item) => item.prompt && !taskIds.has(item.id))
    .map((item) => ({
      key: `asset-${item.id}`,
      id: item.id,
      text: item.prompt,
      imagePreview: item.imagePreview,
      imageName: item.imageName,
      createdAt: Number(item.createdAt) || Date.now()
    }));
});

// 点击跳转事件
const goToViewer = (bubble) => {
  if (!bubble.imageName && !bubble.imagePreview && !bubble.id) return;

  // 优先使用 imageName，其次使用 id 兜底
  const rawName = bubble.imageName || bubble.id || 'untitled';
  const formattedName = encodeURIComponent(rawName);

  // 打开独立的 viewertool 页面并把图片名称作为 name 参数传递
  // 同时如果有直接的图片 URL（imagePreview），也作为备用的 image 参数传入
  const viewerPath = `${window.location.origin}/viewertool.html?name=${formattedName}`;
  const imageParam = bubble.imagePreview ? `&image=${encodeURIComponent(bubble.imagePreview)}` : '';
  // 使用新窗口打开，避免破坏当前 SPA 状态
  window.open(viewerPath + imageParam, '_blank', 'noopener');
};

const userBubbles = computed(() =>
  [...taskEntries.value, ...historyEntries.value].sort((a, b) => a.createdAt - b.createdAt)
);
const loadingTaskExists = computed(() =>
  taskStore.tasks.some((task) => ['queued', 'inferencing', 'compositing'].includes(task.status))
);

const showPublishedAuthor = computed(() => Boolean(currentArchive.value && currentArchive.value.isPrivate === false));
const publishedAuthorName = computed(() => currentArchive.value?.ownerName || '社区用户');

const hasNewInputAfterBaseline = computed(() =>
  userBubbles.value.some((item) => item.createdAt > forceWelcomeBaseline.value)
);

const showWelcome = computed(() => {
  if (forceWelcome.value) {
    return !hasNewInputAfterBaseline.value;
  }

  return userBubbles.value.length === 0;
});

const runWelcomeAnimation = () => {
  if (!homeRef.value || !showWelcome.value) return;

  if (welcomeCtx) {
    welcomeCtx.revert();
    welcomeCtx = null;
  }

  if (prefersReducedMotion.value) return;

  welcomeCtx = gsap.context(() => {
    const items = homeRef.value.querySelectorAll('.js-welcome-item');
    if (!items.length) return;

    gsap.fromTo(
      items,
      { opacity: 0, y: 15 },
      {
        opacity: 1,
        y: 0,
        duration: MOTION_DURATION.slow,
        stagger: 0.1,
        ease: MOTION_EASE.enter
      }
    );
  }, homeRef.value);
};

const syncThemeMode = () => {
  const shell = homeRef.value?.closest('.app-shell');
  isDarkTheme.value = Boolean(shell && shell.classList.contains('theme-dark'));
};

const markBubblesAnimated = (keys) => {
  if (!keys.length) return;
  animatedBubbleKeys.value = [...new Set([...animatedBubbleKeys.value, ...keys])];
};

const runBubbleAnimation = () => {
  if (!homeRef.value || showWelcome.value) return;
  const bubbleNodes = Array.from(homeRef.value.querySelectorAll('.bubble-item-new[data-bubble-key]'));
  if (!bubbleNodes.length) return;

  const bubbleKeys = bubbleNodes.map((node) => node.dataset.bubbleKey).filter(Boolean);
  if (!bubbleKeys.length) return;

  if (prefersReducedMotion.value) {
    markBubblesAnimated(bubbleKeys);
    return;
  }

  gsap.killTweensOf(bubbleNodes);
  gsap.fromTo(
    bubbleNodes,
    { opacity: 0, y: 8 },
    {
      opacity: 1,
      y: 0,
      duration: MOTION_DURATION.normal,
      stagger: 0.04,
      ease: MOTION_EASE.enter,
      onComplete: () => markBubblesAnimated(bubbleKeys)
    }
  );
};

onMounted(() => {
  if (sessionStorage.getItem(FORCE_WELCOME_ONCE_KEY) === '1') {
    forceWelcome.value = true;
    forceWelcomeBaseline.value = Number(sessionStorage.getItem(FORCE_WELCOME_BASELINE_KEY) || 0);
    sessionStorage.removeItem(FORCE_WELCOME_ONCE_KEY);
    sessionStorage.removeItem(FORCE_WELCOME_BASELINE_KEY);
  }

  // Clear legacy query-based trigger to avoid welcome state sticking to URL.
  if (route.query.welcome === '1') {
    forceWelcome.value = true;
    forceWelcomeBaseline.value = Math.max(...userBubbles.value.map((item) => item.createdAt), 0);
    const nextQuery = { ...route.query };
    delete nextQuery.welcome;
    router.replace({ path: route.path, query: nextQuery });
  }

  nextTick(() => {
    syncThemeMode();
    const shell = homeRef.value?.closest('.app-shell');
    if (shell) {
      shellObserver = new MutationObserver(syncThemeMode);
      shellObserver.observe(shell, { attributes: true, attributeFilter: ['class'] });
    }

    runWelcomeAnimation();
    runBubbleAnimation();
  });
});

onBeforeUnmount(() => {
  if (welcomeCtx) {
    welcomeCtx.revert();
    welcomeCtx = null;
  }
  if (shellObserver) {
    shellObserver.disconnect();
    shellObserver = null;
  }
});

watch(
  () => hasNewInputAfterBaseline.value,
  (value) => {
    if (forceWelcome.value && value) {
      forceWelcome.value = false;
      forceWelcomeBaseline.value = 0;
    }
  }
);

watch(
  () => [showWelcome.value, userBubbles.value.length, prefersReducedMotion.value],
  () => {
    nextTick(() => {
      runWelcomeAnimation();
      runBubbleAnimation();
    });
  }
);

watch(
  () => userBubbles.value.map((item) => item.key).join('|'),
  () => {
    nextTick(() => runBubbleAnimation());
  }
);

watch(
  () => currentArchive.value?.id || '',
  () => {
    animatedBubbleKeys.value = [];
    nextTick(() => runBubbleAnimation());
  }
);
</script>

<style scoped>
.home-wrap {
  min-height: 100%;
  box-sizing: border-box;
  padding: 36px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;
  gap: 16px;
}

.home-wrap.has-chat {
  justify-content: flex-start;
  align-items: stretch;
  padding-top: 22px;
}

.welcome-card {
  position: relative;
  max-width: none;
  width: 100%;
  background: transparent;
  padding: 10px 0 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
}

.welcome-logo {
  position: relative;
  margin-left: 0;
  filter: drop-shadow(0 6px 12px rgba(72, 161, 255, 0.35));
  pointer-events: none;
  opacity: 0.95;
  width: clamp(64px, 7vw, 96px);
  height: auto;
  display: block;
}

h2 {
  margin: 12px 0 10px;
  font-size: 34px;
  line-height: 1.3;
  font-weight: 600;
}

.desc {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 15px;
  line-height: 1.6;
}

.shortcut-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-start;
  width: 100%;
  margin-top: 6px;
}

.shortcut {
  text-decoration: none;
  color: var(--color-primary);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  padding: 10px 14px;
  border-radius: 10px;
}

.chat-wrap {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  gap: 12px;
}

.chat-title {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

.bubble-list {
  display: grid;
  gap: 10px;
}

.chat-empty {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

.bubble-item {
  display: grid;
  gap: 4px;
}

.bubble-item.user-item {
  justify-items: end;
}

.bubble-item.assistant-item {
  justify-items: start;
  margin-top: 8px;
}

.bubble-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 80%;
}

.bubble-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.bubble-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.user-item .bubble-row {
  flex-direction: row-reverse;
}

.assistant-item .bubble-row {
  flex-direction: row;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.assistant-avatar {
  background: white;
  border: 1px solid var(--color-border);
  padding: 4px;
}

.avatar-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.bubble {
  max-width: min(620px, 90vw);
  border-radius: 14px 14px 4px 14px;
  padding: 10px 12px;
  background: var(--color-primary);
  color: var(--color-primary-contrast);
  line-height: 1.45;
  word-break: break-word;
  box-shadow: var(--shadow-card);
}

.bubble.assistant {
  border-radius: 14px 14px 14px 4px;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.bubble-image {
  max-width: 100%;
  border-radius: 8px;
  margin-top: 8px;
  display: block;
}

.bubble-author {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text);
}

.bubble-time {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 屏幕中央加载动画覆盖层 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px);
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.panorama-btn {
  margin-top: 12px;
  width: 100%;
  padding: 10px 14px;
  border: none;
  border-radius: 8px;
  background-color: var(--color-primary);
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, background-color 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.panorama-btn:hover {
  background-color: color-mix(in srgb, var(--color-primary) 85%, black);
  transform: translateY(-2px);
}

.panorama-btn:active {
  transform: translateY(0);
}



@media (max-width: 768px) {
  .home-wrap {
    padding: 18px 0;
  }

  .welcome-card {
    padding: 18px;
  }

  h2 {
    font-size: 28px;
  }
}
</style>
