<template>
  <div class="token-page art-full-height">
    <!-- 搜索栏 -->
    <TokenSearch
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></TokenSearch>

    <ElCard class="art-table-card">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" @click="showDialog('add')" v-ripple>新增访问令牌</ElButton>
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

      <!-- 访问令牌弹窗 -->
      <TokenDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :token-id="currentTokenId"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetTokenList, fetchDeleteToken, fetchDeleteBatchTokens } from '@/api/token'
  import TokenSearch from './modules/token-search.vue'
  import TokenDialog from './modules/token-dialog.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import { DialogType } from '@/types'

  defineOptions({ name: 'TokenManagement' })

  interface TokenItem {
    id: number
    name: string
    token: string
    createdAt: string
    updatedAt: string
  }

  // 选中行
  const selectedRows = ref<TokenItem[]>([])

  // 搜索表单
  const searchForm = ref({
    keyword: undefined
  })

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentTokenId = ref<number | undefined>()

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
      apiFn: fetchGetTokenList,
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
          label: '令牌名称',
          minWidth: 50
        },
        {
          prop: 'token',
          label: '令牌',
          minWidth: 200
        },
        {
          prop: 'operation',
          label: '操作',
          width: 130,
          fixed: 'right',
          formatter: (row: TokenItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => deleteToken(row)
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
   * 删除令牌
   */
  const deleteToken = (row: TokenItem): void => {
    console.log('删除令牌:', row)
    ElMessageBox.confirm(`确定要删除该访问令牌吗？`, '删除令牌', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    }).then(async () => {
      await fetchDeleteToken(row.id)
      ElMessage.success('删除成功')
      refreshData()
    })
  }

  /**
   * 批量删除令牌
   */
  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) return

    ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个访问令牌吗？`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    ).then(async () => {
      const ids = selectedRows.value.map((row) => row.id)
      await fetchDeleteBatchTokens(ids)
      ElMessage.success('批量删除成功')
      refreshData()
    })
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: TokenItem[]): void => {
    selectedRows.value = selection
    console.log('选中行数据:', selectedRows.value)
  }

  /**
   * 显示对话框
   */
  const showDialog = (type: DialogType, row?: TokenItem): void => {
    console.log('打开对话框:', { type, row })
    dialogType.value = type
    currentTokenId.value = row?.id
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 处理对话框提交
   */
  const handleDialogSubmit = async () => {
    dialogVisible.value = false
    currentTokenId.value = undefined
    refreshData()
  }
</script>

<style lang="scss" scoped></style>
