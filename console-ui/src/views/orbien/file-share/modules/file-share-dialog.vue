<template>
  <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? t('orbien.fileShare.add') : t('orbien.fileShare.edit')"
      width="720px"
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

      <ElFormItem :label="t('orbien.proxy.rootDir')" prop="rootPath">
        <ElInput v-model="formData.rootPath" :placeholder="t('orbien.proxy.rootDirPlaceholder')" clearable/>
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
            :placeholder="t('orbien.proxy.customDomainPlaceholderFileShare')"
        />
      </ElFormItem>

      <ElFormItem :label="t('orbien.proxy.authEnabled')">
        <div class="auth-switch-row">
          <ElSwitch v-model="formData.authEnabled"/>
          <span class="auth-switch-tip">{{ t('orbien.proxy.authTipFileShare') }}</span>
        </div>
      </ElFormItem>

      <template v-if="formData.authEnabled">
        <ElFormItem :label="t('orbien.proxy.authUsers')" required>
          <div class="auth-users-panel">
            <ElTable :data="authUsers" border size="small" :empty-text="t('orbien.proxy.authUserRequired')">
              <ElTableColumn prop="username" :label="t('orbien.proxy.username')" min-width="140">
                <template #default="{ row }">
                  <ElInput v-model="row.username" size="small" :placeholder="t('orbien.proxy.username')" clearable/>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="password" :label="t('orbien.proxy.password')" min-width="140">
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
              <ElTableColumn prop="permission" :label="t('orbien.proxy.permissions')" width="120">
                <template #default="{ row }">
                  <ElSelect v-model="row.permission" size="small" style="width: 100%">
                    <ElOption
                        v-for="option in permissionOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                    />
                  </ElSelect>
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

      <ElFormItem :label="t('orbien.proxy.uploadLimit')" prop="maxUploadSizeMb">
        <ElInput
            v-model.number="formData.maxUploadSizeMb"
            type="number"
            :min="1"
            :placeholder="t('orbien.proxy.maxUploadPlaceholder')"
            style="width: 200px"
        >
          <template #append>MB</template>
        </ElInput>
      </ElFormItem>

      <BandwidthLimitField v-model="formData.limitTotal"/>

      <ElFormItem :label="t('orbien.proxy.operationPermissions')">
        <ElSpace wrap>
          <ElCheckbox v-model="formData.allowUpload">{{ t('orbien.proxy.allowUpload') }}</ElCheckbox>
          <ElCheckbox v-model="formData.allowDelete">{{ t('orbien.proxy.allowDelete') }}</ElCheckbox>
          <ElCheckbox v-model="formData.allowMkdir">{{ t('orbien.proxy.allowMkdir') }}</ElCheckbox>
          <ElCheckbox v-model="formData.allowMove">{{ t('orbien.proxy.allowMove') }}</ElCheckbox>
          <ElCheckbox v-model="formData.allowRename">{{ t('orbien.proxy.allowRename') }}</ElCheckbox>
        </ElSpace>
      </ElFormItem>
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
import {fetchCreateFileShare, fetchUpdateFileShare, fetchGetFileShareById} from '@/api/file-share'
import {DomainType} from '@/enums/orbien/business'
import {formatAgentOptionLabel} from '@/views/orbien/agent/shared/format-agent-option'
import {
  useRootDomainOptions,
  validateSubdomainBindings,
  buildSubdomainBindingsPayload
} from '@/views/orbien/proxy/shared/use-root-domain-options'
import SubdomainBindingRows from '@/views/orbien/proxy/shared/subdomain-binding-rows.vue'
import BandwidthLimitField from '@/views/orbien/proxy/shared/bandwidth-limit-field.vue'
import {
  LIMIT_TOTAL_RULES,
  toLimitTotalPayload,
  type LimitTotalMbps
} from '@/views/orbien/proxy/shared/bandwidth-limit'

defineOptions({name: 'FileShareDialog'})

const {t} = useI18n()

const BYTES_PER_MB = 1024 * 1024
const DEFAULT_MAX_UPLOAD_MB = 500

type AuthUserForm = Api.FileShare.FileShareAuthUserParam & { id?: number }

interface FormDataState {
  agentId: string
  name: string
  rootPath: string
  domainType: string
  subdomainBindings: Api.Proxy.SubdomainBindingParam[]
  customDomains: string
  authEnabled: boolean
  maxUploadSizeMb: number
  limitTotal: LimitTotalMbps
  allowUpload: boolean
  allowDelete: boolean
  allowMkdir: boolean
  allowMove: boolean
  allowRename: boolean
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

const permissionOptions = computed(() => [
  {label: t('orbien.proxy.readonly'), value: 'read'},
  {label: t('orbien.proxy.readwrite'), value: 'read_write'}
] as const)

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogType = computed(() => props.type)
const formRef = ref<FormInstance>()
const agents = ref<Api.Agent.AgentDTO[]>([])
const authUsers = ref<AuthUserForm[]>([])
const subdomainErrorIndexes = ref<number[]>([])

const {
  rootDomains,
  rootDomainLoading,
  loadRootDomains,
  createDefaultSubdomainBinding,
  normalizeSubdomainBindings
} = useRootDomainOptions()

let openSession = 0

const createDefaultFormData = (): FormDataState => ({
  agentId: '',
  name: '',
  rootPath: '',
  domainType: String(DomainType.AUTO),
  subdomainBindings: [createDefaultSubdomainBinding()],
  customDomains: '',
  authEnabled: false,
  maxUploadSizeMb: DEFAULT_MAX_UPLOAD_MB,
  limitTotal: undefined,
  allowUpload: true,
  allowDelete: true,
  allowMkdir: true,
  allowMove: true,
  allowRename: true
})

const formData = reactive<FormDataState>(createDefaultFormData())

const isStaleSession = (session: number) => session !== openSession || !props.visible

const emptyAuthUser = (): AuthUserForm => ({username: '', password: '', permission: 'read'})

const rules = computed<FormRules>(() => ({
  agentId: [{required: true, message: t('orbien.proxy.selectClient'), trigger: 'change'}],
  name: [{required: true, message: t('orbien.proxy.enterName'), trigger: 'blur'}],
  rootPath: [{required: true, message: t('orbien.proxy.enterRootDir'), trigger: 'blur'}],
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
  maxUploadSizeMb: [
    {required: true, message: t('orbien.proxy.enterUploadLimit'), trigger: 'blur'},
    {type: 'number', min: 1, message: t('orbien.proxy.uploadLimitMin'), trigger: 'blur'}
  ],
  limitTotal: LIMIT_TOTAL_RULES
}))

const resetSubdomainErrors = () => {
  subdomainErrorIndexes.value = []
}

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

const resetForm = () => {
  Object.assign(formData, createDefaultFormData())
  authUsers.value = []
  subdomainErrorIndexes.value = []
  refreshSubdomainBindings()
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

const buildAuthPayload = (): Api.FileShare.FileShareAuthUserParam[] => {
  if (!formData.authEnabled) return []
  return authUsers.value
      .filter((user) => user.username?.trim())
      .map((user) => ({
        ...(user.id ? {id: user.id} : {}),
        username: user.username.trim(),
        permission: user.permission || 'read',
        ...(user.password?.trim() ? {password: user.password.trim()} : {})
      }))
}

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

const bytesToMb = (bytes?: number): number => {
  if (bytes == null || bytes <= 0) {
    return DEFAULT_MAX_UPLOAD_MB
  }
  return Math.round(bytes / BYTES_PER_MB)
}

const mbToBytes = (mb: number): number => Math.round(mb * BYTES_PER_MB)

const applyDetail = (detail: Api.FileShare.FileShareDetailDTO) => {
  Object.assign(formData, {
    ...createDefaultFormData(),
    agentId: detail.agentId || '',
    name: detail.name || '',
    rootPath: detail.rootPath || '',
    domainType: detail.domainType?.toString() || String(DomainType.AUTO),
    subdomainBindings: normalizeSubdomainBindings(detail.subdomainBindings),
    customDomains: (detail.customDomains || []).join('\n'),
    authEnabled: detail.authEnabled ?? false,
    maxUploadSizeMb: bytesToMb(detail.maxUploadSize),
    limitTotal: detail.limitTotal ?? undefined,
    allowUpload: detail.allowUpload ?? true,
    allowDelete: detail.allowDelete ?? true,
    allowMkdir: detail.allowMkdir ?? true,
    allowMove: detail.allowMove ?? true,
    allowRename: detail.allowRename ?? true
  })
  authUsers.value = (detail.authUsers || []).map((user) => ({
    id: user.id,
    username: user.username,
    password: '',
    permission: user.permission || 'read'
  }))
  ensureAuthUsers()
  refreshSubdomainBindings()
}

const loadEditForm = async (session: number, proxyId: string) => {
  try {
    const detail = await fetchGetFileShareById(proxyId)
    if (isStaleSession(session) || props.type !== 'edit' || props.proxyData?.id !== proxyId) return
    applyDetail(detail)
  } catch (error) {
    console.error('获取文件共享详情失败:', error)
    if (!isStaleSession(session)) {
      ElMessage.error(t('orbien.proxy.fetchFileShareDetailFail'))
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

  try {
    await loadRootDomains()
  } catch (error) {
    console.error('获取根域名列表失败:', error)
  }
  if (isStaleSession(session)) return

  if (props.type === 'edit' && props.proxyData?.id) {
    await loadEditForm(session, props.proxyData.id)
  } else if (!formData.agentId && agents.value.length > 0) {
    formData.agentId = agents.value[0].id
  }

  refreshSubdomainBindings()
}

watch(() => formData.authEnabled, ensureAuthUsers)

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

  if (!formValid || !subdomainResult.valid || !validateAuthUsers()) return

  try {
    const payload: Omit<Api.FileShare.FileShareUpdateParam, 'id'> = {
      name: formData.name,
      domainType: parseInt(formData.domainType, 10),
      rootPath: formData.rootPath.trim(),
      limitTotal: toLimitTotalPayload(formData.limitTotal),
      authEnabled: formData.authEnabled,
      authUsers: buildAuthPayload(),
      maxUploadSize: mbToBytes(formData.maxUploadSizeMb),
      allowUpload: formData.allowUpload,
      allowDelete: formData.allowDelete,
      allowMkdir: formData.allowMkdir,
      allowMove: formData.allowMove,
      allowRename: formData.allowRename,
      ...buildDomainPayload()
    }

    if (dialogType.value === 'add') {
      await fetchCreateFileShare({agentId: formData.agentId, ...payload})
    } else {
      await fetchUpdateFileShare({id: props.proxyData?.id || '', ...payload})
    }

    dialogVisible.value = false
    emit('submit')
  } catch (error) {
    console.error('提交失败:', error)
  }
}
</script>

<style scoped lang="scss">
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
