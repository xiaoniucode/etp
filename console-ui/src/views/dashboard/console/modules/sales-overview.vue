<template>
  <div class="traffic-card art-card h-105 p-5 mb-5 max-sm:mb-4">
    <!-- header -->
    <div class="art-card-header">
      <div class="title">
        <h4>流量监控</h4>
        <p>最近 24 小时上下行流量趋势</p>
      </div>
    </div>

    <!-- metrics -->
    <div class="metrics-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-item">
        <div class="metric-dot" :style="{ background: item.color }" />

        <div class="metric-label">
          {{ item.label }}
        </div>

        <div class="metric-value">
          {{ item.value }}
        </div>
      </div>
    </div>

    <!-- chart -->
    <ArtLineChart
      height="calc(100% - 140px)"
      :data="data"
      :xAxisData="xAxisData"
      :showAreaColor="true"
      :showAxisLine="false"
      :showLegend="true"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue'

  const data = ref([
    {
      name: '下行流量',
      data: [
        60, 50, 40, 35, 30, 40, 80, 200, 450, 800, 1100, 950, 1000, 900, 850, 820, 900, 1050, 1300,
        1450, 1250, 900, 550, 250
      ] as number[],
      showAreaColor: true
    },
    {
      name: '上行流量',
      data: [
        42, 35, 28, 24, 21, 28, 56, 140, 315, 560, 770, 665, 700, 630, 595, 574, 630, 735, 910,
        1015, 875, 630, 385, 175
      ] as number[],
      showAreaColor: true
    }
  ])

  const xAxisData = ref<string[]>([
    '00:00',
    '01:00',
    '02:00',
    '03:00',
    '04:00',
    '05:00',
    '06:00',
    '07:00',
    '08:00',
    '09:00',
    '10:00',
    '11:00',
    '12:00',
    '13:00',
    '14:00',
    '15:00',
    '16:00',
    '17:00',
    '18:00',
    '19:00',
    '20:00',
    '21:00',
    '22:00',
    '23:00'
  ])

  const totalSent = computed(() => {
    return data.value[1].data.reduce((sum, val) => sum + val, 0)
  })

  const totalReceived = computed(() => {
    return data.value[0].data.reduce((sum, val) => sum + val, 0)
  })

  const metrics = computed(() => {
    const downData = data.value[0].data
    const upData = data.value[1].data

    const currentDown = downData.length > 0 ? downData[downData.length - 1] : 0
    const currentUp = upData.length > 0 ? upData[upData.length - 1] : 0

    return [
      {
        label: '上行',
        value: formatBytes(currentUp),
        color: '#22c55e'
      },
      {
        label: '下行',
        value: formatBytes(currentDown),
        color: '#f59e0b'
      },
      {
        label: '总发送',
        value: formatBytes(totalSent.value),
        color: '#3b82f6'
      },
      {
        label: '总接收',
        value: formatBytes(totalReceived.value),
        color: '#8b5cf6'
      }
    ]
  })

  const formatBytes = (bytes: number): string => {
    if (bytes < 1024) return `${bytes.toFixed(1)} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`
  }
</script>

<style scoped lang="scss">
  .traffic-card {
    display: flex;
    flex-direction: column;
  }

  .metrics-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 14px;
    margin-bottom: 20px;
  }

  .metric-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 18px;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 14px;
    transition: all 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgb(0 0 0 / 6%);
      border-color: var(--el-color-primary-light-5);
    }
  }

  .metric-dot {
    position: relative;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;

    &::before {
      position: absolute;
      inset: 0;
      border-radius: 50%;
      background: inherit;
      content: '';
      animation: pulse 2s infinite;
    }
  }
  @keyframes pulse {
    0% {
      transform: scale(1);
      opacity: 0.8;
    }

    70% {
      transform: scale(2.4);
      opacity: 0;
    }

    100% {
      transform: scale(2.4);
      opacity: 0;
    }
  }

  .metric-label {
    color: var(--el-text-color-regular);
    font-size: 14px;
    white-space: nowrap;
  }

  .metric-value {
    margin-left: auto;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    white-space: nowrap;
  }

  @media (max-width: 1200px) {
    .metrics-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 768px) {
    .metrics-grid {
      grid-template-columns: 1fr;
    }

    .metric-value {
      font-size: 18px;
    }
  }
</style>
