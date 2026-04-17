import * as THREE from 'three';
export const setupRaycaster = (engine, canvas) => {
  const raycaster = new THREE.Raycaster();
  const mouse = new THREE.Vector2();
  const handleClick = (event) => {
    const rect = canvas.getBoundingClientRect();
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
    raycaster.setFromCamera(mouse, engine.camera);
    if (engine.rootSphere) {
      const intersects = raycaster.intersectObject(engine.rootSphere);
      if (intersects.length > 0) {
        const uv = intersects[0].uv;
        if (uv) {
          showMockMask(engine);
        }
      }
    }
  };
  canvas.addEventListener('click', handleClick);

  return () => {
    canvas.removeEventListener('click', handleClick);
  };
};
const showMockMask = (engine) => {
  if (engine.maskSphere) {
    engine.scene.remove(engine.maskSphere);
    engine.maskSphere.geometry.dispose();
    if (Array.isArray(engine.maskSphere.material)) {
        engine.maskSphere.material.forEach(m => m.dispose());
    } else {
        engine.maskSphere.material.dispose();
    }
  }
  const maskGeo = new THREE.SphereGeometry(500, 60, 40);
  maskGeo.scale(-1, 1, 1);
  const maskMat = new THREE.MeshBasicMaterial({ 
    color: 0xff00ff, 
    transparent: true, 
    opacity: 0.5,
    depthWrite: false 
  });
  engine.maskSphere = new THREE.Mesh(maskGeo, maskMat);
  engine.maskSphere.scale.set(1.001, 1.001, 1.001);
  engine.scene.add(engine.maskSphere);
  engine.needsUpdate = true;
};
