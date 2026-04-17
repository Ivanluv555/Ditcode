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
