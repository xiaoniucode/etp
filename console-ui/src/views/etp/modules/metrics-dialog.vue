<template>
  <ElDialog v-model="dialogVisible" title="流量统计" width="1200px" height="800px" align-center>
    <div v-if="loading" class="loading-state">
      <ElSkeleton :rows="10" animated />
    </div>
    <div v-else>
      <div class="dialog-header">
        <h2>流量统计概览</h2>
        <ElButton type="primary" @click="getData" v-ripple>
          <template #icon>
            <ElIcon><Refresh /></ElIcon>
          </template>
          刷新
        </ElButton>
      </div>
      <div class="metrics-overview">
        <div class="overview-item">
          <h3>活动连接数</h3>
          <div class="overview-value">{{ metricsData.activeChannels || 0 }}</div>
        </div>
        <div class="overview-item">
          <h3>上行流量</h3>
          <div class="overview-value">{{ ByteUtils.formatBytes(metricsData.readBytes || 0) }}</div>
        </div>
        <div class="overview-item">
          <h3>下行流量</h3>
          <div class="overview-value">{{ ByteUtils.formatBytes(metricsData.writeBytes || 0) }}</div>
        </div>
        <div class="overview-item">
          <h3>上行速率</h3>
          <div class="overview-value">{{ ByteUtils.formatBytes(parseFloat((metricsData.readRate || 0).toFixed(2))) }}/s</div>
        </div>
        <div class="overview-item">
          <h3>下行速率</h3>
          <div class="overview-value"
            >{{ ByteUtils.formatBytes(parseFloat((metricsData.writeRate || 0).toFixed(2))) }}/s</div
          >
        </div>
      </div>
      <div class="chart-container">
        <div class="chart-row">
          <div class="chart-item">
            <h3>流量对比</h3>
            <ArtRingChart
              :data="[
                { name: '上行流量', value: metricsData.readBytes || 0 },
                { name: '下行流量', value: metricsData.writeBytes || 0 }
              ]"
              :colors="['#409EFF', '#67C23A']"
              :showLegend="true"
              :showLabel="true"
              :centerText="`总流量\n${ByteUtils.formatBytes((metricsData.readBytes || 0) + (metricsData.writeBytes || 0))}`"
              :radius="['40%', '70%']"
              height="300px"
            />
          </div>
          <div class="chart-item">
            <h3>速率对比</h3>
            <ArtHBarChart
              :data="[
                { name: '上行速率', data: [parseFloat((metricsData.readRate || 0).toFixed(2))] },
                { name: '下行速率', data: [parseFloat((metricsData.writeRate || 0).toFixed(2))] }
              ]"
              :xAxisData="['速率']"
              :colors="['#409EFF', '#67C23A']"
              :showLegend="true"
              height="300px"
            />
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">关闭</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'
  import ArtHBarChart from '@/components/core/charts/art-h-bar-chart/index.vue'
  import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'
  import { fetchGetMetrics } from '@/api/metrics'
  import { ByteUtils } from '@/utils/format/byteFormatter'
  import { ElButton, ElIcon } from 'element-plus'
  import { Refresh } from '@element-plus/icons-vue'

  interface Props {
    visible: boolean
    proxyId: string
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const metricsData = ref({
    activeChannels: 0,
    readBytes: 0,
    writeBytes: 0,
    readMessages: 0,
    writeMessages: 0,
    readRate: 0,
    writeRate: 0,
    lastActiveTime: ''
  })

  // 获取流量统计数据
  const getData = async () => {
    if (!props.proxyId) return

    loading.value = true
    try {
      const response = await fetchGetMetrics(props.proxyId)
      if (response) {
        metricsData.value = response
      }
    } finally {
      loading.value = false
    }
  }

  watch(
    () => [props.visible, props.proxyId],
    ([visible]) => {
      if (visible) {
        getData()
      }
    },
    { immediate: true }
  )
</script>

<style scoped>
  .loading-state {
    margin: 20px 0;
  }

  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .dialog-header h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  .metrics-overview {
    display: flex;
    gap: 20px;
    margin-bottom: 30px;
    flex-wrap: wrap;
  }

  .overview-item {
    flex: 1;
    min-width: 180px;
    background: #f9f9f9;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    text-align: center;
  }

  .overview-item h3 {
    margin: 0 0 10px 0;
    font-size: 14px;
    font-weight: 500;
    color: #666;
  }

  .overview-value {
    font-size: 24px;
    font-weight: 600;
    color: #333;
  }

  .chart-container {
    padding: 20px 0;
  }

  .chart-row {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
  }

  .chart-item {
    flex: 1;
    background: #f9f9f9;
    border-radius: 8px;
    padding: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .chart-item h3 {
    margin: 0 0 15px 0;
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
</style>
