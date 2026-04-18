<template>
  <section ref="homeRef" class="home-wrap" :class="{ 'has-chat': !showWelcome }">
    <template v-if="showWelcome">
      <div class="welcome-card">
        <Vue3Lottie
          class="welcome-lottie js-welcome-item"
          :animationData="welcomeAnimation"
          :height="48"
          :width="48"
          :loop="false"
          :autoPlay="true"
        />
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
          <div
            v-for="bubble in userBubbles"
            :key="bubble.key"
            class="bubble-item"
            :class="{ 'bubble-item-new': !animatedBubbleKeys.includes(bubble.key) }"
            :data-bubble-key="bubble.key"
          >
            <span v-if="showPublishedAuthor" class="bubble-author">{{ publishedAuthorName }}</span>
            <div class="bubble user">{{ bubble.text }}</div>
            <span class="bubble-time">{{ formatDate(bubble.createdAt) }}</span>
          </div>
          <div v-if="loadingTaskExists" class="bubble-item bubble-item-loading">
            <div class="bubble loading">
              <Icon icon="solar:refresh-circle-outline" class="bubble-loading-icon" />
              <span>正在生成中...</span>
            </div>
          </div>
          <p v-if="userBubbles.length === 0 && !loadingTaskExists" class="chat-empty">这个存档还没有对话，先输入你的想法吧。</p>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { Vue3Lottie } from 'vue3-lottie';
import { gsap } from 'gsap';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import welcomeAnimation from '@/shared/assets/AnimationIndex.json';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';
import { MOTION_DURATION, MOTION_EASE } from '@/shared/motion/preset';

const FORCE_WELCOME_ONCE_KEY = 'ditapp_force_welcome_once';
const FORCE_WELCOME_BASELINE_KEY = 'ditapp_force_welcome_baseline';

const taskStore = useTaskStore();
const route = useRoute();
const router = useRouter();
const forceWelcome = ref(false);
const forceWelcomeBaseline = ref(0);
const homeRef = ref(null);
const prefersReducedMotion = useReducedMotion();
let welcomeCtx = null;
const currentArchive = computed(() => taskStore.currentArchive);
const animatedBubbleKeys = ref([]);

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
      createdAt: Number(item.createdAt) || Date.now()
    }));
});

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
    runWelcomeAnimation();
    runBubbleAnimation();
  });
});

onBeforeUnmount(() => {
  if (welcomeCtx) {
    welcomeCtx.revert();
    welcomeCtx = null;
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

.welcome-lottie {
  position: relative;
  margin-left: -5px;
  filter: drop-shadow(0 6px 12px rgba(72, 161, 255, 0.35));
  pointer-events: none;
  opacity: 0.95;
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
  justify-items: end;
  gap: 4px;
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

.bubble.loading {
  border-radius: 12px;
  background: var(--color-bg-soft);
  color: var(--color-text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.bubble-loading-icon {
  width: 14px;
  height: 14px;
  display: block;
  color: currentColor;
  animation: spin 1.2s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.bubble-author {
  font-size: 12px;
  color: var(--color-text-muted);
}

.bubble-time {
  font-size: 11px;
  color: var(--color-text-muted);
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
