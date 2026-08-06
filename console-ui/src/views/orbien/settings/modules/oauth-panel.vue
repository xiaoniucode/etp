<template>
  <div class="oauth-panel" v-loading="loading">
    <div class="provider-list">
      <div
          v-for="item in providers"
          :key="item.provider"
          class="provider-block"
          v-show="forms[item.provider]"
      >
        <div class="provider-head">
          <div class="provider-title">
            <span class="provider-icon-wrap">
              <OAuthProviderIcon :provider="item.provider" :size="24" :alt="item.displayName"/>
            </span>
            <span>{{ item.displayName }}</span>
            <ElTag v-if="item.enabled" type="success" effect="plain" size="small">
              {{ $t('orbien.settings.oauth.enabled') }}
            </ElTag>
            <ElTag v-else type="info" effect="plain" size="small">
              {{ $t('orbien.settings.oauth.disabled') }}
            </ElTag>
          </div>
          <div class="provider-switch">
            <span class="switch-label">{{ $t('orbien.settings.oauth.enableLogin') }}</span>
            <ElTooltip
                :disabled="canToggle(item)"
                :content="$t('orbien.settings.oauth.saveCredentialsFirst')"
                placement="top"
            >
              <span>
                <ElSwitch
                    :model-value="item.enabled"
                    :loading="togglingProvider === item.provider"
                    :disabled="!canToggle(item)"
                    @change="(val: string | number | boolean) => onToggleEnabled(item.provider, Boolean(val))"
                />
              </span>
            </ElTooltip>
          </div>
        </div>

        <ElForm class="provider-form" label-position="top" @submit.prevent>
          <div class="form-grid">
            <ElFormItem :label="$t('orbien.settings.oauth.clientId')" required>
              <ElInput
                  v-model="forms[item.provider].clientId"
                  :placeholder="$t('orbien.settings.oauth.clientIdPlaceholder')"
                  clearable
              />
            </ElFormItem>
            <ElFormItem>
              <template #label>
                <span class="secret-label">
                  {{ $t('orbien.settings.oauth.clientSecret') }}
                  <ElTag v-if="item.secretConfigured" size="small" type="success" effect="plain">
                    {{ $t('orbien.settings.oauth.configured') }}
                  </ElTag>
                </span>
              </template>
              <ElInput
                  v-model="forms[item.provider].clientSecret"
                  type="password"
                  show-password
                  :placeholder="
                  item.secretConfigured
                    ? $t('orbien.settings.oauth.secretPlaceholderKeep')
                    : $t('orbien.settings.oauth.secretPlaceholder')
                "
                  clearable
              />
            </ElFormItem>
          </div>

          <ElFormItem :label="$t('orbien.settings.oauth.callbackUrl')">
            <div class="callback-row">
              <ElInput :model-value="item.callbackUrl" readonly/>
              <ElButton @click="copyText(item.callbackUrl)">
                {{ $t('orbien.settings.oauth.copy') }}
              </ElButton>
            </div>
          </ElFormItem>

          <div class="provider-actions">
            <ElButton
                type="primary"
                :loading="savingProvider === item.provider"
                @click="saveCredentials(item.provider)"
            >
              {{ $t('orbien.settings.oauth.saveConfig') }}
            </ElButton>
          </div>
        </ElForm>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage} from 'element-plus'
import {
  fetchOAuthProviderConfigs,
  fetchSaveOAuthProvider,
  fetchUpdateOAuthProviderEnabled
} from '@/api/oauth'
import OAuthProviderIcon from '@/components/oauth/OAuthProviderIcon.vue'

defineOptions({name: 'OAuthPanel'})

const {t} = useI18n()

const loading = ref(false)
const savingProvider = ref('')
const togglingProvider = ref('')
const providers = ref<Api.OAuth.ProviderConfig[]>([])
const forms = reactive<Record<string, { clientId: string; clientSecret: string }>>({})

const canToggle = (item: Api.OAuth.ProviderConfig) => {
  return item.enabled || (!!item.clientId && item.secretConfigured)
}

const syncForm = (item: Api.OAuth.ProviderConfig) => {
  forms[item.provider] = {
    clientId: item.clientId || '',
    clientSecret: ''
  }
}

const applyProvider = (saved: Api.OAuth.ProviderConfig) => {
  const idx = providers.value.findIndex((p) => p.provider === saved.provider)
  if (idx >= 0) {
    providers.value[idx] = saved
  }
  syncForm(saved)
}

const loadProviders = async () => {
  loading.value = true
  try {
    providers.value = (await fetchOAuthProviderConfigs()) || []
    providers.value.forEach(syncForm)
  } finally {
    loading.value = false
  }
}

const saveCredentials = async (provider: string) => {
  const form = forms[provider]
  const item = providers.value.find((p) => p.provider === provider)
  if (!form.clientId?.trim()) {
    ElMessage.warning(t('orbien.settings.oauth.fillClientId'))
    return
  }
  if (!form.clientSecret?.trim() && !item?.secretConfigured) {
    ElMessage.warning(t('orbien.settings.oauth.fillClientSecret'))
    return
  }

  savingProvider.value = provider
  try {
    const saved = await fetchSaveOAuthProvider(provider, {
      clientId: form.clientId.trim(),
      clientSecret: form.clientSecret?.trim() || undefined
    })
    applyProvider(saved)
    ElMessage.success(t('orbien.settings.oauth.configSaved'))
  } finally {
    savingProvider.value = ''
  }
}

const onToggleEnabled = async (provider: string, enabled: boolean) => {
  togglingProvider.value = provider
  try {
    const saved = await fetchUpdateOAuthProviderEnabled(provider, {enabled})
    applyProvider(saved)
    ElMessage.success(
        enabled ? t('orbien.settings.oauth.loginEnabled') : t('orbien.settings.oauth.loginDisabled')
    )
  } catch {
    await loadProviders()
  } finally {
    togglingProvider.value = ''
  }
}

const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('orbien.settings.oauth.copied'))
  } catch {
    ElMessage.error(t('orbien.settings.oauth.copyFailed'))
  }
}

onMounted(loadProviders)

defineExpose({reload: loadProviders})
</script>

<style lang="scss" scoped>
.provider-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.provider-block {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 20px 22px 18px;
  background: var(--el-bg-color);
}

.provider-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.provider-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
}

.provider-icon-wrap {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 50%;
  background: var(--el-fill-color-blank);
}

.provider-switch {
  display: flex;
  align-items: center;
  gap: 10px;
}

.switch-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.secret-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.callback-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.provider-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 4px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}
</style>
