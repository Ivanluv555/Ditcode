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
