<template>
  <div class="stats-page art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>

      <MetricsDialog
        v-model:visible="metricsDialogVisible"
        :proxy-id="currentMetricsProxyId"
        @close="handleMetricsClose"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h } from 'vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetMetricsList } from '@/api/metrics'
  import MetricsDialog from '../modules/metrics-dialog.vue'
  import { ByteUtils } from '@/utils/format/byteFormatter'

  defineOptions({ name: 'Stats' })

  type MetricsItem = {
    proxyId: string
    activeChannels: number
    readBytes: number
    writeBytes: number
    readMessages: number
    writeMessages: number
    readRate: number
    writeRate: number
    lastActiveTime: string
  }

  const metricsDialogVisible = ref(false)
  const currentMetricsProxyId = ref('')

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchGetMetricsList,
      apiParams: {
        page: 0,
        size: 10
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'proxyId',
          label: '代理ID',
          minWidth: 200
        },
        {
          prop: 'activeChannels',
          label: '活动连接数',
          width: 120
        },
        {
          prop: 'readBytes',
          label: '上行流量',
          width: 150,
          formatter: (row: MetricsItem) => ByteUtils.formatBytes(row.readBytes || 0)
        },
        {
          prop: 'writeBytes',
          label: '下行流量',
          width: 150,
          formatter: (row: MetricsItem) => ByteUtils.formatBytes(row.writeBytes || 0)
        },
        {
          prop: 'readMessages',
          label: '上行消息数',
          width: 120
        },
        {
          prop: 'writeMessages',
          label: '下行消息数',
          width: 120
        },
        {
          prop: 'readRate',
          label: '上行速率',
          width: 100,
          formatter: (row: MetricsItem) => {
            if (row.readRate === undefined || row.readRate === null) return ''
            return `${ByteUtils.formatBytes(parseFloat((row.readRate || 0).toFixed(2)))}`
          }
        },
        {
          prop: 'writeRate',
          label: '下行速率',
          width: 100,
          formatter: (row: MetricsItem) => {
            if (row.writeRate === undefined || row.writeRate === null) return ''
            return `${ByteUtils.formatBytes(parseFloat((row.writeRate || 0).toFixed(2)))}`
          }
        },
        {
          prop: 'lastActiveTime',
          label: '最后活动时间',
          minWidth: 180,
          formatter: (row: MetricsItem) => {
            if (!row.lastActiveTime) return ''
            const date = new Date(row.lastActiveTime)
            return date.toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit'
            })
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 150,
          fixed: 'right',
          formatter: (row: MetricsItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'text',
                text: '查看详情',
                onClick: () => handleViewMetrics(row)
              })
            ])
        }
      ]
    },
    transform: {
      dataTransformer: (records) => {
        if (!Array.isArray(records)) {
          console.warn('数据转换器: 期望数组类型，实际收到:', typeof records)
          return []
        }
        return records
      }
    }
  })

  const handleViewMetrics = (metrics: MetricsItem) => {
    currentMetricsProxyId.value = metrics.proxyId
    metricsDialogVisible.value = true
  }

  const handleMetricsClose = () => {
    currentMetricsProxyId.value = ''
  }
</script>

<style scss scoped></style>
