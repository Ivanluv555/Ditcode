<template>
  <section class="auth-page">
    <div class="auth-shell">
      <div class="promo">
        <p class="logo">ditapp</p>
        <h2>Create Account</h2>
        <p class="promo-desc">注册后即可在社区和工作台中保存你的数字资产历史。</p>
      </div>

      <div class="auth-card">
        <h3>注册账号</h3>
        <p class="subtitle">创建你的 ditapp 账户</p>

        <label>用户名</label>
        <input v-model.trim="username" type="text" placeholder="3-20 位，字母数字下划线" />

        <label>邮箱</label>
        <input v-model.trim="email" type="email" placeholder="请输入邮箱" />

        <label>密码</label>
        <input v-model="password" type="password" placeholder="8-32 位，至少字母+数字" />

        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

        <button class="primary" @click="submitRegister">注册</button>

        <RouterLink class="link" to="/login">已有账号？去登录</RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/packages/auth/store/useAuthStore';
import { isValidEmail, isValidPassword, isValidUsername } from '@/packages/auth/utils/authValidation';

const username = ref('');
const email = ref('');
const password = ref('');
const errorMsg = ref('');

const authStore = useAuthStore();
const router = useRouter();

const validate = () => {
  errorMsg.value = '';
  if (!isValidUsername(username.value)) {
    errorMsg.value = '用户名格式不正确：3-20 位，仅支持字母、数字、下划线';
    return false;
  }

  if (!isValidEmail(email.value)) {
    errorMsg.value = '邮箱格式不正确';
    return false;
  }

  if (!isValidPassword(password.value)) {
    errorMsg.value = '密码格式不正确：8-32 位，至少包含字母和数字';
    return false;
  }
  return true;
};

const submitRegister = async () => {
  if (!validate()) return;
  await authStore.init();
  const result = await authStore.register({
    username: username.value,
    email: email.value,
    password: password.value
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
  min-height: 100vh;
  background: var(--auth-page-bg);
  padding: 20px;
  box-sizing: border-box;
}

.auth-shell {
  min-height: calc(100vh - 40px);
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  max-width: 980px;
  margin: 0 auto;
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid var(--auth-card-border);
  box-shadow: var(--auth-card-shadow);
}

.promo {
  background: linear-gradient(160deg, color-mix(in srgb, var(--color-primary) 38%, transparent), color-mix(in srgb, var(--color-bg-page) 70%, transparent));
  padding: 48px 40px;
  color: var(--color-text-primary);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo {
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.08em;
  opacity: 0.9;
}

.promo h2 {
  margin: 10px 0;
  font-size: 40px;
}

.promo-desc {
  margin: 0;
  color: var(--color-text-muted);
  max-width: 380px;
}

.auth-card {
  background: var(--auth-card-bg);
  padding: 38px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

h3 {
  margin: 0;
  font-size: 28px;
  color: var(--auth-card-text);
}

.subtitle {
  margin: 6px 0 16px;
  color: var(--auth-card-subtle);
}

label {
  display: block;
  margin: 8px 0 6px;
  font-size: 14px;
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

.error {
  color: var(--color-danger);
  font-size: 13px;
  margin: 10px 0;
}

.primary {
  margin-top: 10px;
  width: 100%;
  border: none;
  border-radius: 10px;
  padding: 11px 14px;
  color: var(--auth-btn-text);
  background: var(--auth-btn-bg);
  font-weight: 700;
  cursor: pointer;
}

.link {
  display: inline-block;
  margin-top: 12px;
  text-decoration: none;
  color: var(--auth-link);
  font-size: 14px;
}

@media (max-width: 900px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .promo {
    padding: 26px 22px;
  }

  .promo h2 {
    font-size: 28px;
  }
}
</style>
