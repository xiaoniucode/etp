<template>
  <div class="port-pool-page art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" @click="showDialog('add')" v-ripple>{{ $t('orbien.portPool.add') }}</ElButton>
            <ElButton
              @click="handleBatchDelete"
              v-ripple
              :disabled="selectedRows.length === 0"
            >
              {{ $t('common.batchDelete') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />

      <PortPoolDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :port-pool-id="currentPortPoolId"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import { useI18n } from 'vue-i18n'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { ElTag, ElMessage, ElMessageBox, ElSpace } from 'element-plus'
  import {
    fetchGetPortPoolListByPage,
    fetchDeleteBatchPortPools
  } from '@/api/port-pool'
  import PortPoolDialog from './modules/port-pool-dialog.vue'
  import { DialogType } from '@/types'
  import { getPortPoolTypeLabel } from '@/enums/orbien/business'

  defineOptions({ name: 'PortPool' })

  const { t } = useI18n()

  type PortPoolItem = Api.PortPool.PortPoolDTO

  const selectedRows = ref<PortPoolItem[]>([])
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentPortPoolId = ref<number | undefined>()

  const formatPort = (row: PortPoolItem) => {
    return row.endPort ? `${row.startPort}-${row.endPort}` : `${row.startPort}`
  }

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
      apiFn: fetchGetPortPoolListByPage,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'startPort',
          label: t('orbien.common.port'),
          formatter: (row: PortPoolItem) => formatPort(row)
        },
        {
          prop: 'type',
          label: t('orbien.common.protocol'),
          formatter: (row: PortPoolItem) => {
            const config = getPortPoolTypeLabel(row.type)
            return h(ElTag, { type: config.type, size: 'small' }, () => config.text)
          }
        },
        {
          prop: 'remark',
          label: t('orbien.common.remark'),
          formatter: (row: PortPoolItem) => row.remark || ''
        },
        {
          prop: 'createdAt',
          label: t('common.createTime')
        },
        {
          prop: 'operation',
          label: t('common.actions'),
          width: 150,
          fixed: 'right',
          formatter: (row: PortPoolItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.edit'),
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.delete'),
                onClick: () => handleDelete(row)
              })
            ])
        }
      ]
    }
  })

  const handleSelectionChange = (selection: PortPoolItem[]): void => {
    selectedRows.value = selection
  }

  const deletePortPools = async (rows: PortPoolItem[], title: string, message: string) => {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await fetchDeleteBatchPortPools(rows.map((row) => row.id))
    ElMessage.success(t('common.success.delete'))
    refreshData()
  }

  const handleDelete = (row: PortPoolItem): void => {
    deletePortPools(
      [row],
      t('orbien.portPool.deleteTitle'),
      t('orbien.portPool.deleteConfirm', { port: formatPort(row) })
    ).catch(() => {})
  }

  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(t('orbien.portPool.selectToDelete'))
      return
    }
    deletePortPools(
      selectedRows.value,
      t('common.batchDelete'),
      t('orbien.portPool.batchDeleteConfirm', { count: selectedRows.value.length })
    ).catch(() => {})
  }

  const showDialog = (type: DialogType, row?: PortPoolItem): void => {
    dialogType.value = type
    currentPortPoolId.value = row?.id
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const handleDialogSubmit = () => {
    refreshData()
  }
</script>

<style lang="scss" scoped>
  .port-pool-page {
    width: 100%;
  }
</style>
