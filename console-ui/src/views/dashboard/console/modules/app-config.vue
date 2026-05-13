<template>
  <div class="app-config">
    <div class="app-config-content art-card p-4 h-105">
      <div v-if="loading" class="loading-state">
        <ElSkeleton :rows="6" animated />
      </div>
      <div v-else class="config-grid">
        <div class="config-item">
          <span class="text-xs text-g-500">服务器地址:</span>
          <span class="config-value">{{ configInfo?.serverAddr || '-' }}</span>
        </div>
        <div class="config-item">
          <span class="text-xs text-g-500">服务器端口:</span>
          <span class="config-value">{{ configInfo?.serverPort || '-' }}</span>
        </div>
        <div class="config-item">
          <span class="text-xs text-g-500">HTTP 代理端口:</span>
          <span class="config-value">{{ configInfo?.httpProxyPort || '-' }}</span>
        </div>
        <div class="config-item">
          <span class="text-xs text-g-500">基础域名:</span>
          <span class="config-value">{{ configInfo?.baseDomain || '-' }}</span>
        </div>
        <div class="config-item">
          <span class="text-xs text-g-500">端口范围:</span>
          <span class="config-value">{{ configInfo?.portStart || '-' }} - {{ configInfo?.portEnd || '-' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { ElSkeleton } from 'element-plus'
  import { fetchGetAppConfig } from '@/api/app'

  defineOptions({ name: 'AppConfig' })

  const loading = ref(false)
  const configInfo = ref<Api.App.AppConfigInfoDTO | null>(null)

  const getData = async () => {
    loading.value = true
    try {
      configInfo.value = await fetchGetAppConfig()
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    getData()
  })
</script>

<style scoped>
  .app-config {
    margin-bottom: 1.25rem;
  }

  @media (max-width: 640px) {
    .app-config {
      margin-bottom: 1rem;
    }
  }

  .loading-state {
    padding: 20px 0;
  }

  .config-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
  }

  .config-item {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .config-value {
      font-weight: 500;
      color: var(--el-text-color-primary);
      font-size: 14px;
    }
  }
</style>