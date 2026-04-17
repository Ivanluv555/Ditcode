<template>
  <div />
</template>

<script setup>
import { onMounted } from 'vue';

onMounted(() => {
  window.dispatchEvent(new CustomEvent('ditapp-open-settings-modal'));
});
</script>
