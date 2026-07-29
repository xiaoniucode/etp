<template>
  <div class="dns-credential-panel">
    <ArtTableHeader :loading="loading" @refresh="loadData">
      <template #left>
        <ElButton type="primary" @click="handleAdd" v-ripple>{{ $t('orbien.tls.dns.addCredential') }}</ElButton>
      </template>
    </ArtTableHeader>

    <ArtTable :loading="loading" :data="data" :columns="columns" :show-pagination="false"/>

    <DnsCredentialDialog
        v-model:visible="dialogVisible"
        :record="currentRecord"
        @submit="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, h, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import DnsCredentialDialog from './dns-credential-dialog.vue'
import {
  fetchDeleteDnsCredential,
  fetchDnsCredentialList,
  fetchTestDnsCredential
} from '@/api/dns-credential'
import {resolveDnsCredentialStatusTagType} from '@/utils/ui/status-tag'

defineOptions({name: 'DnsCredentialPanel'})

const {t} = useI18n()

const loading = ref(false)
const data = ref<Api.DnsCredential.CredentialDTO[]>([])
const dialogVisible = ref(false)
const currentRecord = ref<Api.DnsCredential.CredentialDTO | null>(null)

const statusTag = (status: number) => {
  const type = resolveDnsCredentialStatusTagType(status)
  const label =
      status === 1
          ? t('orbien.tls.dns.status.ok')
          : status === 2
            ? t('orbien.tls.dns.status.invalid')
            : t('orbien.tls.dns.status.untested')
  return h(ElTag, {type, size: 'small'}, () => label)
}

const formatDateTime = (value?: string) => value || ''

const columns = computed(() => [
  {prop: 'name', label: t('common.name'), minWidth: 140},
  {prop: 'providerLabel', label: t('orbien.tls.dns.columns.provider'), width: 120},
  {prop: 'accountHint', label: t('orbien.tls.dns.columns.accountHint'), minWidth: 160},
  {
    prop: 'status',
    label: t('common.status'),
    width: 90,
    formatter: (row: Api.DnsCredential.CredentialDTO) => statusTag(row.status)
  },
  {
    prop: 'lastTestAt',
    label: t('orbien.tls.dns.columns.lastTest'),
    width: 170,
    formatter: (row: Api.DnsCredential.CredentialDTO) => formatDateTime(row.lastTestAt)
  },
  {prop: 'lastTestMessage', label: t('orbien.tls.dns.columns.testMessage'), minWidth: 140},
  {
    prop: 'operation',
    label: t('common.actions'),
    width: 180,
    fixed: 'right' as const,
    formatter: (row: Api.DnsCredential.CredentialDTO) =>
        h('div', [
          h(ArtButtonTable, {
            type: 'link',
            text: t('orbien.tls.dns.actions.test'),
            onClick: () => handleTest(row)
          }),
          h(ArtButtonTable, {
            type: 'link',
            text: t('common.edit'),
            onClick: () => handleEdit(row)
          }),
          h(ArtButtonTable, {
            type: 'link',
            text: t('common.delete'),
            onClick: () => handleDelete(row)
          })
        ])
  }
])

const loadData = async () => {
  loading.value = true
  try {
    data.value = await fetchDnsCredentialList()
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  currentRecord.value = null
  dialogVisible.value = true
}

const handleEdit = (row: Api.DnsCredential.CredentialDTO) => {
  currentRecord.value = row
  dialogVisible.value = true
}

const handleTest = async (row: Api.DnsCredential.CredentialDTO) => {
  try {
    await fetchTestDnsCredential(row.id)
    ElMessage.success(t('orbien.tls.dns.messages.testSuccess'))
    loadData()
  } catch {
    loadData()
  }
}

const handleDelete = async (row: Api.DnsCredential.CredentialDTO) => {
  try {
    await ElMessageBox.confirm(
        t('orbien.tls.dns.messages.confirmDelete', {name: row.name}),
        t('orbien.tls.dns.messages.deleteTitle'),
        {type: 'warning'}
    )
    await fetchDeleteDnsCredential(row.id)
    ElMessage.success(t('common.success.delete'))
    loadData()
  } catch (error) {
    if (error === 'cancel') return
  }
}

onMounted(loadData)

defineExpose({loadData})
</script>
