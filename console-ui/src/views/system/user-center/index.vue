<!-- 个人中心 -->
<template>
  <div class="user-center-page">
    <div class="profile-card art-card-sm">
      <img class="avatar" src="@imgs/user/avatar.webp" alt="avatar" />
      <h2 class="username">{{ userInfo.username || '—' }}</h2>
      <ElTag type="primary" effect="plain" size="small">{{ $t('system.userCenter.admin') }}</ElTag>
    </div>

    <div class="content-col">
      <div class="section-card art-card-sm">
        <div class="section-head">
          <h3>{{ $t('system.userCenter.changePassword') }}</h3>
        </div>
        <ElForm :model="pwdForm" class="section-body" label-position="top" @submit.prevent>
          <ElFormItem :label="$t('system.userCenter.currentPassword')">
            <ElInput
              v-model="pwdForm.password"
              type="password"
              show-password
              autocomplete="current-password"
            />
          </ElFormItem>
          <ElFormItem :label="$t('system.userCenter.newPassword')">
            <ElInput
              v-model="pwdForm.newPassword"
              type="password"
              show-password
              autocomplete="new-password"
            />
          </ElFormItem>
          <ElFormItem :label="$t('system.userCenter.confirmPassword')">
            <ElInput
              v-model="pwdForm.confirmPassword"
              type="password"
              show-password
              autocomplete="new-password"
            />
          </ElFormItem>
          <div class="section-actions">
            <ElButton type="primary" :loading="pwdLoading" @click="savePassword">
              {{ $t('system.userCenter.savePassword') }}
            </ElButton>
          </div>
        </ElForm>
      </div>

      <div v-if="providerRows.length" class="section-card art-card-sm">
        <div class="section-head">
          <h3>{{ $t('system.userCenter.oauth') }}</h3>
        </div>
        <div class="section-body bind-list" v-loading="bindLoading">
          <div v-for="item in providerRows" :key="item.provider" class="bind-row">
            <div class="bind-left">
              <span class="bind-icon-wrap">
                <OAuthProviderIcon :provider="item.provider" :size="22" :alt="item.displayName" />
              </span>
              <div class="bind-info">
                <div class="bind-name">{{ item.displayName }}</div>
                <div class="bind-meta" :class="{ ok: item.bound }">
                  <template v-if="item.bound">
                    {{
                      $t('system.userCenter.bound', {
                        account: item.externalLogin || '—'
                      })
                    }}
                  </template>
                  <template v-else>{{ $t('system.userCenter.unbound') }}</template>
                </div>
              </div>
            </div>

            <div class="bind-actions">
              <template v-if="item.bound">
                <ElButton
                  type="danger"
                  plain
                  size="small"
                  :loading="unbindingProvider === item.provider"
                  @click="unbind(item.provider)"
                >
                  {{ $t('system.userCenter.unbind') }}
                </ElButton>
              </template>
              <template v-else>
                <ElButton
                  type="primary"
                  plain
                  size="small"
                  :loading="bindingProvider === item.provider"
                  @click="startBind(item.provider)"
                >
                  {{ $t('system.userCenter.bind') }}
                </ElButton>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useUserStore } from '@/store/modules/user'
  import { fetchUpdatePassword } from '@/api/user'
  import {
    fetchOAuthBindings,
    fetchOAuthProviderConfigs,
    fetchStartOAuthBind,
    fetchUnbindOAuth
  } from '@/api/oauth'
  import OAuthProviderIcon from '@/components/oauth/OAuthProviderIcon.vue'

  defineOptions({ name: 'UserCenter' })

  interface ProviderRow {
    provider: string
    displayName: string
    bound: boolean
    externalLogin?: string
  }

  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()
  const { t } = useI18n()
  const userInfo = computed(() => userStore.getUserInfo)

  const pwdLoading = ref(false)
  const bindLoading = ref(false)
  const bindingProvider = ref('')
  const unbindingProvider = ref('')
  const providers = ref<Api.OAuth.ProviderConfig[]>([])
  const bindings = ref<Api.OAuth.Binding[]>([])

  const pwdForm = reactive({
    password: '',
    newPassword: '',
    confirmPassword: ''
  })

  const providerRows = computed<ProviderRow[]>(() => {
    const bindMap = new Map(bindings.value.map((b) => [b.provider, b]))
    return providers.value
      .filter((p) => p.enabled)
      .map((p) => {
        const bind = bindMap.get(p.provider)
        return {
          provider: p.provider,
          displayName: p.displayName,
          bound: !!bind?.bound,
          externalLogin: bind?.externalLogin
        }
      })
  })

  const loadOAuthData = async () => {
    bindLoading.value = true
    try {
      const [providerList, bindingList] = await Promise.all([
        fetchOAuthProviderConfigs(),
        fetchOAuthBindings()
      ])
      providers.value = providerList || []
      bindings.value = bindingList || []
    } catch {
      providers.value = []
      bindings.value = []
    } finally {
      bindLoading.value = false
    }
  }

  const savePassword = async () => {
    if (!pwdForm.password) {
      ElMessage.warning(t('system.userCenter.enterCurrentPassword'))
      return
    }
    if (!pwdForm.newPassword) {
      ElMessage.warning(t('system.userCenter.enterNewPassword'))
      return
    }
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
      ElMessage.warning(t('system.userCenter.passwordMismatch'))
      return
    }
    pwdLoading.value = true
    try {
      await fetchUpdatePassword(pwdForm)
      ElMessage.success(t('system.userCenter.passwordUpdated'))
      pwdForm.password = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } finally {
      pwdLoading.value = false
    }
  }

  const startBind = async (provider: string) => {
    bindingProvider.value = provider
    try {
      const { authorizeUrl } = await fetchStartOAuthBind(provider)
      if (!authorizeUrl) {
        ElMessage.error(t('system.userCenter.noAuthorizeUrl'))
        return
      }
      window.location.href = authorizeUrl
    } finally {
      bindingProvider.value = ''
    }
  }

  const unbind = async (provider: string) => {
    await ElMessageBox.confirm(
      t('system.userCenter.unbindConfirmMessage'),
      t('system.userCenter.unbindConfirmTitle'),
      {
        type: 'warning',
        confirmButtonText: t('system.userCenter.unbind'),
        cancelButtonText: t('common.cancel')
      }
    )
    unbindingProvider.value = provider
    try {
      await fetchUnbindOAuth(provider)
      ElMessage.success(t('system.userCenter.unboundSuccess'))
      await loadOAuthData()
    } finally {
      unbindingProvider.value = ''
    }
  }

  const handleBindResult = async () => {
    const bind = route.query.bind as string
    if (bind !== 'ok' && bind !== 'fail') return
    if (bind === 'ok') {
      ElMessage.success(t('system.userCenter.bindSuccess'))
    } else {
      ElMessage.error(t('system.userCenter.bindFailed'))
    }
    await loadOAuthData()
    router.replace({ path: '/system/user-center' })
  }

  onMounted(async () => {
    await loadOAuthData()
    await handleBindResult()
  })
</script>

<style lang="scss" scoped>
  .user-center-page {
    display: grid;
    grid-template-columns: 280px minmax(0, 1fr);
    gap: 16px;
    padding: 8px 0 20px;

    @media (max-width: 900px) {
      grid-template-columns: 1fr;
    }
  }

  .profile-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 36px 24px;
    text-align: center;
    height: fit-content;
  }

  .avatar {
    width: 88px;
    height: 88px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid var(--el-bg-color);
    box-shadow: 0 0 0 1px var(--el-border-color-lighter);
  }

  .username {
    margin: 16px 0 10px;
    font-size: 20px;
    font-weight: 600;
  }

  .content-col {
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-width: 0;
  }

  .section-card {
    overflow: hidden;
  }

  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-extra-light);

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }

  .section-body {
    padding: 20px;
  }

  .section-actions {
    display: flex;
    justify-content: flex-end;
  }

  .bind-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 80px;
  }

  .bind-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    background: var(--el-fill-color-blank);
  }

  .bind-left {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
  }

  .bind-icon-wrap {
    width: 40px;
    height: 40px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 50%;
    background: var(--el-bg-color);
  }

  .bind-info {
    min-width: 0;
  }

  .bind-name {
    font-size: 14px;
    font-weight: 600;
  }

  .bind-meta {
    margin-top: 4px;
    font-size: 13px;
    color: var(--el-text-color-secondary);

    &.ok {
      color: var(--el-color-success);
    }
  }

  .bind-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
</style>
