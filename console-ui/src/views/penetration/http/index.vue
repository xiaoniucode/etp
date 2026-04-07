<template>
  <div class="http-page art-full-height">
    <!-- 搜索栏 -->
    <HttpSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></HttpSearch>

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

      <!-- HTTP 代理弹窗 -->
      <HttpDialog
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
  import { fetchGetHttpProxyList } from '@/api/http-proxy'
  import HttpSearch from './modules/http-search.vue'
  import HttpDialog from './modules/http-dialog.vue'
  import { ElTag, ElMessage } from 'element-plus'
  import { DialogType } from '@/types'

  defineOptions({ name: 'HttpPenetration' })

  type HttpProxyItem = Api.HttpProxy.HttpProxyDTO

  // 选中行
  const selectedRows = ref<HttpProxyItem[]>([])

  // 搜索表单
  const searchForm = ref({
    keyword: undefined
  })

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentProxyData = ref<Partial<HttpProxyItem>>({})

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
      apiFn: fetchGetHttpProxyList,
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
          prop: 'domains',
          label: '域名',
          minWidth: 200,
          formatter: (row: HttpProxyItem) => {
            return row.domains.join(', ')
          }
        },
        {
          prop: 'targets',
          label: '服务数量',
          width: 100,
          formatter: (row: HttpProxyItem) => {
            return row.targets.length
          }
        },

        {
          prop: 'status',
          label: '状态',
          width: 80,
          formatter: (row: HttpProxyItem) => {
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
          width: 280,
          fixed: 'right',
          formatter: (row: HttpProxyItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'text',
                text: 'IP访问控制',
                onClick: () => handleIpControl(row)
              }),
              h(ArtButtonTable, {
                type: 'text',
                text: 'BasicAuth',
                onClick: () => handleBasicAuth(row)
              }),
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

  const handleSelectionChange = (selection: HttpProxyItem[]): void => {
    selectedRows.value = selection
    console.log('选中行数据:', selectedRows.value)
  }

  const showDialog = (type: DialogType, row?: HttpProxyItem): void => {
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

  const handleIpControl = (proxy: HttpProxyItem) => {
    ElMessage.info(`IP访问控制: ${proxy.name}`)
  }

  const handleBasicAuth = (proxy: HttpProxyItem) => {
    ElMessage.info(`BasicAuth: ${proxy.name}`)
  }
</script>

<style lang="scss" scoped>
  .http-page {
    padding: 20px;
  }
</style>
