<template>
  <ElDialog v-model="dialogVisible" :title="$t('orbien.tls.bind.title')" width="720px" align-center>
    <div v-loading="loading">
      <ElAlert
        v-if="certSanText"
        type="info"
        :closable="false"
        show-icon
        class="mb-4"
        :title="$t('orbien.tls.bind.certSan', { san: certSanText })"
      />
      <ElTable
        ref="tableRef"
        row-key="proxyDomainId"
        :data="domainList"
        max-height="420"
        @selection-change="handleSelectionChange"
      >
        <ElTableColumn type="selection" width="48" :selectable="isRowSelectable" />
        <ElTableColumn prop="fullDomain" :label="$t('orbien.tls.bind.columns.domain')" min-width="180" />
        <ElTableColumn prop="proxyName" :label="$t('orbien.tls.bind.columns.proxy')" min-width="120" />
        <ElTableColumn :label="$t('orbien.tls.bind.columns.matchStatus')" width="110">
          <template #default="{ row }">
            <ElTag :type="row.matched ? 'primary' : 'danger'" size="small">
              {{ row.matched ? $t('orbien.tls.bind.match.matched') : $t('orbien.tls.bind.match.unmatched') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('orbien.tls.bind.columns.currentCert')" min-width="120">
          <template #default="{ row }">
            <span v-if="row.hasBinding" class="text-warning">{{ $t('orbien.tls.bind.match.bound') }}</span>
            <span v-else class="text-secondary">{{ $t('orbien.tls.bind.match.unbound') }}</span>
          </template>
        </ElTableColumn>
      </ElTable>
      <div v-if="selectedRows.length" class="selection-tip">
        {{ $t('orbien.tls.bind.selection', { n: selectedRows.length }) }}
        <span v-if="overrideCount">{{ $t('orbien.tls.bind.overrideHint', { n: overrideCount }) }}</span>
      </div>
    </div>

    <template #footer>
      <ElButton @click="handleCancel">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" :disabled="selectedRows.length === 0" :loading="submitting" @click="handleSubmit">
        {{ $t('orbien.tls.bind.confirmBind') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { fetchListBindableDomains, fetchBindCert } from '@/api/cert-binding'
  import { fetchGetCertListByPage } from '@/api/tls'

  defineOptions({ name: 'BindDialog' })

  const { t } = useI18n()

  interface Props {
    visible: boolean
    certId: string | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), { certId: null })
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const submitting = ref(false)
  const domainList = ref<Api.CertBinding.PreviewItem[]>([])
  const selectedRows = ref<Api.CertBinding.PreviewItem[]>([])
  const certSanText = ref('')

  const overrideCount = computed(
    () => selectedRows.value.filter((row) => row.hasBinding).length
  )

  const isRowSelectable = (row: Api.CertBinding.PreviewItem) => row.matched

  const loadData = async () => {
    if (!props.certId) return
    loading.value = true
    try {
      const [domains, certPage] = await Promise.all([
        fetchListBindableDomains(props.certId),
        fetchGetCertListByPage({ current: 1, size: 100 })
      ])
      domainList.value = domains || []
      const cert = certPage.records?.find((item) => item.id === props.certId)
      certSanText.value = cert?.sanDomains?.join(', ') || ''
      selectedRows.value = []
    } finally {
      loading.value = false
    }
  }

  watch(dialogVisible, (visible) => {
    if (visible) {
      loadData()
    }
  })

  const handleSelectionChange = (rows: Api.CertBinding.PreviewItem[]) => {
    selectedRows.value = rows
  }

  const handleCancel = () => {
    dialogVisible.value = false
  }

  const handleSubmit = async () => {
    if (!props.certId || selectedRows.value.length === 0) return

    const override = overrideCount.value > 0
    if (override) {
      await ElMessageBox.confirm(t('orbien.tls.bind.confirmOverride'), t('orbien.tls.bind.confirmOverrideTitle'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
    }

    submitting.value = true
    try {
      const result = await fetchBindCert({
        certId: props.certId,
        proxyDomainIds: selectedRows.value.map((row) => row.proxyDomainId),
        override: true
      })
      if (result.failedCount > 0) {
        ElMessage.warning(t('orbien.tls.bind.resultPartial', { success: result.successCount, failed: result.failedCount }))
      } else {
        ElMessage.success(t('orbien.tls.bind.resultSuccess', { n: result.successCount }))
      }
      dialogVisible.value = false
      emit('submit')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .mb-4 {
    margin-bottom: 16px;
  }

  .selection-tip {
    margin-top: 12px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .text-warning {
    color: var(--el-color-warning);
  }

  .text-secondary {
    color: var(--el-text-color-secondary);
  }
</style>
