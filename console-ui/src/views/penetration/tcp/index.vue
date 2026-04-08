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
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetTcpProxyList } from '@/api/tcp-proxy'
  import TcpSearch from './modules/tcp-search.vue'
  import TcpDialog from './modules/tcp-dialog.vue'
  import { ElTag, ElMessage } from 'element-plus'
  import { DialogType } from '@/types'

  defineOptions({ name: 'TcpPenetration' })

  type TcpProxyItem = Api.TcpProxy.TcpProxyDTO

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
          prop: 'targets',
          label: '服务列表',
          width: 100,
          formatter: (row: TcpProxyItem) => {
            return row.targets.length
          }
        },

        {
          prop: 'status',
          label: '状态',
          width: 80,
          formatter: (row: TcpProxyItem) => {
            const statusConfig = getProxyStatusConfig(row.status)
            return h(ElTag, { type: statusConfig.type }, () => statusConfig.text)
          }
        },
        {
          prop: 'createdAt',
          label: '创建时间',
          width: 180
        },
        {
          prop: 'operation',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: TcpProxyItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                onClick: () => showDialog('edit', row)
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
</script>

<style lang="scss" scoped></style>