<template>
  <div class="tcp-page art-full-height">
    <!-- 搜索栏 -->
    <TcpSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></TcpSearch>

    <ElCard class="art-table-card">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" @click="showDialog('add')" v-ripple>新增</ElButton>
            <ElButton
              type="danger"
              @click="handleBatchDelete"
              v-ripple
              :disabled="selectedRows.length === 0"
              >批量删除</ElButton
            >
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>

      <!-- TCP 代理弹窗 -->
      <TcpDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :proxy-data="currentProxyData"
        @submit="handleDialogSubmit"
      />

      <!-- 访问控制弹窗 -->
      <AccessControlDialog
        v-model:visible="accessControlDialogVisible"
        :proxy-id="currentAccessControlProxyId"
        @close="handleAccessControlClose"
      />

      <!-- 流量统计弹窗 -->
      <MetricsDialog
        v-model:visible="metricsDialogVisible"
        :proxy-id="currentMetricsProxyId"
        @close="handleMetricsClose"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetTcpProxyList, fetchBatchDeleteTcpProxy } from '@/api/tcp-proxy'
  import TcpSearch from './modules/tcp-search.vue'
  import TcpDialog from './modules/tcp-dialog.vue'
  import AccessControlDialog from '../modules/access-control-dialog.vue'
  import MetricsDialog from '../modules/metrics-dialog.vue'
  import { ElTag, ElMessage, ElMessageBox } from 'element-plus'
  import { DialogType } from '@/types'

  defineOptions({ name: 'TcpPenetration' })

  type TcpProxyItem = {
    id: string
    agentId: string
    name: string
    protocol: number
    agentType: number
    status: number
    remotePort: number
  }

  // 选中行
  const selectedRows = ref<TcpProxyItem[]>([])

  // 搜索表单
  const searchForm = ref({
    keyword: undefined
  })

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentProxyData = ref<Partial<TcpProxyItem>>({})

  // 访问控制弹窗相关
  const accessControlDialogVisible = ref(false)
  const currentAccessControlProxyId = ref('')

  // 流量统计弹窗相关
  const metricsDialogVisible = ref(false)
  const currentMetricsProxyId = ref('')

  const getProxyStatusConfig = (status: number) => {
    return status === 1
      ? { type: 'success' as const, text: '运行中' }
      : { type: 'info' as const, text: '已停止' }
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchGetTcpProxyList,
      apiParams: {
        keyword: undefined,
        page: 1,
        size: 10
      },
      paginationKey: {
        current: 'page',
        size: 'size'
      },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'name',
          label: '代理名称',
          minWidth: 120
        },
        {
          prop: 'remotePort',
          label: '远程端口',
          width: 120
        },
        {
          prop: 'agentType',
          label: '客户端类型',
          width: 120,
          formatter: (row: TcpProxyItem) => {
            return h(ElTag, { type: row.agentType === 1 ? 'primary' : 'warning' }, () =>
              row.agentType === 1 ? 'BINARY' : 'SESSION'
            )
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 120,
          formatter: (row: TcpProxyItem) => {
            const statusConfig = getProxyStatusConfig(row.status)
            return h(ElTag, { type: statusConfig.type }, () => statusConfig.text)
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 300,
          fixed: 'right',
          formatter: (row: TcpProxyItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'text',
                text: 'IP访问控制',
                onClick: () => handleIpControl(row)
              }),
              h(ArtButtonTable, {
                type: 'text',
                text: '流量统计',
                onClick: () => handleMetrics(row)
              }),
              h(ArtButtonTable, { type: 'edit', onClick: () => showDialog('edit', row) }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => handleSingleDelete(row)
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

  const handleSearch = (params: { keyword?: string }) => {
    console.log(params)
    Object.assign(searchParams, params)
    getData()
  }

  const handleSelectionChange = (selection: TcpProxyItem[]): void => {
    selectedRows.value = selection
    console.log('选中行数据:', selectedRows.value)
  }

  const showDialog = (type: DialogType, row?: TcpProxyItem): void => {
    console.log('打开弹窗:', { type, row })
    dialogType.value = type
    currentProxyData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const handleDialogSubmit = async () => {
    try {
      dialogVisible.value = false
      currentProxyData.value = {}
      refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的代理')
      return
    }

    try {
      await ElMessageBox.confirm('确定要删除选中的代理吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const ids = selectedRows.value.map((item) => item.id)
      await fetchBatchDeleteTcpProxy({ ids })
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
      }
    }
  }

  const handleSingleDelete = async (proxy: TcpProxyItem) => {
    try {
      await ElMessageBox.confirm(`确定要删除代理「${proxy.name}」吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await fetchBatchDeleteTcpProxy({ ids: [proxy.id] })
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
      }
    }
  }

  const handleIpControl = (proxy: TcpProxyItem) => {
    currentAccessControlProxyId.value = proxy.id
    accessControlDialogVisible.value = true
  }

  const handleAccessControlClose = () => {
    currentAccessControlProxyId.value = ''
  }

  const handleMetrics = (proxy: TcpProxyItem) => {
    currentMetricsProxyId.value = proxy.id
    metricsDialogVisible.value = true
  }

  const handleMetricsClose = () => {
    currentMetricsProxyId.value = ''
  }
</script>

<style lang="scss" scoped></style>
