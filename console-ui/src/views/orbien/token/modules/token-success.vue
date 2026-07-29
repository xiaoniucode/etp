<template>
  <ElDialog
    :model-value="visible"
    @update:model-value="handleClose"
    :title="$t('orbien.token.successTitle')"
    width="500px"
    align-center
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="token-success-content">
      <p class="success-desc">{{ $t('orbien.token.successDesc') }}</p>
      <div class="token-display">
        <ElInput :model-value="token" size="large" readonly class="token-input" placeholder="Token">
          <template #suffix>
            <button class="copy-btn" @click="handleCopyOnly">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect width="14" height="14" x="8" y="8" rx="2" ry="2" />
                <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" />
              </svg>
            </button>
          </template>
        </ElInput>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <ElButton type="primary" @click="handleCopyAndClose" :loading="copyAndCloseLoading">
          {{ $t('orbien.token.copyAndClose') }}
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { ClipboardUtils } from '@/utils/ui'

  interface Props {
    visible: boolean
    token: string
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'close'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const { t } = useI18n()

  const copyAndCloseLoading = ref(false)

  const doCopy = async (closeAfterCopy = false): Promise<boolean> => {
    if (!props.token) {
      ElMessage.warning(t('orbien.token.copyEmpty'))
      return false
    }

    const success = await ClipboardUtils.copy(props.token)
    
    if (success) {
      ElMessage.success(t('common.success.copy'))
      if (closeAfterCopy) {
        emit('update:visible', false)
        emit('close')
      }
      return true
    } else {
      await showManualCopyDialog()
      if (closeAfterCopy) {
        emit('update:visible', false)
        emit('close')
      }
      return true
    }
  }

  const showManualCopyDialog = async (): Promise<void> => {
    return new Promise((resolve) => {
      ElMessageBox({
        title: t('orbien.token.manualCopyTitle'),
        message: `<input type="text" value="${ClipboardUtils.escapeHtml(props.token)}" id="clipboard_manual_input" style="width:100%;padding:8px;font-size:14px;font-family:monospace;" onclick="this.select()">`,
        showCancelButton: false,
        confirmButtonText: t('orbien.token.copied'),
        dangerouslyUseHTMLString: true,
        beforeClose: () => {
          resolve()
        }
      }).then(() => {
        ElMessage.success(t('common.success.copy'))
      })

      setTimeout(() => {
        const input = document.getElementById('clipboard_manual_input') as HTMLInputElement
        if (input) {
          input.select()
          input.focus()
        }
      }, 100)
    })
  }

  const handleCopyOnly = async () => {
    await doCopy(false)
  }

  const handleCopyAndClose = async () => {
    copyAndCloseLoading.value = true
    try {
      await doCopy(true)
    } finally {
      copyAndCloseLoading.value = false
    }
  }

  const handleClose = () => {
    emit('update:visible', false)
    emit('close')
  }
</script>

<style lang="scss" scoped>
  .token-success-content {
    padding: 0 16px;
  }

  .token-input {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
    letter-spacing: 1px;
  }

  .success-desc {
    font-size: 16px;
  }

  .copy-btn {
    background: none;
    border: none;
    padding: 8px;
    cursor: pointer;
    color: #909399;
    transition: color 0.2s;

    &:hover {
      color: #409eff;
    }

    &:active {
      color: #67c23a;
    }
  }
</style>
