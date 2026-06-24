<template>
  <div class="feedback-modal user-dropdown">
    <div class="modal-header">
      <button class="back-btn" @click="$emit('back')">
        <Icon icon="fa6-solid:arrow-left" class="back-icon" />
      </button>
      <h3 class="modal-title">意见反馈</h3>
    </div>

    <div class="modal-body">
      <form @submit.prevent="handleSubmit" class="feedback-form">
        <div class="form-group">
          <label class="form-label">反馈类型</label>
          <div class="feedback-type-grid">
            <button
              type="button"
              v-for="type in feedbackTypes"
              :key="type.value"
              class="type-option"
              :class="{ active: selectedType === type.value }"
              @click="selectedType = type.value"
            >
              <Icon :icon="type.icon" class="type-icon" />
              <span>{{ type.label }}</span>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">详细描述</label>
          <textarea
            v-model="feedbackContent"
            class="form-textarea"
            placeholder="请详细描述您的问题或建议..."
            rows="6"
            required
          ></textarea>
        </div>

        <div class="form-group">
          <label class="form-label">联系方式（可选）</label>
          <input
            v-model="contactInfo"
            type="text"
            class="form-input"
            placeholder="邮箱或手机号，方便我们回复您"
          />
        </div>

        <div class="form-actions">
          <button type="button" class="modal-btn" @click="$emit('back')">取消</button>
          <button type="submit" class="modal-btn primary" :disabled="!canSubmit">提交反馈</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Icon } from '@iconify/vue';

const emit = defineEmits(['back', 'submit']);

const feedbackTypes = [
  { value: 'bug', label: '问题反馈', icon: 'fa6-solid:bug' },
  { value: 'feature', label: '功能建议', icon: 'fa6-solid:lightbulb' },
  { value: 'improvement', label: '体验优化', icon: 'fa6-solid:wand-magic-sparkles' },
  { value: 'other', label: '其他', icon: 'fa6-solid:comment' }
];

const selectedType = ref('bug');
const feedbackContent = ref('');
const contactInfo = ref('');

const canSubmit = computed(() => {
  return selectedType.value && feedbackContent.value.trim().length > 0;
});

const handleSubmit = () => {
  if (!canSubmit.value) return;

  const feedback = {
    type: selectedType.value,
    content: feedbackContent.value.trim(),
    contact: contactInfo.value.trim(),
    timestamp: new Date().toISOString()
  };

  console.log('提交反馈:', feedback);

  selectedType.value = 'bug';
  feedbackContent.value = '';
  contactInfo.value = '';

  emit('submit', feedback);
};
</script>

<style scoped>
.feedback-modal {
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
}

.feedback-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.feedback-type-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.type-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: var(--color-bg-soft);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-option:hover {
  background: var(--color-bg-hover);
  border-color: var(--color-primary);
}

.type-option.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-primary-contrast);
}

.type-icon {
  width: 20px;
  height: 20px;
}

.type-option span {
  font-size: 12px;
  font-weight: 500;
}

.form-textarea,
.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-bg-soft);
  color: var(--color-text-primary);
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: all 0.2s ease;
}

.form-textarea:focus,
.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: var(--color-bg-card);
}

.form-textarea::placeholder,
.form-input::placeholder {
  color: var(--color-text-muted);
}

.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.modal-btn {
  flex: 1;
  border: 1px solid var(--color-border);
  background: var(--color-bg-soft);
  color: var(--color-text-primary);
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-btn:hover {
  background: var(--color-bg-hover);
}

.modal-btn.primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-primary-contrast);
}

.modal-btn.primary:hover {
  opacity: 0.9;
}

.modal-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
