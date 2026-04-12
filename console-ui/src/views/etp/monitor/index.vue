<template>
  <div class="monitor-page art-full-height">
    <ElCard class="art-table-card">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-xl font-semibold m-0">服务器监控</h2>
        <ElButton type="primary" @click="getData" v-ripple> 刷新 </ElButton>
      </div>

      <div v-if="loading" class="loading-state">
        <ElSkeleton :rows="10" animated />
      </div>

      <div v-else>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-5 mb-8">
          <div class="art-card-sm p-5 transition-all duration-300 hover:-translate-y-1">
            <h3 class="text-lg font-semibold m-0 mb-4 text-g-900">CPU 信息</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-xs text-g-500">核心数:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.cpu?.total || 0 }} 核</span>
              </div>
              <div class="flex justify-between">
                <span class="text-xs text-g-500">使用率:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.cpu?.usage || 0 }}%</span>
              </div>
            </div>
          </div>

          <div class="art-card-sm p-5 transition-all duration-300 hover:-translate-y-1">
            <h3 class="text-lg font-semibold m-0 mb-4 text-g-900">JVM 内存</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-xs text-g-500">总内存:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.jvmMem?.total || '0MB' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-xs text-g-500">已使用:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.jvmMem?.used || '0MB' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-xs text-g-500">使用率:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.jvmMem?.usage || 0 }}%</span>
              </div>
            </div>
          </div>

          <div class="art-card-sm p-5 transition-all duration-300 hover:-translate-y-1">
            <h3 class="text-lg font-semibold m-0 mb-4 text-g-900">系统内存</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-xs text-g-500">总内存:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.osMem?.total || '0MB' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-xs text-g-500">已使用:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.osMem?.used || '0MB' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-xs text-g-500">使用率:</span>
                <span class="font-semibold text-g-900">{{ serverInfo?.osMem?.usage || 0 }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 仪表盘 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="text-center">
            <h3 class="text-lg font-semibold mb-4 text-g-900">CPU 使用率</h3>
            <ArtGaugeChart
              :value="serverInfo?.cpu?.usage || 0"
              name="CPU"
              :min="0"
              :max="100"
              height="300px"
            />
          </div>
          <div class="text-center">
            <h3 class="text-lg font-semibold mb-4 text-g-900">JVM 内存使用率</h3>
            <ArtGaugeChart
              :value="serverInfo?.jvmMem?.usage || 0"
              name="JVM"
              :min="0"
              :max="100"
              height="300px"
            />
          </div>
          <div class="text-center">
            <h3 class="text-lg font-semibold mb-4 text-g-900">系统内存使用率</h3>
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

  onMounted(() => {
    getData()
  })
</script>

<style scss scoped>
  .loading-state {
    padding: 20px 0;
  }
</style>
