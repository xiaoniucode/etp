<template>
  <div class="client-page art-full-height">
    <!-- 搜索栏 -->
    <AgentSearch
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></AgentSearch>

    <ElCard class="art-table-card">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>

      <!-- 客户端详情弹窗 -->
      <AgentDialog v-model:visible="detailDialogVisible" :client-data="selectedClient" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetAgentListByPage, fetchKickoutAgent } from '@/api/agent'
  import AgentSearch from './modules/agent-search.vue'
  import AgentDialog from './modules/agent-dialog.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'

  defineOptions({ name: 'ClientManagement' })

  type ClientItem = Api.Agent.AgentDTO

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
      apiFn: fetchGetAgentListByPage,
      apiParams: {
        keyword: undefined,
        page: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'id',
          label: '客户端标识',
          width: 160
        },
        {
          prop: 'name',
          label: '客户端名称',
          minWidth: 120
        },

        {
          prop: 'os',
          label: '操作系统',
          width: 100
        },
        {
          prop: 'arch',
          label: '系统架构',
          width: 100
        },
        {
          prop: 'version',
          label: '客户端版本',
          width: 100
        },
        {
          prop: 'agentType',
          label: '客户端类型',
          width: 100,
          formatter: (row: ClientItem) => {
            return h(ElTag, { type: row.agentType === 1 ? 'primary' : 'warning' }, () =>
              row.agentType === 1 ? 'BINARY' : 'SESSION'
            )
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
          prop: 'operation',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: ClientItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'text',
                text: '详情',
                onClick: () => showClientDetail(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                text: '强制下线',
                onClick: () => kickoutClient(row),
                disabled: !row.isOnline
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
   * 剔除在线客户端
   */
  const kickoutClient = (row: ClientItem): void => {
    console.log('剔除客户端:', row)
    ElMessageBox.confirm(`确定要剔除该在线客户端吗？`, '剔除客户端', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await fetchKickoutAgent(row.id)
      ElMessage.success('剔除成功')
      refreshData()
    })
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
