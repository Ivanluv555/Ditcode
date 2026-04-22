/*
 * useSceneStore.js
 * -----------------
 * 小型 store，用于保存与场景显示相关的 UI 状态（例如视场角 fov、选中的位置、遮罩可见性）
 * 该 store 不包含 WebGL 渲染逻辑，只是前端 UI 状态的集中位置，方便跨组件共享。
 */
import { defineStore } from 'pinia';
export const useSceneStore = defineStore('scene', {
  state: () => ({
    fov: 75,
    selectedPos: null,
    isMaskVisible: false
  }),
  actions: {
    setFov(fov) {
      this.fov = fov;
    },
    setSelectedPos(pos) {
      this.selectedPos = pos;
    },
    setMaskVisible(visible) {
      this.isMaskVisible = visible;
    }
  }
});
