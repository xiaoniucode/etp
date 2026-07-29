<template>
  <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? t('orbien.socks5.add') : t('orbien.socks5.edit')"
      width="650px"
      align-center
  >
    <ElForm
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        :show-message="false"
    >
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

      <ElFormItem :label="t('orbien.proxy.remotePort')" prop="remotePort">
        <div class="remote-port-field">
          <ElInput
              v-model="remotePortInput"
              type="number"
              :placeholder="t('orbien.proxy.autoPort')"
              class="remote-port-input"
              @input="selectedSuggestPort = null"
          />
          <template v-if="suggestedPorts.length">
            <ElButton
                v-for="port in suggestedPorts"
                :key="port"
                size="small"
                :type="selectedSuggestPort === port ? 'primary' : 'default'"
                plain
                @click="selectSuggestedPort(port)"
            >
              {{ port }}
            </ElButton>
            <ElButton link type="primary" :loading="suggestLoading" @click="loadSuggestedPorts()">
              {{ t('orbien.proxy.refreshPorts') }}
            </ElButton>
          </template>
          <ElLink
              v-else-if="!suggestLoading"
              type="primary"
              :underline="false"
              @click="goToPortPool"
          >
            {{ t('orbien.proxy.goAddPort') }}
          </ElLink>
        </div>
      </ElFormItem>

      <BandwidthLimitField v-model="formData.limitTotal"/>

      <ElFormItem :label="t('orbien.proxy.authEnabled')">
        <div class="auth-switch-row">
          <ElSwitch v-model="formData.authEnabled"/>
          <span class="auth-switch-tip">{{ t('orbien.proxy.authTipSocks5') }}</span>
        </div>
      </ElFormItem>

      <template v-if="formData.authEnabled">
        <ElFormItem :label="t('orbien.proxy.authUsers')" required>
          <div class="auth-users-panel">
            <ElTable :data="authUsers" border size="small" :empty-text="t('orbien.proxy.authUserRequired')">
              <ElTableColumn prop="username" :label="t('orbien.proxy.username')" min-width="160">
                <template #default="{ row }">
                  <ElInput v-model="row.username" size="small" :placeholder="t('orbien.proxy.username')" clearable/>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="password" :label="t('orbien.proxy.password')" min-width="160">
                <template #default="{ row }">
                  <ElInput
                      v-model="row.password"
                      size="small"
                      :placeholder="row.id ? t('orbien.proxy.leaveBlank') : t('orbien.proxy.enterPassword')"
                      type="password"
                      show-password
                      clearable
                  />
                </template>
              </ElTableColumn>
              <ElTableColumn :label="t('common.actions')" width="80" fixed="right">
                <template #default="{ $index }">
                  <ElButton
                      link
                      type="primary"
                      size="small"
                      :disabled="authUsers.length <= 1"
                      @click="authUsers.splice($index, 1)"
                  >
                    {{ t('common.delete') }}
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
            <ElButton type="primary" plain size="small" class="add-user-btn" @click="authUsers.push(emptyAuthUser())">
              {{ t('orbien.proxy.addUser') }}
            </ElButton>
          </div>
        </ElFormItem>
      </template>
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
import {useRouter} from 'vue-router'
import {useI18n} from 'vue-i18n'
import {ElMessage} from 'element-plus'
import type {FormInstance, FormRules} from 'element-plus'
import {DialogType} from '@/types'
import {fetchGetAgentsForProxySelection} from '@/api/agent'
import {fetchCreateSocks5Proxy, fetchUpdateSocks5Proxy, fetchGetSocks5ProxyById} from '@/api/proxy'
import {fetchSuggestAvailablePorts} from '@/api/port-pool'
import {PortPoolType} from '@/enums/orbien/business'
import {formatAgentOptionLabel} from '@/views/orbien/agent/shared/format-agent-option'
import BandwidthLimitField from '@/views/orbien/proxy/shared/bandwidth-limit-field.vue'
import {
  LIMIT_TOTAL_RULES,
  toLimitTotalPayload,
  type LimitTotalMbps
} from '@/views/orbien/proxy/shared/bandwidth-limit'

defineOptions({name: 'Socks5Dialog'})

const {t} = useI18n()

type AuthUserForm = Api.Proxy.Socks5AuthUserParam & { id?: number }

interface FormDataState {
  agentId: string
  name: string
  limitTotal: LimitTotalMbps
  authEnabled: boolean
}

interface Props {
  visible: boolean
  type: DialogType
  proxyData?: Partial<{ id: string; agentId: string; name: string }>
}

interface Emits {
  (e: 'update:visible', value: boolean): void

  (e: 'submit'): void
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
const suggestedPorts = ref<number[]>([])
const selectedSuggestPort = ref<number | null>(null)
const suggestLoading = ref(false)
const remotePortInput = ref('')
const authUsers = ref<AuthUserForm[]>([])
const router = useRouter()

let openSession = 0

const DEFAULT_FORM: FormDataState = {
  agentId: '',
  name: '',
  limitTotal: undefined,
  authEnabled: false
}

const formData = reactive<FormDataState>({...DEFAULT_FORM})

const isStaleSession = (session: number) => session !== openSession || !props.visible

const emptyAuthUser = (): AuthUserForm => ({username: '', password: ''})

const rules = computed<FormRules>(() => ({
  agentId: [{required: true, message: t('orbien.proxy.selectClient'), trigger: 'change'}],
  name: [{required: true, message: t('orbien.proxy.enterName'), trigger: 'blur'}],
  remotePort: [{
    validator: (_rule, _value, callback) => {
      if (!remotePortInput.value) return callback()
      const port = parseInt(remotePortInput.value, 10)
      if (isNaN(port) || port < 1 || port > 65535) {
        callback(new Error(t('orbien.proxy.remotePortRange')))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }],
  limitTotal: LIMIT_TOTAL_RULES
}))

const resetForm = () => {
  Object.assign(formData, {...DEFAULT_FORM})
  remotePortInput.value = ''
  authUsers.value = []
  suggestedPorts.value = []
  selectedSuggestPort.value = null
}

const ensureAuthUsers = () => {
  if (formData.authEnabled && authUsers.value.length === 0) {
    authUsers.value = [emptyAuthUser()]
  }
}

const validateAuthUsers = (): boolean => {
  if (!formData.authEnabled) return true

  const users = authUsers.value.filter((user) => user.username?.trim())
  if (users.length === 0) {
    ElMessage.warning(t('orbien.proxy.authUserMinOne'))
    return false
  }

  for (const user of users) {
    const needPassword = !user.id
    if (needPassword && !user.password?.trim()) {
      ElMessage.warning(t('orbien.proxy.fillUsernamePassword'))
      return false
    }
  }
  return true
}

const buildAuthPayload = (): Api.Proxy.Socks5AuthUserParam[] => {
  if (!formData.authEnabled) return []
  return authUsers.value
      .filter((user) => user.username?.trim())
      .map((user) => ({
        ...(user.id ? {id: user.id} : {}),
        username: user.username.trim(),
        ...(user.password?.trim() ? {password: user.password.trim()} : {})
      }))
}

const parseRemotePort = (): number | undefined => {
  if (!remotePortInput.value) return undefined
  const port = parseInt(remotePortInput.value, 10)
  return isNaN(port) ? undefined : port
}

const selectSuggestedPort = (port: number) => {
  remotePortInput.value = String(port)
  selectedSuggestPort.value = port
  formRef.value?.clearValidate('remotePort')
}

const loadSuggestedPorts = async (session = openSession) => {
  suggestLoading.value = true
  selectedSuggestPort.value = null
  try {
    const ports = await fetchSuggestAvailablePorts(PortPoolType.TCP, 4)
    if (!isStaleSession(session)) {
      suggestedPorts.value = ports
    }
  } catch (error) {
    console.error('获取可用端口失败:', error)
    if (!isStaleSession(session)) {
      suggestedPorts.value = []
    }
  } finally {
    if (!isStaleSession(session)) {
      suggestLoading.value = false
    }
  }
}

const goToPortPool = () => {
  dialogVisible.value = false
  router.push('/port-pool')
}

const loadEditForm = async (session: number, proxyId: string) => {
  try {
    const detail = await fetchGetSocks5ProxyById(proxyId)
    if (isStaleSession(session) || props.type !== 'edit' || props.proxyData?.id !== proxyId) return

    Object.assign(formData, {
      agentId: detail.agentId || '',
      name: detail.name || '',
      limitTotal: detail.limitTotal ?? undefined,
      authEnabled: detail.authEnabled ?? false
    })
    const displayPort = detail.remotePort ?? detail.listenPort
    remotePortInput.value = displayPort != null ? String(displayPort) : ''
    authUsers.value = (detail.authUsers || []).map((user) => ({
      id: user.id,
      username: user.username,
      password: ''
    }))
    ensureAuthUsers()
  } catch (error) {
    console.error('获取代理详情失败:', error)
    if (!isStaleSession(session)) {
      ElMessage.error(t('orbien.proxy.fetchDetailFail'))
    }
  }
}

const openDialog = async () => {
  const session = ++openSession
  resetForm()
  formRef.value?.clearValidate()

  try {
    const includeId = props.type === 'edit' ? props.proxyData?.agentId : undefined
    agents.value = await fetchGetAgentsForProxySelection(includeId) || []
  } catch (error) {
    console.error('获取客户端列表失败:', error)
    ElMessage.error(t('orbien.proxy.fetchClientFail'))
  }
  if (isStaleSession(session)) return

  if (props.type === 'edit' && props.proxyData?.id) {
    await loadEditForm(session, props.proxyData.id)
  } else if (!formData.agentId && agents.value.length > 0) {
    formData.agentId = agents.value[0].id
  }

  if (!isStaleSession(session)) {
    await loadSuggestedPorts(session)
  }
}

watch(() => formData.authEnabled, ensureAuthUsers)

watch(
    () => [props.visible, props.type, props.proxyData?.id] as const,
    ([visible]) => {
      if (visible) {
        openDialog()
      } else {
        openSession++
        resetForm()
        formRef.value?.clearValidate()
      }
    }
)

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid || !validateAuthUsers()) return

    try {
      const remotePort = parseRemotePort()
      const payload: Omit<Api.Proxy.Socks5ProxyUpdateParam, 'id'> = {
        name: formData.name,
        limitTotal: toLimitTotalPayload(formData.limitTotal),
        authEnabled: formData.authEnabled,
        authUsers: buildAuthPayload(),
        ...(remotePort != null ? {remotePort} : {})
      }

      if (dialogType.value === 'add') {
        await fetchCreateSocks5Proxy({agentId: formData.agentId, ...payload})
      } else {
        await fetchUpdateSocks5Proxy({id: props.proxyData?.id || '', ...payload})
      }

      dialogVisible.value = false
      emit('submit')
    } catch (error) {
      console.error('提交失败:', error)
    }
  })
}
</script>

<style scoped lang="scss">
.remote-port-field {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.remote-port-input {
  width: 146px;
  flex-shrink: 0;
}

.auth-switch-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.auth-switch-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.auth-users-panel {
  width: 100%;
}

.add-user-btn {
  margin-top: 12px;
}
</style>
