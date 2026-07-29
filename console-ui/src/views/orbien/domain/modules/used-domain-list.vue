<template>
  <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData"/>

  <ArtTable
      :loading="loading"
      :data="data"
      :columns="columns"
      :pagination="pagination"
      @pagination:size-change="handleSizeChange"
      @pagination:current-change="handleCurrentChange"
  />
</template>

<script setup lang="ts">
import {h} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import type {RouteLocationRaw} from 'vue-router'
import {ElTag} from 'element-plus'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useTable} from '@/hooks/core/useTable'
import {ProtocolType, getDomainTypeLabel} from '@/enums/orbien/business'
import {fetchGetUsedDomainListByPage} from '@/api/domain'

defineOptions({name: 'UsedDomainList'})

const { t } = useI18n()

const emit = defineEmits<{ change: [] }>()
const router = useRouter()

type UsedDomainItem = Api.Domain.UsedDomainDTO

const PROTOCOL_LIST_ROUTE: Partial<Record<ProtocolType, RouteLocationRaw>> = {
  [ProtocolType.HTTP]: {name: 'HTTP'},
  [ProtocolType.HTTPS]: {name: 'HTTPS'},
  [ProtocolType.TCP]: {name: 'TCP'},
  [ProtocolType.UDP]: {name: 'UDP'},
  [ProtocolType.SOCKS5]: {name: 'SOCKS5'},
  [ProtocolType.FILE]: {name: 'FileShare'}
}

const resolveProxyListRoute = (protocol?: number | null): RouteLocationRaw | null => {
  if (protocol == null) return null
  return PROTOCOL_LIST_ROUTE[protocol as ProtocolType] ?? null
}

const renderProxyLink = (row: UsedDomainItem) => {
  const label = row.proxyName || row.proxyId || ''
  if (!label) return ''

  const route = resolveProxyListRoute(row.protocol)
  if (!route) return label

  return h(ArtButtonTable, {
    type: 'link',
    text: label,
    onClick: () => router.push(route)
  })
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
    apiFn: fetchGetUsedDomainListByPage,
    apiParams: {
      current: 1,
      size: 20
    },
    columnsFactory: () => [
      {
        prop: 'fullDomain',
        label: t('orbien.common.fullDomain')
      },
      {
        prop: 'domainType',
        label: t('orbien.common.domainType'),
        formatter: (row: UsedDomainItem) => {
          const config = getDomainTypeLabel(row.domainType)
          return h(ElTag, {type: config.type, size: 'small'}, () => config.text)
        }
      },
      {
        prop: 'proxyName',
        label: t('orbien.common.linkedProxy'),
        formatter: (row: UsedDomainItem) => renderProxyLink(row)
      },
      {
        prop: 'rootDomain',
        label: t('orbien.common.rootDomain'),
        formatter: (row: UsedDomainItem) => row.rootDomain || ''
      }
    ]
  },
  hooks: {
    onSuccess: () => emit('change')
  }
})
</script>
