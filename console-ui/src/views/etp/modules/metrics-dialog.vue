<template>
  <ElDialog v-model="dialogVisible" title="流量统计" width="1200px" height="800px" align-center>
    <div v-if="loading" class="my-5">
      <ElSkeleton :rows="10" animated />
    </div>
    <div v-else>
      <div class="flex justify-between items-center mb-5">
        <h2 class="text-xl font-semibold m-0 text-g-900">流量统计概览</h2>
        <ElButton type="primary" @click="getData" v-ripple>
          <template #icon>
            <ElIcon><Refresh /></ElIcon>
          </template>
          刷新
        </ElButton>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-5 gap-5 mb-8">
        <div class="art-card-sm p-5 text-center">
          <h3 class="text-sm font-medium m-0 mb-2 text-g-500">活动连接数</h3>
          <div class="text-2xl font-semibold text-g-900">{{ metricsData.activeChannels || 0 }}</div>
        </div>
        <div class="art-card-sm p-5 text-center">
          <h3 class="text-sm font-medium m-0 mb-2 text-g-500">上行流量</h3>
          <div class="text-2xl font-semibold text-g-900">{{ ByteUtils.formatBytes(metricsData.readBytes || 0) }}</div>
        </div>
        <div class="art-card-sm p-5 text-center">
          <h3 class="text-sm font-medium m-0 mb-2 text-g-500">下行流量</h3>
          <div class="text-2xl font-semibold text-g-900">{{ ByteUtils.formatBytes(metricsData.writeBytes || 0) }}</div>
        </div>
        <div class="art-card-sm p-5 text-center">
          <h3 class="text-sm font-medium m-0 mb-2 text-g-500">上行速率</h3>
          <div class="text-2xl font-semibold text-g-900">{{ ByteUtils.formatBytes(parseFloat((metricsData.readRate || 0).toFixed(2))) }}/s</div>
        </div>
        <div class="art-card-sm p-5 text-center">
          <h3 class="text-sm font-medium m-0 mb-2 text-g-500">下行速率</h3>
          <div class="text-2xl font-semibold text-g-900"
            >{{ ByteUtils.formatBytes(parseFloat((metricsData.writeRate || 0).toFixed(2))) }}/s</div
          >
        </div>
      </div>
      <div class="pt-5">
        <div class="flex gap-5 mb-5">
          <div class="art-card-sm p-4 flex-1">
            <h3 class="text-lg font-medium m-0 mb-4 text-g-900">流量对比</h3>
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
          <div class="art-card-sm p-4 flex-1">
            <h3 class="text-lg font-medium m-0 mb-4 text-g-900">速率对比</h3>
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
  
  // 初始数据
  const initialMetricsData = {
    activeChannels: 0,
    readBytes: 0,
    writeBytes: 0,
    readMessages: 0,
    writeMessages: 0,
    readRate: 0,
    writeRate: 0,
    lastActiveTime: ''
  }
  
  const metricsData = ref({ ...initialMetricsData })

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
        // 清空之前的数据，避免数据残留
        metricsData.value = { ...initialMetricsData }
        getData()
      }
    },
    { immediate: true }
  )
</script>


