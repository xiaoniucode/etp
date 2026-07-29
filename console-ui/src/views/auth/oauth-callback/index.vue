<template>
  <div class="oauth-callback-page">
    <ElResult
      :icon="errorMessage ? 'error' : 'info'"
      :title="errorMessage ? $t('login.oauthCallback.failed') : $t('login.oauthCallback.processing')"
      :sub-title="errorMessage || $t('login.oauthCallback.pleaseWait')"
    >
      <template v-if="errorMessage" #extra>
        <ElButton type="primary" @click="goLogin">{{ $t('login.oauthCallback.backToLogin') }}</ElButton>
      </template>
    </ElResult>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { fetchOAuthToken } from '@/api/oauth'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'OAuthCallback' })

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  const { t } = useI18n()
  const errorMessage = ref('')

  const goLogin = () => {
    router.replace({ name: 'Login' })
  }

  onMounted(async () => {
    const ticket = route.query.ticket as string
    if (!ticket) {
      errorMessage.value = t('login.oauthCallback.retryFailed')
      return
    }
    try {
      const { token, refreshToken } = await fetchOAuthToken(ticket)
      if (!token) {
        errorMessage.value = t('login.oauthCallback.retryFailed')
        return
      }
      userStore.setToken(token, refreshToken)
      userStore.setLoginStatus(true)
      ElMessage.success(t('login.success.title'))
      router.replace('/')
    } catch {
      errorMessage.value = t('login.oauthCallback.retryFailed')
    }
  })
</script>

<style scoped>
  .oauth-callback-page {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
  }
</style>
