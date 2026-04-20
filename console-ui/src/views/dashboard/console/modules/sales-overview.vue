<template>
  <div class="art-card h-105 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>24小时总流量统计</h4>
        <p>最近24小时流量变化</p>
      </div>
    </div>
    <ArtLineChart
      height="calc(100% - 56px)"
      :data="data"
      :xAxisData="xAxisData"
      :showAreaColor="true"
      :showAxisLine="false"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { fetchGet24hMetrics } from '@/api/metrics'

  /**
   * 24小时流量数据
   */
  const data = ref<number[]>([])

  /**
   * X 轴小时标签
   */
  const xAxisData = ref<string[]>([])

  /**
   * 获取24小时流量数据
   */
  const get24hMetrics = async () => {
    const metricsData = await fetchGet24hMetrics()
    if (metricsData) {
      data.value = metricsData.yAxis
      xAxisData.value = metricsData.xAxis
    }
  }

  /**
   * 组件挂载时获取数据
   */
  onMounted(() => {
    get24hMetrics()
  })
</script>
