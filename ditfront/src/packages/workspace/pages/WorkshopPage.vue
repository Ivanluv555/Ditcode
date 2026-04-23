<!--
  WorkshopPage.vue
  -------------------
  功能：工作区主页面，采用“聊天气泡”样式展示交互记录（用户提示在右侧气泡，后端生成的图片在左侧气泡）。

  对非前端读者说明：
  - messages 列表由 store 提供（taskStore.currentArchiveMessages），页面只负责遍历并渲染这些消息。
  - 左侧（assistant）气泡可包含图片和“在查看器中打开”按钮；右侧（user）气泡只显示用户输入文本。

  UI 设计要点：
  - 图片气泡宽度限制为主面板的一半，避免占满全文区域，阅读体验更像聊天应用（例如 Gemini）。
  - 任务完成后，左侧助手气泡直接展示平面全景图预览，用户点击图片才会跳转到 /viewer 查看器页面。

  可扩展：可为图片气泡添加下载、分享或放大预览等操作。
-->
<template>
  <div class="workshop-container">
    <div class="messages-wrap">
      <div class="messages-list">
        <div v-for="msg in messages" :key="msg.id" :class="['message-row', msg.role === 'user' ? 'align-right' : 'align-left']">
          <!-- assistant (left) shows image bubble if available -->
          <div v-if="msg.role !== 'user'" class="bubble assistant">
            <div v-if="msg.imagePreview" class="image-wrap" @click="openViewer(msg.imagePreview)">
              <img :src="msg.imagePreview" alt="平面全景图预览" />
              <div class="preview-badge">点击查看全景</div>
            </div>
            <div v-else class="text-content">{{ msg.text }}</div>
          </div>

          <!-- user (right) shows text bubble -->
          <div v-if="msg.role === 'user'" class="bubble user">
            <div class="text-content">{{ msg.text }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="ui-layer">
      <TaskPod />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import TaskPod from '@/packages/workspace/components/TaskPod.vue';

const router = useRouter();
const taskStore = useTaskStore();
const messages = computed(() => taskStore.currentArchiveMessages || []);

const openViewer = (imageUrl) => {
  if (!imageUrl) return;
  router.push({ path: '/viewer', query: { image: imageUrl } });
};
</script>

/*
 * WorkshopPage styles
 * --------------------
 * 该样式定义消息流页（聊天式布局）：
 * - .workshop-container: 整体容器，使用 padding 保持内容与侧边栏间距。
 * - .messages-list: 垂直堆叠的消息列，每条消息通过 .message-row 左右对齐展示。
 * - .bubble: 气泡样式，限制 max-width 为 50%（使图片气泡不超过主面板一半宽度），并设置不同背景区分用户/助手。
 * - .ui-layer: 叠加层用于放置交互面板（TaskPod），通过 pointer-events 控制可点击性。
 *
 * 可维护建议：如需支持更多消息类型（视频、文件），保持消息容器结构并为新类型增加单独类名与样式。
 */
<style scoped>
.workshop-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;
  background: var(--color-bg-page);
  padding: 24px 32px;
  box-sizing: border-box;
}
.messages-wrap {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
}
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.message-row {
  display: flex;
  align-items: flex-end;
}
.message-row.align-right { justify-content: flex-end; }
.message-row.align-left { justify-content: flex-start; }
.bubble {
  max-width: 50%;
  box-sizing: border-box;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f3f7fb;
  color: #102030;
}
.bubble.user {
  background: #2b6ef6;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.bubble.assistant {
  background: rgba(255,255,255,0.9);
  color: #102030;
}
.text-content { white-space: pre-wrap; word-break: break-word; }
.image-wrap {
  position: relative;
  cursor: pointer;
}
.image-wrap img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 8px;
}
.preview-badge {
  position: absolute;
  left: 12px;
  bottom: 12px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  pointer-events: none;
}
.ui-layer { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 10; pointer-events: none; }
.ui-layer > * { pointer-events: auto; }
</style>
