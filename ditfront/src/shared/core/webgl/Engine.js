import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import * as TWEEN from '@tweenjs/tween.js';
import { setupRaycaster } from './Raycast';
export class WebGLEngine {
  constructor(canvas) {
    this.needsUpdate = true;
    this.isMobile = window.innerWidth <= 768;
    this.animationFrameId = null;
    this.rootSphere = null;
    this.maskSphere = null;
    this.disposeRaycaster = null;
    this.onControlsChange = () => {
      this.needsUpdate = true;
    };
    this.onResize = this.onWindowResize.bind(this);
    this.onCrossFadeTrigger = this.onCrossFade.bind(this);
    this.renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: !this.isMobile,
      alpha: true,
      powerPreference: 'high-performance'
    });
    const maxDpr = this.isMobile ? 1.5 : 2;
    const dpr = Math.min(window.devicePixelRatio, maxDpr);
    this.renderer.setPixelRatio(dpr);
    this.renderer.setSize(window.innerWidth, window.innerHeight);
    this.scene = new THREE.Scene();
    this.camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
    this.camera.position.set(0, 0, 0.1);
    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.enablePan = false;
    this.controls.enableZoom = false;
    this.controls.addEventListener('change', this.onControlsChange);
    window.addEventListener('resize', this.onResize);
    window.addEventListener('cross-fade-trigger', this.onCrossFadeTrigger);
    this.initScene();
    this.disposeRaycaster = setupRaycaster(this, canvas);
  }
  async initScene() {
    const geometry = new THREE.SphereGeometry(500, 60, 40);
    geometry.scale(-1, 1, 1);
    const material = new THREE.MeshBasicMaterial({ color: 0x333333, wireframe: true });
    this.rootSphere = new THREE.Mesh(geometry, material);
    this.scene.add(this.rootSphere);
    this.needsUpdate = true;
    try {
      // Texture loading placeholder
    } catch(e) {
      console.warn("Could not load ktx2 map in mock", e);
    }
  }
  async onCrossFade(event) {
    if (!this.rootSphere) return;
    if (this.maskSphere) {
      this.scene.remove(this.maskSphere);
      this.maskSphere.geometry?.dispose();
      if (Array.isArray(this.maskSphere.material)) {
        this.maskSphere.material.forEach((material) => material?.dispose && material.dispose());
      } else if (this.maskSphere.material) {
        this.maskSphere.material.dispose();
      }
      this.maskSphere = null;
    }

    const geometry = new THREE.SphereGeometry(500, 60, 40);
    geometry.scale(-1, 1, 1);
    const textureUrl = event?.detail?.imagePreview || '';
    let material = null;

    if (textureUrl) {
      // helper: convert dataURL to Blob
      const dataUrlToBlob = (dataUrl) => {
        const parts = dataUrl.split(',');
        const header = parts[0] || '';
        const matches = header.match(/:(.*?);/);
        const mime = matches ? matches[1] : 'image/png';
        const bstr = atob(parts[1] || '');
        let n = bstr.length;
        const u8arr = new Uint8Array(n);
        while (n--) u8arr[n] = bstr.charCodeAt(n);
        return new Blob([u8arr], { type: mime });
      };

      try {
        console.log('[Engine] onCrossFade loading texture:', textureUrl?.slice?.(0,80) ? textureUrl.slice(0,80) + '...' : textureUrl);
        // Try using createImageBitmap path first to offload decode from main thread when possible
        let texture = null;
        if (typeof createImageBitmap === 'function') {
          try {
            let blob = null;
            if (textureUrl.startsWith('data:')) {
              blob = dataUrlToBlob(textureUrl);
            } else {
              const resp = await fetch(textureUrl, { mode: 'cors' });
              if (!resp.ok) throw new Error(`fetch failed: ${resp.status}`);
              blob = await resp.blob();
            }
            const imageBitmap = await createImageBitmap(blob);
            texture = new THREE.Texture(imageBitmap);
            texture.needsUpdate = true;
          } catch (e) {
            console.warn('[Engine] createImageBitmap path failed, falling back to TextureLoader:', e);
            texture = await new THREE.TextureLoader().loadAsync(textureUrl);
          }
        } else {
          texture = await new THREE.TextureLoader().loadAsync(textureUrl);
        }

        if (texture) {
          // Ensure correct color space for sRGB images
          if ('colorSpace' in texture) {
            texture.colorSpace = THREE.SRGBColorSpace;
          }
          // Fix orientation and reduce GPU work for large single panorama
          texture.flipY = false;
          texture.minFilter = THREE.LinearFilter;
          texture.generateMipmaps = false;
          texture.needsUpdate = true;

          console.log('[Engine] texture loaded', texture.image?.width, 'x', texture.image?.height, 'rendererMem:', this.renderer?.info?.memory);
          material = new THREE.MeshBasicMaterial({
            map: texture,
            transparent: true,
            opacity: 0,
            depthWrite: false,
            depthTest: false,
            side: THREE.BackSide
          });
        }
      } catch (error) {
        console.warn('Failed to load panorama texture:', error);
      }
    }

    if (!material) {
      material = new THREE.MeshBasicMaterial({
        color: 0x00ffaa,
        transparent: true,
        opacity: 0,
        depthWrite: false,
        depthTest: false,
        side: THREE.BackSide
      });
    }

    const newSphere = new THREE.Mesh(geometry, material);
    newSphere.renderOrder = 1000;
    // Prevent frustum culling for large inward-facing sphere
    newSphere.frustumCulled = false;
    this.scene.add(newSphere);
    new TWEEN.Tween(material)
      .to({ opacity: 1 }, 1500)
      .easing(TWEEN.Easing.Quadratic.Out)
      .onUpdate(() => { this.needsUpdate = true; })
      .onComplete(() => {
        if (this.rootSphere) {
          if (this.rootSphere.material?.map) {
            this.rootSphere.material.map.dispose();
          }
          this.rootSphere.material?.dispose();
          this.rootSphere.geometry?.dispose();
          this.scene.remove(this.rootSphere);
        }
        this.rootSphere = newSphere;
      })
      .start();
  }
  onWindowResize() {
    this.camera.aspect = window.innerWidth / window.innerHeight;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(window.innerWidth, window.innerHeight);
    this.needsUpdate = true;
  }
  start() {
    const loop = (time) => {
      this.animationFrameId = requestAnimationFrame(loop);
      TWEEN.update(time);
      this.controls.update();
      if (this.needsUpdate) {
        this.renderer.render(this.scene, this.camera);
        this.needsUpdate = false;
      }
    };
    loop(performance.now());
  }
  stop() {
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
    this.controls.removeEventListener('change', this.onControlsChange);
    window.removeEventListener('resize', this.onResize);
    window.removeEventListener('cross-fade-trigger', this.onCrossFadeTrigger);
    if (typeof this.disposeRaycaster === 'function') {
      this.disposeRaycaster();
      this.disposeRaycaster = null;
    }
  }
}
