/*
 * useResponsive.js
 * -----------------
 * 简介：一个小型的响应式 Hook（组合式函数），用于检测窗口大小是否处于移动端阈值（<=768px）。
 *
 * 返回：{ isMobile }
 *  - isMobile 是一个 ref（响应式对象），可以在组件 template 中直接使用，例如：v-if="isMobile"
 *
 * 为什么使用 Hook：把与窗口尺寸相关的逻辑封装成 Hook，可以在多个组件间复用，避免重复代码。
 */
import { ref, onMounted, onBeforeUnmount } from 'vue';
export function useResponsive() {
  const isMobile = ref(window.innerWidth <= 768);
  const resize = () => {
    isMobile.value = window.innerWidth <= 768;
  };
  onMounted(() => window.addEventListener('resize', resize));
  onBeforeUnmount(() => window.removeEventListener('resize', resize));
  return { isMobile };
}
