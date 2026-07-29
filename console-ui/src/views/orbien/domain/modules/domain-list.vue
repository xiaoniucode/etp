<template>
  <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
    <template #left>
      <ElSpace wrap>
        <ElButton type="primary" @click="showDialog('add')" v-ripple>{{ $t('orbien.domain.addRootDomain') }}</ElButton>
        <ElButton @click="handleBatchDelete" :disabled="selectedRows.length === 0" v-ripple>
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

  <DomainDialog
    v-model:visible="dialogVisible"
    :type="dialogType"
    :domain-id="currentDomainId"
    @submit="handleDialogSubmit"
  />
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetDomainListByPage, fetchDeleteBatchDomains } from '@/api/domain'
  import DomainDialog from './domain-dialog.vue'
  import { DialogType } from '@/types'

  defineOptions({ name: 'DomainList' })

  const { t } = useI18n()

  const emit = defineEmits<{ change: [] }>()

  type DomainItem = Api.Domain.DomainDTO

  const selectedRows = ref<DomainItem[]>([])
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentDomainId = ref<number | undefined>()

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
      apiFn: fetchGetDomainListByPage,
      apiParams: {
        current: 1,
        size: 10
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'domain',
          label: t('orbien.common.rootDomain'),
          minWidth: 180
        },
        {
          prop: 'remark',
          label: t('common.description'),
          minWidth: 160,
          formatter: (row: DomainItem) => row.remark || ''
        },
        {
          prop: 'createdAt',
          label: t('common.createTime'),
          minWidth: 170
        },
        {
          prop: 'updatedAt',
          label: t('common.updateTime'),
          minWidth: 170
        },
        {
          prop: 'operation',
          label: t('common.actions'),
          width: 150,
          fixed: 'right',
          formatter: (row: DomainItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.edit'),
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.delete'),
                onClick: () => deleteDomain(row)
              })
            ])
        }
      ]
    },
    hooks: {
      onSuccess: () => emit('change')
    }
  })

  const handleSelectionChange = (selection: DomainItem[]): void => {
    selectedRows.value = selection
  }

  const deleteDomains = async (rows: DomainItem[], title: string, message: string) => {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'error'
    })
    await fetchDeleteBatchDomains(rows.map((row) => row.id))
    ElMessage.success(t('common.success.delete'))
    refreshData()
  }

  const deleteDomain = (row: DomainItem): void => {
    deleteDomains(
      [row],
      t('orbien.domain.deleteTitle'),
      t('orbien.domain.deleteConfirm', { domain: row.domain })
    ).catch(() => {})
  }

  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) return
    deleteDomains(
      selectedRows.value,
      t('common.batchDelete'),
      t('orbien.domain.batchDeleteConfirm', { count: selectedRows.value.length })
    ).catch(() => {})
  }

  const showDialog = (type: DialogType, row?: DomainItem): void => {
    dialogType.value = type
    currentDomainId.value = row?.id
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const handleDialogSubmit = () => {
    refreshData()
  }
</script>
