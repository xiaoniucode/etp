<template>
  <div class="tls-page">
    <div class="tls-page-content">
      <div v-if="matrix" class="tls-summary">
        <span>{{ t('orbien.plugin.tls.summary', { bound: matrix.boundCount, total: matrix.totalDomains }) }}</span>
        <ElTag v-if="matrix.warningCount > 0" type="danger" size="small">
          {{ t('orbien.plugin.tls.warnings', { count: matrix.warningCount }) }}
        </ElTag>
      </div>

      <ElTabs v-model="activeName" type="card">
        <ElTabPane :label="t('orbien.plugin.tls.domainCert')" name="domain-cert">
          <div class="tab-content">
            <div class="toolbar">
              <ElButton type="primary" @click="wizardVisible = true">{{ t('orbien.plugin.tls.freeApply') }}</ElButton>
              <ElButton v-ripple @click="openUploadDialog">{{ t('orbien.plugin.tls.manualUpload') }}</ElButton>
            </div>

            <ArtTable
                row-key="proxyDomainId"
                :show-table-header="false"
                :loading="matrixLoading"
                :data="matrix?.domains || []"
                :columns="domainColumns"
            />
          </div>
        </ElTabPane>

        <ElTabPane :label="t('orbien.plugin.tls.certFolder')" name="cert-folder">
          <div class="cert-folder-content">
            <ArtTable
                row-key="id"
                :show-table-header="false"
                :loading="certLoading"
                :data="certTableData"
                :columns="certColumns"
                :pagination="certPagination"
                @pagination:size-change="handleCertSizeChange"
                @pagination:current-change="handleCertCurrentChange"
            />
          </div>
        </ElTabPane>
      </ElTabs>
    </div>

    <ElDialog v-model="uploadDialogVisible" :title="t('orbien.plugin.tls.uploadAndBind')" width="720px" align-center>
      <div class="upload-form">
        <div class="form-item">
          <div class="form-label">{{ t('orbien.plugin.tls.bindDomains') }}</div>
          <ElSelect v-model="uploadForm.proxyDomainIds" multiple :placeholder="t('orbien.plugin.tls.selectBindDomains')" style="width: 100%">
            <ElOption
                v-for="item in matrix?.domains || []"
                :key="item.proxyDomainId"
                :label="item.fullDomain"
                :value="item.proxyDomainId"
            />
          </ElSelect>
        </div>
        <div class="form-item">
          <div class="form-label">{{ t('orbien.plugin.tls.privateKey') }}</div>
          <ElInput v-model="uploadForm.keyContent" type="textarea" :rows="8" resize="none"/>
        </div>
        <div class="form-item">
          <div class="form-label">{{ t('orbien.plugin.tls.certificate') }}</div>
          <ElInput v-model="uploadForm.certContent" type="textarea" :rows="8" resize="none"/>
        </div>
      </div>
      <template #footer>
        <ElButton @click="uploadDialogVisible = false">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="uploadSubmitting" @click="handleUploadAndBind">{{ t('orbien.plugin.tls.saveAndBind') }}</ElButton>
      </template>
    </ElDialog>

    <PluginCertBindDialog
        v-model:visible="bindDialogVisible"
        :domains="bindDialogDomains"
        @success="handleBindSuccess"
    />

    <PluginAcmeApplyWizard
        v-model:visible="wizardVisible"
        :proxy-id="proxyId"
        :proxy-name="proxyName"
        @success="handleApplySuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, watch, h, computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useTable} from '@/hooks/core/useTable'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import PluginAcmeApplyWizard from './acme-apply-wizard.vue'
import PluginCertBindDialog from './cert-bind-dialog.vue'
import {fetchGetCertListByPage, fetchDeleteCert, fetchSaveAndDeployCert} from '@/api/tls'
import {
  fetchGetProxyCertMatrix,
  fetchBindCert,
  fetchDisableBinding,
  fetchEnableBinding,
  fetchUnbindBinding,
  fetchRedeployBinding
} from '@/api/cert-binding'
import {resolveTlsBindStatusTagType} from '@/utils/ui/status-tag'

defineOptions({name: 'TlsPage'})

const {t, locale} = useI18n()

const props = defineProps<{ proxyId: string; proxyName?: string }>()

const activeName = ref('domain-cert')
const wizardVisible = ref(false)
const matrixLoading = ref(false)
const matrix = ref<Api.CertBinding.ProxyCertMatrix | null>(null)

const uploadDialogVisible = ref(false)
const uploadSubmitting = ref(false)
const uploadForm = reactive({
  proxyDomainIds: [] as number[],
  keyContent: '',
  certContent: ''
})

const bindDialogVisible = ref(false)
const bindDialogDomains = ref<Api.CertBinding.ProxyDomainCertItem[]>([])

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const bindStatusText = (status?: number) => {
  const map: Record<number, string> = {
    1: t('orbien.plugin.tls.bindStatus.normal'),
    2: t('orbien.plugin.tls.bindStatus.disabled'),
    3: t('orbien.plugin.tls.bindStatus.sanMismatch'),
    4: t('orbien.plugin.tls.bindStatus.expired'),
    5: t('orbien.plugin.tls.bindStatus.deployFailed')
  }
  if (!status) {
    return t('orbien.plugin.tls.bindStatus.notConfigured')
  }
  return map[status] || t('orbien.plugin.tls.bindStatus.unknown')
}

const bindStatusTag = (status?: number) => {
  if (!status) {
    return h(ElTag, {type: 'info', size: 'small'}, () => bindStatusText(status))
  }
  return h(ElTag, {type: resolveTlsBindStatusTagType(status), size: 'small'}, () => bindStatusText(status))
}

const isDomainMatchedByCert = (domain: string, cert: Api.Tls.CertDTO) => {
  const sanList = cert.sanDomains || []
  const normalized = domain.trim().toLowerCase()
  return sanList.some((san) => {
    const s = san.trim().toLowerCase()
    if (normalized === s) return true
    if (s.startsWith('*.')) {
      const suffix = s.substring(1)
      return normalized.endsWith(suffix) && normalized.length > suffix.length && !normalized.slice(0, -suffix.length).includes('.')
    }
    return false
  })
}

const domainColumns = computed(() => {
  void locale.value
  return [
    {
      prop: 'fullDomain',
      label: t('orbien.plugin.tls.domain'),
      minWidth: 160
    },
    {
      prop: 'binding',
      label: t('orbien.plugin.tls.certSan'),
      minWidth: 160,
      formatter: (row: Api.CertBinding.ProxyDomainCertItem) =>
          row.binding?.certSanDomains?.join(', ') || ''
    },
    {
      prop: 'notAfter',
      label: t('orbien.plugin.tls.notAfter'),
      width: 120,
      formatter: (row: Api.CertBinding.ProxyDomainCertItem) =>
          row.binding?.notAfter ? formatDate(row.binding.notAfter) : ''
    },
    {
      prop: 'status',
      label: t('orbien.plugin.tls.status'),
      width: 100,
      formatter: (row: Api.CertBinding.ProxyDomainCertItem) =>
          bindStatusTag(row.binding?.bindStatus)
    },
    {
      prop: 'operation',
      label: t('common.actions'),
      width: 220,
      formatter: (row: Api.CertBinding.ProxyDomainCertItem) => {
        const binding = row.binding
        if (!binding) {
          return h(ArtButtonTable, {
            type: 'link',
            text: t('orbien.plugin.tls.bindCert'),
            onClick: () => openBindDialog(row)
          })
        }
        const actions = []
        if (binding.bindStatus === 2) {
          actions.push(
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.plugin.tls.enable'),
                onClick: () => handleEnable(binding.bindingId)
              })
          )
        } else if (binding.bindStatus === 5) {
          actions.push(
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.plugin.tls.retry'),
                onClick: () => handleRedeploy(binding.bindingId)
              })
          )
        } else if (binding.bindStatus === 1) {
          actions.push(
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.plugin.tls.replaceCert'),
                onClick: () => openBindDialog(row)
              }),
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.plugin.tls.disable'),
                onClick: () => handleDisable(binding.bindingId)
              })
          )
        } else {
          actions.push(
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.plugin.tls.replaceCert'),
                onClick: () => openBindDialog(row)
              })
          )
        }
        actions.push(
            h(ArtButtonTable, {
              type: 'link',
              text: t('orbien.plugin.tls.unbind'),
              onClick: () => handleUnbind(binding.bindingId)
            })
        )
        return h('div', actions)
      }
    }
  ]
})

const {
  columns: certColumns,
  data: certTableData,
  loading: certLoading,
  pagination: certPagination,
  handleSizeChange: handleCertSizeChange,
  handleCurrentChange: handleCertCurrentChange,
  getData: reloadCertList
} = useTable({
  core: {
    apiFn: fetchGetCertListByPage,
    apiParams: {current: 1, size: 10},
    columnsFactory: () => {
      void locale.value
      return [
        {
          prop: 'sanDomains',
          label: t('orbien.plugin.tls.authDomain'),
          minWidth: 150,
          formatter: (row: Api.Tls.CertDTO) => row.sanDomains?.join(', ') || ''
        },
        {
          prop: 'issuer',
          label: t('orbien.plugin.tls.issuer'),
          minWidth: 100
        },
        {
          prop: 'notAfter',
          label: t('orbien.plugin.tls.notAfter'),
          minWidth: 120,
          formatter: (row: Api.Tls.CertDTO) => formatDate(row.notAfter)
        },
        {
          prop: 'operation',
          label: t('common.actions'),
          width: 160,
          formatter: (row: Api.Tls.CertDTO) =>
              h('div', [
                h(ArtButtonTable, {
                  type: 'link',
                  text: t('orbien.plugin.tls.bindToProxy'),
                  onClick: () => handleBindCertToProxy(row)
                }),
                h(ArtButtonTable, {
                  type: 'link',
                  text: t('common.delete'),
                  onClick: () => handleCertDelete(row)
                })
              ])
        }
      ]
    }
  }
})

const loadMatrix = async () => {
  if (!props.proxyId) return
  matrixLoading.value = true
  try {
    matrix.value = await fetchGetProxyCertMatrix(props.proxyId)
  } finally {
    matrixLoading.value = false
  }
}

watch(
    () => props.proxyId,
    async () => {
      await Promise.all([loadMatrix(), reloadCertList()])
    },
    {immediate: true}
)

watch(locale, () => {
  reloadCertList()
})

const openUploadDialog = () => {
  uploadForm.proxyDomainIds = (matrix.value?.domains || [])
      .filter((item) => !item.binding)
      .map((item) => item.proxyDomainId)
  uploadForm.keyContent = ''
  uploadForm.certContent = ''
  uploadDialogVisible.value = true
}

const handleUploadAndBind = async () => {
  if (!uploadForm.keyContent.trim() || !uploadForm.certContent.trim()) {
    ElMessage.warning(t('orbien.plugin.tls.keyCertRequired'))
    return
  }
  if (uploadForm.proxyDomainIds.length === 0) {
    ElMessage.warning(t('orbien.plugin.tls.selectDomainRequired'))
    return
  }
  uploadSubmitting.value = true
  try {
    const result = await fetchSaveAndDeployCert({
      proxyId: props.proxyId,
      key: uploadForm.keyContent.trim(),
      fullChain: uploadForm.certContent.trim(),
      proxyDomainIds: uploadForm.proxyDomainIds
    })
    if (result.failedCount > 0) {
      ElMessage.warning(t('orbien.plugin.tls.bindResult', { success: result.successCount, failed: result.failedCount }))
    } else {
      ElMessage.success(t('orbien.plugin.tls.savedAndBound'))
    }
    uploadDialogVisible.value = false
    await Promise.all([loadMatrix(), reloadCertList()])
    activeName.value = 'domain-cert'
  } finally {
    uploadSubmitting.value = false
  }
}

const openBindDialog = (row: Api.CertBinding.ProxyDomainCertItem) => {
  bindDialogDomains.value = [row]
  bindDialogVisible.value = true
}

const handleBindSuccess = async () => {
  await loadMatrix()
}

const handleBindCertToProxy = async (cert: Api.Tls.CertDTO) => {
  const matchedDomainIds = (matrix.value?.domains || [])
      .filter((item) => isDomainMatchedByCert(item.fullDomain, cert))
      .map((item) => item.proxyDomainId)
  if (matchedDomainIds.length === 0) {
    ElMessage.warning(t('orbien.plugin.tls.sanMismatchWarning'))
    return
  }
  await ElMessageBox.confirm(
      t('orbien.plugin.tls.bindToMatched', { count: matchedDomainIds.length }),
      t('orbien.plugin.tls.bindConfirmTitle'),
      { type: 'warning' }
  )
  const result = await fetchBindCert({
    certId: cert.id,
    proxyDomainIds: matchedDomainIds,
    override: true
  })
  if (result.failedCount > 0) {
    ElMessage.warning(t('orbien.plugin.tls.bindResult', { success: result.successCount, failed: result.failedCount }))
  } else {
    ElMessage.success(t('orbien.plugin.tls.bindSuccess'))
  }
  await loadMatrix()
  activeName.value = 'domain-cert'
}

const handleDisable = async (bindingId: number) => {
  await fetchDisableBinding(bindingId)
  ElMessage.success(t('orbien.plugin.tls.disabled'))
  await loadMatrix()
}

const handleEnable = async (bindingId: number) => {
  await fetchEnableBinding(bindingId)
  ElMessage.success(t('orbien.plugin.tls.enabled'))
  await loadMatrix()
}

const handleUnbind = async (bindingId: number) => {
  await ElMessageBox.confirm(t('orbien.plugin.tls.unbindConfirm'), t('orbien.plugin.tls.unbindTitle'), {type: 'warning'})
  await fetchUnbindBinding(bindingId)
  ElMessage.success(t('orbien.plugin.tls.unbound'))
  await loadMatrix()
}

const handleRedeploy = async (bindingId: number) => {
  await fetchRedeployBinding(bindingId)
  ElMessage.success(t('orbien.plugin.tls.redeployed'))
  await loadMatrix()
}

const handleApplySuccess = async () => {
  wizardVisible.value = false
  await Promise.all([loadMatrix(), reloadCertList()])
}

const handleCertDelete = async (row: Api.Tls.CertDTO) => {
  await ElMessageBox.confirm(t('orbien.plugin.tls.deleteCertConfirm'), t('common.warning'), {type: 'warning'})
  await fetchDeleteCert([row.id])
  ElMessage.success(t('common.success.delete'))
  await reloadCertList()
}
</script>

<style scoped>
.tls-page {
  height: 100%;
}

.tls-page-content {
  min-height: 100%;
  padding: 0 15px;
}

.tls-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 14px;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar {
  display: flex;
  gap: 8px;
}

.cert-folder-content {
  min-height: 300px;
}

.upload-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}
</style>
