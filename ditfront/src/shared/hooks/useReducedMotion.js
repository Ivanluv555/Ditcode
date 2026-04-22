/*
 * useReducedMotion.js
 * -------------------
 * 辅助 Hook：检测系统或用户是否开启了“减少动画/运动”的偏好（prefers-reduced-motion），
 * 以便在执行动画（例如 gsap 或 CSS 动画）时尊重用户设置，提供更好的可访问性体验。
 *
 * 返回值：prefersReducedMotion（ref 布尔）
 */
import { onBeforeUnmount, onMounted, ref } from 'vue';

export function useReducedMotion() {
  const prefersReducedMotion = ref(false);
  let mediaQuery = null;
  let onChange = null;

  onMounted(() => {
    mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    prefersReducedMotion.value = mediaQuery.matches;

    onChange = (event) => {
      prefersReducedMotion.value = event.matches;
    };

    mediaQuery.addEventListener('change', onChange);
  });

  onBeforeUnmount(() => {
    if (mediaQuery && onChange) {
      mediaQuery.removeEventListener('change', onChange);
    }
  });

  return prefersReducedMotion;
}

