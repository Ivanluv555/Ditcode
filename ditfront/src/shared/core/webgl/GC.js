export const disposeScene = (scene, renderer) => {
  scene.traverse((object) => {
    if (!object.isMesh) return;
    if (object.geometry) {
      object.geometry.dispose();
    }
    if (object.material) {
      if (object.material.isMaterial) {
        cleanMaterial(object.material);
      } else if (Array.isArray(object.material)) {
        for (const material of object.material) cleanMaterial(material);
      }
    }
  });
  renderer.dispose();
  renderer.forceContextLoss();
};
const cleanMaterial = (material) => {
  if (material.dispose) {
    material.dispose();
  }
  for (const key of Object.keys(material)) {
    const value = material[key];
    if (value && typeof value === 'object' && 'minFilter' in value) {
      value.dispose();
    }
  }
};
