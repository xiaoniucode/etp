<template>
  <div class="client-page art-full-height">
    <ElCard class="art-table-card">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="handleBatchDelete" :disabled="selectedRows.length === 0" v-ripple>
              {{ $t('common.batchDelete') }}
            </ElButton>
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
      <AgentDialog v-model:visible="detailDialogVisible" :client-data="selectedClient" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, h, nextTick } from 'vue'
  import { useI18n } from 'vue-i18n'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetAgentListByPage, fetchKickoutAgent, fetchDeleteBatchAgents } from '@/api/agent'
  import AgentDialog from './modules/agent-dialog.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'
  import { getAgentTypeTag } from '@/enums/orbien/business'

  defineOptions({ name: 'ClientManagement' })

  const { t } = useI18n()

  type ClientItem = Api.Agent.AgentDTO

  const selectedRows = ref<ClientItem[]>([])

  // 详情弹窗状态
  const detailDialogVisible = ref(false)

  // 选中的客户端
  const selectedClient = ref<ClientItem | null>(null)

  /**
   * 获取客户端状态标签配置
   */
  const getClientStatusConfig = (isOnline: boolean) => {
    return isOnline
      ? { type: 'primary' as const, text: t('common.online') }
      : { type: 'info' as const, text: t('common.offline') }
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
      apiFn: fetchGetAgentListByPage,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'id',
          label: t('orbien.common.clientId'),
          width: 180
        },
        {
          prop: 'name',
          label: t('common.name')
        },
        {
          prop: 'agentType',
          label: t('orbien.common.type'),
          formatter: (row: ClientItem) => {
            const typeConfig = getAgentTypeTag(row.agentType)
            return h(ElTag, { type: typeConfig.type, size: 'small' }, () => typeConfig.text)
          }
        },
        {
          prop: 'os',
          label: t('orbien.common.os')
        },
        {
          prop: 'arch',
          label: t('orbien.common.systemArch')
        },
        {
          prop: 'version',
          label: t('orbien.common.version')
        },
        {
          prop: 'isOnline',
          label: t('common.status'),
          formatter: (row: ClientItem) => {
            const statusConfig = getClientStatusConfig(row.isOnline)
            return h(ElTag, { type: statusConfig.type, size: 'small' }, () => statusConfig.text)
          }
        },
        {
          prop: 'operation',
          label: t('common.actions'),
          width: 220,
          fixed: 'right',
          formatter: (row: ClientItem) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.detail'),
                onClick: () => showClientDetail(row)
              }),
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.agent.kickout'),
                onClick: () => kickoutClient(row),
                disabled: !row.isOnline
              }),
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.delete'),
                onClick: () => deleteClient(row)
              })
            ])
        }
      ]
    }
  })

  const handleSelectionChange = (selection: ClientItem[]): void => {
    selectedRows.value = selection
  }

  const deleteClients = async (rows: ClientItem[], title: string, message: string) => {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'error'
    })
    await fetchDeleteBatchAgents(rows.map((row) => row.id))
    ElMessage.success(t('common.success.delete'))
    refreshData()
  }

  const deleteClient = (row: ClientItem): void => {
    deleteClients(
      [row],
      t('orbien.agent.deleteTitle'),
      t('orbien.agent.deleteConfirm', { name: row.name })
    ).catch(() => {})
  }

  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) return
    deleteClients(
      selectedRows.value,
      t('common.batchDelete'),
      t('orbien.agent.batchDeleteConfirm', { count: selectedRows.value.length })
    ).catch(() => {})
  }

  /**
   * 剔除在线客户端
   */
  const kickoutClient = (row: ClientItem): void => {
    ElMessageBox.confirm(t('orbien.agent.kickoutConfirm'), t('orbien.agent.kickoutTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }).then(async () => {
      await fetchKickoutAgent(row.id)
      ElMessage.success(t('common.success.kickout'))
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
