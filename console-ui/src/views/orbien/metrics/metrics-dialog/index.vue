<template>
  <ElDialog v-model="dialogVisible" :title="$t('orbien.metrics.title')" width="1250px" align-center>
    <div v-if="loading" class="py-2">
      <ElSkeleton :rows="10" animated/>
    </div>
    <div v-else class="flex flex-col gap-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div v-if="showTimeRange" class="flex flex-wrap items-center gap-2">
          <ElSelect v-model="timeRange" :placeholder="$t('orbien.metrics.timeRange')" style="width: 140px">
            <ElOption v-for="item in timeRangeOptions" :key="item.value" :label="item.label" :value="item.value"/>
          </ElSelect>
          <ElDatePicker
              v-if="timeRange === 'custom'"
              v-model="customDate"
              type="daterange"
              :range-separator="$t('orbien.metrics.dateRangeSeparator')"
              :start-placeholder="$t('orbien.metrics.startDate')"
              :end-placeholder="$t('orbien.metrics.endDate')"
              :disabled-date="disabledDate"
              @calendar-change="handleCalendarChange"
          />
        </div>
        <div v-else/>
        <ElButton type="primary" @click="getData" v-ripple>
          <template #icon>
            <ElIcon>
              <Refresh/>
            </ElIcon>
          </template>
          {{ $t('common.refresh') }}
        </ElButton>
      </div>

      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <ArtStatsCard
            :title="$t('orbien.metrics.cards.connections')"
            :count="metricsData.activeChannels || 0"
            :description="$t('orbien.metrics.cards.connectionsDesc')"
            icon="ri:share-line"
            iconStyle="bg-cyan-500"
        />
        <ArtStatsCard
            :title="$t('orbien.metrics.cards.upload')"
            :count="upTotalParts.value"
            :decimals="upTotalParts.decimals"
            :suffix="` ${upTotalParts.unit}`"
            :description="$t('orbien.metrics.cards.uploadDesc')"
            icon="ri:arrow-up-line"
            iconStyle="bg-green-500"
        />
        <ArtStatsCard
            :title="$t('orbien.metrics.cards.download')"
            :count="downTotalParts.value"
            :decimals="downTotalParts.decimals"
            :suffix="` ${downTotalParts.unit}`"
            :description="$t('orbien.metrics.cards.downloadDesc')"
            icon="ri:arrow-down-line"
            iconStyle="bg-orange-500"
        />
        <ArtStatsCard
            :title="$t('orbien.metrics.cards.upRate')"
            :count="upRateParts.value"
            :decimals="upRateParts.decimals"
            :suffix="` ${upRateParts.unit}/s`"
            :description="$t('orbien.metrics.cards.upRateDesc')"
            icon="ri:arrow-up-circle-line"
            iconStyle="bg-purple-500"
        />
        <ArtStatsCard
            :title="$t('orbien.metrics.cards.downRate')"
            :count="downRateParts.value"
            :decimals="downRateParts.decimals"
            :suffix="` ${downRateParts.unit}/s`"
            :description="$t('orbien.metrics.cards.downRateDesc')"
            icon="ri:arrow-down-circle-line"
            iconStyle="bg-indigo-500"
        />
      </div>

      <div class="art-card-sm p-4">
        <div class="mb-4 flex items-center justify-between gap-3">
          <h3 class="m-0 text-base font-medium text-g-900">
            {{ $t('orbien.metrics.charts.trend') }}
            <span class="ml-2 text-sm font-normal text-g-500">{{
                $t('orbien.metrics.charts.unit', {unit: unitLabel})
              }}</span>
          </h3>
        </div>
        <ArtLineChart
            height="300px"
            :data="lineChartData"
            :xAxisData="lineChartXAxis"
            :showAreaColor="true"
            :showAxisLine="false"
            :showLegend="true"
            :yAxisLabelFormatter="yAxisLabelFormatter"
            :tooltipFormatter="tooltipFormatter"
        />
      </div>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
import {ref, watch, computed} from 'vue'
import {useI18n} from 'vue-i18n'
import ArtLineChart from '@/components/core/charts/art-line-chart/index.vue'
import ArtStatsCard from '@/components/core/cards/art-stats-card/index.vue'
import {fetchGetProxyMetrics} from '@/api/metrics'
import {ByteUtils} from '@/utils/format/byteFormatter'
import {ElButton, ElIcon} from 'element-plus'
import {Refresh} from '@element-plus/icons-vue'

const {t} = useI18n()

interface Props {
  visible: boolean
  proxyId: string
  showTimeRange?: boolean
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

const DEFAULT_TIME_RANGE = '24h'

const timeRangeOptions = computed(() => [
  {value: '24h', label: t('orbien.metrics.ranges.24h')},
  {value: '3d', label: t('orbien.metrics.ranges.3d')},
  {value: '7d', label: t('orbien.metrics.ranges.7d')},
  {value: '15d', label: t('orbien.metrics.ranges.15d')},
  {value: 'custom', label: t('orbien.metrics.ranges.custom')}
])

const loading = ref(false)
const timeRange = ref(DEFAULT_TIME_RANGE)
const customDate = ref<string | [string, string] | ''>('')
const startDateAnchor = ref<Date | null>(null)

const createEmptyMetricsData = (): Api.Metrics.TrafficChartVO => ({
  up: {xAxis: [], yAxis: []},
  down: {xAxis: [], yAxis: []},
  upTotal: 0,
  downTotal: 0,
  upRate: 0,
  downRate: 0,
  activeChannels: 0
})

const metricsData = ref<Api.Metrics.TrafficChartVO>(createEmptyMetricsData())
const lineChartData = ref([
  {
    name: '',
    data: [] as number[],
    showAreaColor: true
  },
  {
    name: '',
    data: [] as number[],
    showAreaColor: true
  }
])
const lineChartXAxis = ref<string[]>([])
const unitDivisor = ref(1)
const unitLabel = ref('B')

const upTotalParts = computed(() => ByteUtils.formatParts(metricsData.value.upTotal || 0))
const downTotalParts = computed(() => ByteUtils.formatParts(metricsData.value.downTotal || 0))
const upRateParts = computed(() => ByteUtils.formatParts(metricsData.value.upRate || 0))
const downRateParts = computed(() => ByteUtils.formatParts(metricsData.value.downRate || 0))

/** 重置 timeRange 时不触发 timeRange 的 watch，避免重复请求 */
let suppressRangeWatch = false

const resetLineChartLabels = () => {
  lineChartData.value[0].name = t('orbien.metrics.charts.downloadSeries')
  lineChartData.value[1].name = t('orbien.metrics.charts.uploadSeries')
}

const clearDisplayState = () => {
  metricsData.value = createEmptyMetricsData()
  lineChartData.value[0].data = []
  lineChartData.value[1].data = []
  resetLineChartLabels()
  lineChartXAxis.value = []
  unitDivisor.value = 1
  unitLabel.value = 'B'
}

const resetDialogState = () => {
  suppressRangeWatch = true
  timeRange.value = DEFAULT_TIME_RANGE
  customDate.value = ''
  startDateAnchor.value = null
  clearDisplayState()
  suppressRangeWatch = false
}

const yAxisLabelFormatter = (value: number): string => {
  if (value <= 0) return '0'
  return Math.round(value / unitDivisor.value).toString()
}

const tooltipFormatter = (params: any[]): string => {
  if (!params || params.length === 0) return ''
  let html = `${t('orbien.metrics.charts.tooltipTime', {time: params[0].name})}<br/>`
  params.forEach((item: any) => {
    html += `${item.marker} ${item.seriesName}: ${ByteUtils.formatBytes(item.value)}<br/>`
  })
  return html
}

const handleCalendarChange = (dates: Date[]) => {
  if (dates && dates.length > 0) {
    startDateAnchor.value = dates[0] || null
  } else {
    startDateAnchor.value = null
  }
}

const disabledDate = (time: Date): boolean => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  if (time.getTime() > today.getTime()) {
    return true
  }

  if (startDateAnchor.value) {
    const maxEndDate = new Date(startDateAnchor.value)
    maxEndDate.setDate(maxEndDate.getDate() + 30)
    if (time.getTime() > maxEndDate.getTime()) {
      return true
    }
  }

  return false
}

const getData = async () => {
  if (!props.proxyId) return

  clearDisplayState()
  loading.value = true
  try {
    const requestParams: {
      proxyId: string
      queryType: string
      startDate?: string
      endDate?: string
    } = {
      proxyId: props.proxyId,
      queryType: timeRange.value
    }

    if (timeRange.value === 'custom' && customDate.value && customDate.value.length === 2) {
      requestParams.startDate = customDate.value[0]
      requestParams.endDate = customDate.value[1]
    }

    const response = (await fetchGetProxyMetrics(requestParams)) as Api.Metrics.TrafficChartVO
    if (response) {
      metricsData.value = response

      lineChartData.value[0].data = response.down?.yAxis || []
      lineChartData.value[1].data = response.up?.yAxis || []
      resetLineChartLabels()
      const rawXAxis = response.down?.xAxis || []
      // 后端返回 timeUnit='hour' 时为小时粒度，需加 :00
      lineChartXAxis.value =
          response.timeUnit === 'hour' ? rawXAxis.map((h: string) => `${h}:00`) : rawXAxis

      const allValues = [...(response.down?.yAxis || []), ...(response.up?.yAxis || [])]
      const dataMax = allValues.length > 0 ? Math.max(...allValues, 0) : 0
      const unitInfo = ByteUtils.getUnitInfo(dataMax)
      unitDivisor.value = unitInfo.divisor
      unitLabel.value = unitInfo.unit
    }
  } finally {
    loading.value = false
  }
}

watch(
    () => [props.visible, props.proxyId] as const,
    ([visible, proxyId], previous) => {
      if (!visible) {
        if (previous?.[0]) {
          resetDialogState()
        }
        return
      }
      const wasVisible = previous?.[0] ?? false
      const prevProxyId = previous?.[1] ?? ''
      if (!wasVisible || proxyId !== prevProxyId) {
        resetDialogState()
        getData()
      }
    },
    {immediate: true}
)

watch(timeRange, () => {
  if (suppressRangeWatch || !props.visible) return
  if (timeRange.value !== 'custom') {
    getData()
  }
})

watch(customDate, () => {
  if (
      props.visible &&
      timeRange.value === 'custom' &&
      customDate.value &&
      customDate.value.length === 2
  ) {
    getData()
  }
})

resetLineChartLabels()
</script>
