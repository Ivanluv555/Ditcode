<template>
  <router-view v-if="isStandaloneRoute"></router-view>

  <div
    ref="appShellRef"
    v-else
    class="app-shell"
    :class="[`theme-${activeTheme}`]"
    :style="layoutVars"
  >
    <aside ref="sidebarRef" class="sidebar" :class="{ collapsed: isSidebarCollapsed }">
      <div class="sidebar-top">
        <button class="collapse-btn" @click="toggleSidebar" style="background: transparent; box-shadow: none; border: none; padding: 4px; cursor: pointer;">
          <Icon icon="fa6-solid:bars" class="hamburger-icon" />
        </button>
      </div>

      <div class="sidebar-body">
        <nav class="sidebar-nav" v-show="!isSidebarCollapsed">
          <RouterLink
            to="/"
            class="sidebar-action primary"
            :class="{ active: route.path === '/' }"
            @click="startNewWork"
          >
            <Icon icon="fa6-solid:circle-plus" class="action-icon" aria-hidden="true" />
            <span>发起新工作</span>
          </RouterLink>

          <RouterLink
            to="/my-content"
            class="sidebar-action"
            :class="{ active: route.path.startsWith('/my-content') }"
            @click="collapseModelPanels"
          >
            <Icon icon="fa6-solid:folder-open" class="action-icon" aria-hidden="true" />
            <span>我的内容</span>
          </RouterLink>
          
          <RouterLink
            to="/community"
            class="sidebar-action"
            :class="{ active: route.path === '/community' }"
            @click="collapseModelPanels"
          >
            <Icon icon="fa6-solid:users" class="action-icon" aria-hidden="true" />
            <span>社区作品</span>
          </RouterLink>

          <RouterLink
            to="/featured"
            class="sidebar-action"
            :class="{ active: route.path === '/featured' }"
            @click="collapseModelPanels"
          >
            <Icon icon="fa6-solid:star" class="action-icon" aria-hidden="true" />
            <span>优秀案例</span>
          </RouterLink>

          <RouterLink
            to="/resources"
            class="sidebar-action"
            :class="{ active: route.path === '/resources' }"
            @click="collapseModelPanels"
          >
            <Icon icon="fa6-solid:lightbulb" class="action-icon" aria-hidden="true" />
            <span>灵感素材库</span>
          </RouterLink>
        </nav>

        <div v-if="history.length > 0" class="history-block" v-show="!isSidebarCollapsed">
          <div class="history-title">最近工作</div>
          <div
            v-for="item in history"
            :key="item.id"
            class="history-item"
          >
            <button class="history-open" @click="openModelFromHistory(item.id)">
              <span class="history-open-main">
                <span class="history-main">{{ item.title || item.lastMessage || '未命名存档' }}</span>
                <span class="history-meta">{{ formatDate(item.updatedAt || item.createdAt) }}</span>
              </span>
            </button>
            <button
              v-if="item.canDelete"
              class="history-delete"
              title="删除存档"
              @click.stop="requestDeleteArchive(item)"
            >
              <Icon icon="fa6-solid:trash-can" aria-hidden="true" />
            </button>
          </div>
        </div>

        <div class="sidebar-footer">
          <button
            type="button"
            ref="settingsBtnRef"
            class="sidebar-action footer-action"
            @click="openSettingsModal"
            :style="{ justifyContent: isSidebarCollapsed ? 'center' : 'flex-start', padding: isSidebarCollapsed ? '12px' : '12px 14px' }"
          >
            <Icon icon="fa6-solid:gear" class="action-icon" aria-hidden="true" :style="{ margin: isSidebarCollapsed ? '0' : '' }" />
            <span v-show="!isSidebarCollapsed">设置与帮助</span>
          </button>
        </div>
      </div>
    </aside>

    <main
      class="content"
      :class="{
        'workspace-hidden': !showWorkspacePanels,
        'welcome-locked': isHomeWelcomeState
      }"
    >
      <header class="topbar">
        <div class="topbar-left">
          <button
            class="brand"
            ref="brandRef"
            @click="goHome"
          >
            <img
              class="brand-name"
              :class="{ light: activeTheme !== 'dark' }"
              :src="brandNameWhite"
              alt="见筑"
            />
          </button>

          <div class="title-block" v-if="showBackBtn && !isCommunityRoute">
            <p class="title-sub">见筑 workspace</p>
            <h2 class="title-main">{{ pageTitle }}</h2>
          </div>
        </div>

        <div class="top-right">
          <button ref="avatarBtnRef" class="avatar-btn" style="overflow: hidden; padding: 0;" @click="onAvatarClick">
            <Icon icon="fa6-solid:circle-user" class="avatar-icon" />
          </button>
        </div>
      </header>

      <router-view></router-view>

      <Transition
        :css="false"
        @before-enter="onComposerBeforeEnter"
        @enter="onComposerEnter"
        @leave="onComposerLeave"
      >
        <div v-show="showComposerPanel" class="bottom-composer-wrap">
          <div class="bottom-composer">
            <textarea
              ref="composerInputRef"
              v-model="promptText"
              rows="1"
              class="composer-input"
              placeholder="输入你的建模描述，支持附带参考图..."
              @input="onComposerInput"
            ></textarea>

            <div class="composer-tools">
              <template v-if="allowImageAttachment">
                <label class="tool-btn" for="app-image-upload">上传图片</label>
                <input id="app-image-upload" type="file" accept="image/*" @change="onPickFile" />
                <span v-if="imageFile" class="file-tag">{{ imageFile.name }}</span>
                <button v-if="imageFile" class="tool-btn ghost" @click="clearImage">移除</button>
              </template>
              <button class="send-btn" aria-label="发送生成请求" @click="generateFromComposer">
                <Icon icon="fa6-solid:arrow-up" class="send-icon" />
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </main>

    <Teleport to="body">
      <Transition
        :css="false"
        @before-enter="onFadeBeforeEnter"
        @enter="onFadeEnter"
        @leave="onFadeLeave"
      >
        <div v-if="showUserModal" class="dropdown-shell" @click="closeUserModal">
          <Transition
            :css="false"
            @before-enter="onDropdownPanelBeforeEnter"
            @enter="(el, done) => onDropdownPanelEnter(el, done, 'user')"
            @leave="onDropdownPanelLeave"
          >
            <div class="user-modal dropdown-panel user-dropdown" :style="userDropdownStyle" @click.stop>
              <!-- 顶部用户信息区 -->
              <div class="user-info-section">
                <div class="user-avatar dropdown-item" style="--i: 0">
                  <Icon icon="fa6-solid:circle-user" class="user-avatar-icon" />
                </div>
                <div class="user-name dropdown-item" style="--i: 1">{{ currentUser?.username || '未命名用户' }}</div>
                <div class="user-email dropdown-item" style="--i: 2">{{ currentUser?.email || 'no-email@example.com' }}</div>
              </div>

              <div class="dropdown-divider"></div>

              <!-- 中段主题设置区 -->
              <div class="theme-section">
                <div class="theme-label dropdown-item" style="--i: 3">外观主题</div>
                <div class="theme-toggle dropdown-item" style="--i: 4">
                  <button
                    class="theme-option"
                    :class="{ active: activeTheme === 'light' }"
                    @click="setThemePreference('light')"
                  >
                    <Icon icon="fa6-solid:sun" class="theme-icon" />
                    <span>亮色</span>
                  </button>
                  <button
                    class="theme-option"
                    :class="{ active: activeTheme === 'dark' }"
                    @click="setThemePreference('dark')"
                  >
                    <Icon icon="fa6-solid:moon" class="theme-icon" />
                    <span>深色</span>
                  </button>
                </div>
              </div>

              <div class="dropdown-divider"></div>

              <!-- 底部功能列表区 -->
              <div class="actions-section">
                <button class="action-item dropdown-item" style="--i: 5" @click="handleHistoryClick">
                  <Icon icon="fa6-solid:clock-rotate-left" class="action-icon" />
                  <span>历史记录</span>
                </button>
                <button class="action-item dropdown-item" style="--i: 6" @click="handleNotificationClick">
                  <Icon icon="fa6-solid:bell" class="action-icon" />
                  <span>使用通知</span>
                </button>
                <button class="action-item dropdown-item" style="--i: 7" @click="handleSwitchAccountClick">
                  <Icon icon="fa6-solid:right-left" class="action-icon" />
                  <span>切换账号</span>
                </button>
                <button class="action-item dropdown-item" style="--i: 8" @click="handleContactClick">
                  <Icon icon="fa6-solid:envelope" class="action-icon" />
                  <span>联系我们</span>
                </button>
                <button class="action-item dropdown-item" style="--i: 9" @click="handleFeedbackClick">
                  <Icon icon="fa6-solid:comment-dots" class="action-icon" />
                  <span>意见反馈</span>
                </button>
                <button class="action-item danger dropdown-item" style="--i: 10" @click="logoutFromModal">
                  <Icon icon="fa6-solid:arrow-right-from-bracket" class="action-icon" />
                  <span>注销账号</span>
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition
        :css="false"
        @before-enter="onFadeBeforeEnter"
        @enter="onFadeEnter"
        @leave="onFadeLeave"
      >
        <div v-if="showContactModal" class="dropdown-shell" @click="closeContactModal">
          <Transition
            :css="false"
            @before-enter="onDropdownPanelBeforeEnter"
            @enter="(el, done) => onDropdownPanelEnter(el, done, 'user')"
            @leave="onDropdownPanelLeave"
          >
            <div class="user-modal dropdown-panel" :style="userDropdownStyle" @click.stop>
              <ContactModal @back="backToUserModal" />
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition
        :css="false"
        @before-enter="onFadeBeforeEnter"
        @enter="onFadeEnter"
        @leave="onFadeLeave"
      >
        <div v-if="showFeedbackModal" class="dropdown-shell" @click="closeFeedbackModal">
          <Transition
            :css="false"
            @before-enter="onDropdownPanelBeforeEnter"
            @enter="(el, done) => onDropdownPanelEnter(el, done, 'user')"
            @leave="onDropdownPanelLeave"
          >
            <div class="user-modal dropdown-panel" :style="userDropdownStyle" @click.stop>
              <FeedbackModal @back="backToUserModal" @submit="handleFeedbackSubmit" />
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition
        :css="false"
        @before-enter="onFadeBeforeEnter"
        @enter="onFadeEnter"
        @leave="onFadeLeave"
      >
        <div v-if="showSettingsModal" class="dropdown-shell" @click="closeSettingsModal">
          <Transition
            :css="false"
            @before-enter="onDropdownPanelBeforeEnter"
            @enter="(el, done) => onDropdownPanelEnter(el, done, 'settings')"
            @leave="onDropdownPanelLeave"
          >
            <div class="user-modal settings-modal dropdown-panel settings-dropdown" :style="settingsDropdownStyle" @click.stop>
              <h3 class="dropdown-item" style="--i: 0">设置与帮助</h3>
              <div class="settings-group">
                <div class="settings-row dropdown-item" style="--i: 2">
                  <span>主题模式</span>
                  <div class="settings-actions">
                    <button class="modal-btn" :class="{ active: activeTheme === 'light' }" @click="setThemePreference('light')">浅色</button>
                    <button class="modal-btn" :class="{ active: activeTheme === 'dark' }" @click="setThemePreference('dark')">深色</button>
                    <button class="modal-btn" @click="followSystemThemeFromModal">跟随系统</button>
                  </div>
                </div>

                <div class="settings-row dropdown-item" style="--i: 3">
                  <span>本地数据</span>
                  <div class="settings-actions">
                    <button class="modal-btn danger" @click="clearLocalWorkspace">清空内容与任务</button>
                    <button class="modal-btn danger" @click="logoutFromSettings">退出登录</button>
                  </div>
                </div>

                <div class="settings-help dropdown-item" style="--i: 4">
                  <p>发起新工作会回到首页并保留当前工作区哦</p>
                  <p>查看我的内容可以看到最近生成的资产和进行中的任务哦</p>
                </div>
              </div>
              <div class="modal-actions dropdown-item" style="--i: 5">
                <button class="modal-btn" @click="closeSettingsModal">关闭</button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <Transition
      :css="false"
      @before-enter="onFadeBeforeEnter"
      @enter="onFadeEnter"
      @leave="onFadeLeave"
    >
      <div v-if="showLoginPromptModal" class="modal-mask" @click="closeLoginPromptModal">
        <Transition
          :css="false"
          @before-enter="onModalBeforeEnter"
          @enter="onModalEnter"
          @leave="onModalLeave"
        >
          <div class="user-modal" @click.stop>
            <h3>建议先登录</h3>
            <p>新用户请先登录，登录后就可以保存作品、历史和偏好设置哦</p>
            <div class="modal-actions">
              <button class="modal-btn" @click="closeLoginPromptModal">稍后再说</button>
              <button class="modal-btn" @click="goLoginFromPrompt">去登录</button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>

    <Transition
      :css="false"
      @before-enter="onFadeBeforeEnter"
      @enter="onFadeEnter"
      @leave="onFadeLeave"
    >
      <div v-if="showDeleteArchiveModal" class="modal-mask" @click="closeDeleteArchiveModal">
        <Transition
          :css="false"
          @before-enter="onModalBeforeEnter"
          @enter="onModalEnter"
          @leave="onModalLeave"
        >
          <div class="user-modal" @click.stop>
            <h3>删除存档</h3>
            <p v-if="!isPendingDeletePublished">
              删除后将无法恢复，你确定要删除这个存档吗？
            </p>
            <p v-else-if="!deleteArchiveSecondConfirm">
              该存档已发布到社区，删除后社区中的对应内容也会消失。请再次确认是否继续删除。
            </p>
            <p v-else>
              这是第二次确认：删除后会同时移除社区发布内容，且无法恢复。
            </p>
            <div class="modal-actions">
              <button class="modal-btn" @click="closeDeleteArchiveModal">取消</button>
              <button class="modal-btn danger" @click="confirmDeleteArchive">
                {{ isPendingDeletePublished && !deleteArchiveSecondConfirm ? '继续删除' : '确认删除' }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </div>
</template>

<script setup>
/*
 * App.vue（应用外壳）注释说明
 * ---------------------------
 * 这个文件是应用的根组件，负责：
 *  - 渲染左侧侧边栏（存档/历史）和顶部工具栏
 *  - 在中间区域通过 <router-view> 渲染不同页面（Home / Workshop / Viewer 等）
 *  - 提供底部的 composer 用于输入 prompt 并触发模型生成
 *  - 管理主题、下拉菜单、模态框、以及侧边栏折叠状态等全局 UI 行为
 *
 * 对完全不懂前端的人：
 *  - template 部分类似 HTML，定义了页面的结构
 *  - script setup 部分定义了页面的数据（变量）和函数（事件处理），这些变量用 ref()/computed() 创建并在模版中直接使用
 *  - style 部分定义样式（CSS），控制视觉与布局
 *
 * 关键点（便于理解的工作流）：
 *  1) 用户在底部 composer 输入文本并点击发送 -> 调用 generateFromComposer
 *  2) generateFromComposer 会调用 taskStore.generateModelAsset 向后端请求生成
 *  3) 成功后页面会导航到 /viewer 并把图片地址作为 query 参数传递，以便在查看器中查看完整全景
 *
 * 维护提示：修改路由或导航逻辑时，需同时更新组件中调用 router.push 的所有位置（例如 EditorPanel）。
 */
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { gsap } from 'gsap';
import { useAuthStore } from '@/packages/auth/store/useAuthStore';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import { fileToDataUrl } from '@/shared/utils/fileToDataUrl';
import { useReducedMotion } from '@/shared/hooks/useReducedMotion';
import { MOTION_DURATION, MOTION_EASE } from '@/shared/motion/preset';
import brandNameWhite from '../ditlogos/name_front_line.svg';
import ContactModal from '@/shared/components/ContactModal.vue';
import FeedbackModal from '@/shared/components/FeedbackModal.vue';

const taskStore = useTaskStore();
const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const bootstrapStores = async () => {
  try {
    await authStore.init();
    await taskStore.init();
    if (!isReloadNavigation()) {
      taskStore.clearCurrentArchiveSelection();
    }
  } catch (error) {
    console.warn('Failed to bootstrap stores:', error);
  }
};

const isSidebarCollapsed = ref(false);
const isModelDrawerOpen = ref(false);
const showUserModal = ref(false);
const showContactModal = ref(false);
const showFeedbackModal = ref(false);
const showSettingsModal = ref(false);
const showLoginPromptModal = ref(false);
const showDeleteArchiveModal = ref(false);
const pendingDeleteArchiveId = ref('');
const pendingDeleteArchivePublished = ref(false);
const deleteArchiveSecondConfirm = ref(false);
const appShellRef = ref(null);
const sidebarRef = ref(null);
const brandRef = ref(null);
const avatarBtnRef = ref(null);
const settingsBtnRef = ref(null);
const userDropdownStyle = ref({});
const settingsDropdownStyle = ref({});
const activeTheme = ref('light');

const prefersReducedMotion = useReducedMotion();

const THEME_PREF_KEY = 'ditapp_theme_pref';
const FIRST_REQUEST_PROMPT_KEY = 'ditapp_first_request_login_prompted';
const FORCE_WELCOME_ONCE_KEY = 'ditapp_force_welcome_once';
const FORCE_WELCOME_BASELINE_KEY = 'ditapp_force_welcome_baseline';
const HAS_SEEN_WELCOME_KEY = 'ditapp_has_seen_welcome';
let colorSchemeMedia = null;
let colorSchemeChangeHandler = null;

const composerInputRef = ref(null);
const promptText = ref('');
const imageFile = ref(null);
const imagePreview = ref('');

const currentUser = computed(() => authStore.currentUser);
const history = computed(() =>
  taskStore.archiveSummaries.slice(0, 12).map((item) => ({
    ...item,
    canDelete: Boolean(
      currentUser.value &&
      (item.ownerId === currentUser.value.id || (!item.ownerId && !item.ownerName))
    )
  }))
);
const allowImageAttachment = computed(() => taskStore.currentArchiveMessages.length === 0);
const hasModelDrawer = computed(
  () => Boolean(taskStore.currentArchive && taskStore.currentArchive.modelAsset)
);
const leftPanelWidth = computed(() => (isSidebarCollapsed.value ? '78px' : '280px'));
const rightPanelWidth = computed(() =>
  hasModelDrawer.value && isModelDrawerOpen.value ? '70vw' : '0px'
);
const showWorkspaceControls = computed(() => !route.meta?.hideWorkspace);
const showWorkspacePanels = computed(() => !route.meta?.hideWorkspace);
const showComposerPanel = computed(() => showWorkspacePanels.value && !route.meta?.hideComposer);
const isCommunityRoute = computed(() => route.path === '/community');
const isHomeWelcomeState = computed(
  () =>
    route.path === '/' &&
    taskStore.currentArchiveMessages.length === 0 &&
    !promptText.value.trim() &&
    !imageFile.value
);
const routeTitleMap = {
  '/': '创作主页',
  '/community': '发现社区',
  '/resources': '资源灵感库',
  '/featured': '优秀案例',
  '/my-content': '我的内容',
  '/settings': '设置与帮助'
};
const pageTitle = computed(() => {
  if (route.path.startsWith('/workshop/')) return '工作台';
  return routeTitleMap[route.path] || '见筑';
});
const layoutVars = computed(() => ({
  '--left-panel-width': leftPanelWidth.value,
  '--right-panel-width': rightPanelWidth.value
}));

const isStandaloneRoute = computed(() => Boolean(route.meta && route.meta.standalone));
const showBackBtn = computed(() => route.path !== '/' && !isCommunityRoute.value);
const communityIcon = computed(() => 'fa6-solid:users');

const formatDate = (value) => {
  if (!value) return '';
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const resizeComposer = () => {
  if (!composerInputRef.value) return;
  composerInputRef.value.style.height = 'auto';
  const nextHeight = Math.min(composerInputRef.value.scrollHeight, 200);
  composerInputRef.value.style.height = `${nextHeight}px`;
};

const onComposerInput = () => {
  resizeComposer();
};

const onPickFile = (event) => {
  if (!allowImageAttachment.value) return;

  const file = event.target.files && event.target.files[0];
  if (!file) return;
  if (imagePreview.value) URL.revokeObjectURL(imagePreview.value);
  imageFile.value = file;
  imagePreview.value = URL.createObjectURL(file);
};

const clearImage = () => {
  if (imagePreview.value) URL.revokeObjectURL(imagePreview.value);
  imageFile.value = null;
  imagePreview.value = '';
};

const resolveSystemTheme = () => {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

const applyTheme = (theme) => {
  activeTheme.value = theme;
  document.documentElement.setAttribute('data-theme', theme);
};

const syncThemeByAuthState = () => {
  if (!currentUser.value) {
    localStorage.removeItem(THEME_PREF_KEY);
    applyTheme(resolveSystemTheme());
    return;
  }

  const savedTheme = localStorage.getItem(THEME_PREF_KEY);
  if (savedTheme === 'dark' || savedTheme === 'light') {
    applyTheme(savedTheme);
    return;
  }

  applyTheme(resolveSystemTheme());
};

const setThemePreference = (theme) => {
  localStorage.setItem(THEME_PREF_KEY, theme);
  applyTheme(theme);
};

const generateFromComposer = async () => {
  const prompt = promptText.value.trim();
  const sourceFile = allowImageAttachment.value ? imageFile.value : null;
  if (!prompt && !sourceFile) return;

  if (!currentUser.value && localStorage.getItem(FIRST_REQUEST_PROMPT_KEY) !== '1') {
    localStorage.setItem(FIRST_REQUEST_PROMPT_KEY, '1');
    showLoginPromptModal.value = true;
    return;
  }

  let referenceImage = '';
  try {
    referenceImage = sourceFile ? await fileToDataUrl(sourceFile) : '';
  } catch (error) {
    console.warn('Failed to read composer image:', error);
  }

  const result = await taskStore.generateModelAsset({
    prompt,
    imageName: sourceFile ? sourceFile.name : '',
    imageBase64: referenceImage
  });

  if (result.ok) {
    // show generated result (static preview) in the workspace; do NOT auto-open viewer
    // The assistant message with the flat preview will be appended by the store (addAssetRecord).
    isSidebarCollapsed.value = true;
    await nextTick();
  }

  promptText.value = '';
  clearImage();
  nextTick(() => resizeComposer());
};

const logout = async () => {
  await authStore.logout();
  taskStore.resetForAuthChange();
  isModelDrawerOpen.value = false;
  showUserModal.value = false;
};

const clearLocalWorkspace = async () => {
  await taskStore.resetWorkspace();
  isModelDrawerOpen.value = false;
};

const openSettingsModal = () => {
  showSettingsModal.value = true;
};

const closeSettingsModal = () => {
  showSettingsModal.value = false;
};

const followSystemThemeFromModal = () => {
  localStorage.removeItem(THEME_PREF_KEY);
  applyTheme(resolveSystemTheme());
};

const logoutFromSettings = async () => {
  await authStore.logout();
  taskStore.resetForAuthChange();
  isModelDrawerOpen.value = false;
  closeSettingsModal();
  router.push('/login');
};

const collapseModelPanels = () => {
  isModelDrawerOpen.value = false;
};

const startNewWork = () => {
  const baseline = 0;

  taskStore.clearCurrentArchiveSelection();
  isModelDrawerOpen.value = false;
  isSidebarCollapsed.value = false;
  sessionStorage.setItem(FORCE_WELCOME_ONCE_KEY, '1');
  sessionStorage.setItem(FORCE_WELCOME_BASELINE_KEY, String(baseline));
  router.push('/');
};

const toggleSidebar = () => {
  if (isSidebarCollapsed.value && isModelDrawerOpen.value) {
    isModelDrawerOpen.value = false;
  }
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};

const toggleModelDrawer = () => {
  if (!hasModelDrawer.value) return;
  const nextState = !isModelDrawerOpen.value;
  isModelDrawerOpen.value = nextState;
  if (nextState) {
    isSidebarCollapsed.value = true;
  }
};

const openModelFromHistory = (id) => {
  if (!id) return;

  taskStore.openArchive(id, { createIfMissing: true, title: `项目 ${id}` });
  isModelDrawerOpen.value = false;
  isSidebarCollapsed.value = false;
  router.push({ path: '/', query: { archive: id } });
};



const goCommunity = () => {
  router.push('/community');
};

const goHome = () => {
  router.push('/');
};

const onAvatarClick = () => {
  if (!currentUser.value) {
    router.push('/login');
    return;
  }
  showUserModal.value = true;
};

const closeUserModal = () => {
  showUserModal.value = false;
};

const closeContactModal = () => {
  showContactModal.value = false;
};

const closeFeedbackModal = () => {
  showFeedbackModal.value = false;
};

const backToUserModal = () => {
  showContactModal.value = false;
  showFeedbackModal.value = false;
  showUserModal.value = true;
};

const handleFeedbackSubmit = (feedback) => {
  console.log('收到反馈:', feedback);
  alert('感谢您的反馈！我们会尽快处理。');
  closeFeedbackModal();
};

const logoutFromModal = async () => {
  await logout();
};

const handleHistoryClick = () => {
  closeUserModal();
  router.push('/my-content');
};

const handleNotificationClick = () => {
  closeUserModal();
  // TODO: 实现通知功能
  console.log('打开使用通知');
};

const handleSwitchAccountClick = () => {
  closeUserModal();
  router.push('/login');
};

const handleContactClick = () => {
  showUserModal.value = false;
  showContactModal.value = true;
};

const handleFeedbackClick = () => {
  showUserModal.value = false;
  showFeedbackModal.value = true;
};

const closeLoginPromptModal = () => {
  showLoginPromptModal.value = false;
};

const requestDeleteArchive = (archive) => {
  pendingDeleteArchiveId.value = archive?.id || '';
  pendingDeleteArchivePublished.value = Boolean(archive && archive.isPrivate === false);
  deleteArchiveSecondConfirm.value = false;
  showDeleteArchiveModal.value = true;
};

const closeDeleteArchiveModal = () => {
  showDeleteArchiveModal.value = false;
  pendingDeleteArchiveId.value = '';
  pendingDeleteArchivePublished.value = false;
  deleteArchiveSecondConfirm.value = false;
};

const confirmDeleteArchive = async () => {
  if (!pendingDeleteArchiveId.value) return;
  if (pendingDeleteArchivePublished.value && !deleteArchiveSecondConfirm.value) {
    deleteArchiveSecondConfirm.value = true;
    return;
  }
  await taskStore.deleteArchive(pendingDeleteArchiveId.value);
  closeDeleteArchiveModal();
};

const isPendingDeletePublished = computed(() => pendingDeleteArchivePublished.value);

const goLoginFromPrompt = () => {
  showLoginPromptModal.value = false;
  router.push('/login');
};

const toggleThemeManually = () => {
  if (!currentUser.value) return;
  const nextTheme = activeTheme.value === 'dark' ? 'light' : 'dark';
  setThemePreference(nextTheme);
};

const handleThemePreferenceChange = (event) => {
  const nextTheme = event?.detail?.theme;
  if (nextTheme === 'dark' || nextTheme === 'light') {
    applyTheme(nextTheme);
  }
};

const isReloadNavigation = () => {
  const entry = performance.getEntriesByType('navigation')[0];
  if (entry && 'type' in entry) {
    return entry.type === 'reload';
  }

  return performance?.navigation?.type === 1;
};

const runShellIntroAnimation = () => {
  if (!sidebarRef.value || !brandRef.value || prefersReducedMotion.value) return;

  gsap.fromTo(
    sidebarRef.value,
    { xPercent: -100, opacity: 0 },
    { xPercent: 0, opacity: 1, duration: MOTION_DURATION.intro, ease: MOTION_EASE.enter }
  );

  const dot = brandRef.value.querySelector('.brand-dot');
  const title = brandRef.value.querySelector('.brand-title');
  const subtitle = brandRef.value.querySelector('.brand-subtitle');

  const timeline = gsap.timeline({ delay: 0.45 });

  if (dot) {
    timeline.fromTo(
      dot,
      { opacity: 0, scale: 0.8, boxShadow: '0 0 0 0 rgba(58, 139, 255, 0.14)' },
      {
        opacity: 1,
        scale: 1,
        boxShadow: '0 0 0 5px rgba(58, 139, 255, 0.14)',
        duration: MOTION_DURATION.slow,
        ease: MOTION_EASE.bounceEnter
      }
    );
  }

  if (title) {
    timeline.fromTo(
      title,
      { opacity: 0, x: -10 },
      { opacity: 1, x: 0, duration: MOTION_DURATION.normal, ease: MOTION_EASE.enter },
      '<0.1'
    );
  }

  if (subtitle) {
    timeline.fromTo(
      subtitle,
      { opacity: 0, y: 5 },
      { opacity: 1, y: 0, duration: MOTION_DURATION.normal, ease: MOTION_EASE.enter },
      '<0.1'
    );
  }
};

const onFadeBeforeEnter = (el) => {
  el.style.opacity = '0';
};

const onFadeEnter = (el, done) => {
  if (prefersReducedMotion.value) {
    el.style.opacity = '1';
    done();
    return;
  }

  gsap.to(el, { opacity: 1, duration: MOTION_DURATION.fast, ease: MOTION_EASE.subtleEnter, onComplete: done });
};

const onFadeLeave = (el, done) => {
  if (prefersReducedMotion.value) {
    done();
    return;
  }

  gsap.to(el, { opacity: 0, duration: MOTION_DURATION.fast, ease: MOTION_EASE.subtleExit, onComplete: done });
};

const onComposerBeforeEnter = (el) => {
  el.style.opacity = '0';
  el.style.transform = 'translateY(100%)';
};

const onComposerEnter = (el, done) => {
  if (prefersReducedMotion.value) {
    el.style.opacity = '1';
    el.style.transform = 'translateY(0)';
    done();
    return;
  }

  gsap.fromTo(
    el,
    { opacity: 0, yPercent: 100 },
    { opacity: 1, yPercent: 0, duration: MOTION_DURATION.slow, ease: MOTION_EASE.enter, onComplete: done }
  );
};

const onComposerLeave = (el, done) => {
  if (prefersReducedMotion.value) {
    done();
    return;
  }

  gsap.to(el, {
    opacity: 0,
    yPercent: 100,
    duration: MOTION_DURATION.normal,
    ease: MOTION_EASE.exit,
    onComplete: done
  });
};

const onDropdownPanelBeforeEnter = (el) => {
  el.style.opacity = '0';
};

const onDropdownPanelEnter = (el, done, panelType) => {
  if (prefersReducedMotion.value) {
    el.style.opacity = '1';
    done();
    return;
  }

  const enterFrom = panelType === 'settings'
    ? { x: -16, y: 16 }
    : { x: 16, y: -16 };

  gsap.fromTo(
    el,
    { opacity: 0, x: enterFrom.x, y: enterFrom.y, scale: 0.985 },
    {
      opacity: 1,
      x: 0,
      y: 0,
      scale: 1,
      duration: MOTION_DURATION.normal,
      ease: MOTION_EASE.enter,
      onComplete: done
    }
  );

  const itemFromX = panelType === 'settings' ? -14 : 14;
  const items = el.querySelectorAll('.dropdown-item');
  if (items.length) {
    gsap.fromTo(
      items,
      { opacity: 0, x: itemFromX },
      {
        opacity: 1,
        x: 0,
        duration: MOTION_DURATION.normal,
        ease: MOTION_EASE.enter,
        stagger: 0.056,
        delay: 0.064
      }
    );
  }
};

const onDropdownPanelLeave = (el, done) => {
  if (prefersReducedMotion.value) {
    done();
    return;
  }

  gsap.to(el, {
    opacity: 0,
    scale: 0.985,
    duration: MOTION_DURATION.fast,
    ease: MOTION_EASE.exit,
    onComplete: done
  });
};

const onModalBeforeEnter = (el) => {
  el.style.opacity = '0';
};

const onModalEnter = (el, done) => {
  if (prefersReducedMotion.value) {
    el.style.opacity = '1';
    done();
    return;
  }

  gsap.fromTo(
    el,
    { opacity: 0, y: -18, scale: 0.98 },
    { opacity: 1, y: 0, scale: 1, duration: MOTION_DURATION.normal, ease: MOTION_EASE.enter, onComplete: done }
  );
};

const onModalLeave = (el, done) => {
  if (prefersReducedMotion.value) {
    done();
    return;
  }

  gsap.to(el, {
    opacity: 0,
    y: -12,
    scale: 0.98,
    duration: MOTION_DURATION.fast,
    ease: MOTION_EASE.exit,
    onComplete: done
  });
};

const bootstrapFirstVisitWelcome = async () => {
  if (route.meta?.standalone) return;
  if (localStorage.getItem(HAS_SEEN_WELCOME_KEY) === '1') return;

  const baseline = taskStore.currentArchiveMessages.length;

  localStorage.setItem(HAS_SEEN_WELCOME_KEY, '1');
  sessionStorage.setItem(FORCE_WELCOME_ONCE_KEY, '1');
  sessionStorage.setItem(FORCE_WELCOME_BASELINE_KEY, String(baseline));

  if (route.path !== '/') {
    await router.replace('/');
  }
};

const clamp = (value, min, max) => Math.max(min, Math.min(value, max));

const computeDropdownStyle = (anchorEl, placement, width) => {
  if (!anchorEl) return {};

  const rect = anchorEl.getBoundingClientRect();
  const gap = 10;
  const edge = 10;
  const safeWidth = Math.min(width, Math.max(260, window.innerWidth - edge * 2));

  let left = edge;
  let top = edge;
  let transform = 'none';

  if (placement === 'top-right') {
    left = rect.right - safeWidth;
    top = rect.bottom + gap;
  }

  if (placement === 'bottom-left') {
    left = rect.left;
    top = rect.top - gap;
    transform = 'translateY(-100%)';
  }

  left = clamp(left, edge, window.innerWidth - safeWidth - edge);

  return {
    position: 'fixed',
    left: `${left}px`,
    top: `${top}px`,
    width: `${safeWidth}px`,
    transform
  };
};

const refreshDropdownPositions = () => {
  userDropdownStyle.value = computeDropdownStyle(avatarBtnRef.value, 'top-right', 320);
  settingsDropdownStyle.value = computeDropdownStyle(settingsBtnRef.value, 'bottom-left', 560);
};

const syncArchiveFromRoute = async () => {
  if (route.path.startsWith('/workshop/')) {
    const id = String(route.params.id || '');
    if (id) {
      taskStore.openArchive(id, { createIfMissing: true, title: `项目 ${id}` });
    }

    isModelDrawerOpen.value = false;
    isSidebarCollapsed.value = false;

    await router.replace({ path: '/', query: id ? { archive: id } : route.query });
    return;
  }

  if (route.path === '/' && route.query.archive) {
    const archiveId = String(route.query.archive);
    if (route.query.source === 'community') {
      const nextArchiveId = await taskStore.openArchiveFromCommunity(archiveId);
      if (nextArchiveId) {
        await router.replace({ path: '/', query: { archive: nextArchiveId } });
      }
      return;
    }

    taskStore.openArchive(archiveId, { createIfMissing: true, title: `项目 ${archiveId}` });
  }
};

onMounted(() => {
  void bootstrapStores();
  void bootstrapFirstVisitWelcome();
  nextTick(runShellIntroAnimation);

  colorSchemeMedia = window.matchMedia('(prefers-color-scheme: dark)');
  syncThemeByAuthState();

  colorSchemeChangeHandler = () => {
    if (!currentUser.value) {
      applyTheme(resolveSystemTheme());
    }
  };

  colorSchemeMedia.addEventListener('change', colorSchemeChangeHandler);
  window.addEventListener('ditapp-theme-change', handleThemePreferenceChange);
  window.addEventListener('ditapp-open-settings-modal', openSettingsModal);
  window.addEventListener('resize', refreshDropdownPositions);
  window.addEventListener('scroll', refreshDropdownPositions, true);
});

onBeforeUnmount(() => {
  if (colorSchemeMedia && colorSchemeChangeHandler) {
    colorSchemeMedia.removeEventListener('change', colorSchemeChangeHandler);
  }
  window.removeEventListener('ditapp-theme-change', handleThemePreferenceChange);
  window.removeEventListener('ditapp-open-settings-modal', openSettingsModal);
  window.removeEventListener('resize', refreshDropdownPositions);
  window.removeEventListener('scroll', refreshDropdownPositions, true);
});

watch(
  () => [showUserModal.value, showSettingsModal.value],
  async ([userOpen, settingsOpen]) => {
    if (!userOpen && !settingsOpen) return;
    await nextTick();
    refreshDropdownPositions();
  }
);

watch(
  () => route.fullPath,
  () => {
    void syncArchiveFromRoute();
  },
  { immediate: true }
);

watch(
  () => allowImageAttachment.value,
  (value) => {
    if (!value && imageFile.value) {
      clearImage();
    }
  }
);

watch(
  () => currentUser.value?.id || '',
  async (nextUserId, prevUserId) => {
    syncThemeByAuthState();

    if (nextUserId === prevUserId) return;

    taskStore.resetForAuthChange();
    if (nextUserId) {
      await taskStore.init();
    } else {
      isModelDrawerOpen.value = false;
    }
  }
);

nextTick(() => resizeComposer());
</script>

<style scoped>
/*
 * App.vue styles
 * ----------------
 * 说明（面向新手）：
 * - 这个 <style scoped> 块定义了应用整体布局与视觉主题的 CSS。
 * - 使用了 CSS 自定义属性（variables），例如 --left-panel-width, --color-bg-page 等，这些变量能在不同主题间被切换（见 :root[data-theme="dark"])。
 * - 主要区域说明：
 *   .app-shell: 根容器，定义左右面板宽度变量并控制整体高度和背景。
 *   .sidebar: 左侧导航栏，包含历史、操作按钮等。
 *   .content: 中央内容区，包含顶部固定的 topbar 和 router-view 渲染的页面。
 *   .model-drawer: 右侧抽屉（旧的模型查看区），通过 CSS 变量 --right-panel-width 控制宽度。
 * - 主题切换：变量通过 data-theme="dark" 的 :root 中定义不同配色。切换主题时只需更新 document.documentElement.setAttribute('data-theme', 'dark'|'light')。
 * - 可访问性提示：注意在修改颜色对比时保持充足对比度，避免影响弱视用户。
 */
.app-shell {
  --left-panel-width: 280px;
  --right-panel-width: 0px;
  --panel-gap: 16px;
  --drawer-top-offset: 84px;
  --drawer-edge-gap: 16px;
  --composer-width: min(900px, max(300px, calc(100vw - var(--left-panel-width) - var(--right-panel-width) - 26px)));
  height: 100vh;
  display: flex;
  overflow: hidden;
  background: radial-gradient(circle at 20% 10%, #f0f5ff 0%, #f6f8fb 35%, #f2f3f7 100%);
  color: #1b2430;
  transition: --right-panel-width 0.24s ease;
}

.app-shell.theme-dark {
  background: #0d1420;
  color: #e6edf6;
}

.sidebar {
  width: 280px;
  min-width: 280px;
  height: 100vh;
  border-right: 1px solid #d9e0ec;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  padding: 20px 14px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: 18px;
  transition: transform 0.65s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.65s ease, width 0.24s ease, min-width 0.24s ease, padding 0.24s ease;
}

.app-shell.theme-dark .sidebar {
  border-right-color: #2a2a2a;
  background: rgba(14, 14, 14, 0.92);
}

.app-shell.theme-dark .collapse-btn {
  border-color: #334055;
  background: #2a2a2a;
}

.sidebar.collapsed {
  width: 78px;
  min-width: 78px;
  padding-left: 10px;
  padding-right: 10px;
}

.sidebar-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: 14px;
}

.sidebar-nav {
  display: grid;
  gap: 8px;
}

.sidebar-action {
  text-decoration: none;
  color: #e8eef6;
  background: #202632;
  border: none;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 46px;
  box-sizing: border-box;
}

.sidebar-action:hover,
.sidebar-action.active {
  background: #2c3442;
}

.sidebar-action.primary {
  background: #eef3f8;
  color: #1f2a37;
}

.sidebar-action.primary:hover,
.sidebar-action.primary.active {
  background: #f5f8fc;
}

.sidebar-action.footer-action {
  width: 100%;
}

.action-icon {
  width: 18px;
  height: 18px;
  flex: 0 0 16px;
  color: currentColor;
}

.sidebar-top {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: 8px;
}

.brand {
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 6px 8px;
  text-align: left;
}

.brand-name {
  display: block;
  width: auto;
  height: 24px;
}

.brand-name.light {
  filter: brightness(0) saturate(100%);
}

.collapse-btn {
  border: none;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  height: 32px;
  min-width: 38px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.collapse-icon {
  width: 16px;
  height: 16px;
  transition: transform 0.2s ease;
}

.hamburger-icon {
  width: 22px;
  height: 22px;
  color: #475569;
  transition: color 0.2s ease;
}

.app-shell.theme-dark .hamburger-icon {
  color: #b3b3b3;
}

.app-shell.theme-dark .hamburger-icon:hover {
  color: #dbe4ef;
}

.sidebar.collapsed .collapse-icon {
  transform: rotate(180deg);
}

.sidebar-footer {
  margin-top: auto;
  flex: 0 0 auto;
  padding-top: 8px;
}

.menu {
  display: grid;
  gap: 8px;
}

.menu-item {
  text-decoration: none;
  color: #1f2937;
  background: #eef3fc;
  border: none;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-item.router-link-exact-active {
  background: #dbe9ff;
  color: #0f3f8a;
}

.app-shell.theme-dark .menu-item {
  color: #e6edf6;
  background: #19202b;
  border-color: #2f3b4f;
}

.app-shell.theme-dark .menu-item.router-link-exact-active {
  background: #243041;
  border-color: #4a5a73;
  color: #edf4fb;
}

.menu-icon {
  width: 18px;
  height: 18px;
}

.history-block {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.history-title {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  margin-bottom: 10px;
}

.history-item {
  display: flex;
  align-items: stretch;
  gap: 8px;
  margin-bottom: 8px;
  background: #f8fbff;
  border-radius: 12px;
  padding: 6px 8px;
}

.history-open {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  text-align: left;
  background: transparent;
  border: none;
  border-radius: 0;
  padding: 4px 2px;
  margin-bottom: 0;
  cursor: pointer;
}

.history-open-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.history-delete {
  margin-bottom: 0;
  width: 20px;
  flex: 0 0 20px;
  border: none;
  border-radius: 0;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: color 0.18s ease;
  font-size: 16px;
}

.history-delete:hover {
  color: #ef4444;
}


.app-shell.theme-dark .history-open {
  background: transparent;
}

.app-shell.theme-dark .history-delete {
  background: transparent;
}

.app-shell.theme-dark .history-item {
  background: #19202b;
}

.app-shell.theme-dark .history-delete-icon {
  background-color: currentColor;
}

.app-shell.theme-dark .history-main {
  color: #e6edf6;
}

.app-shell.theme-dark .history-meta,
.app-shell.theme-dark .history-title,
.app-shell.theme-dark .history-empty,
.app-shell.theme-dark .brand-subtitle {
  color: #aeb9c7;
}

.history-main {
  color: #1f2937;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-meta {
  font-size: 11px;
  color: #64748b;
  margin-top: 4px;
}

.content {
  flex: 1;
  min-width: 0;
  height: 100vh;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding-top: 68px;
  padding-bottom: 132px;
  box-sizing: border-box;
  padding-right: calc(var(--right-panel-width) + var(--panel-gap));
  transition: padding-right 0.24s ease;
}

.content.workspace-hidden {
  padding-bottom: 0;
}

.content.welcome-locked {
  overflow: hidden;
}

.topbar {
  position: fixed;
  top: 0;
  left: var(--left-panel-width);
  right: 0;
  width: auto;
  z-index: 45;
  height: 68px;
  padding: 0 16px;
  box-sizing: border-box;
  background: rgba(246, 248, 252, 0.88);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(210, 221, 237, 0.9);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
  flex-shrink: 0;
}

.title-block {
  flex: 0 0 auto;
  flex-shrink: 0;
  white-space: nowrap;
}

.title-sub {
  margin: 0;
  font-size: 11px;
  color: #5f6f85;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.title-main {
  margin: 2px 0 0;
  font-size: 18px;
  line-height: 1.15;
  color: #12253f;
  white-space: nowrap;
}

.app-shell.theme-dark .topbar {
  background: rgba(13, 18, 28, 0.88);
  border-bottom-color: rgba(48, 60, 78, 0.95);
}

.app-shell.theme-dark .widget-btn,
.app-shell.theme-dark .avatar-btn {
  background: rgba(20, 26, 36, 0.95);
  color: #e6edf6;
}

.app-shell.theme-dark .title-sub {
  color: #a3b2c6;
}

.app-shell.theme-dark .title-main {
  color: #eef4fb;
}

.top-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.widget-btn {
  width: 36px;
  height: 36px;
  border-radius: 11px;
  border: 1px solid #d6e0ef;
  background: rgba(255, 255, 255, 0.96);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s ease;
}

.widget-btn:hover {
  border-color: #c2d2ea;
  transform: translateY(-1px);
}

.widget-icon {
  width: 18px;
  height: 18px;
  color: currentColor;
}

.avatar-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #d6e0ef;
  background: rgba(255, 255, 255, 0.94);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s ease;
}

.avatar-btn:hover {
  border-color: #c2d2ea;
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

.avatar-icon {
  width: 22px;
  height: 22px;
  color: currentColor;
}

.model-drawer {
  position: absolute;
  top: var(--drawer-top-offset);
  right: var(--drawer-edge-gap);
  width: calc(var(--right-panel-width) - var(--drawer-edge-gap));
  background: rgba(255, 255, 255, 0.96);
  z-index: 24;
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.1);
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #d9e0ec;
  height: calc(100vh - var(--drawer-top-offset) - var(--drawer-edge-gap));
  transition: width 0.24s ease;
}

.app-shell.theme-dark .model-drawer {
  background: rgba(18, 24, 34, 0.97);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  border-color: #2f3b4f;
}

.app-shell.theme-dark .model-drawer-head {
  border-bottom-color: #2f3b4f;
  color: #e6edf6;
}

.app-shell.theme-dark .drawer-close {
  border-color: #334055;
  background: #1a2230;
  color: #e6edf6;
}

.model-drawer-head {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  border-bottom: 1px solid #d9e0ec;
  font-size: 13px;
  color: #1f2937;
}

.drawer-close {
  border: none;
  background: #f8fbff;
  color: #103f8a;
  border-radius: 8px;
  padding: 4px 10px;
  cursor: pointer;
}

.model-drawer-body {
  flex: 1;
  min-height: 0;
}

.bottom-composer-wrap {
  position: fixed;
  left: var(--left-panel-width, 280px);
  right: calc(var(--right-panel-width, 0px) + var(--panel-gap, 16px));
  bottom: 0;
  z-index: 40;
  padding: 12px 16px;
  box-sizing: border-box;
  pointer-events: none;
  transition: right 0.24s ease, opacity 0.24s ease;
  display: flex;
  justify-content: center;
}

.bottom-composer {
  width: min(100%, var(--composer-width));
  max-width: var(--composer-width);
  margin: 0 auto;
  border: 1px solid #d7e2f5;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.16);
  padding: 10px;
  pointer-events: auto;
}

.app-shell :deep(.home-wrap) {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  align-items: stretch;
}

.content.welcome-locked :deep(.home-wrap) {
  min-height: calc(100dvh - 132px);
  height: calc(100dvh - 132px);
  overflow: hidden;
}

.app-shell :deep(.welcome-card),
.app-shell :deep(.shortcut-row) {
  width: 100%;
  max-width: 900px;
}

.app-shell.theme-dark .bottom-composer {
  border-color: #2f3b4f;
  background: rgba(13, 18, 28, 0.94);
}

.app-shell.theme-dark .composer-input {
  color: #e6edf6;
}

.app-shell.theme-dark .composer-input::placeholder {
  color: #9dadc0;
}

.app-shell.theme-dark .tool-btn {
  border-color: #334055;
  background: #1a2230;
  color: #e6edf6;
}

.app-shell.theme-dark .tool-btn.ghost {
  background: #121824;
  color: #d6deea;
}

.app-shell.theme-dark .file-tag {
  color: #aeb9c7;
}

.app-shell.theme-dark .send-btn {
  background: #d9e2ee;
  color: #101826;
}

.composer-input {
  width: 100%;
  border: none;
  resize: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.5;
  max-height: 200px;
  outline: none;
  padding: 2px 4px;
  box-sizing: border-box;
}

.composer-tools {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

#app-image-upload {
  display: none;
}

.tool-btn {
  border: none;
  border-radius: 8px;
  background: #eff5ff;
  color: #13418d;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 12px;
}

.tool-btn.ghost {
  background: #f8fafc;
  color: #334155;
}

.file-tag {
  font-size: 12px;
  color: #64748b;
}

.send-btn {
  margin-left: auto;
  border: none;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #d9e2ee;
  color: #101826;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.send-icon {
  width: 18px;
  height: 18px;
  transition: color 0.2s;
  transform: translateX(-1px);
}

.app-shell.theme-dark .send-icon {
  color: #101826;
}

.dropdown-shell {
  position: fixed;
  inset: 0;
  z-index: 120;
  background: transparent;
}

.dropdown-panel {
  max-height: min(72vh, 680px);
  overflow: auto;
}

.dropdown-item {
  will-change: transform, opacity;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: var(--color-bg-overlay);
  z-index: 100;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 64px 14px 14px;
  box-sizing: border-box;
}

.user-modal {
  width: 320px;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  box-shadow: var(--shadow-elevated);
  padding: 0;
  transform-origin: top right;
  color: var(--color-text-primary);
  overflow: hidden;
}

.user-info-section {
  padding: 20px 16px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-avatar-icon {
  width: 40px;
  height: 40px;
  color: #ffffff;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  text-align: center;
}

.user-email {
  font-size: 13px;
  color: var(--color-text-muted);
  text-align: center;
  word-break: break-all;
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0;
}

.theme-section {
  padding: 14px 16px;
}

.theme-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
}

.theme-toggle {
  display: flex;
  gap: 8px;
}

.theme-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: var(--color-bg-soft);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-option:hover {
  background: var(--color-bg-hover);
  border-color: var(--color-primary);
}

.theme-option.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-primary-contrast);
}

.theme-icon {
  width: 20px;
  height: 20px;
}

.theme-option span {
  font-size: 12px;
  font-weight: 500;
}

.actions-section {
  padding: 8px 0;
}

.action-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  color: var(--color-text-primary);
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease;
}

.action-item:hover {
  background: var(--color-bg-hover);
}

.action-item.danger {
  color: var(--color-danger);
}

.action-item.danger:hover {
  background: color-mix(in srgb, var(--color-danger) 8%, transparent);
}

.action-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  color: currentColor;
}

.settings-modal {
  width: min(560px, calc(100vw - 28px));
}

.settings-group {
  display: grid;
  gap: 14px;
  margin-top: 12px;
}

.settings-row {
  display: grid;
  gap: 10px;
}

.settings-row > span {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 700;
}

.settings-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.settings-help {
  display: grid;
  gap: 8px;
  padding: 12px;
  border-radius: 12px;
  background: var(--color-bg-soft);
  border: 1px solid var(--color-border);
}

.settings-help p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.theme-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin: 10px 0;
  font-size: 13px;
}

.app-shell.theme-dark :deep(.home-wrap),
.app-shell.theme-dark :deep(.discovery-hall) {
  color: #e6edf6;
}

.app-shell.theme-dark :deep(.badge),
.app-shell.theme-dark :deep(.shortcut) {
  color: #e6edf6;
  background: #19202b;
  border-color: #2f3b4f;
}

.app-shell.theme-dark :deep(.desc),
.app-shell.theme-dark :deep(.shortcut.hint),
.app-shell.theme-dark :deep(.community-header p),
.app-shell.theme-dark :deep(.title-eyebrow) {
  color: #aeb9c7;
}

.app-shell.theme-dark :deep(.title-eyebrow) {
  background: #19202b;
}

.app-shell.theme-dark :deep(.asset-card) {
  background: #111823;
  border-color: #2f3b4f;
  color: #e6edf6;
}

.app-shell.theme-dark :deep(.tag),
.app-shell.theme-dark :deep(.remix-badge) {
  background: #1f2733;
  color: #e6edf6;
}

.user-modal h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.user-modal p {
  margin: 6px 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  word-break: break-all;
}

.user-modal strong {
  color: var(--color-text-primary);
}

.modal-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.modal-btn {
  flex: 1;
  border: 1px solid var(--color-border);
  background: var(--color-bg-soft);
  color: var(--color-text-primary);
  border-radius: 8px;
  padding: 7px 8px;
  cursor: pointer;
}

.modal-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-primary-contrast);
}

.modal-btn.danger {
  background: color-mix(in srgb, var(--color-danger) 14%, transparent);
  border-color: color-mix(in srgb, var(--color-danger) 34%, var(--color-border));
  color: var(--color-danger);
}


@media (max-width: 920px) {
  .app-shell {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
    overflow: visible;
    --left-panel-width: 0px !important;
    --right-panel-width: 0px !important;
    --composer-width: calc(100vw - 18px);
  }

  .sidebar {
    width: 100%;
    min-width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #d9e0ec;
    max-height: 48vh;
  }

  .content {
    height: auto;
    min-height: auto;
    overflow: visible;
    padding-top: 60px;
    padding-bottom: 150px;
  }

  .content.welcome-locked {
    overflow: hidden;
  }

  .content.welcome-locked :deep(.home-wrap) {
    min-height: calc(100dvh - 150px);
    height: calc(100dvh - 150px);
  }

  .topbar {
    left: 0;
    right: 0;
    height: 60px;
    padding: 0 10px;
  }

  .title-sub {
    font-size: 10px;
  }

  .title-main {
    font-size: 15px;
  }

  .model-drawer {
    width: 100%;
  }

  .bottom-composer {
    max-width: var(--composer-width);
    transform: none;
  }

  .app-shell :deep(.welcome-card),
  .app-shell :deep(.shortcut-row) {
    max-width: var(--composer-width);
    transform: none;
  }
}
</style>
