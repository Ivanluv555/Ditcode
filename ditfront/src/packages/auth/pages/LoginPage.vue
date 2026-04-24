<template>
  <section class="auth-page" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave">
    <canvas ref="canvasRef" class="bg-canvas"></canvas>

    <header class="topbar">
      <div class="brand">
        <span class="brand-square"></span>
        <span class="brand-text">用户登录</span>
      </div>
    </header>

    <div class="auth-card">
      <h3>登录</h3>
      <p class="subtitle">登录以轻松管理你的内容</p>

      <label>邮箱或用户名</label>
      <input v-model="account" type="text" placeholder="请输入邮箱或用户名" />

      <label>密码</label>
      <input v-model="password" type="password" placeholder="请输入密码" />

      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
      <button class="primary" @click="submitLogin">登录</button>

      <div class="divider"></div>
      <RouterLink class="link" to="/register">没有账号？去注册</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/packages/auth/store/useAuthStore';
import { isValidEmail, isValidPassword, isValidUsername } from '@/packages/auth/utils/authValidation';

const account = ref('');
const password = ref('');
const errorMsg = ref('');
const canvasRef = ref(null);

const authStore = useAuthStore();
const router = useRouter();

let gl = null;
let program = null;
let texture = null;
let mouseX = 0.5;
let mouseY = 0.5;
let animationId = null;
let startTime = Date.now();

const vertexShaderSource = `
  attribute vec2 a_position;
  varying vec2 v_texCoord;

  void main() {
    gl_Position = vec4(a_position, 0.0, 1.0);
    v_texCoord = a_position * 0.5 + 0.5;
  }
`;

const fragmentShaderSource = `
  precision mediump float;

  uniform sampler2D u_texture;
  uniform vec2 u_mouse;
  uniform vec2 u_resolution;
  uniform float u_time;

  varying vec2 v_texCoord;

  float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
  }

  void main() {
    vec2 uv = v_texCoord;
    uv.y = 1.0 - uv.y;

    vec4 texColor = texture2D(u_texture, uv);

    vec2 pixelPos = gl_FragCoord.xy;
    float dist = distance(pixelPos, u_mouse * u_resolution);

    float spotlightRadius = 200.0;
    float spotlight = smoothstep(spotlightRadius, spotlightRadius * 0.5, dist);

    // 粒子效果
    vec2 particleUV = pixelPos * 0.05 + u_time * 0.1;
    float particle = random(floor(particleUV));
    particle = step(0.98, particle) * (0.3 + 0.7 * random(floor(particleUV * 2.0)));

    // 粒子闪烁
    float flicker = sin(u_time * 3.0 + particle * 10.0) * 0.5 + 0.5;
    particle *= flicker;

    // 只在探照区域显示粒子
    particle *= spotlight * 0.4;

    float maskAlpha = 0.88;
    float alpha = mix(maskAlpha, 0.0, spotlight);

    vec3 darkColor = vec3(0.05, 0.05, 0.08);
    vec3 finalColor = mix(darkColor, texColor.rgb, 1.0 - alpha);

    // 添加粒子光效
    finalColor += vec3(particle * 0.8, particle * 0.9, particle);

    gl_FragColor = vec4(finalColor, 1.0);
  }
`;

const createShader = (gl, type, source) => {
  const shader = gl.createShader(type);
  gl.shaderSource(shader, source);
  gl.compileShader(shader);

  if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
    console.error('Shader compile error:', gl.getShaderInfoLog(shader));
    gl.deleteShader(shader);
    return null;
  }

  return shader;
};

const initWebGL = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;

  gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
  if (!gl) {
    console.error('WebGL not supported');
    return;
  }

  const vertexShader = createShader(gl, gl.VERTEX_SHADER, vertexShaderSource);
  const fragmentShader = createShader(gl, gl.FRAGMENT_SHADER, fragmentShaderSource);

  program = gl.createProgram();
  gl.attachShader(program, vertexShader);
  gl.attachShader(program, fragmentShader);
  gl.linkProgram(program);

  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
    console.error('Program link error:', gl.getProgramInfoLog(program));
    return;
  }

  const positionBuffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([
    -1, -1,
     1, -1,
    -1,  1,
     1,  1,
  ]), gl.STATIC_DRAW);

  const positionLocation = gl.getAttribLocation(program, 'a_position');
  gl.enableVertexAttribArray(positionLocation);
  gl.vertexAttribPointer(positionLocation, 2, gl.FLOAT, false, 0, 0);

  const image = new Image();
  image.onload = () => {
    texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);

    render();
  };
  image.src = '/assets/imgforlogin/login.png';
};

const resizeCanvas = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;

  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  if (gl) {
    gl.viewport(0, 0, canvas.width, canvas.height);
  }
};

const render = () => {
  if (!gl || !program || !texture) return;

  gl.clearColor(0, 0, 0, 1);
  gl.clear(gl.COLOR_BUFFER_BIT);

  gl.useProgram(program);

  const mouseLocation = gl.getUniformLocation(program, 'u_mouse');
  gl.uniform2f(mouseLocation, mouseX, mouseY);

  const resolutionLocation = gl.getUniformLocation(program, 'u_resolution');
  gl.uniform2f(resolutionLocation, canvasRef.value.width, canvasRef.value.height);

  const timeLocation = gl.getUniformLocation(program, 'u_time');
  gl.uniform1f(timeLocation, (Date.now() - startTime) / 1000.0);

  const textureLocation = gl.getUniformLocation(program, 'u_texture');
  gl.uniform1i(textureLocation, 0);

  gl.activeTexture(gl.TEXTURE0);
  gl.bindTexture(gl.TEXTURE_2D, texture);

  gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);

  animationId = requestAnimationFrame(render);
};

const handleMouseMove = (event) => {
  const canvas = canvasRef.value;
  if (!canvas) return;

  const rect = canvas.getBoundingClientRect();
  mouseX = (event.clientX - rect.left) / rect.width;
  mouseY = 1.0 - (event.clientY - rect.top) / rect.height;
};

const handleMouseLeave = () => {
  mouseX = 0.5;
  mouseY = 0.5;
};

onMounted(() => {
  resizeCanvas();
  initWebGL();
  window.addEventListener('resize', resizeCanvas);
});

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas);
  if (animationId) {
    cancelAnimationFrame(animationId);
  }
  if (gl && texture) {
    gl.deleteTexture(texture);
  }
  if (gl && program) {
    gl.deleteProgram(program);
  }
});

const validate = () => {
  errorMsg.value = '';
  if (!account.value) {
    errorMsg.value = '请输入邮箱或用户名';
    return false;
  }

  const accountOk = isValidEmail(account.value) || isValidUsername(account.value);
  if (!accountOk) {
    errorMsg.value = '账号格式不正确：请输入合法邮箱或 3-20 位用户名（字母数字下划线）';
    return false;
  }

  if (!isValidPassword(password.value)) {
    errorMsg.value = '密码格式不正确：8-32 位，至少包含字母和数字';
    return false;
  }

  return true;
};

const submitLogin = async () => {
  if (!validate()) return;
  await authStore.init();
  const result = await authStore.login({
    account: account.value,
    password: password.value,
  });
  if (!result.ok) {
    errorMsg.value = result.message;
    return;
  }
  router.push('/');
};
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: #000;
}

.bg-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.topbar {
  position: relative;
  z-index: 2;
  height: 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--auth-topbar-bg);
  border-bottom: 1px solid var(--auth-topbar-border);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.brand-square {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: var(--auth-brand-square);
  box-shadow: inset 0 0 0 2px var(--auth-brand-square-ring);
}

.brand-text {
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--auth-brand-text);
}

.auth-card {
  position: relative;
  z-index: 2;
  width: min(360px, calc(100vw - 30px));
  margin-top: 82px;
  margin-left: 34px;
  border-radius: 10px;
  border: 1px solid var(--auth-card-border);
  background: var(--auth-card-bg);
  box-shadow: var(--auth-card-shadow);
  padding: 22px;
  color: var(--auth-card-text);
}

h3 {
  margin: 0;
  font-size: 42px;
  line-height: 1.05;
  letter-spacing: -0.02em;
  color: var(--auth-card-text);
}

.subtitle {
  margin: 10px 0 16px;
  color: var(--auth-card-subtle);
}

.hint {
  margin: -6px 0 14px;
  font-size: 12px;
  color: var(--color-text-muted);
}

label {
  display: block;
  margin: 8px 0 6px;
  font-size: 13px;
  color: var(--auth-card-subtle);
}

input {
  width: 100%;
  box-sizing: border-box;
  border-radius: 10px;
  border: 1px solid var(--auth-input-border);
  padding: 10px 12px;
  background: var(--auth-input-bg);
  color: var(--auth-card-text);
}

input::placeholder {
  color: var(--auth-input-placeholder);
}

.error {
  color: var(--color-danger);
  font-size: 13px;
  margin: 10px 0;
}

.primary {
  margin-top: 12px;
  width: 100%;
  border: 1px solid var(--auth-btn-border);
  border-radius: 999px;
  padding: 11px 14px;
  color: var(--auth-btn-text);
  background: var(--auth-btn-bg);
  font-weight: 700;
  cursor: pointer;
}

.divider {
  margin: 12px 0 10px;
  color: var(--color-text-muted);
  text-align: center;
}

.link {
  display: block;
  text-align: center;
  text-decoration: none;
  color: var(--auth-link);
  font-size: 14px;
}

@media (max-width: 900px) {
  .topbar {
    padding: 0 14px;
  }

  .auth-card {
    margin: 70px auto 0;
  }

  h3 {
    font-size: 34px;
  }
}
</style>
