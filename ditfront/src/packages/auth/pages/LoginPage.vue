<template>
  <section class="auth-page">
    <div class="bg-layer bg-grid"></div>
    <div class="bg-layer bg-orb bg-orb-1"></div>
    <div class="bg-layer bg-orb bg-orb-2"></div>
    <div class="bg-layer bg-orb bg-orb-3"></div>
    <div class="bg-layer bg-vignette"></div>

    <header class="topbar">
      <div class="brand">
        <span class="brand-square"></span>
        <span class="brand-text">ditapp</span>
      </div>
    </header>

    <div class="auth-card">
      <h3>ditapp 账号</h3>
      <p class="subtitle">登录后可管理你的内容与偏好设置</p>
      <p class="hint">默认账号：demo_user / demo123456（mock后端）</p>

      <label>邮箱或用户名</label>
      <input v-model="account" type="text" placeholder="请输入邮箱或用户名" />

      <label>密码</label>
      <input v-model="password" type="password" placeholder="请输入密码" />

      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
      <button class="primary" @click="submitLogin">登录</button>

      <div class="divider">或</div>
      <RouterLink class="link" to="/register">没有账号？去注册</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/packages/auth/store/useAuthStore';
import { isValidEmail, isValidPassword, isValidUsername } from '@/packages/auth/utils/authValidation';

const account = ref('');
const password = ref('');
const errorMsg = ref('');

const authStore = useAuthStore();
const router = useRouter();

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
  background: var(--auth-page-bg);
}

.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-grid {
  opacity: 0.18;
  background-image:
    linear-gradient(color-mix(in srgb, var(--color-text-primary) 9%, transparent) 1px, transparent 1px),
    linear-gradient(90deg, color-mix(in srgb, var(--color-text-primary) 9%, transparent) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: radial-gradient(circle at center, color-mix(in srgb, var(--color-bg-page) 88%, black) 30%, transparent 80%);
  animation: gridDrift 36s linear infinite;
}

.bg-orb {
  width: 42vw;
  height: 42vw;
  border-radius: 50%;
  opacity: 0.72;
  will-change: transform, opacity;
}

.bg-orb-1 {
  left: -8vw;
  top: -10vh;
  background: radial-gradient(circle, color-mix(in srgb, var(--color-primary) 24%, transparent), transparent 68%);
  animation: floatOne 24s ease-in-out infinite;
}

.bg-orb-2 {
  right: -10vw;
  top: 8vh;
  background: radial-gradient(circle, color-mix(in srgb, var(--color-primary) 16%, transparent), transparent 68%);
  animation: floatTwo 30s ease-in-out infinite;
}

.bg-orb-3 {
  left: 28vw;
  bottom: -18vh;
  background: radial-gradient(circle, color-mix(in srgb, var(--color-primary) 12%, transparent), transparent 65%);
  animation: floatThree 34s ease-in-out infinite;
}

.bg-vignette {
  background: radial-gradient(circle at center, transparent 28%, color-mix(in srgb, var(--color-bg-page) 65%, black) 100%);
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

@keyframes gridDrift {
  from {
    transform: translate3d(0, 0, 0);
  }

  to {
    transform: translate3d(-56px, -56px, 0);
  }
}

@keyframes floatOne {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(6vw, 4vh, 0) scale(1.08);
  }
}

@keyframes floatTwo {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(-5vw, 5vh, 0) scale(1.12);
  }
}

@keyframes floatThree {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(3vw, -3vh, 0) scale(1.06);
  }
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
