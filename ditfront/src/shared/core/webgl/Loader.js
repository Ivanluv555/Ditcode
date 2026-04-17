import * as THREE from 'three';
import { KTX2Loader } from 'three/examples/jsm/loaders/KTX2Loader.js';
let ktx2Loader = null;
export const initKTX2Loader = (renderer) => {
  if (!ktx2Loader) {
    ktx2Loader = new KTX2Loader()
      .setTranscoderPath('/basis/')
      .detectSupport(renderer);
  }
  return ktx2Loader;
};
export const loadEnvTexture = async (url, renderer) => {
  const loader = initKTX2Loader(renderer);
  const texture = await loader.loadAsync(url);
  texture.mapping = THREE.EquirectangularReflectionMapping;
  texture.generateMipmaps = false;
  texture.minFilter = THREE.LinearFilter;
  return texture;
};
