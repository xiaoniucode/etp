<template>
  <div class="cluster-page" v-loading="loading">
    <div class="mb-6">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.cluster.title') }}</h3>
      <div class="flex items-center gap-3">
        <span class="w-20 font-medium shrink-0">{{ t('orbien.plugin.cluster.strategy') }}</span>
        <ElSelect v-model="loadBalanceStrategy" :placeholder="t('orbien.plugin.cluster.selectStrategy')"
                  style="width: 240px">
          <ElOption
              v-for="item in loadBalanceOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </ElSelect>
      </div>
    </div>

    <div>
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold">{{ t('orbien.plugin.cluster.serviceList') }}</h3>
        <ElButton type="primary" size="small" @click="addTarget">
          <template #icon>
            <Plus/>
          </template>
          {{ t('orbien.plugin.actions.addService') }}
        </ElButton>
      </div>

      <div class="border border-gray-200 rounded p-4">
        <ElEmpty v-if="targets.length === 0" :description="t('orbien.plugin.cluster.emptyServices')" :image-size="72">
          <ElButton type="primary" size="small" @click="addTarget">{{
              t('orbien.plugin.actions.addService')
            }}
          </ElButton>
        </ElEmpty>

        <ElTable v-else :data="targets" border style="width: 100%">
          <ElTableColumn prop="name" :label="t('orbien.plugin.cluster.serviceName')" min-width="140">
            <template #default="{ row }">
              <ElInput v-model="row.name" size="small" :placeholder="t('orbien.plugin.cluster.optional')" clearable/>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="host" :label="t('orbien.plugin.cluster.host')" min-width="160">
            <template #default="{ row }">
              <ElInput v-model="row.host" size="small" :placeholder="t('orbien.plugin.cluster.hostPlaceholder')"
                       clearable/>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="port" :label="t('orbien.plugin.cluster.port')" width="110">
            <template #default="{ row }">
              <ElInput v-model.number="row.port" size="small" type="number"
                       :placeholder="t('orbien.plugin.cluster.portPlaceholder')"/>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="weight" :label="t('orbien.plugin.cluster.weight')" width="100">
            <template #default="{ row }">
              <ElInput
                  v-model.number="row.weight"
                  size="small"
                  type="number"
                  :placeholder="t('orbien.plugin.cluster.weightPlaceholder')"
                  :disabled="loadBalanceStrategy !== LoadBalanceType.WEIGHT"
              />
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('common.actions')" width="80" align="center" fixed="right">
            <template #default="{ $index }">
              <ElButton link size="small" @click="removeTarget($index)">
                <template #icon>
                  <Delete/>
                </template>
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </div>

    <div class="mt-5">
      <ElButton type="primary" :loading="saving" @click="handleSave">{{
          t('orbien.plugin.basic.saveConfig')
        }}
      </ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, watch, computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage} from 'element-plus'
import {Plus, Delete} from '@element-plus/icons-vue'
import {fetchProxyDetail, saveProxyClusterConfig} from '@/api/proxy-plugin'
import type {ProxyConfigProtocol} from '../../menus'
import {LoadBalanceType} from '@/enums/orbien/business'

defineOptions({name: 'ClusterPage'})

const {t} = useI18n()

interface TargetRow {
  host: string
  port: number | string
  weight: number
  name: string
}

const props = defineProps<{
  proxyId: string
  protocol: ProxyConfigProtocol
}>()

const loadBalanceOptions = computed(() => [
  {label: t('orbien.plugin.cluster.strategyOptions.roundRobin'), value: LoadBalanceType.ROUND_ROBIN},
  {label: t('orbien.plugin.cluster.strategyOptions.weight'), value: LoadBalanceType.WEIGHT},
  {label: t('orbien.plugin.cluster.strategyOptions.random'), value: LoadBalanceType.RANDOM},
  {label: t('orbien.plugin.cluster.strategyOptions.leastConn'), value: LoadBalanceType.LEAST_CONN}
])

const loading = ref(false)
const saving = ref(false)
const loadBalanceStrategy = ref<LoadBalanceType>(LoadBalanceType.ROUND_ROBIN)
const targets = ref<TargetRow[]>([])
let detailSnapshot: Awaited<ReturnType<typeof fetchProxyDetail>> | null = null

const toTargetRow = (target: Api.Proxy.TargetDTO): TargetRow => ({
  host: target.host || '',
  port: target.port || '',
  weight: target.weight || 1,
  name: target.name || ''
})

const normalizeWeights = () => {
  if (loadBalanceStrategy.value === LoadBalanceType.WEIGHT) return
  targets.value.forEach((row) => {
    row.weight = 1
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const detail = await fetchProxyDetail(props.protocol, props.proxyId)
    detailSnapshot = detail
    const loadBalance = 'loadBalance' in detail ? detail.loadBalance : undefined
    const detailTargets = 'targets' in detail ? detail.targets : undefined
    loadBalanceStrategy.value =
        (loadBalance?.strategy as LoadBalanceType | undefined) ?? LoadBalanceType.ROUND_ROBIN
    targets.value = detailTargets?.map(toTargetRow) || []
    normalizeWeights()
  } finally {
    loading.value = false
  }
}

watch(loadBalanceStrategy, normalizeWeights)

watch(
    () => [props.proxyId, props.protocol] as const,
    ([proxyId]) => {
      if (proxyId) loadData()
    },
    {immediate: true}
)

const addTarget = () => {
  targets.value.push({host: '127.0.0.1', port: '', weight: 1, name: ''})
}

const removeTarget = (index: number) => {
  targets.value.splice(index, 1)
}

const validateTargets = () => {
  if (targets.value.length === 0) {
    ElMessage.warning(t('orbien.plugin.cluster.minOneService'))
    return false
  }

  for (let i = 0; i < targets.value.length; i++) {
    const {host, port} = targets.value[i]
    if (!host?.trim()) {
      ElMessage.warning(t('orbien.plugin.cluster.hostRequired', {row: i + 1}))
      return false
    }
    const portNum = Number(port)
    if (!portNum || portNum < 1 || portNum > 65535) {
      ElMessage.warning(t('orbien.plugin.cluster.portInvalid', {row: i + 1}))
      return false
    }
  }

  return true
}

const handleSave = async () => {
  if (!detailSnapshot || !validateTargets()) return

  saving.value = true
  try {
    await saveProxyClusterConfig(
        props.protocol,
        detailSnapshot,
        targets.value.map((row) => ({
          host: row.host.trim(),
          port: Number(row.port),
          weight: row.weight || 1,
          name: row.name?.trim() || row.host.trim()
        })),
        {strategy: loadBalanceStrategy.value}
    )
    await loadData()
  } finally {
    saving.value = false
  }
}
</script>
