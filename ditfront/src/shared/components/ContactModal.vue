<template>
  <div class="contact-modal user-dropdown">
    <div class="modal-header">
      <button class="back-btn" @click="$emit('back')">
        <Icon icon="fa6-solid:arrow-left" class="back-icon" />
      </button>
      <h3 class="modal-title">联系我们</h3>
    </div>

    <div class="modal-body">
      <div class="contact-intro">
        <p>我们很高兴为您提供帮助！您可以通过以下方式联系我们：</p>
      </div>

      <div class="contact-methods">
        <div class="contact-item" v-for="method in contactMethods" :key="method.type">
          <div class="contact-icon-wrapper">
            <Icon :icon="method.icon" class="contact-icon" />
          </div>
          <div class="contact-info">
            <div class="contact-label">{{ method.label }}</div>
            <div class="contact-value">{{ method.value }}</div>
            <div class="contact-desc" v-if="method.desc">{{ method.desc }}</div>
          </div>
          <button
            v-if="method.action"
            class="contact-action-btn"
            @click="handleAction(method.type, method.value)"
          >
            <Icon :icon="method.actionIcon" class="action-icon" />
          </button>
        </div>
      </div>

      <div class="contact-footer">
        <div class="working-hours">
          <Icon icon="fa6-solid:clock" class="footer-icon" />
          <span>工作时间：周一至周五 9:00-18:00</span>
        </div>
        <div class="response-time">
          <Icon icon="fa6-solid:bolt" class="footer-icon" />
          <span>我们会在24小时内回复您的消息</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue';

const emit = defineEmits(['back']);

const contactMethods = [
  {
    type: 'email',
    label: '电子邮件',
    value: 'support@ditcode.com',
    desc: '适合详细问题咨询',
    icon: 'fa6-solid:envelope',
    action: true,
    actionIcon: 'fa6-solid:copy'
  },
  {
    type: 'wechat',
    label: '微信客服',
    value: 'ditcode_support',
    desc: '扫码添加客服微信',
    icon: 'fa6-brands:weixin',
    action: true,
    actionIcon: 'fa6-solid:qrcode'
  },
  {
    type: 'qq',
    label: 'QQ群',
    value: '123456789',
    desc: '加入用户交流群',
    icon: 'fa6-brands:qq',
    action: true,
    actionIcon: 'fa6-solid:copy'
  },
  {
    type: 'phone',
    label: '客服热线',
    value: '400-123-4567',
    desc: '工作时间内可拨打',
    icon: 'fa6-solid:phone',
    action: true,
    actionIcon: 'fa6-solid:copy'
  }
];

const handleAction = (type, value) => {
  if (type === 'wechat') {
    console.log('显示微信二维码');
    alert('微信号：' + value + '\n\n请添加客服微信获取帮助');
    return;
  }

  navigator.clipboard.writeText(value).then(() => {
    console.log('已复制到剪贴板:', value);
    alert('已复制到剪贴板：' + value);
  }).catch(err => {
    console.error('复制失败:', err);
    alert('复制失败，请手动复制：' + value);
  });
};
</script>

<style scoped>
.contact-modal {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.back-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: var(--color-bg-soft);
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: var(--color-bg-hover);
  color: var(--color-text-primary);
}

.back-icon {
  width: 16px;
  height: 16px;
}

.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.contact-intro {
  padding: 12px;
  border-radius: 10px;
  background: var(--color-bg-soft);
  border: 1px solid var(--color-border);
}

.contact-intro p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-bg-card);
  transition: all 0.2s ease;
}

.contact-item:hover {
  background: var(--color-bg-hover);
  border-color: var(--color-primary);
}

.contact-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.contact-icon {
  width: 22px;
  height: 22px;
  color: #ffffff;
}

.contact-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.contact-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary);
  word-break: break-all;
}

.contact-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

.contact-action-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-bg-soft);
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.contact-action-btn:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-primary-contrast);
}

.action-icon {
  width: 16px;
  height: 16px;
}

.contact-footer {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: 10px;
  background: var(--color-bg-soft);
  border: 1px solid var(--color-border);
  margin-top: auto;
}

.working-hours,
.response-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.footer-icon {
  width: 14px;
  height: 14px;
  color: var(--color-primary);
  flex-shrink: 0;
}
</style>
