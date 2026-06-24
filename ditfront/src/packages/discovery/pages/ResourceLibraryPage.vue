<template>
  <div class="discovery-hall">
    <header ref="headerRef" class="community-header">
      <p class="title-eyebrow">灵感素材库</p>
      <h2>结构化设计资产中心</h2>
      <p>通过实体导航与细分标签，精准检索海量设计资产，建立视觉与提示词的直觉关联。</p>

      <!-- 一级实体导航 -->
      <div class="entity-nav">
        <button
          v-for="entity in entities"
          :key="entity.value"
          :class="['entity-btn', { active: selectedEntity === entity.value }]"
          @click="selectEntity(entity.value)"
        >
          {{ entity.label }}
        </button>
        <button class="entity-btn" :class="{ active: selectedEntity === '' }" @click="selectEntity('')">全部</button>
      </div>

      <!-- 二级细分标签（动态呼出） -->
      <div v-if="selectedEntity && currentSubTags.length > 0" ref="subTagsRef" class="sub-tags">
        <button
          v-for="tag in currentSubTags"
          :key="tag.value"
          :class="['sub-tag-btn', { active: selectedSubTag === tag.value }]"
          @click="selectSubTag(tag.value)"
        >
          {{ tag.label }}
        </button>
        <button class="sub-tag-btn" :class="{ active: selectedSubTag === '' }" @click="selectSubTag('')">全部</button>
      </div>
    </header>

    <div class="grid-container">
      <ResourceCard
        v-for="(item, index) in filteredCards"
        :key="item.id"
        :item="item"
        :delay="index * 70"
        @copy-prompt="handleCopyPrompt"
      />
    </div>

    <!-- 复制成功提示 -->
    <Transition name="toast">
      <div v-if="showCopyToast" class="copy-toast">
        <Icon icon="fa6-solid:check-circle" />
        <span>提示词已复制到剪贴板</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import ResourceCard from '@/packages/discovery/components/ResourceCard.vue';
import { Icon } from '@iconify/vue';
import { computed, nextTick, onMounted, ref } from 'vue';
import { gsap } from 'gsap';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';
import { MOTION_DURATION, MOTION_EASE } from '@/shared/motion/preset';
import { getMockResourceLibrary } from '@/packages/discovery/data/mockResourceData';

// 实体导航配置（一级筛选）
const entities = [
  { value: 'sofa', label: '沙发' },
  { value: 'table', label: '茶几' },
  { value: 'lamp', label: '灯具' },
  { value: 'bed', label: '床具' },
  { value: 'chair', label: '座椅' },
  { value: 'cabinet', label: '柜体' }
];

// 二级细分标签配置（材质与风格）
const subTagsMap = {
  sofa: [
    { value: 'linen', label: '亚麻布艺' },
    { value: 'leather', label: '意式真皮' },
    { value: 'velvet', label: '复古丝绒' },
    { value: 'modern', label: '模块化科技布' }
  ],
  table: [
    { value: 'marble', label: '天然大理石' },
    { value: 'wood', label: '天然实木' },
    { value: 'glass', label: '钢化玻璃' },
    { value: 'stone', label: '洞石/微水泥' }
  ],
  lamp: [
    { value: 'track', label: '磁吸轨道灯' },
    { value: 'pendant', label: '分子灯' },
    { value: 'chandelier', label: '水晶吊灯' },
    { value: 'art', label: '和纸/藤编' }
  ],
  bed: [
    { value: 'platform', label: '平台床' },
    { value: 'upholstered', label: '软包床' },
    { value: 'wood-frame', label: '实木框架' },
    { value: 'minimalist', label: '极简风' }
  ],
  chair: [
    { value: 'dining', label: '餐椅' },
    { value: 'lounge', label: '休闲椅' },
    { value: 'office', label: '办公椅' },
    { value: 'ergonomic', label: '人体工学' }
  ],
  cabinet: [
    { value: 'wardrobe', label: '衣柜' },
    { value: 'sideboard', label: '边柜' },
    { value: 'bookshelf', label: '书柜' },
    { value: 'storage', label: '储物柜' }
  ]
};

const selectedEntity = ref('');
const selectedSubTag = ref('');
const headerRef = ref(null);
const subTagsRef = ref(null);
const prefersReducedMotion = useReducedMotion();
const showCopyToast = ref(false);
const resourceData = ref([]);

// 当前选中实体的二级标签
const currentSubTags = computed(() => {
  if (!selectedEntity.value) return [];
  return subTagsMap[selectedEntity.value] || [];
});

// 所有卡片数据（使用本地模拟数据）
const allCards = computed(() => resourceData.value);

// 筛选逻辑
const filteredCards = computed(() => {
  let result = allCards.value;

  if (selectedEntity.value) {
    result = result.filter((c) => c.entity === selectedEntity.value);
  }

  if (selectedSubTag.value) {
    result = result.filter((c) => c.subTag === selectedSubTag.value);
  }

  return result;
});

const selectEntity = (value) => {
  selectedEntity.value = value;
  selectedSubTag.value = ''; // 重置二级标签

  // 二级标签动态呼出动画
  if (value && currentSubTags.value.length > 0) {
    nextTick(() => {
      if (!subTagsRef.value || prefersReducedMotion.value) return;
      gsap.fromTo(
        subTagsRef.value,
        { opacity: 0, y: -12, height: 0 },
        {
          opacity: 1,
          y: 0,
          height: 'auto',
          duration: MOTION_DURATION.normal,
          ease: MOTION_EASE.enter
        }
      );
    });
  }
};

const selectSubTag = (value) => {
  selectedSubTag.value = value;
};

const handleCopyPrompt = async (promptText) => {
  try {
    await navigator.clipboard.writeText(promptText);
    showCopyToast.value = true;

    // 3秒后自动隐藏提示
    setTimeout(() => {
      showCopyToast.value = false;
    }, 3000);
  } catch (err) {
    console.error('复制失败:', err);
  }
};

onMounted(async () => {
  // 使用本地模拟数据替代后端请求
  try {
    const response = await getMockResourceLibrary();
    if (response.success) {
      resourceData.value = response.data;
    }
  } catch (error) {
    console.error('加载资源数据失败:', error);
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
.discovery-hall {
  box-sizing: border-box;
  padding: 30px 24px 40px;
  background: transparent;
  min-height: 100vh;
  color: var(--color-text-primary);
}

.community-header {
  max-width: 1180px;
  margin: 0 auto 24px;
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

.community-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
}

.community-header > p {
  margin: 0 0 20px;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

/* 一级实体导航 */
.entity-nav {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.entity-btn {
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-soft);
  color: var(--color-text-primary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.32s cubic-bezier(0.16, 1, 0.3, 1);
}

.entity-btn:hover {
  background: var(--color-bg-soft-strong);
  border-color: var(--color-primary);
  transform: translateY(-1px);
}

.entity-btn.active {
  background: var(--color-primary);
  color: var(--color-primary-contrast);
  border-color: var(--color-primary);
  box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
}

/* 二级细分标签 */
.sub-tags {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  overflow: hidden;
}

.sub-tag-btn {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
}

.sub-tag-btn:hover {
  background: var(--color-bg-soft-strong);
  color: var(--color-text-primary);
  border-color: var(--color-primary);
}

.sub-tag-btn.active {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  border-color: var(--color-primary);
  font-weight: 600;
}

/* 卡片网格 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  max-width: 1180px;
  margin: 0 auto;
}

/* 复制成功提示 */
.copy-toast {
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

.copy-toast svg {
  color: var(--color-primary);
  font-size: 18px;
}

/* Toast 动画 */
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

@media (max-width: 768px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }

  .entity-nav {
    gap: 8px;
  }

  .entity-btn {
    padding: 8px 14px;
    font-size: 13px;
  }

  .sub-tag-btn {
    padding: 5px 12px;
    font-size: 12px;
  }
}
</style>
