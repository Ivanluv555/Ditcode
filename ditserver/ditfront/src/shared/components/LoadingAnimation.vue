<!--
  LoadingAnimation.vue
  --------------------
  功能：使用 Lottie 动画展示加载状态

  使用 lottie-web 库加载 JSON 格式的动画文件
  支持主题切换（暗色模式为白色，亮色模式为黑色）
-->
<template>
  <div ref="lottieContainer" class="lottie-loading" :class="{ 'dark-theme': isDark }"></div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import lottie from 'lottie-web';
import loadingAnimationData from '/assets/animations/Loading.json';

const props = defineProps({
  size: {
    type: Number,
    default: 288 // 48px * 6 = 288px
  }
});

const lottieContainer = ref(null);
const isDark = ref(false);
let animationInstance = null;

// 检测主题
const detectTheme = () => {
  const htmlElement = document.documentElement;
  isDark.value = htmlElement.classList.contains('dark') ||
                 htmlElement.getAttribute('data-theme') === 'dark' ||
                 window.matchMedia('(prefers-color-scheme: dark)').matches;
};

onMounted(() => {
  detectTheme();

  if (!lottieContainer.value) return;

  animationInstance = lottie.loadAnimation({
    container: lottieContainer.value,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    animationData: loadingAnimationData
  });

  // 监听主题变化
  const observer = new MutationObserver(detectTheme);
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class', 'data-theme']
  });

  // 监听系统主题变化
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  mediaQuery.addEventListener('change', detectTheme);

  // 清理函数
  onBeforeUnmount(() => {
    observer.disconnect();
    mediaQuery.removeEventListener('change', detectTheme);
  });
});

onBeforeUnmount(() => {
  if (animationInstance) {
    animationInstance.destroy();
    animationInstance = null;
  }
});
</script>

<style scoped>
.lottie-loading {
  width: v-bind('props.size + "px"');
  height: v-bind('props.size + "px"');
  display: inline-block;
  transition: filter 0.3s ease;
}

/* 亮色模式：黑色 */
.lottie-loading {
  filter: brightness(0);
}

/* 暗色模式：白色 */
.lottie-loading.dark-theme {
  filter: brightness(0) invert(1);
}
</style>
