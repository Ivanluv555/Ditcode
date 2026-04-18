<template>
  <section ref="pageRef" class="content-page">
    <header ref="heroRef" class="page-hero">
      <div class="hero-copy">
        <p class="eyebrow">我的内容</p>
        <div class="stats-row">
          <div class="stat-pill js-stagger">
            <span>已保存</span>
            <strong>{{ assetHistory.length }}</strong>
          </div>
          <div class="stat-pill js-stagger">
            <span>进行中</span>
            <strong>{{ activeTasks.length }}</strong>
          </div>
        </div>
      </div>

      <div class="hero-actions">
        <RouterLink class="hero-btn primary" to="/">发起新工作</RouterLink>
      </div>
    </header>

    <div v-if="contentCards.length" class="content-grid">
      <article
        v-for="card in contentCards"
        :key="card.key"
        class="asset-card js-stagger"
      >
        <div class="cover-wrapper">
          <img :src="card.cover" class="cover" :alt="card.title" />
          <span class="remix-badge">{{ card.badge }}</span>
        </div>

        <div class="info">
          <div class="card-head">
            <div>
              <div class="author">{{ card.author }}</div>
              <p class="card-title">{{ card.title }}</p>
            </div>
            <span class="card-state">{{ card.stateLabel }}</span>
          </div>

          <div class="tags">
            <span v-for="tag in card.tags" :key="tag" class="tag">#{{ tag }}</span>
          </div>

          <div class="card-meta">
            <span>{{ card.meta }}</span>
            <div class="meta-actions">
              <button class="open-link toggle-privacy" @click="toggleArchivePrivacy(card.archiveId, card.isPrivate)">
                {{ card.isPrivate ? '发布到社区' : '设为私密' }}
              </button>
              <RouterLink class="open-link" :to="card.to">{{ card.actionText }}</RouterLink>
            </div>
          </div>
        </div>
      </article>
    </div>

    <div ref="emptyRef" v-else class="empty-state">
      还没有内容哦
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { gsap } from 'gsap';
import { useAuthStore } from '@/packages/auth/store/useAuthStore';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';

const authStore = useAuthStore();
const taskStore = useTaskStore();

const currentUser = computed(() => authStore.currentUser);
const archiveSummaries = computed(() => taskStore.archiveSummaries);
const assetHistory = computed(() => archiveSummaries.value.filter((item) => item.modelAsset));
const activeTasks = computed(() => archiveSummaries.value.filter((item) => !item.modelAsset));
const pageRef = ref(null);
const heroRef = ref(null);
const emptyRef = ref(null);
const prefersReducedMotion = useReducedMotion();
let ctx = null;

const formatDate = (value) => {
  if (!value) return '';
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const splitTags = (value) => {
  if (!value) return [];

  return String(value)
    .split(/[\s，,。.!！？?、/|]+/)
    .filter(Boolean)
    .slice(0, 3);
};

const toggleArchivePrivacy = async (archiveId, isPrivate) => {
  await taskStore.setArchivePrivacy(archiveId, !isPrivate);
};

const contentCards = computed(() => [
  ...archiveSummaries.value.map((item) => ({
    key: `archive-${item.id}`,
    archiveId: item.id,
    isPrivate: item.isPrivate !== false,
    cover: item.modelAsset?.imagePreview || '/assets/index.jpg',
    title: item.lastMessage || item.title || '未命名内容',
    author: currentUser.value ? `${currentUser.value.username} 的作品` : '我的内容',
    badge: item.modelAsset ? '已保存' : '编辑中',
    stateLabel: item.modelAsset ? '最近生成' : '进行中',
    tags: [item.modelAsset ? '最近生成' : '进行中', ...splitTags(item.lastMessage || item.title)],
    meta: formatDate(item.updatedAt || item.createdAt) || '刚刚',
    actionText: '继续编辑',
    to: {
      path: '/',
      query: { archive: item.id }
    }
  }))
]);

const runEnterAnimation = () => {
  if (!pageRef.value) return;

  if (ctx) {
    ctx.revert();
    ctx = null;
  }

  if (prefersReducedMotion.value) return;

  ctx = gsap.context(() => {
    if (heroRef.value) {
      gsap.fromTo(
        heroRef.value,
        { opacity: 0, y: 18 },
        { opacity: 1, y: 0, duration: 0.6, ease: 'power3.out' }
      );
    }

    const staggerTargets = pageRef.value.querySelectorAll('.js-stagger');
    if (staggerTargets.length) {
      gsap.fromTo(
        staggerTargets,
        { opacity: 0, y: 32, scale: 0.985 },
        {
          opacity: 1,
          y: 0,
          scale: 1,
          duration: 0.7,
          stagger: 0.07,
          ease: 'power3.out',
          delay: 0.12
        }
      );
      return;
    }

    if (emptyRef.value) {
      gsap.fromTo(
        emptyRef.value,
        { opacity: 0, y: 24 },
        { opacity: 1, y: 0, duration: 0.45, ease: 'power3.out', delay: 0.12 }
      );
    }
  }, pageRef.value);
};

onMounted(() => {
  nextTick(runEnterAnimation);
});

onBeforeUnmount(() => {
  if (ctx) {
    ctx.revert();
    ctx = null;
  }
});

watch(
  () => [contentCards.value.length, prefersReducedMotion.value],
  () => {
    nextTick(runEnterAnimation);
  }
);
</script>

<style scoped>
.content-page {
  min-height: 100vh;
  padding: 30px 24px 40px;
  box-sizing: border-box;
  color: var(--color-text-primary);
  background: transparent;
}

.page-hero,
.content-grid {
  max-width: 1180px;
  margin: 0 auto;
}

.page-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
  margin-bottom: 18px;
}

.hero-copy {
  max-width: 760px;
}

.eyebrow {
  display: inline-block;
  margin: 0 0 10px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 12px;
}

p {
  margin: 0;
}


.stats-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.stat-pill,
.hero-btn,
.card-state,
.remix-badge,
.tag {
  border-radius: 999px;
}

.stat-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--color-bg-soft-strong);
  border: 1px solid var(--color-border);
  color: var(--color-text-primary);
}

.stat-pill span {
  font-size: 12px;
  color: var(--color-text-muted);
}

.stat-pill strong {
  font-size: 14px;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-btn {
  text-decoration: none;
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  padding: 10px 14px;
  font-size: 14px;
}

.hero-btn.primary {
  background: var(--color-primary);
  color: var(--color-primary-contrast);
  border-color: var(--color-primary);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.asset-card,
.empty-state {
  backdrop-filter: blur(10px);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: var(--shadow-card);
}

.asset-card {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.35s ease;
  color: var(--color-text-primary);
}

.asset-card:hover {
  transform: translateY(-2px) scale(1.01);
  box-shadow: var(--shadow-elevated);
}

.cover-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  background: linear-gradient(135deg, var(--color-bg-soft-strong), var(--color-bg-soft));
}

.cover {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}


.remix-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  padding: 3px 8px;
  font-size: 12px;
  font-weight: 700;
}

.info {
  padding: 14px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
}

.author {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.card-title {
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.4;
}

.card-state {
  flex: none;
  padding: 4px 8px;
  background: var(--color-bg-soft-strong);
  color: var(--color-primary);
  font-size: 12px;
  white-space: nowrap;
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin: 12px 0;
}

.tag {
  background: var(--color-bg-soft-strong);
  padding: 4px 8px;
  font-size: 12px;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  color: var(--color-text-muted);
  font-size: 12px;
}

.meta-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.open-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.toggle-privacy {
  border: 1px solid var(--color-border);
  background: var(--color-bg-soft);
  border-radius: 999px;
  padding: 4px 8px;
  cursor: pointer;
}

.empty-state {
  padding: 22px;
  color: var(--color-text-muted);
}


@media (max-width: 900px) {
  .content-page {
    padding: 20px 16px 36px;
  }

  .page-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
