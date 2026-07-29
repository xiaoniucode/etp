<!-- 工作台页面 -->
<template>
  <div>
    <ServerStatus />
    <SystemOverview />
    <ElRow :gutter="20">
      <ElCol :sm="24" :md="24" :lg="24">
        <TrafficStats />
      </ElCol>
    </ElRow>
    <ElRow :gutter="20" class="console-stats-row">
      <ElCol :sm="24" :md="12" :lg="12" class="console-stats-col">
        <div class="art-card h-105 p-5 mb-5 max-sm:mb-4">
          <div class="art-card-header">
            <div class="title">
              <h4>{{ $t('dashboard.proxy.title') }}</h4>
              <p>{{ $t('dashboard.proxy.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center justify-center h-[calc(100%-56px)]">
            <ArtRingChart
              :data="proxyData"
              :loading="proxyLoading"
              :radius="['0%', '70%']"
              :showLegend="true"
              legendPosition="bottom"
            />
          </div>
        </div>
      </ElCol>
      <ElCol :sm="24" :md="12" :lg="12" class="console-stats-col">
        <ServerConfig />
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import ServerStatus from './modules/server-status.vue'
  import SystemOverview from './modules/system-overview.vue'
  import TrafficStats from './modules/traffic-stats.vue'
  import ServerConfig from './modules/server-config.vue'
  import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'
  import { fetchGetProxyProtocolStats } from '@/api/monitor'
  import { ProtocolType, getProtocolLabel } from '@/enums/orbien/business'

  defineOptions({ name: 'Console' })

  const proxyLoading = ref(false)

  const buildProxyData = (counts: Partial<Api.Monitor.ProxyProtocolCountDTO> = {}) => [
    { name: getProtocolLabel(ProtocolType.HTTP), value: counts.httpCount ?? 0 },
    { name: getProtocolLabel(ProtocolType.HTTPS), value: counts.httpsCount ?? 0 },
    { name: getProtocolLabel(ProtocolType.TCP), value: counts.tcpCount ?? 0 },
    { name: getProtocolLabel(ProtocolType.UDP), value: counts.udpCount ?? 0 },
    { name: getProtocolLabel(ProtocolType.SOCKS5), value: counts.socks5Count ?? 0 },
    { name: getProtocolLabel(ProtocolType.FILE), value: counts.fileCount ?? 0 }
  ]

  const proxyData = ref(buildProxyData())

  const getProxyProtocolStats = async () => {
    proxyLoading.value = true
    try {
      const data = await fetchGetProxyProtocolStats()
      if (data) {
        proxyData.value = buildProxyData(data)
      }
    } finally {
      proxyLoading.value = false
    }
  }

  onMounted(() => {
    getProxyProtocolStats()
  })
</script>

<style scoped lang="scss">
  .console-stats-row {
    align-items: stretch;
  }

  .console-stats-col {
    display: flex;
    flex-direction: column;
  }
</style>
