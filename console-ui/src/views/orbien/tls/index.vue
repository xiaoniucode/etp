<template>
  <div class="tls-page art-full-height">
    <ElCard class="art-table-card tls-table-card">
      <ElTabs v-model="activeTab" class="tls-tabs" type="card">
        <ElTabPane :label="$t('orbien.tls.tabs.certs')" name="certs">
          <div class="tab-panel-content">
            <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
              <template #left>
                <ElSpace wrap>
                  <ElButton type="primary" @click="wizardVisible = true" v-ripple>{{ $t('orbien.tls.actions.applyFree') }}</ElButton>
                  <ElButton v-ripple @click="handleAdd">{{ $t('orbien.tls.actions.upload') }}</ElButton>
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
          </div>
        </ElTabPane>

        <ElTabPane :label="$t('orbien.tls.tabs.orders')" name="orders">
          <div class="tab-panel-content">
            <AcmeOrderPanel ref="orderPanelRef" @apply="wizardVisible = true"/>
          </div>
        </ElTabPane>

        <ElTabPane :label="$t('orbien.tls.tabs.dns')" name="dns">
          <div class="tab-panel-content">
            <DnsCredentialPanel ref="dnsPanelRef"/>
          </div>
        </ElTabPane>
      </ElTabs>
    </ElCard>

    <TlsDialog v-model:visible="dialogVisible" @submit="handleUploadSubmit"/>
    <BindDialog v-model:visible="bindDialogVisible" :cert-id="currentCertId" @submit="handleBindSubmit"/>
    <AcmeApplyWizard
        v-model:visible="wizardVisible"
        @success="handleApplySuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {ref, h} from 'vue'
import {useI18n} from 'vue-i18n'
import {useTable} from '@/hooks/core/useTable'
import {ElMessage, ElMessageBox, ElTag, ElSwitch} from 'element-plus'
import TlsDialog from './modules/tls-dialog.vue'
import BindDialog from './modules/bind-dialog.vue'
import AcmeOrderPanel from './modules/acme-order-panel.vue'
import DnsCredentialPanel from './modules/dns-credential-panel.vue'
import AcmeApplyWizard from './modules/acme-apply-wizard.vue'
import {fetchGetCertListByPage, fetchDownloadCert, fetchDeleteCert, fetchUpdateCertAutoRenew} from '@/api/tls'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {downloadBlob} from '@/utils/download'

defineOptions({name: 'TlsManagement'})

const {t} = useI18n()

type TlsItem = Api.Tls.CertDTO

const activeTab = ref('certs')
const selectedRows = ref<TlsItem[]>([])
const dialogVisible = ref(false)
const bindDialogVisible = ref(false)
const wizardVisible = ref(false)
const currentCertId = ref<string | null>(null)
const orderPanelRef = ref<InstanceType<typeof AcmeOrderPanel>>()
const dnsPanelRef = ref<InstanceType<typeof DnsCredentialPanel>>()
const renewingCertId = ref<string | null>(null)

const sourceLabel = (source?: number) => {
  if (source === 2) {
    return h(ElTag, {type: 'primary', size: 'small'}, () => 'ACME')
  }
  return h(ElTag, {type: 'warning', size: 'small'}, () => t('orbien.tls.source.manual'))
}

const getExpireDays = (item: TlsItem) => {
  const now = new Date()
  const notAfter = new Date(item.notAfter)
  if (now > notAfter) {
    return h('span', {style: {color: 'var(--el-color-danger)'}}, t('orbien.tls.expire.expired'))
  }
  const diffTime = notAfter.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return h('span', {style: {color: 'var(--el-color-primary)'}}, t('orbien.tls.expire.remainingDays', {n: diffDays}))
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
    apiFn: fetchGetCertListByPage,
    apiParams: {
      current: 1,
      size: 10
    },
    columnsFactory: () => [
      {type: 'selection'},
      {
        prop: 'sanDomains',
        label: t('orbien.tls.columns.sanDomains'),
        minWidth: 120,
        formatter: (row: TlsItem) => row.sanDomains?.join(', ') || ''
      },
      {
        prop: 'source',
        label: t('orbien.tls.columns.source'),
        width: 90,
        formatter: (row: TlsItem) => sourceLabel(row.source)
      },
      {
        prop: 'org',
        label: t('orbien.tls.columns.org'),
        minWidth: 100
      },
      {
        prop: 'issuer',
        label: t('orbien.tls.columns.issuer'),
        minWidth: 120
      },
      {
        prop: 'boundDomainCount',
        label: t('orbien.tls.columns.boundDomainCount'),
        width: 100,
        formatter: (row: TlsItem) => row.boundDomainCount ?? 0
      },
      {
        prop: 'notAfter',
        label: t('orbien.tls.columns.notAfter'),
        minWidth: 110,
        formatter: (row: TlsItem) => getExpireDays(row)
      },
      {
        prop: 'autoRenew',
        label: t('orbien.tls.columns.autoRenew'),
        width: 100,
        formatter: (row: TlsItem) => {
          if (row.source !== 2) {
            return ''
          }
          const now = new Date()
          const notAfter = new Date(row.notAfter)
          if (now > notAfter) {
            return ''
          }
          return h(ElSwitch, {
            modelValue: row.autoRenew ?? false,
            size: 'small',
            loading: renewingCertId.value === row.id,
            onChange: (value: string | number | boolean) => handleAutoRenewChange(row, Boolean(value))
          })
        }
      },
      {
        prop: 'operation',
        label: t('common.actions'),
        width: 150,
        fixed: 'right',
        formatter: (row: TlsItem) => {
          const now = new Date()
          const notAfter = new Date(row.notAfter)
          const isExpired = now > notAfter
          const children = []
          if (!isExpired) {
            children.push(
                h(ArtButtonTable, {
                  type: 'link',
                  text: t('orbien.tls.actions.bind'),
                  onClick: () => handleBind(row)
                }),
                h(ArtButtonTable, {
                  type: 'link',
                  text: t('orbien.tls.actions.download'),
                  onClick: () => handleDownload(row)
                })
            )
          }
          children.push(
              h(ArtButtonTable, {
                type: 'link',
                text: t('common.delete'),
                onClick: () => handleDelete(row)
              })
          )
          return h('div', children)
        }
      }
    ]
  }
})

const handleSelectionChange = (selection: TlsItem[]): void => {
  selectedRows.value = selection
}

const handleAdd = () => {
  dialogVisible.value = true
}

const handleUploadSubmit = () => {
  refreshData()
}

const handleBindSubmit = () => {
  refreshData()
}

const handleApplySuccess = () => {
  activeTab.value = 'orders'
  orderPanelRef.value?.refreshData()
  refreshData()
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t('orbien.tls.messages.selectCertsToDelete'))
    return
  }

  try {
    await ElMessageBox.confirm(t('orbien.tls.messages.confirmBatchDeleteCerts'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    const ids = selectedRows.value.map((row) => row.id)
    await fetchDeleteCert(ids)
    ElMessage.success(t('common.success.delete'))
    refreshData()
  } catch (error) {
    if (error === 'cancel') return
  }
}

const handleBind = (row: TlsItem) => {
  currentCertId.value = row.id || null
  bindDialogVisible.value = true
}

const handleAutoRenewChange = async (row: TlsItem, autoRenew: boolean) => {
  if (!row.id) return
  renewingCertId.value = row.id
  try {
    const result = await fetchUpdateCertAutoRenew(row.id, autoRenew)
    row.autoRenew = autoRenew
    if (autoRenew && result.acmeRenewJobAutoEnabled) {
      ElMessage.success(t('orbien.tls.messages.autoRenewEnabledWithJob'))
    } else {
      ElMessage.success(autoRenew ? t('orbien.tls.messages.autoRenewEnabled') : t('orbien.tls.messages.autoRenewDisabled'))
    }
  } catch {
    row.autoRenew = !autoRenew
  } finally {
    renewingCertId.value = null
  }
}

const handleDownload = async (row: TlsItem) => {
  try {
    const blob = await fetchDownloadCert(row.id)
    const fileName = `${row.sanDomains?.join('_') || 'cert'}.zip`
    downloadBlob(blob, fileName)
  } catch (error: any) {
    ElMessage.error(error?.message || t('orbien.tls.messages.downloadFailed'))
  }
}

const handleDelete = async (row: TlsItem) => {
  try {
    await ElMessageBox.confirm(t('orbien.tls.messages.confirmDeleteCert'), t('orbien.tls.messages.deleteCertTitle'), {
      confirmButtonText: t('common.delete'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await fetchDeleteCert([row.id])
    ElMessage.success(t('common.success.delete'))
    refreshData()
  } catch (error) {
    if (error === 'cancel') return
  }
}
</script>

<style lang="scss" scoped>
.tls-table-card {
  :deep(.el-card__body) {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
  }
}

.tls-tabs {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;

  :deep(.el-tabs__header) {
    flex-shrink: 0;
    margin-bottom: 16px;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    min-height: 0;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }
}

.tab-panel-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;

  :deep(.acme-order-panel),
  :deep(.dns-credential-panel) {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;
  }

  :deep(.art-table) {
    flex: 1;
    min-height: 0;
  }
}

:deep(.el-dialog__body) {
  padding: 0 !important;
}
</style>
