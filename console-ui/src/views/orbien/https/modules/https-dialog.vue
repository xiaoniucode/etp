<template>
  <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? t('orbien.https.add') : t('orbien.https.edit')"
      width="650px"
      align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="120px" :show-message="false">
      <ElFormItem :label="t('orbien.proxy.client')" prop="agentId">
        <ElSelect
            v-model="formData.agentId"
            :placeholder="t('orbien.proxy.selectClient')"
            :disabled="dialogType === 'edit'"
            style="width: 250px"
        >
          <ElOption
              v-for="agent in agents"
              :key="agent.id"
              :label="formatAgentOptionLabel(agent)"
              :value="agent.id"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem :label="t('orbien.proxy.name')" prop="name">
        <ElInput v-model="formData.name" :placeholder="t('orbien.proxy.enterName')" clearable/>
      </ElFormItem>

      <ElFormItem :label="t('orbien.proxy.domainType')" prop="domainType">
        <ElRadioGroup v-model="formData.domainType">
          <ElRadio :label="String(DomainType.AUTO)">{{ t('orbien.proxy.auto') }}</ElRadio>
          <ElRadio :label="String(DomainType.SUBDOMAIN)">{{ t('orbien.proxy.subdomain') }}</ElRadio>
          <ElRadio :label="String(DomainType.CUSTOM_DOMAIN)">{{ t('orbien.proxy.customDomain') }}</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem v-if="formData.domainType === String(DomainType.SUBDOMAIN)">
        <SubdomainBindingRows
            v-model="formData.subdomainBindings"
            :root-domains="rootDomains"
            :loading="rootDomainLoading"
            :error-indexes="subdomainErrorIndexes"
            @clear-error="resetSubdomainErrors"
        />
      </ElFormItem>

      <ElFormItem
          v-if="formData.domainType === String(DomainType.CUSTOM_DOMAIN)"
          :label="t('orbien.proxy.customDomain')"
          prop="customDomains"
      >
        <ElInput
            v-model="formData.customDomains"
            type="textarea"
            :rows="3"
            :placeholder="t('orbien.proxy.customDomainPlaceholder')"
        />
      </ElFormItem>

      <BackendServiceField
          :cluster-mode="clusterMode"
          :targets="targets"
          @open-cluster="handleOpenCluster"
      >
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElInput v-model="formData.localHost" :placeholder="t('orbien.proxy.hostPlaceholder')"/>
          </ElCol>
          <ElCol :span="12">
            <LocalPortInput v-model="formData.localPort" :presets="commonLocalPortPresets"/>
          </ElCol>
        </ElRow>
      </BackendServiceField>

      <ElFormItem :label="t('orbien.proxy.forceHttps')">
        <ElSwitch v-model="formData.forceHttps"/>
      </ElFormItem>

      <BandwidthLimitField v-model="formData.bandwidth"/>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="handleSubmit">{{ t('common.submit') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {ref, reactive, watch, computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage} from 'element-plus'
import type {FormInstance, FormRules} from 'element-plus'
import {DialogType} from '@/types'
import {fetchGetAgentsForProxySelection} from '@/api/agent'
import {fetchCreateHttpsProxy, fetchUpdateHttpsProxy, fetchGetHttpsProxyById} from '@/api/proxy'
import {DomainType} from '@/enums/orbien/business'
import {formatAgentOptionLabel} from '@/views/orbien/agent/shared/format-agent-option'
import {
  useRootDomainOptions,
  validateSubdomainBindings,
  buildSubdomainBindingsPayload
} from '@/views/orbien/proxy/shared/use-root-domain-options'
import SubdomainBindingRows from '@/views/orbien/proxy/shared/subdomain-binding-rows.vue'
import BackendServiceField from '@/views/orbien/proxy/shared/backend-service-field.vue'
import LocalPortInput from '@/views/orbien/proxy/shared/local-port-input.vue'
import BandwidthLimitField from '@/views/orbien/proxy/shared/bandwidth-limit-field.vue'
import {
  BANDWIDTH_RULES,
  toBandwidthPayload,
  type BandwidthMbps
} from '@/views/orbien/proxy/shared/bandwidth-limit'
import {getCommonLocalPortPresets} from '@/views/orbien/proxy/shared/port-presets'
import {isClusterMode} from '@/views/orbien/proxy/shared/is-cluster-mode'

defineOptions({name: 'HttpsDialog'})

const {t} = useI18n()

interface FormDataState {
  agentId: string
  name: string
  domainType: string
  subdomainBindings: Api.Proxy.SubdomainBindingParam[]
  customDomains: string
  localHost: string
  localPort: number | undefined
  forceHttps: boolean
  bandwidth: BandwidthMbps
}

interface Props {
  visible: boolean
  type: DialogType
  proxyData?: Partial<{
    id: string
    agentId: string
    name: string
  }>
}

interface Emits {
  (e: 'update:visible', value: boolean): void

  (e: 'submit'): void

  (e: 'open-cluster-config', payload: { id: string; name: string }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogType = computed(() => props.type)
const formRef = ref<FormInstance>()
const agents = ref<Api.Agent.AgentDTO[]>([])
const commonLocalPortPresets = computed(() => getCommonLocalPortPresets())
const {
  rootDomains,
  rootDomainLoading,
  loadRootDomains,
  createDefaultSubdomainBinding,
  normalizeSubdomainBindings
} = useRootDomainOptions()

const createDefaultFormData = (): FormDataState => ({
  agentId: '',
  name: '',
  domainType: String(DomainType.AUTO),
  subdomainBindings: [createDefaultSubdomainBinding()],
  customDomains: '',
  localHost: '127.0.0.1',
  localPort: undefined,
  forceHttps: true,
  bandwidth: undefined
})

const formData = reactive<FormDataState>(createDefaultFormData())
const subdomainErrorIndexes = ref<number[]>([])
const targets = ref<Api.Proxy.TargetDTO[]>([])

const clusterMode = computed(() => isClusterMode(targets.value))

const resetSubdomainErrors = () => {
  subdomainErrorIndexes.value = []
}

const rules = computed<FormRules>(() => ({
  agentId: [{required: true, message: t('orbien.proxy.selectClient'), trigger: 'change'}],
  name: [{required: true, message: t('orbien.proxy.enterName'), trigger: 'blur'}],
  domainType: [{required: true, message: t('orbien.proxy.selectDomainType'), trigger: 'change'}],
  customDomains: [
    {
      validator: (_rule, value: string, callback) => {
        if (formData.domainType !== String(DomainType.CUSTOM_DOMAIN)) {
          callback()
          return
        }
        if (!value?.trim()) {
          callback(new Error(t('orbien.proxy.enterCustomDomain')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  ...(clusterMode.value
      ? {}
      : {
        localHost: [{required: true, message: t('orbien.proxy.enterHost'), trigger: 'blur'}],
        localPort: [
          {required: true, message: t('orbien.proxy.enterPort'), trigger: 'blur'},
          {type: 'number', message: t('orbien.proxy.portNumber'), trigger: 'blur'},
          {min: 1, max: 65535, message: t('orbien.proxy.portRange'), trigger: 'blur'}
        ]
      }),
  bandwidth: BANDWIDTH_RULES
}))

const parseLines = (value: string): string[] =>
    value
        .split('\n')
        .map((item) => item.trim())
        .filter(Boolean)

const refreshSubdomainBindings = () => {
  if (formData.domainType !== String(DomainType.SUBDOMAIN)) {
    return
  }
  formData.subdomainBindings = normalizeSubdomainBindings(formData.subdomainBindings)
}

const applyDetail = (detail: Api.Proxy.HttpsProxyDetailDTO) => {
  targets.value = detail.targets ?? []
  Object.assign(formData, {
    ...createDefaultFormData(),
    agentId: detail.agentId || '',
    name: detail.name || '',
    domainType: detail.domainType?.toString() || String(DomainType.AUTO),
    subdomainBindings: normalizeSubdomainBindings(detail.subdomainBindings),
    customDomains: (detail.customDomains || []).join('\n'),
    localHost: detail.localHost || '127.0.0.1',
    localPort: detail.localPort,
    forceHttps: detail.forceHttps ?? false,
    bandwidth: detail.bandwidth ?? undefined
  })
  refreshSubdomainBindings()
}

const fetchAgents = async () => {
  try {
    const includeId = props.type === 'edit' ? props.proxyData?.agentId : undefined
    agents.value = (await fetchGetAgentsForProxySelection(includeId)) || []
  } catch (error) {
    console.error('获取客户端列表失败:', error)
    ElMessage.error(t('orbien.proxy.fetchClientFail'))
  }
}

const applyDefaultAgentIfNeeded = () => {
  if (props.type === 'edit' || formData.agentId || agents.value.length === 0) {
    return
  }
  formData.agentId = agents.value[0].id
}

const resetFormData = () => {
  targets.value = []
  Object.assign(formData, createDefaultFormData())
  refreshSubdomainBindings()
}

const handleOpenCluster = () => {
  const proxyId = props.proxyData?.id
  if (!proxyId) return
  emit('open-cluster-config', {id: proxyId, name: formData.name})
  dialogVisible.value = false
}

const initFormData = async () => {
  const isEdit = props.type === 'edit' && props.proxyData?.id

  if (!isEdit) {
    resetFormData()
    return
  }

  try {
    applyDetail(await fetchGetHttpsProxyById(props.proxyData!.id!))
  } catch (error) {
    console.error('获取代理详情失败:', error)
    ElMessage.error(t('orbien.proxy.fetchDetailFail'))
    Object.assign(formData, {
      ...createDefaultFormData(),
      agentId: props.proxyData?.agentId || '',
      name: props.proxyData?.name || ''
    })
  }
}

watch(
    () => [props.visible, props.type, props.proxyData] as const,
    async ([visible]) => {
      if (visible) {
        if (props.type === 'add') {
          resetFormData()
        }
        formRef.value?.clearValidate()
        resetSubdomainErrors()
        await Promise.all([fetchAgents(), loadRootDomains()])
        await initFormData()
        applyDefaultAgentIfNeeded()
        refreshSubdomainBindings()
      }
    },
    {immediate: true}
)

watch(
    () => formData.domainType,
    (domainType) => {
      resetSubdomainErrors()
      if (domainType === String(DomainType.SUBDOMAIN)) {
        if (formData.subdomainBindings.length === 0) {
          formData.subdomainBindings = [createDefaultSubdomainBinding()]
        }
        refreshSubdomainBindings()
      }
    }
)

watch(rootDomains, () => {
  refreshSubdomainBindings()
})

watch(dialogVisible, (visible) => {
  emit('update:visible', visible)
  if (!visible) {
    resetFormData()
    resetSubdomainErrors()
    formRef.value?.clearValidate()
  }
})

const buildDomainPayload = () => {
  const domainType = parseInt(formData.domainType, 10)
  if (domainType === DomainType.SUBDOMAIN) {
    return {
      subdomainBindings: buildSubdomainBindingsPayload(formData.subdomainBindings, rootDomains.value)
    }
  }
  if (domainType === DomainType.CUSTOM_DOMAIN) {
    return {customDomains: parseLines(formData.customDomains)}
  }
  return {}
}

const handleSubmit = async () => {
  if (!formRef.value) return

  resetSubdomainErrors()

  let formValid = false
  try {
    await formRef.value.validate()
    formValid = true
  } catch {
    formValid = false
  }

  const subdomainResult =
      formData.domainType === String(DomainType.SUBDOMAIN)
          ? validateSubdomainBindings(formData.subdomainBindings, rootDomains.value)
          : {valid: true, errorIndexes: [] as number[]}

  if (!subdomainResult.valid) {
    subdomainErrorIndexes.value = subdomainResult.errorIndexes
    if (subdomainResult.message) {
      ElMessage.warning(subdomainResult.message)
    }
  }

  if (!formValid || !subdomainResult.valid) return

  try {
    const commonData: Omit<Api.Proxy.HttpsProxyUpdateParam, 'id'> = {
      name: formData.name,
      domainType: parseInt(formData.domainType, 10),
      localHost: formData.localHost,
      localPort: formData.localPort!,
      forceHttps: formData.forceHttps,
      bandwidth: toBandwidthPayload(formData.bandwidth),
      ...buildDomainPayload()
    }

    if (dialogType.value === 'add') {
      await fetchCreateHttpsProxy({
        agentId: formData.agentId,
        ...commonData
      })
    } else {
      await fetchUpdateHttpsProxy({
        id: props.proxyData!.id!,
        ...commonData
      })
    }

    dialogVisible.value = false
    emit('submit')
    resetFormData()
    resetSubdomainErrors()
    formRef.value?.clearValidate()
  } catch (error) {
    console.error('提交失败:', error)
  }
}
</script>
