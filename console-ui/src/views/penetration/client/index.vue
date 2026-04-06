<template>
  <div class="client-page art-full-height">
    <!-- 搜索栏 -->
    <ClientSearch
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></ClientSearch>

    <ElCard class="art-table-card">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="handleBatchDelete" :disabled="selectedRows.length === 0" v-ripple
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

      <!-- 客户端详情弹窗 -->
      <ClientDetailDialog
        v-model:visible="detailDialogVisible"
        :client-data="selectedClient"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetClientList,
    fetchDeleteClient,
    fetchDeleteBatchClients,
    fetchKickoutClient
  } from '@/api/agent'
  import ClientSearch from './modules/client-search.vue'
  import ClientDetailDialog from './modules/client-detail-dialog.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'

  defineOptions({ name: 'ClientManagement' })

  interface ClientItem {
    id: string
    token: string
    isOnline: boolean
    name: string
    os: string
    arch: string
    version: string
    agentType: number
    createdAt: string
    updatedAt: string
  }

  // 选中行
  const selectedRows = ref<ClientItem[]>([])

  // 搜索表单
  const searchForm = ref({
    keyword: undefined
  })

  // 详情弹窗状态
  const detailDialogVisible = ref(false)
  
  // 选中的客户端
  const selectedClient = ref<ClientItem | null>(null)

  /**
   * 获取客户端状态标签配置
   */
  const getClientStatusConfig = (isOnline: boolean) => {
    return isOnline
      ? { type: 'success' as const, text: '在线' }
      : { type: 'info' as const, text: '离线' }
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
    // 核心配置
    core: {
      apiFn: fetchGetClientList,
      apiParams: {
        keyword: undefined,
        page: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'name',
          label: '客户端名称',
          minWidth: 120
        },
        {
          prop: 'token',
          label: '访问令牌',
          minWidth: 240,
          formatter: (row: ClientItem) => {
            return row.token.substring(0, 10) + '...'
          }
        },
        {
          prop: 'isOnline',
          label: '状态',
          width: 80,
          formatter: (row: ClientItem) => {
            const statusConfig = getClientStatusConfig(row.isOnline)
            return h(ElTag, { type: statusConfig.type }, () => statusConfig.text)
          }
        },
        {
          prop: 'os',
          label: '操作系统',
          width: 100
        },
        {
          prop: 'arch',
          label: '架构',
          width: 80
        },
        {
          prop: 'version',
          label: '版本',
          width: 100
        },
        {
          prop: 'agentType',
          label: '类型',
          width: 80,
          formatter: (row: ClientItem) => {
            return row.agentType === 1 ? 'BINARY' : 'SESSION'
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 240,
          fixed: 'right', // 固定列
          formatter: (row: ClientItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'text',
                text: '详情',
                onClick: () => showClientDetail(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                text: '剔除',
                onClick: () => kickoutClient(row),
                disabled: !row.isOnline
              }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => deleteClient(row)
              })
            ])
        }
      ]
    },
    // 数据处理
    transform: {
      // 数据转换器
      dataTransformer: (records) => {
        // 类型守卫检查
        if (!Array.isArray(records)) {
          console.warn('数据转换器: 期望数组类型，实际收到:', typeof records)
          return []
        }

        return records
      }
    }
  })

  /**
   * 搜索处理
   * @param params 参数
   */
  const handleSearch = (params: { keyword?: string }) => {
    console.log(params)
    // 搜索参数赋值
    Object.assign(searchParams, params)
    getData()
  }

  /**
   * 删除客户端
   */
  const deleteClient = (row: ClientItem): void => {
    console.log('删除客户端:', row)
    ElMessageBox.confirm(`确定要删除该客户端吗？`, '删除客户端', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }).then(async () => {
      try {
        await fetchDeleteClient(row.id)
        ElMessage.success('删除成功')
        refreshData()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
  }

  /**
   * 批量删除客户端
   */
  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) return

    ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个客户端吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }).then(async () => {
      try {
        const ids = selectedRows.value.map((row) => row.id)
        await fetchDeleteBatchClients(ids)
        ElMessage.success('批量删除成功')
        refreshData()
      } catch (error) {
        ElMessage.error('批量删除失败')
      }
    })
  }

  /**
   * 剔除在线客户端
   */
  const kickoutClient = (row: ClientItem): void => {
    console.log('剔除客户端:', row)
    ElMessageBox.confirm(`确定要剔除该在线客户端吗？`, '剔除客户端', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await fetchKickoutClient(row.id)
        ElMessage.success('剔除成功')
        refreshData()
      } catch (error) {
        ElMessage.error('剔除失败')
      }
    })
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: ClientItem[]): void => {
    selectedRows.value = selection
    console.log('选中行数据:', selectedRows.value)
  }

  /**
   * 显示客户端详情
   */
  const showClientDetail = (client: ClientItem) => {
    selectedClient.value = client
    nextTick(() => {
      detailDialogVisible.value = true
    })
  }
</script>

<style lang="scss" scoped></style>
