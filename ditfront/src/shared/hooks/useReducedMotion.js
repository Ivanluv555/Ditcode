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

