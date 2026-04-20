<template>
  <div class="art-card h-105 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>代理类型分布</h4>
        <p>HTTP 与 TCP 代理数量对比</p>
      </div>
    </div>
    <div class="flex items-center justify-center h-[calc(100%-56px)]">
      <ArtRingChart
        height="200px"
        :data="proxyData"
        :showLegend="true"
        legendPosition="right"
        :centerText="totalCount"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, computed } from 'vue'
  import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'
  import { fetchGetProxyProtocolStats } from '@/api/monitor'

  /**
   * 代理类型数据
   * 包含 HTTP 和 TCP 代理的数量
   */
  const proxyData = ref([
    {
      name: 'HTTP 代理',
      value: 0
    },
    {
      name: 'TCP 代理',
      value: 0
    }
  ])

  /**
   * 计算代理总数
   */
  const totalCount = computed(() => {
    const total = proxyData.value.reduce((sum, item) => sum + item.value, 0)
    return `代理总数\n${total}`
  })

  /**
   * 获取代理协议统计数据
   */
  const getProxyProtocolStats = async () => {
    const data = await fetchGetProxyProtocolStats()
    if (data) {
      proxyData.value = [
        {
          name: 'HTTP 代理',
          value: data.httpCount
        },
        {
          name: 'TCP 代理',
          value: data.tcpCount
        }
      ]
    }
  }

  /**
   * 组件挂载时获取数据
   */
  onMounted(() => {
    getProxyProtocolStats()
  })
</script>