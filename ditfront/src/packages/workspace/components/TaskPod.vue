<!--
  TaskPod.vue
  ----------------
  功能说明：页面右上角的小浮动任务面板，展示当前正在运行或排队的生成任务。点击/悬停可以展开任务详情列表。

  给初学者的直观理解：
  - 该组件只负责从 store（useTaskStore）读取任务并展示进度；组件自身不启动任务。
  - previewMode 用于在只读演示场景下替换真实任务数据。

  结构与行为：
  - pod-icon：圆形图标，当有任务时显示旋转图标提示处理中；否则显示对号。
  - task-list：展开时显示任务条目（状态 + 进度）。

  可扩展点：
  - 可在任务项添加取消/查看按钮，或增加更详细的错误信息提示。
-->
<template>
  <div class="task-pod" :class="{ expanded }" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
    <div class="pod-icon">
      <Icon v-if="tasks.length > 0" icon="fa6-solid:arrows-rotate" class="spinner" />
      <Icon v-else icon="fa6-solid:circle-check" class="check" />
    </div>
    <div class="task-list" v-if="expanded">
      <div v-if="tasks.length === 0" class="empty">No active tasks</div>
      <div v-for="task in tasks" :key="task.id" class="task-item">
        <span class="status">{{ task.status }}</span>
        <span class="progress">{{ Math.round(task.progress) }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Icon } from '@iconify/vue';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';

const props = defineProps({
  previewMode: {
    type: Boolean,
    default: false
  },
  previewTasks: {
    type: Array,
    default: () => []
  },
  forceExpanded: {
    type: Boolean,
    default: false
  }
});

const taskStore = useTaskStore();
const tasks = computed(() =>
  props.previewMode ? props.previewTasks : taskStore.tasks.filter((task) => task.status !== 'success')
);

const isExpanded = ref(false);
const expanded = computed(() => props.forceExpanded || props.previewMode || isExpanded.value);

const handleMouseEnter = () => {
  if (props.forceExpanded || props.previewMode) return;
  isExpanded.value = true;
};

const handleMouseLeave = () => {
  if (props.forceExpanded || props.previewMode) return;
  isExpanded.value = false;
};
</script>

<style scoped>
.task-pod {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 48px;
  height: 48px;
  background: var(--color-bg-card);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  transition: all 0.3s;
  pointer-events: auto;
  overflow: hidden;
  color: var(--color-text-primary);
}
.task-pod.expanded {
  width: 250px;
  height: auto;
  min-height: 48px;
  border-radius: 12px;
  align-items: flex-start;
  flex-direction: column;
}
.pod-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.spinner {
  animation: rotate 2s linear infinite;
  width: 24px;
  height: 24px;
  color: var(--color-success);
}
@keyframes rotate { 100% { transform: rotate(360deg); } }
.check { color: var(--color-success); width: 22px; height: 22px; }
.task-list {
  padding: 12px; width: 100%; box-sizing: border-box;
}
.task-item {
  display: flex; justify-content: space-between;
  margin-bottom: 8px; font-size: 14px;
}
.empty { font-size: 12px; opacity: 0.6; text-align: center; }
</style>
