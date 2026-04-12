<template>
  <div class="monitor-page art-full-height">
    <ElCard class="art-table-card">
      <!-- 页面标题和刷新按钮 -->
      <div class="page-header">
        <h2>服务器监控</h2>
        <ElButton type="primary" @click="getData" v-ripple>
          刷新
        </ElButton>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <ElSkeleton :rows="10" animated />
      </div>

      <!-- 监控数据 -->
      <div v-else>
        <!-- 概览卡片 -->
        <div class="overview-cards">
          <div class="art-card-sm overview-card">
            <div class="card-content">
              <h3>CPU 信息</h3>
              <div class="info-item">
                <span class="info-label">核心数:</span>
                <span class="info-value">{{ serverInfo?.cpu?.total || 0 }} 核</span>
              </div>
              <div class="info-item">
                <span class="info-label">使用率:</span>
                <span class="info-value">{{ serverInfo?.cpu?.usage || 0 }}%</span>
              </div>
            </div>
          </div>

          <div class="art-card-sm overview-card">
            <div class="card-content">
              <h3>JVM 内存</h3>
              <div class="info-item">
                <span class="info-label">总内存:</span>
                <span class="info-value">{{ serverInfo?.jvmMem?.total || '0MB' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">已使用:</span>
                <span class="info-value">{{ serverInfo?.jvmMem?.used || '0MB' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">使用率:</span>
                <span class="info-value">{{ serverInfo?.jvmMem?.usage || 0 }}%</span>
              </div>
            </div>
          </div>

          <div class="art-card-sm overview-card">
            <div class="card-content">
              <h3>系统内存</h3>
              <div class="info-item">
                <span class="info-label">总内存:</span>
                <span class="info-value">{{ serverInfo?.osMem?.total || '0MB' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">已使用:</span>
                <span class="info-value">{{ serverInfo?.osMem?.used || '0MB' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">使用率:</span>
                <span class="info-value">{{ serverInfo?.osMem?.usage || 0 }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 仪表盘 -->
        <div class="gauge-container">
          <div class="gauge-item">
            <h3>CPU 使用率</h3>
            <ArtGaugeChart
              :value="serverInfo?.cpu?.usage || 0"
              name="CPU"
              :min="0"
              :max="100"
              height="300px"
            />
          </div>
          <div class="gauge-item">
            <h3>JVM 内存使用率</h3>
            <ArtGaugeChart
              :value="serverInfo?.jvmMem?.usage || 0"
              name="JVM"
              :min="0"
              :max="100"
              height="300px"
            />
          </div>
          <div class="gauge-item">
            <h3>系统内存使用率</h3>
            <ArtGaugeChart
              :value="serverInfo?.osMem?.usage || 0"
              name="System"
              :min="0"
              :max="100"
              height="300px"
            />
          </div>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { ElButton, ElCard, ElSkeleton } from 'element-plus'
  import ArtGaugeChart from '@/components/core/charts/art-gauge-chart/index.vue'
  import { fetchGetServerInfo } from '@/api/monitor'

  defineOptions({ name: 'Monitor' })

  // 服务器信息
  const serverInfo = ref<Api.Monitor.ServerInfo | null>(null)
  const loading = ref(false)

  // 获取服务器信息
  const getData = async () => {
    loading.value = true
    try {
      serverInfo.value = await fetchGetServerInfo()
    } finally {
      loading.value = false
    }
  }

  // 组件挂载时获取数据
  onMounted(() => {
    getData()
  })
</script>

<style scss scoped>
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .loading-state {
    padding: 20px 0;
  }

  .overview-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    margin-bottom: 32px;
  }

  .overview-card {
    transition: all 0.3s ease;
    padding: 20px;

    &:hover {
      transform: translateY(-2px);
    }

    h3 {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 16px 0;
      color: var(--el-text-color-primary);
    }
  }

  .card-content {
    .info-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;

      .info-label {
        color: var(--el-text-color-secondary);
      }

      .info-value {
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }
  }

  .gauge-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 32px;

    .gauge-item {
      text-align: center;

      h3 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 16px;
        color: var(--el-text-color-primary);
      }
    }
  }
</style>