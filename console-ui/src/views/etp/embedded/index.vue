<template>
  <div class="embedded-page art-full-height">
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
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetEmbeddedList, fetchDeleteEmbedded } from '@/api/embedded'
  import MetricsDialog from '../modules/metrics-dialog.vue'
  import { ElCard, ElTag, ElMessage, ElMessageBox, ElSpace } from 'element-plus'

  defineOptions({ name: 'EmbeddedManagement' })

  const metricsDialogVisible = ref(false)
  const currentMetricsProxyId = ref('')

  const getStatusConfig = (status: number) => {
    return status === 1
      ? { type: 'success' as const, text: '运行中' }
      : { type: 'info' as const, text: '已停止' }
  }

  const getProtocolText = (protocol: number) => {
    return protocol === 1 ? 'HTTP' : 'TCP'
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    refreshRemove
  } = useTable({
    core: {
      apiFn: fetchGetEmbeddedList,
      apiParams: {
        page: 1,
        size: 10
      },
      paginationKey: {
        current: 'page',
        size: 'size'
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'tunnel.name',
          label: '隧道名',
          minWidth: 120,
          formatter: (row: Api.Embedded.TunnelListDTO) => row.tunnel?.name || ''
        },
        {
          prop: 'tunnel.protocol',
          label: '协议',
          width: 80,
          formatter: (row: Api.Embedded.TunnelListDTO) => getProtocolText(row.tunnel?.protocol || 0)
        },
        {
          prop: 'remoteAddress',
          label: '远程地址',
          minWidth: 180,
          formatter: (row: Api.Embedded.TunnelListDTO) => {
            const tunnel = row.tunnel
            if (!tunnel) return ''

            if ('domains' in tunnel) {
              return h(ElSpace, { direction: 'horizontal', size: 4, wrap: true }, () =>
                tunnel.domains.map((domain) => {
                  const fullAddress =
                    row.httpProxyPort && row.httpProxyPort !== 80
                      ? `${domain}:${row.httpProxyPort}`
                      : domain
                  return h(ElTag, { type: 'warning', size: 'small' }, () => fullAddress)
                })
              )
            } else if ('listenPort' in tunnel) {
              return h(ElTag, { type: 'primary', size: 'small' }, () => `:${tunnel.listenPort}`)
            }
            return ''
          }
        },
        {
          prop: 'tunnel.status',
          label: '状态',
          width: 80,
          formatter: (row: Api.Embedded.TunnelListDTO) => {
            const config = getStatusConfig(row.tunnel?.status || 0)
            return h(ElTag, { type: config.type }, () => config.text)
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: Api.Embedded.TunnelListDTO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'text',
                text: '流量统计',
                onClick: () => handleMetrics(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => handleDelete(row)
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

  const handleMetrics = (row: Api.Embedded.TunnelListDTO) => {
    currentMetricsProxyId.value = row.tunnel?.proxyId || ''
    metricsDialogVisible.value = true
  }

  const handleMetricsClose = () => {
    currentMetricsProxyId.value = ''
  }

  const handleDelete = async (row: Api.Embedded.TunnelListDTO) => {
    try {
      await ElMessageBox.confirm(`确定要删除隧道「${row.tunnel?.name || ''}」吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await fetchDeleteEmbedded(row.tunnel?.proxyId || '')
      ElMessage.success('删除成功')
      refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
      }
    }
  }
</script>

<style lang="scss" scoped></style>
