<template>
  <section ref="storyRef" class="register-story">
    <header class="story-floatbar" :class="{ visible: showFloatingHeader }">
        <div class="floatbar-inner">
          <div class="floatbar-brand">
          <strong>见筑 室内设计平台</strong>
          </div>
        <div class="floatbar-meta">
          <span class="mono">0{{ currentSceneIndex + 1 }} / 04</span>
          <span>{{ sceneNames[currentSceneIndex] }}</span>
        </div>
        <button class="floatbar-cta" type="button" @click="scrollToRegister">立即注册</button>
      </div>
    </header>

    <section ref="scene1Ref" class="story-scene scene-1" :style="{ minHeight: sceneMinHeights.scene1 }">
      <div class="scene-stage">
        <div ref="heroGridRef" class="hero-grid">
          <article class="hero-copy">
            <h1>上传图片文字需求<br>轻松漫游室内场景</h1>
            <p class="scene-desc">
              见筑专注室内设计，把二维平面图与文本描述快速转为 360° 空间方案，打通概念设计流程
            </p>
            <div class="hero-actions">
              <button class="primary-action" type="button" @click="scrollToRegister">创建账号</button>
              <RouterLink class="ghost-action" to="/login">登录</RouterLink>
            </div>
            <div class="hero-metrics">
              <article>
                <strong>平面图驱动</strong>
                <span>自动理解结构</span>
              </article>
              <article>
                <strong>数字资产设计</strong>
                <span>方案与展示直观成型</span>
              </article>
            </div>
          </article>

          <aside ref="heroDemoRef" class="hero-demo mono">
            <div class="hero-demo-frame">
              <div class="hero-layer layer-1"></div>
              <div class="hero-layer layer-2"></div>
              <div class="hero-layer layer-3"></div>
              <div class="hero-layer-content">
                <span class="hero-chip">Analyzing floor plan...</span>
                <span class="hero-line"></span>
                <span class="hero-line short"></span>
                <span class="hero-block"></span>
              </div>
              <span class="hero-scan"></span>
            </div>
          </aside>
        </div>
      </div>
    </section>

    <section ref="scene2Ref" class="story-scene scene-2" :style="{ minHeight: sceneMinHeights.scene2 }">
      <div ref="scene2PinRef" class="scene-stage">
        <div ref="featuresLayoutRef" class="features-layout">
          <article class="scene-copy">
            <h2>从平面图片<br>到室内360全景<br>核心能力一目了然</h2>
            <p class="scene-desc">理解意图-识别图片-构建思路-模型构建-渲染展示</p>
            <p class="scene-desc">全通路一站式完成，轻松获得作品</p>

            <div class="feature-tabs mono" role="tablist">
              <button
                  v-for="(card, index) in featureCards"
                  :key="card.id"
                  :id="`tab-${card.id}`"
                  class="feature-tab"
                  :class="{ active: index === activeFeatureIndex }"
                  type="button"
                  role="tab"
                  :aria-selected="index === activeFeatureIndex"
                  :aria-controls="`panel-${card.id}`"
                  :tabindex="index === activeFeatureIndex ? 0 : -1"
                  @click="scrollToFeature(index)"
              >
                {{ card.short }}
              </button>
            </div>
          </article>

          <div class="feature-stack">
            <article
                v-for="(card, index) in featureCards"
                :key="card.id"
                :id="`panel-${card.id}`"
                class="feature-card"
                :class="{ active: index === activeFeatureIndex }"
                :style="featureCardStyle(index)"
                role="tabpanel"
                :aria-labelledby="`tab-${card.id}`"
                :hidden="index !== activeFeatureIndex"
            >
              <h3>{{ card.title }}</h3>
              <p>{{ card.desc }}</p>
              <div class="feature-anim" :class="card.preview ? 'with-component' : `anim-${card.anim}`">
                <template v-if="card.preview === 'editor'">
                  <div class="workspace-preview workspace-preview-editor">
                    <EditorPanel preview-mode :preview-prompt="editorPreviewPrompt" />
                    <span class="workspace-shimmer" aria-hidden="true"></span>
                  </div>
                </template>
                <template v-else-if="card.preview === 'task'">
                  <div class="workspace-preview workspace-preview-task">
                    <TaskPod preview-mode force-expanded :preview-tasks="taskPodPreviewTasks" />
                    <span class="workspace-shimmer" aria-hidden="true"></span>
                  </div>
                </template>
                <template v-else>
                  <span aria-hidden="true"></span>
                  <span aria-hidden="true"></span>
                  <span aria-hidden="true"></span>
                </template>
              </div>
            </article>
          </div>
        </div>
      </div>
    </section>

    <section ref="scene3Ref" class="story-scene scene-3" :style="{ minHeight: sceneMinHeights.scene3 }">
      <div class="scene-stage">
        <section ref="showcaseShellRef" class="showcase-shell">
          <div class="showcase-nav mono">
            <button
              v-for="(item, index) in showcaseItems"
              :key="item.id"
              class="showcase-tab"
              :class="{ active: index === showcaseIndex }"
              type="button"
              @click="selectShowcase(index)"
            >
              {{ item.label }}
            </button>
          </div>
          <div class="showcase-progress">
            <span :style="{ width: showcaseProgressWidth }"></span>
          </div>

          <div class="showcase-main">
            <button class="showcase-arrow" type="button" @click="prevShowcase" aria-label="上一张">
              <Icon icon="fa6-solid:chevron-left" />
            </button>

            <div class="showcase-visual">
              <div class="visual-stack">
                <div class="visual-panel back"></div>
                <div class="visual-panel middle"></div>
                <div class="visual-panel front">
                  <span class="bar long"></span>
                  <span class="bar"></span>
                  <span class="bar"></span>
                  <span class="bar short"></span>
                  <div class="visual-grid">
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            </div>

            <article class="showcase-copy">
              <h2>{{ currentShowcase.title }}</h2>
              <p>{{ currentShowcase.desc }}</p>
              <button type="button" class="showcase-cta">{{ currentShowcase.cta }}</button>
            </article>

            <button class="showcase-arrow" type="button" @click="nextShowcase" aria-label="下一张">
              <Icon icon="fa6-solid:chevron-right" />
            </button>
          </div>
        </section>
      </div>
    </section>

    <section ref="scene4Ref" class="story-scene scene-4" :style="{ minHeight: sceneMinHeights.scene4 }">
      <div class="scene-stage">
        <div ref="formStageRef" class="register-stage" @mousemove="handleGlowMove" @mouseleave="handleGlowLeave">
          <div ref="formGlowRef" class="form-glow" aria-hidden="true"></div>

          <form ref="registerCardRef" class="register-card" @submit.prevent="submitRegister">
            <h2>创建见筑账号</h2>
            <p class="scene-desc">填写信息后即可开始制作与管理你的室内设计模型与全景方案。</p>

            <label for="username">Username</label>
            <input id="username" v-model.trim="username" type="text" placeholder="3-20 位，字母数字下划线" autocomplete="username" />

            <label for="email">Email</label>
            <input id="email" v-model.trim="email" type="email" placeholder="请输入邮箱" autocomplete="email" />

            <label for="password">Password</label>
            <input id="password" v-model="password" type="password" placeholder="8-32 位，至少字母+数字" autocomplete="new-password" />

            <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

            <button class="primary" type="submit">注册</button>

            <div class="switch-row">
              <label class="switch-control">
                <input v-model="allowRemix" type="checkbox" />
                <span class="switch-core" aria-hidden="true"></span>
              </label>
              <span class="mono switch-text">[{{ allowRemix ? 'x' : ' ' }}] 允许内容被 Remix</span>
            </div>

            <RouterLink class="link" to="/login">已有账号？去登录</RouterLink>
          </form>
        </div>
      </div>
    </section>

    <footer class="story-footer">
      <div class="footer-inner">
        <div class="footer-social">
          <span>掌握我们的最新动态</span>
          <a href="#" aria-label="News">
            <Icon icon="fa6-solid:newspaper" />
          </a>
          <a href="#" aria-label="Blog">
            <Icon icon="fa6-solid:blog" />
          </a>
          <a href="#" aria-label="Share">
            <Icon icon="fa6-solid:share-nodes" />
          </a>
          <a href="#" aria-label="Video">
            <Icon icon="fa6-solid:play" />
          </a>
        </div>

        <div class="footer-grid">
          <section>
            <h4>产品</h4>
            <a href="#">Floorplan Modeler</a>
            <a href="#">Interior Panorama</a>
            <a href="#">Workspace Studio</a>
            <a href="#">版本比较</a>
          </section>

          <section>
            <h4>开始使用</h4>
            <a href="#">获取产品说明</a>
            <a href="#">设计模板中心</a>
            <a href="#">常见问题</a>
            <a href="#">联系销售人员</a>
          </section>

          <section>
            <h4>使用场景</h4>
            <a href="#">住宅设计</a>
            <a href="#">商业空间</a>
            <a href="#">软装提案</a>
            <a href="#">设计协作</a>
          </section>

          <section>
            <h4>更多服务</h4>
            <a href="#">我们的理念</a>
            <a href="#">隐私权与安全性</a>
            <a href="#">开发者中心</a>
            <a href="#">API 文档</a>
          </section>
        </div>
      </div>
    </footer>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { useAuthStore } from '@/packages/auth/store/useAuthStore';
import { isValidEmail, isValidPassword, isValidUsername } from '@/packages/auth/utils/authValidation';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';
import EditorPanel from '@/packages/workspace/components/EditorPanel.vue';
import TaskPod from '@/packages/workspace/components/TaskPod.vue';

gsap.registerPlugin(ScrollTrigger);

const storyRef = ref(null);
const heroDemoRef = ref(null);
const scene1Ref = ref(null);
const scene2Ref = ref(null);
const scene2PinRef = ref(null);
const scene3Ref = ref(null);
const scene4Ref = ref(null);
const heroGridRef = ref(null);
const featuresLayoutRef = ref(null);
const showcaseShellRef = ref(null);
const formStageRef = ref(null);
const formGlowRef = ref(null);
const registerCardRef = ref(null);

const username = ref('');
const email = ref('');
const password = ref('');
const errorMsg = ref('');
const allowRemix = ref(true);

const currentSceneIndex = ref(0);
const showFloatingHeader = ref(false);
const activeFeatureIndex = ref(0);
const showcaseIndex = ref(0);
const editorPreviewPrompt = ref('现代简约客厅，暖色灯光，木纹地板，增加落地灯与地毯。');
const taskPodPreviewTasks = ref([
  { id: 'preview-task-1', status: 'inferencing', progress: 18 },
  { id: 'preview-task-2', status: 'queued', progress: 0 }
]);
const sceneMinHeights = ref({
  scene1: '120vh',
  scene2: '108vh',
  scene3: '116vh',
  scene4: '122vh'
});

const sceneNames = ['产品概览', '核心能力', '设计场景', '创建账号'];

// 五张卡片的内容
const featureCards = [
  {
    id: 'capture',
    short: '识别',
    title: '平面图识别',
    desc: '自动提取空间结构、区域关系与关键路径。',
    anim: 'pulse'
  },
  {
    id: 'prompt',
    short: '理解',
    title: '文本语义理解',
    desc: '根据文字需求补全风格、功能分区与空间细节。',
    anim: 'flow',
    preview: 'editor'
  },
  {
    id: 'render',
    short: '生成',
    title: '360° 全景生成',
    desc: '一键输出可漫游全景图，适配多端浏览。',
    anim: 'orbit'
  },
  {
    id: 'remix',
    short: '组织',
    title: '设计资产组织',
    desc: '户型、材质、灯光等方案资产可统一管理与复用。',
    anim: 'nodes',
    preview: 'task'
  },
  {
    id: 'policy',
    short: '治理',
    title: '协作与治理',
    desc: '支持 Remix 协作、版本追踪与版权链路管理。',
    anim: 'cutoff'
  }
];

// 4个板块的内容
const showcaseItems = [
  {
    id: 'research',
    label: '户型重建',
    title: 'Floorplan Rebuild',
    desc: '从 2D 户型图快速重建 3D 室内空间结构。',
    cta: '查看重建效果'
  },
  {
    id: 'coach',
    label: '材质搭配',
    title: 'Material Studio',
    desc: '批量替换材质与配色，快速产出多套风格方案。',
    cta: '查看材质方案'
  },
  {
    id: 'create',
    label: '灯光预演',
    title: 'Lighting Preview',
    desc: '模拟自然光与灯光参数，提前预览空间氛围。',
    cta: '查看灯光预演'
  },
  {
    id: 'learn',
    label: '方案评审',
    title: 'Design Review',
    desc: '通过可漫游全景进行远程评审与修改反馈。',
    cta: '进入评审空间'
  }
];

const currentShowcase = computed(() => showcaseItems[showcaseIndex.value]);
const showcaseProgressWidth = computed(
  () => `${((showcaseIndex.value + 1) / showcaseItems.length) * 100}%`
);

const prefersReducedMotion = useReducedMotion();
const authStore = useAuthStore();
const router = useRouter();

const sectionThemes = [
  {
    '--bg-color': '#0d1117',
    '--text-color': '#e6edf6',
    '--muted-color': '#a7b4c7',
    '--surface-color': 'rgba(21, 29, 41, 0.9)',
    '--border-color': 'rgba(230, 237, 246, 0.2)',
    '--accent-color': '#6da2ff',
    '--halo-a': 'rgba(103, 156, 255, 0.3)',
    '--halo-b': 'rgba(155, 119, 255, 0.22)'
  },
  {
    '--bg-color': '#eef3ff',
    '--text-color': '#111827',
    '--muted-color': '#4b5563',
    '--surface-color': 'rgba(255, 255, 255, 0.76)',
    '--border-color': 'rgba(53, 70, 107, 0.18)',
    '--accent-color': '#3b82f6',
    '--halo-a': 'rgba(61, 128, 255, 0.34)',
    '--halo-b': 'rgba(90, 210, 255, 0.22)'
  }
];

let gsapCtx = null;
let activeScroller = null;
let activeThemeIndex = -1;
let refreshRaf = 0;
let scene2ProgressTrigger = null;
let glowXTo = null;
let glowYTo = null;
let featureEditorTimer = null;
let featureTaskTimer = null;
let featureEditorIndex = 0;
const FEATURE_PREVIEW_SCENE_INDEX = 1;

const editorPromptPool = [
  '现代简约客厅，暖色灯光，木纹地板，增加落地灯与地毯。',
  '奶油风卧室，低饱和配色，窗边阅读区，加入隐藏灯带。',
  '北欧餐厨一体，白橡木柜体，强化动线与自然采光。'
];

const setActiveScene = (index, themeDuration = 0.38) => {
  const next = Math.max(0, Math.min(index, sceneNames.length - 1));
  currentSceneIndex.value = next;
  showFloatingHeader.value = next > 0;
  const themeIndex = next === 0 ? 0 : 1;
  applyTheme(themeIndex, themeDuration);
  syncFeaturePreviewAnimations();
};

const clampFeatureIndex = (index) =>
  Math.max(0, Math.min(index, featureCards.length - 1));

const syncFeatureFromScroll = (index) => {
  const nextIndex = clampFeatureIndex(index);
  if (nextIndex === activeFeatureIndex.value) return;
  activeFeatureIndex.value = nextIndex;
};

const scrollToFeature = (index) => {
  if (!scene2ProgressTrigger) return;
  const nextIndex = clampFeatureIndex(index);
  const steps = Math.max(1, featureCards.length - 1);
  const progress = nextIndex / steps;
  const targetScroll = scene2ProgressTrigger.start + (scene2ProgressTrigger.end - scene2ProgressTrigger.start) * progress;
  const scroller = getActiveScroller();
  const behavior = prefersReducedMotion.value ? 'auto' : 'smooth';

  if (scroller === window) {
    window.scrollTo({ top: targetScroll, behavior });
    return;
  }
  scroller.scrollTo({ top: targetScroll, behavior });
};

const selectShowcase = (index) => {
  showcaseIndex.value = Math.max(0, Math.min(index, showcaseItems.length - 1));
};

const nextShowcase = () => {
  showcaseIndex.value = (showcaseIndex.value + 1) % showcaseItems.length;
};

const prevShowcase = () => {
  showcaseIndex.value = (showcaseIndex.value + showcaseItems.length - 1) % showcaseItems.length;
};

const featureCardStyle = (index) => {
  const delta = index - activeFeatureIndex.value;
  const abs = Math.abs(delta);
  return {
    zIndex: String(featureCards.length - abs),
    transform: `translate3d(${delta * 42}px, ${abs * 28}px, 0) scale(${1 - abs * 0.06})`,
    opacity: '1'
  };
};

const applyTheme = (index, duration = 0.45) => {
  if (!storyRef.value) return;
  if (activeThemeIndex === index && duration > 0) return;
  activeThemeIndex = index;
  gsap.to(storyRef.value, {
    ...sectionThemes[index],
    duration,
    ease: 'power2.out',
    overwrite: 'auto'
  });
};

const resolveScrollContainer = () => {
  if (!storyRef.value) return window;
  return storyRef.value.closest('.content') || window;
};

const getActiveScroller = () => activeScroller || resolveScrollContainer();

const getScrollTriggerOptions = () => {
  const scroller = getActiveScroller();
  if (scroller === window) return {};
  return { scroller };
};

const buildSceneMinHeight = (contentHeight, viewportHeight, runway, minimumScreens) => {
  const next = Math.max(contentHeight + viewportHeight * runway, viewportHeight * minimumScreens);
  return `${Math.ceil(next)}px`;
};

const updateSceneMinHeights = () => {
  if (typeof window === 'undefined') return;
  const viewportHeight = window.innerHeight || 0;
  if (!viewportHeight) return false;

  const heroHeight = heroGridRef.value?.getBoundingClientRect().height || viewportHeight;
  const featureHeight = featuresLayoutRef.value?.getBoundingClientRect().height || viewportHeight;
  const showcaseHeight = showcaseShellRef.value?.getBoundingClientRect().height || viewportHeight;
  const registerHeight = formStageRef.value?.getBoundingClientRect().height || viewportHeight;

  const nextHeights = {
    scene1: buildSceneMinHeight(heroHeight, viewportHeight, 0.48, 1.18),
    scene2: buildSceneMinHeight(featureHeight, viewportHeight, 0.34, 1.08),
    scene3: buildSceneMinHeight(showcaseHeight, viewportHeight, 0.42, 1.14),
    scene4: buildSceneMinHeight(registerHeight, viewportHeight, 0.52, 1.16)
  };

  if (
    sceneMinHeights.value.scene1 === nextHeights.scene1 &&
    sceneMinHeights.value.scene2 === nextHeights.scene2 &&
    sceneMinHeights.value.scene3 === nextHeights.scene3 &&
    sceneMinHeights.value.scene4 === nextHeights.scene4
  ) {
    return false;
  }

  sceneMinHeights.value = nextHeights;
  return true;
};

const refreshScrollTriggers = (force = false) => {
  if (refreshRaf) {
    window.cancelAnimationFrame(refreshRaf);
  }
  refreshRaf = window.requestAnimationFrame(() => {
    refreshRaf = 0;
    const hasHeightChange = updateSceneMinHeights();
    if (force || hasHeightChange) {
      ScrollTrigger.refresh();
    }
  });
};
const handleViewportResize = () => {
  refreshScrollTriggers(true);
};

const scrollToRegister = () => {
  if (!scene4Ref.value) return;
  const scroller = getActiveScroller();
  if (scroller === window) {
    const targetTop = scene4Ref.value.getBoundingClientRect().top + window.scrollY;
    window.scrollTo({ top: targetTop, behavior: prefersReducedMotion.value ? 'auto' : 'smooth' });
    return;
  }

  const containerRect = scroller.getBoundingClientRect();
  const targetTop = scene4Ref.value.getBoundingClientRect().top - containerRect.top + scroller.scrollTop;
  scroller.scrollTo({ top: targetTop, behavior: prefersReducedMotion.value ? 'auto' : 'smooth' });
};

const setupGlow = () => {
  if (!formStageRef.value || !formGlowRef.value) return;
  const centerX = formStageRef.value.clientWidth * 0.5;
  const centerY = formStageRef.value.clientHeight * 0.5;
  gsap.set(formGlowRef.value, { xPercent: -50, yPercent: -50, x: centerX, y: centerY });

  if (prefersReducedMotion.value) return;
  glowXTo = gsap.quickTo(formGlowRef.value, 'x', { duration: 0.3, ease: 'power3.out' });
  glowYTo = gsap.quickTo(formGlowRef.value, 'y', { duration: 0.3, ease: 'power3.out' });
};

const moveGlowTo = (x, y) => {
  if (!formGlowRef.value) return;
  if (prefersReducedMotion.value || !glowXTo || !glowYTo) {
    gsap.set(formGlowRef.value, { x, y });
    return;
  }
  glowXTo(x);
  glowYTo(y);
};

const handleGlowMove = (event) => {
  if (!formStageRef.value) return;
  const rect = formStageRef.value.getBoundingClientRect();
  moveGlowTo(event.clientX - rect.left, event.clientY - rect.top);
};

const handleGlowLeave = () => {
  if (!formStageRef.value) return;
  moveGlowTo(formStageRef.value.clientWidth * 0.5, formStageRef.value.clientHeight * 0.5);
};

const resetTaskPodPreview = () => {
  taskPodPreviewTasks.value = [
    { id: 'preview-task-1', status: 'inferencing', progress: 18 },
    { id: 'preview-task-2', status: 'queued', progress: 0 }
  ];
};

const tickTaskPodPreview = () => {
  const [firstTask, secondTask] = taskPodPreviewTasks.value;
  if (!firstTask || !secondTask) {
    resetTaskPodPreview();
    return;
  }

  if (firstTask.progress < 100) {
    const nextFirst = Math.min(100, firstTask.progress + 8);
    const nextSecondProgress = nextFirst > 44 ? Math.min(100, secondTask.progress + 6) : secondTask.progress;
    taskPodPreviewTasks.value = [
      { ...firstTask, status: 'inferencing', progress: nextFirst },
      { ...secondTask, status: nextFirst > 44 ? 'compositing' : 'queued', progress: nextSecondProgress }
    ];
    return;
  }

  if (secondTask.progress < 100) {
    taskPodPreviewTasks.value = [
      { ...firstTask, status: 'success', progress: 100 },
      { ...secondTask, status: 'compositing', progress: Math.min(100, secondTask.progress + 9) }
    ];
    return;
  }

  resetTaskPodPreview();
};

const isDocumentVisible = () =>
  typeof document === 'undefined' || document.visibilityState === 'visible';

const shouldRunFeaturePreviewAnimations = () =>
  currentSceneIndex.value === FEATURE_PREVIEW_SCENE_INDEX &&
  isDocumentVisible() &&
  !prefersReducedMotion.value;

const startFeaturePreviewAnimations = () => {
  if (!shouldRunFeaturePreviewAnimations()) return;

  if (!featureEditorTimer) {
    featureEditorTimer = window.setInterval(() => {
      featureEditorIndex = (featureEditorIndex + 1) % editorPromptPool.length;
      editorPreviewPrompt.value = editorPromptPool[featureEditorIndex];
    }, 2200);
  }

  if (!featureTaskTimer) {
    featureTaskTimer = window.setInterval(() => {
      tickTaskPodPreview();
    }, 320);
  }
};

const stopFeaturePreviewAnimations = () => {
  if (featureEditorTimer) {
    window.clearInterval(featureEditorTimer);
    featureEditorTimer = null;
  }
  if (featureTaskTimer) {
    window.clearInterval(featureTaskTimer);
    featureTaskTimer = null;
  }
};

const syncFeaturePreviewAnimations = () => {
  if (shouldRunFeaturePreviewAnimations()) {
    startFeaturePreviewAnimations();
    return;
  }
  stopFeaturePreviewAnimations();
};

const handleVisibilityChange = () => {
  syncFeaturePreviewAnimations();
};

const validate = () => {
  errorMsg.value = '';
  if (!isValidUsername(username.value)) {
    errorMsg.value = '用户名格式不正确：3-20 位，仅支持字母、数字、下划线';
    return false;
  }

  if (!isValidEmail(email.value)) {
    errorMsg.value = '邮箱格式不正确';
    return false;
  }

  if (!isValidPassword(password.value)) {
    errorMsg.value = '密码格式不正确：8-32 位，至少包含字母和数字';
    return false;
  }
  return true;
};

const submitRegister = async () => {
  if (!validate()) return;
  await authStore.init();
  const result = await authStore.register({
    username: username.value,
    email: email.value,
    password: password.value
  });
  if (!result.ok) {
    errorMsg.value = result.message;
    return;
  }
  router.push('/');
};

onMounted(async () => {
  await nextTick();
  updateSceneMinHeights();
  setActiveScene(0, 0);
  setupGlow();
  resetTaskPodPreview();
  syncFeaturePreviewAnimations();

  activeScroller = resolveScrollContainer();
  const scrollTriggerOptions = getScrollTriggerOptions();
  window.addEventListener('resize', handleViewportResize);
  document.addEventListener('visibilitychange', handleVisibilityChange);

  gsapCtx = gsap.context(() => {
    const scenes = [scene1Ref.value, scene2Ref.value, scene3Ref.value, scene4Ref.value];

    scenes.forEach((scene, index) => {
      if (!scene) return;
      ScrollTrigger.create({
        ...scrollTriggerOptions,
        trigger: scene,
        start: 'top top',
        end: 'bottom top',
        onEnter: () => setActiveScene(index),
        onEnterBack: () => setActiveScene(index)
      });
    });

    if (scene2Ref.value && scene2PinRef.value) {
      scene2ProgressTrigger = ScrollTrigger.create({
        ...scrollTriggerOptions,
        trigger: scene2Ref.value,
        start: 'top top',
        end: `+=${(featureCards.length - 1) * 95}%`,
        pin: scene2PinRef.value,
        pinSpacing: true,
        scrub: true,
        anticipatePin: 1,
        onUpdate: (self) => {
          const nextIndex = Math.min(featureCards.length - 1, Math.round(self.progress * (featureCards.length - 1)));
          syncFeatureFromScroll(nextIndex);
        }
      });
    }

    if (registerCardRef.value && !prefersReducedMotion.value) {
      gsap.fromTo(
        registerCardRef.value,
        { y: 80, opacity: 0.35, scale: 0.97 },
        {
          y: 0,
          opacity: 1,
          scale: 1,
          ease: 'none',
          scrollTrigger: {
            ...scrollTriggerOptions,
            trigger: scene4Ref.value,
            start: 'top 80%',
            end: 'top 32%',
            scrub: true
          }
        }
      );
    }

    if (heroDemoRef.value && !prefersReducedMotion.value) {
      gsap.fromTo(
        heroDemoRef.value,
        { y: 24, opacity: 0 },
        {
          y: 0,
          opacity: 1,
          duration: 0.75,
          ease: 'power3.out'
        }
      );
    }
  }, storyRef.value);
  refreshScrollTriggers(true);
});

watch(
  () => prefersReducedMotion.value,
  () => {
    syncFeaturePreviewAnimations();
  }
);

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleViewportResize);
  document.removeEventListener('visibilitychange', handleVisibilityChange);
  if (refreshRaf) {
    window.cancelAnimationFrame(refreshRaf);
    refreshRaf = 0;
  }
  if (gsapCtx) {
    gsapCtx.revert();
    gsapCtx = null;
  }
  scene2ProgressTrigger = null;
  activeScroller = null;
  glowXTo = null;
  glowYTo = null;
  stopFeaturePreviewAnimations();
});
</script>

<style scoped>
.register-story {
  --bg-color: #f7f9fc;
  --text-color: #111827;
  --muted-color: #4b5563;
  --surface-color: rgba(255, 255, 255, 0.74);
  --border-color: rgba(30, 41, 59, 0.14);
  --accent-color: #2563eb;
  --halo-a: rgba(74, 130, 255, 0.32);
  --halo-b: rgba(127, 91, 255, 0.24);
  --mono-font: 'Maple Mono', 'Fira Code', 'JetBrains Mono', monospace;
  --sans-font: 'Inter', 'Noto Sans', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  min-height: 100vh;
  background: var(--bg-color);
  color: var(--text-color);
  font-family: var(--sans-font);
}

.mono {
  font-family: var(--mono-font);
}

.story-floatbar {
  position: fixed;
  top: 14px;
  left: 0;
  right: 0;
  z-index: 90;
  display: flex;
  justify-content: center;
  opacity: 0;
  pointer-events: none;
  transform: translateY(-12px);
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.story-floatbar.visible {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}

.floatbar-inner {
  width: min(1120px, calc(100vw - 28px));
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--border-color) 92%, transparent);
  background: color-mix(in srgb, var(--surface-color) 92%, rgba(255, 255, 255, 0.4));
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 30px rgba(17, 24, 39, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 10px 14px;
}

.floatbar-brand {
  display: grid;
  gap: 2px;
}

.floatbar-brand strong {
  font-size: 13px;
}

.floatbar-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--muted-color);
}

.floatbar-cta {
  border: 1px solid color-mix(in srgb, var(--accent-color) 36%, transparent);
  background: color-mix(in srgb, var(--accent-color) 86%, #fff);
  color: #f8fbff;
  border-radius: 10px;
  font-weight: 700;
  font-size: 12px;
  padding: 8px 12px;
  cursor: pointer;
}

.story-scene {
  min-height: 100vh;
  position: relative;
}

.scene-stage {
  position: sticky;
  top: 0;
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: clamp(20px, 4vh, 42px) clamp(18px, 5vw, 68px);
  box-sizing: border-box;
}

.scene-2 .scene-stage,
.scene-3 .scene-stage,
.scene-4 .scene-stage {
  padding-top: clamp(88px, 11vh, 132px);
}

.scene-2 .scene-stage {
  position: relative;
}

.scene-desc {
  margin: 0;
  max-width: 46ch;
  color: var(--muted-color);
  line-height: 1.7;
}

.hero-grid {
  width: min(1180px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(320px, 1.05fr) minmax(320px, 1fr);
  gap: clamp(22px, 4vw, 60px);
  align-items: center;
}

.hero-copy {
  display: grid;
  gap: 14px;
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(34px, 5vw, 64px);
  line-height: 1.08;
  letter-spacing: -0.03em;
}

.hero-actions {
  margin-top: 6px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.primary-action,
.ghost-action {
  text-decoration: none;
  border-radius: 999px;
  padding: 10px 16px;
  font-size: 14px;
}

.primary-action {
  border: none;
  color: #f8fbff;
  cursor: pointer;
  font-weight: 700;
  background: linear-gradient(120deg, #2c6cf6, #4f90ff);
}

.ghost-action {
  border: 1px solid color-mix(in srgb, var(--border-color) 92%, transparent);
  color: var(--text-color);
  background: color-mix(in srgb, var(--surface-color) 90%, transparent);
}

.hero-metrics {
  margin-top: 6px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.hero-metrics article {
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--border-color) 90%, transparent);
  background: color-mix(in srgb, var(--surface-color) 90%, transparent);
  padding: 12px;
}

.hero-metrics strong {
  display: block;
  font-size: 22px;
  line-height: 1.2;
}

.hero-metrics span {
  color: var(--muted-color);
  font-size: 12px;
}

.hero-demo {
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.14);
  padding: 14px;
  background: linear-gradient(145deg, #0a1020 2%, #0f172c 98%);
  color: #e6edf6;
  box-shadow: 0 30px 68px rgba(15, 23, 42, 0.25);
}

.hero-demo-frame {
  height: clamp(260px, 36vw, 380px);
  border-radius: 16px;
  position: relative;
  overflow: hidden;
  background: #070b15;
  border: 1px solid rgba(148, 163, 184, 0.22);
}

.hero-layer {
  position: absolute;
  inset: 18px 18px 18px 18px;
  border-radius: 16px;
  border: 1px solid rgba(100, 170, 255, 0.3);
  background: rgba(18, 30, 58, 0.66);
  transform: translateX(-30px);
  opacity: 0;
  animation: layer-once 3s ease-out forwards;
}

.hero-layer.layer-1 {
  animation-delay: 0.2s;
}

.hero-layer.layer-2 {
  animation-delay: 0.45s;
  inset: 32px 30px 14px 14px;
}

.hero-layer.layer-3 {
  animation-delay: 0.7s;
  inset: 46px 42px 10px 20px;
}

.hero-layer-content {
  position: absolute;
  inset: 62px 44px 44px;
  display: grid;
  gap: 10px;
  z-index: 2;
}

.hero-chip {
  font-size: 14px;
  color: #d2e3ff;
}

.hero-line {
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(to right, #4b8ffb, #3a6fe0);
  transform-origin: left center;
  animation: draw-once 3s linear forwards;
}

.hero-line.short {
  width: 64%;
}

.hero-block {
  height: 100%;
  border-radius: 14px;
  background: linear-gradient(130deg, rgba(75, 143, 251, 0.24), rgba(52, 106, 220, 0.74));
}

.hero-scan {
  position: absolute;
  left: 0;
  right: 0;
  top: -22%;
  height: 28%;
  background: linear-gradient(to bottom, transparent, rgba(115, 176, 255, 0.38), transparent);
  animation: scan-once 3s ease-out forwards;
}

@keyframes layer-once {
  0% {
    transform: translateX(-38px);
    opacity: 0;
  }
  45% {
    opacity: 1;
  }
  100% {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes draw-once {
  0% {
    transform: scaleX(0);
  }
  100% {
    transform: scaleX(1);
  }
}

@keyframes scan-once {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(480%);
  }
}

.features-layout {
  width: min(1180px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(300px, 1fr) minmax(300px, 1.1fr);
  gap: clamp(20px, 4vw, 58px);
  align-items: center;
}

.scene-copy {
  display: grid;
  gap: 14px;
}

.scene-copy h2 {
  margin: 0;
  font-size: clamp(30px, 4.8vw, 56px);
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.feature-tabs {
  display: flex;
  gap: 8px;
}

.feature-tab {
  border: 1px solid color-mix(in srgb, var(--border-color) 90%, transparent);
  border-radius: 999px;
  min-width: 36px;
  height: 36px;
  background: color-mix(in srgb, var(--surface-color) 92%, transparent);
  color: var(--muted-color);
  cursor: pointer;
}

.feature-tab.active {
  background: color-mix(in srgb, var(--accent-color) 18%, var(--surface-color));
  color: var(--accent-color);
  border-color: color-mix(in srgb, var(--accent-color) 40%, transparent);
}

.feature-stack {
  position: relative;
  --feature-card-height: 372px;
  --feature-anim-height: 188px;
  height: calc(var(--feature-card-height) + 128px);
}

.feature-card {
  position: absolute;
  inset: auto 0 0;
  margin: auto;
  width: min(520px, 100%);
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--border-color) 92%, transparent);
  background: color-mix(in srgb, var(--surface-color) 96%, transparent);
  box-shadow: 0 18px 46px color-mix(in srgb, var(--bg-color) 36%, transparent);
  padding: 18px;
  box-sizing: border-box;
  height: var(--feature-card-height);
  display: flex;
  flex-direction: column;
  color: var(--text-color);
  transition: transform 0.34s ease;
  will-change: transform;
}

.feature-card.active {
  box-shadow:
    0 26px 56px color-mix(in srgb, var(--bg-color) 40%, transparent),
    0 0 0 1px color-mix(in srgb, var(--accent-color) 24%, transparent);
}

.card-badge {
  margin: 0;
  font-size: 11px;
  color: var(--accent-color);
}

.feature-card h3 {
  margin: 8px 0 6px;
  font-size: 20px;
}

.feature-card p {
  margin: 0;
  color: var(--muted-color);
  line-height: 1.55;
  font-size: 14px;
}

.feature-anim {
  margin-top: auto;
  height: var(--feature-anim-height);
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--border-color) 90%, transparent);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12), rgba(56, 189, 248, 0.15));
  position: relative;
  overflow: hidden;
}

.feature-anim.with-component {
  height: var(--feature-anim-height);
  background: linear-gradient(145deg, rgba(59, 130, 246, 0.08), rgba(30, 41, 59, 0.02));
}

.workspace-preview {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.workspace-shimmer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(115deg, transparent 28%, rgba(255, 255, 255, 0.2) 46%, transparent 62%);
  transform: translateX(-120%);
  animation: preview-shimmer 2.4s linear infinite;
}

.workspace-preview-editor :deep(.editor-panel) {
  right: 10px;
  top: 50%;
  width: 320px;
  transform: translateY(-50%) scale(0.62);
  transform-origin: center right;
  pointer-events: none;
}

.workspace-preview-editor :deep(.editor-panel *) {
  pointer-events: none;
}

.workspace-preview-editor :deep(.header) {
  padding: 12px;
}

.workspace-preview-editor :deep(.content) {
  padding: 12px;
  gap: 8px;
}

.workspace-preview-editor :deep(textarea) {
  min-height: 66px;
}

.workspace-preview-task :deep(.task-pod) {
  top: 14px;
  right: 14px;
  transform: scale(0.78);
  transform-origin: top right;
  pointer-events: none;
}

.workspace-preview-task :deep(.task-pod *) {
  pointer-events: none;
}

.workspace-preview-task :deep(.task-list) {
  padding: 10px;
}

.feature-anim span {
  position: absolute;
  display: block;
}

.anim-pulse span {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #3b82f6;
  top: 40px;
  left: 28px;
  animation: pulse-dot 1.8s infinite ease-in-out;
}

.anim-pulse span:nth-child(2) {
  left: 56px;
  animation-delay: 0.2s;
}

.anim-pulse span:nth-child(3) {
  left: 84px;
  animation-delay: 0.4s;
}

.anim-flow span {
  left: -30%;
  top: 20px;
  width: 28%;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(to right, transparent, #3b82f6);
  animation: flow-line 2.2s infinite linear;
}

.anim-flow span:nth-child(2) {
  top: 42px;
  animation-delay: 0.35s;
}

.anim-flow span:nth-child(3) {
  top: 64px;
  animation-delay: 0.7s;
}

.anim-orbit span {
  width: 58px;
  height: 58px;
  border: 1px solid rgba(59, 130, 246, 0.5);
  border-radius: 50%;
  top: 16px;
  left: 28px;
}

.anim-orbit span:nth-child(2) {
  left: 110px;
  animation: rotate-ring 3.4s linear infinite;
}

.anim-orbit span:nth-child(3) {
  left: 192px;
  animation: rotate-ring 3.4s linear infinite reverse;
}

.anim-nodes span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #3b82f6;
  top: 38px;
  left: 42px;
  box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.45);
  animation: pulse-node 2s infinite;
}

.anim-nodes span:nth-child(2) {
  left: 112px;
}

.anim-nodes span:nth-child(3) {
  left: 182px;
}

.anim-cutoff span {
  top: 20px;
  left: 30px;
  width: 200px;
  height: 4px;
  border-radius: 999px;
  background: #3b82f6;
  animation: cutoff-line 2.4s infinite;
}

.anim-cutoff span:nth-child(2) {
  top: 40px;
  width: 160px;
  animation-delay: 0.35s;
}

.anim-cutoff span:nth-child(3) {
  top: 60px;
  width: 120px;
  animation-delay: 0.7s;
}

@keyframes pulse-dot {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.35;
  }
  50% {
    transform: scale(1.35);
    opacity: 1;
  }
}

@keyframes flow-line {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(470%);
  }
}

@keyframes rotate-ring {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse-node {
  0% {
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.45);
  }
  70% {
    box-shadow: 0 0 0 12px rgba(59, 130, 246, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0);
  }
}

@keyframes cutoff-line {
  0%,
  35% {
    opacity: 1;
    transform: scaleX(1);
    transform-origin: left center;
  }
  55%,
  100% {
    opacity: 0.16;
    transform: scaleX(0.08);
    transform-origin: right center;
  }
}

@keyframes preview-shimmer {
  from {
    transform: translateX(-120%);
  }
  to {
    transform: translateX(120%);
  }
}

.showcase-shell {
  width: min(1500px, 96vw);
  margin: 0 auto;
  min-height: 80vh;
  border-radius: 20px;
  border: 1px solid color-mix(in srgb, var(--border-color) 88%, transparent);
  background: color-mix(in srgb, var(--surface-color) 96%, #10131d);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.1);
  padding: clamp(20px, 3.6vw, 34px);
  box-sizing: border-box;
}

.showcase-nav {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.showcase-tab {
  border: none;
  background: transparent;
  color: var(--muted-color);
  text-align: left;
  padding: 8px 8px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
}

.showcase-tab.active {
  color: var(--text-color);
  background: color-mix(in srgb, var(--accent-color) 10%, transparent);
}

.showcase-progress {
  margin-top: 10px;
  height: 4px;
  background: rgba(148, 163, 184, 0.25);
  border-radius: 999px;
  overflow: hidden;
}

.showcase-progress span {
  display: block;
  height: 100%;
  background: linear-gradient(to right, #f59e0b, #22c55e, #3b82f6);
  transition: width 0.26s ease;
}

.showcase-main {
  margin-top: 18px;
  display: grid;
  grid-template-columns: auto minmax(560px, 1.35fr) minmax(340px, 1fr) auto;
  gap: 16px;
  align-items: center;
}

.showcase-arrow {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid color-mix(in srgb, var(--border-color) 92%, transparent);
  background: rgba(255, 255, 255, 0.88);
  color: var(--accent-color);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.showcase-arrow :deep(svg) {
  width: 18px;
  height: 18px;
}

.showcase-visual {
  border-radius: 22px;
  background: #03060e;
  color: #edf4fd;
  padding: 20px;
  min-height: min(64vh, 620px);
  box-sizing: border-box;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.visual-stack {
  position: relative;
  height: min(54vh, 520px);
}

.visual-panel {
  position: absolute;
  inset: 0;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.visual-panel.back {
  transform: translate(-26px, 14px);
  opacity: 0.2;
}

.visual-panel.middle {
  transform: translate(-12px, 8px);
  opacity: 0.34;
}

.visual-panel.front {
  background: rgba(22, 32, 54, 0.88);
  padding: 24px;
  box-sizing: border-box;
}

.bar {
  display: block;
  height: 12px;
  border-radius: 999px;
  margin-bottom: 10px;
  width: 76%;
  background: linear-gradient(90deg, #4f8ff6, #3b82f6);
  animation: bar-breathe 2.4s infinite ease-in-out;
}

.bar.long {
  width: 88%;
}

.bar.short {
  width: 48%;
}

.visual-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 10px;
  height: 150px;
}

.visual-grid span {
  border-radius: 14px;
  background: linear-gradient(140deg, #4f8ff6, #3b82f6);
}

.showcase-copy h2 {
  margin: 0;
  font-size: clamp(36px, 4.2vw, 64px);
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.showcase-copy p {
  margin: 14px 0 0;
  color: var(--muted-color);
  line-height: 1.65;
  font-size: clamp(18px, 1.5vw, 24px);
}

.showcase-cta {
  margin-top: 18px;
  border: none;
  border-radius: 999px;
  padding: 12px 20px;
  background: linear-gradient(120deg, #2c6cf6, #4f90ff);
  color: #f8fbff;
  font-size: clamp(18px, 1.5vw, 24px);
  font-weight: 700;
  cursor: pointer;
}

.story-footer {
  margin-top: 20px;
  background: #eceef2;
  color: #2f3947;
  border-top: 1px solid #d9dde4;
}

.footer-inner {
  width: min(1360px, calc(100vw - 28px));
  margin: 0 auto;
  padding: 22px 0 32px;
}

.footer-social {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
}

.footer-social a {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #181c24;
  color: #f5f7fb;
  text-decoration: none;
  font-size: 14px;
}

.footer-social a :deep(svg) {
  width: 14px;
  height: 14px;
}

.footer-grid {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #d2d7df;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.footer-grid section {
  display: grid;
  gap: 8px;
  align-content: start;
}

.footer-grid h4 {
  margin: 0 0 8px;
  font-size: 20px;
}

.footer-grid a {
  color: #48566a;
  text-decoration: none;
  font-size: 19px;
  line-height: 1.4;
}

.footer-grid a:hover {
  color: #1f2f46;
}

@keyframes bar-breathe {
  0%,
  100% {
    opacity: 0.7;
  }
  50% {
    opacity: 1;
  }
}

.register-stage {
  position: relative;
  width: min(560px, 100%);
  margin: 0 auto;
}

.form-glow {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  opacity: 0.15;
  pointer-events: none;
  z-index: 0;
  background: radial-gradient(circle, var(--halo-a) 0%, var(--halo-b) 45%, transparent 72%);
  filter: blur(24px);
  will-change: transform;
}

.register-card {
  position: relative;
  z-index: 1;
  padding: clamp(24px, 4vw, 36px);
  border-radius: 20px;
  border: 1px solid color-mix(in srgb, var(--border-color) 92%, transparent);
  background: color-mix(in srgb, var(--surface-color) 88%, rgba(255, 255, 255, 0.46));
  backdrop-filter: blur(18px);
  box-shadow: 0 20px 56px rgba(22, 30, 55, 0.2);
  will-change: transform, opacity;
}

.register-card h2 {
  margin: 6px 0 8px;
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.15;
}

.register-card .scene-desc {
  margin-bottom: 16px;
}

.register-card label {
  display: block;
  font-size: 13px;
  margin: 12px 0 6px;
  color: var(--muted-color);
}

.register-card input[type='text'],
.register-card input[type='email'],
.register-card input[type='password'] {
  width: 100%;
  box-sizing: border-box;
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--border-color) 92%, transparent);
  background: color-mix(in srgb, var(--surface-color) 92%, rgba(255, 255, 255, 0.25));
  color: var(--text-color);
  padding: 11px 13px;
  outline: none;
  transition: border-color 0.24s ease, box-shadow 0.24s ease, transform 0.24s ease;
}

.register-card input[type='text']::placeholder,
.register-card input[type='email']::placeholder,
.register-card input[type='password']::placeholder {
  color: color-mix(in srgb, var(--muted-color) 82%, transparent);
}

.register-card input[type='text']:focus,
.register-card input[type='email']:focus,
.register-card input[type='password']:focus {
  border-color: color-mix(in srgb, var(--accent-color) 84%, #fff);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--accent-color) 18%, transparent);
  transform: translateY(-1px);
}

.error {
  margin: 10px 0 0;
  color: var(--color-danger);
  font-size: 13px;
}

.primary {
  margin-top: 16px;
  width: 100%;
  border: 0;
  border-radius: 12px;
  padding: 12px 14px;
  background: linear-gradient(120deg, color-mix(in srgb, var(--accent-color) 92%, #172a58), color-mix(in srgb, var(--accent-color) 56%, #87c4ff));
  color: #f8fbff;
  font-weight: 700;
  cursor: pointer;
}

.switch-row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.switch-control {
  position: relative;
  width: 44px;
  height: 24px;
  flex: 0 0 auto;
  margin: 0;
}

.switch-control input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.switch-core {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: color-mix(in srgb, var(--muted-color) 38%, transparent);
  border: 1px solid color-mix(in srgb, var(--border-color) 92%, transparent);
  transition: background-color 0.24s ease;
}

.switch-core::after {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  left: 2px;
  top: 2px;
  background: #fff;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.2);
  transition: transform 0.24s ease;
}

.switch-control input:checked + .switch-core {
  background: color-mix(in srgb, var(--accent-color) 48%, transparent);
}

.switch-control input:checked + .switch-core::after {
  transform: translateX(20px);
}

.switch-text {
  color: var(--muted-color);
  font-size: 12px;
}

.link {
  display: inline-block;
  margin-top: 14px;
  text-decoration: none;
  color: var(--accent-color);
  font-size: 14px;
}

@media (max-width: 1160px) {
  .hero-grid,
  .features-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .hero-copy h1 {
    font-size: clamp(30px, 8.8vw, 52px);
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .showcase-main {
    grid-template-columns: 1fr;
  }

  .showcase-arrow {
    display: none;
  }

  .showcase-shell {
    width: min(96vw, 1100px);
    min-height: auto;
  }

  .showcase-visual {
    min-height: 420px;
  }

  .visual-stack {
    height: 330px;
  }

  .footer-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .story-floatbar {
    top: 8px;
  }

  .floatbar-inner {
    width: calc(100vw - 16px);
    padding: 8px 10px;
    gap: 8px;
  }

  .floatbar-meta {
    display: none;
  }

  .scene-stage {
    padding: 16px;
  }

  .scene-2 .scene-stage,
  .scene-3 .scene-stage,
  .scene-4 .scene-stage {
    padding-top: 78px;
  }

  .showcase-nav {
    grid-template-columns: 1fr 1fr;
  }

  .showcase-copy p {
    font-size: 16px;
  }

  .showcase-cta {
    font-size: 18px;
  }

  .feature-stack {
    --feature-card-height: 340px;
    --feature-anim-height: 170px;
    height: calc(var(--feature-card-height) + 120px);
  }

  .form-glow {
    width: 300px;
    height: 300px;
  }

  .footer-inner {
    width: calc(100vw - 16px);
    padding: 18px 0 26px;
  }

  .footer-social {
    gap: 10px;
    flex-wrap: wrap;
  }

  .footer-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .footer-grid h4 {
    font-size: 18px;
  }

  .footer-grid a {
    font-size: 16px;
  }
}
</style>
