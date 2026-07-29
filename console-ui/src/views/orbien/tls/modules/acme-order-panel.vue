<template>
  <div class="acme-order-panel">
    <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
      <template #left>
        <ElSpace wrap>
          <ElButton type="primary" @click="emit('apply')" v-ripple>{{ $t('orbien.tls.actions.applyFree') }}</ElButton>
          <ElButton @click="handleBatchDelete" v-ripple :disabled="selectedRows.length === 0">
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

    <AcmeOrderDetailDrawer
        v-model:visible="detailVisible"
        :order-id="currentOrderId"
        @changed="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
import {h, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import AcmeOrderDetailDrawer from './acme-order-detail-drawer.vue'
import {
  fetchAcmeOrderPage,
  fetchCancelAcmeOrder,
  fetchDeleteAcmeOrders,
  fetchRetryAcmeOrder,
  fetchVerifyAcmeOrder
} from '@/api/acme-order'
import {resolveAcmeOrderStatusTagType} from '@/utils/ui/status-tag'

defineOptions({name: 'AcmeOrderPanel'})

const {t} = useI18n()

interface Emits {
  (e: 'apply'): void
}

const emit = defineEmits<Emits>()

const detailVisible = ref(false)
const currentOrderId = ref<number | null>(null)
const selectedRows = ref<Api.AcmeOrder.OrderDTO[]>([])

const openDetail = (row: Api.AcmeOrder.OrderDTO) => {
  currentOrderId.value = row.id
  detailVisible.value = true
}

const handleSelectionChange = (selection: Api.AcmeOrder.OrderDTO[]) => {
  selectedRows.value = selection
}

const handleVerify = async (row: Api.AcmeOrder.OrderDTO) => {
  await fetchVerifyAcmeOrder(row.id)
  ElMessage.success(t('orbien.tls.acme.order.messages.verifyStarted'))
  refreshData()
}

const handleRetry = async (row: Api.AcmeOrder.OrderDTO) => {
  await fetchRetryAcmeOrder(row.id)
  ElMessage.success(t('orbien.tls.acme.order.messages.retrySubmitted'))
  refreshData()
}

const handleCancel = async (row: Api.AcmeOrder.OrderDTO) => {
  await fetchCancelAcmeOrder(row.id)
  ElMessage.success(t('orbien.tls.acme.order.messages.cancelled'))
  refreshData()
}

const handleDelete = async (row: Api.AcmeOrder.OrderDTO) => {
  try {
    await ElMessageBox.confirm(
        t('orbien.tls.acme.order.messages.confirmDelete', {orderNo: row.orderNo}),
        t('orbien.tls.dns.messages.deleteTitle'),
        {
          confirmButtonText: t('common.delete'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
    )
    await fetchDeleteAcmeOrders([row.id])
    ElMessage.success(t('common.success.delete'))
    if (currentOrderId.value === row.id) {
      detailVisible.value = false
      currentOrderId.value = null
    }
    refreshData()
  } catch (error) {
    if (error === 'cancel') return
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t('orbien.tls.acme.order.messages.selectToDelete'))
    return
  }
  try {
    await ElMessageBox.confirm(
        t('orbien.tls.acme.order.messages.confirmBatchDelete', {n: selectedRows.value.length}),
        t('orbien.tls.acme.order.messages.batchDeleteTitle'),
        {
          confirmButtonText: t('common.delete'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
    )
    const ids = selectedRows.value.map((row) => row.id)
    await fetchDeleteAcmeOrders(ids)
    ElMessage.success(t('common.success.delete'))
    selectedRows.value = []
    detailVisible.value = false
    currentOrderId.value = null
    refreshData()
  } catch (error) {
    if (error === 'cancel') return
  }
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
    apiFn: fetchAcmeOrderPage,
    apiParams: {current: 1, size: 10},
    columnsFactory: () => [
      {type: 'selection'},
      {prop: 'orderNo', label: t('orbien.tls.acme.order.columns.orderNo'), minWidth: 160},
      {
        prop: 'domains',
        label: t('orbien.tls.acme.order.columns.domains'),
        minWidth: 180,
        formatter: (row: Api.AcmeOrder.OrderDTO) => row.domains?.join(', ') || ''
      },
      {
        prop: 'validationMode',
        label: t('orbien.tls.acme.order.columns.validationMode'),
        width: 120,
        formatter: (row: Api.AcmeOrder.OrderDTO) =>
            row.validationMode === 2
                ? t('orbien.tls.acme.order.validationMode.cloud')
                : t('orbien.tls.acme.order.validationMode.manual')
      },
      {
        prop: 'status',
        label: t('common.status'),
        width: 110,
        formatter: (row: Api.AcmeOrder.OrderDTO) =>
            h(
                ElTag,
                {type: resolveAcmeOrderStatusTagType(row.status), size: 'small'},
                () => row.statusLabel
            )
      },
      {prop: 'createdAt', label: t('common.createTime'), width: 170},
      {
        prop: 'operation',
        label: t('common.actions'),
        width: 240,
        fixed: 'right',
        formatter: (row: Api.AcmeOrder.OrderDTO) => {
          const actions = [
            h(ArtButtonTable, {
              type: 'link',
              text: t('common.detail'),
              onClick: () => openDetail(row)
            })
          ]
          if ([1, 2].includes(row.status) && row.validationMode === 1) {
            actions.push(
                h(ArtButtonTable, {
                  type: 'link',
                  text: t('orbien.tls.acme.order.actions.verify'),
                  onClick: () => handleVerify(row)
                })
            )
          }
          if (row.status === 6) {
            actions.push(
                h(ArtButtonTable, {
                  type: 'link',
                  text: t('orbien.tls.acme.order.actions.retry'),
                  onClick: () => handleRetry(row)
                })
            )
          }
          if (![5, 6, 7].includes(row.status)) {
            actions.push(
                h(ArtButtonTable, {
                  type: 'link',
                  text: t('orbien.tls.acme.order.actions.cancel'),
                  onClick: () => handleCancel(row)
                })
            )
          }
          actions.push(
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.delete'),
                onClick: () => handleDelete(row)
              })
          )
          return h('div', actions)
        }
      }
    ]
  }
})

defineExpose({refreshData})
</script>
