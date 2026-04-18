<template>
  <div class="task-pod" :class="{ 'expanded': isExpanded }" @mouseenter="isExpanded = true" @mouseleave="isExpanded = false">
    <div class="pod-icon">
      <Icon v-if="tasks.length > 0" icon="solar:refresh-circle-outline" class="spinner" />
      <Icon v-else icon="solar:check-circle-bold" class="check" />
    </div>
    <div class="task-list" v-if="isExpanded">
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

const taskStore = useTaskStore();
const tasks = computed(() => taskStore.tasks.filter(t => t.status !== 'success'));

const isExpanded = ref(false);
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
