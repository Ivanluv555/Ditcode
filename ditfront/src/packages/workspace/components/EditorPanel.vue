<template>
  <div class="editor-panel" :class="{ 'is-mobile': isMobile }">
    <div class="header">
      <h3>{{ previewMode ? '建模预览' : '编辑模式' }}</h3>
      <button v-if="!previewMode" @click="generateNew" class="btn primary">生成</button>
      <span v-else class="preview-pill">Live</span>
    </div>
    <div class="content">
      <label>Prompt:</label>
      <textarea v-model="promptText" rows="3" placeholder="描述你希望在此区域生成的物体..." :readonly="previewMode" />

      <label v-if="allowImageAttachment" class="upload-label" for="editor-image-upload">参考图（可选）</label>
      <div v-if="allowImageAttachment" class="upload-row">
        <label class="btn upload" for="editor-image-upload">上传图片</label>
        <input id="editor-image-upload" type="file" accept="image/*" @change="onPickFile" />
        <button v-if="imageFile" @click="clearImage" class="btn ghost">移除</button>
      </div>
      <div v-if="allowImageAttachment && imageFile" class="filename">{{ imageFile.name }}</div>
      <div v-if="resolvedImagePreview" class="preview">
        <img :src="resolvedImagePreview" alt="reference" />
      </div>

      <div v-if="previewMode || taskStore.tasks.some((t) => ['queued','inferencing','compositing'].includes(t.status))" class="status-msg">
        {{ previewMode ? '引擎正在处理当前建模任务...' : '正在处理任务... 请查看左上角悬浮舱。' }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useTaskStore } from '@/packages/workspace/store/useTaskStore';
import { fileToDataUrl } from '@/shared/utils/fileToDataUrl';

const props = defineProps({
  previewMode: {
    type: Boolean,
    default: false
  },
  previewPrompt: {
    type: String,
    default: ''
  },
  previewImage: {
    type: String,
    default: ''
  }
});

const isMobile = window.innerWidth <= 768;
const promptText = ref(props.previewPrompt);
const imageFile = ref(null);
const imagePreview = ref('');
const taskStore = useTaskStore();
const allowImageAttachment = computed(() => !props.previewMode && taskStore.currentArchiveMessages.length === 0);
const resolvedImagePreview = computed(() => (props.previewMode ? props.previewImage : imagePreview.value));

const onPickFile = (event) => {
  if (props.previewMode) return;
  if (!allowImageAttachment.value) return;

  const file = event.target.files && event.target.files[0];
  if (!file) return;
  if (imagePreview.value.startsWith('blob:')) URL.revokeObjectURL(imagePreview.value);
  imageFile.value = file;
  imagePreview.value = URL.createObjectURL(file);
};

const clearImage = () => {
  if (imagePreview.value.startsWith('blob:')) URL.revokeObjectURL(imagePreview.value);
  imageFile.value = null;
  imagePreview.value = '';
};

const generateNew = async () => {
  if (props.previewMode) return;
  const referenceFile = allowImageAttachment.value ? imageFile.value : null;
  let referenceImage = '';

  try {
    referenceImage = referenceFile ? await fileToDataUrl(referenceFile) : '';
  } catch (error) {
    console.warn('Failed to read editor image:', error);
  }

  const taskId = 'task_' + Date.now();
  taskStore.addTask({
    id: taskId,
    status: 'inferencing',
    progress: 0,
    prompt: promptText.value.trim(),
    imageName: referenceFile ? referenceFile.name : '',
    imagePreview: referenceImage,
    createdAt: Date.now()
  });

  // mock process
  let progress = 0;
  const interval = setInterval(() => {
    progress += 10;
    if (progress >= 100) {
      clearInterval(interval);
      taskStore.updateTask(taskId, { status: 'success', progress: 100 });
      taskStore.addAssetRecord({
        id: taskId,
        prompt: promptText.value.trim(),
        imagePreview: referenceImage,
        createdAt: Date.now()
      });
      // Notify WebGL cross fade via global event or store
      const event = new CustomEvent('cross-fade-trigger', {
        detail: { id: taskId, prompt: promptText.value.trim(), hasImage: !!referenceFile }
      });
      window.dispatchEvent(event);

      promptText.value = '';
      clearImage();
    } else {
      taskStore.updateTask(taskId, { progress });
    }
  }, 1000);
};

onBeforeUnmount(() => {
  if (imagePreview.value.startsWith('blob:')) {
    URL.revokeObjectURL(imagePreview.value);
  }
});

watch(
  () => props.previewPrompt,
  (value) => {
    if (props.previewMode) {
      promptText.value = value;
    }
  }
);

watch(
  () => allowImageAttachment.value,
  (value) => {
    if (!value && imageFile.value) {
      clearImage();
    }
  }
);
</script>

<style scoped>
.editor-panel {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  width: 300px;
  background: var(--color-bg-card);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  color: var(--color-text-primary);
  pointer-events: auto;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
}

.header {
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

textarea {
  background: var(--color-bg-soft);
  border: 1px solid var(--color-border);
  color: var(--color-text-primary);
  padding: 8px;
  border-radius: 4px;
  resize: none;
}

.upload-label {
  font-size: 13px;
  opacity: 0.9;
}

#editor-image-upload {
  display: none;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn.upload,
.btn.ghost {
  background: var(--color-bg-soft-strong);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.filename {
  font-size: 12px;
  color: var(--color-text-muted);
}

.preview img {
  max-width: 100%;
  max-height: 132px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.btn.primary {
  background: var(--color-success);
  color: var(--color-text-inverse);
}

.preview-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-soft);
  color: var(--color-text-primary);
  font-size: 12px;
  font-weight: 600;
}

.status-msg {
  color: var(--color-warning);
  font-size: 12px;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .editor-panel {
    right: 0;
    top: auto;
    bottom: 0;
    transform: none;
    width: 100%;
    height: 33vh;
    border-radius: 16px 16px 0 0;
    border-bottom: none;
  }
}
</style>
