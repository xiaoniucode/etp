<template>
  <div class="header-rewrite-page">
    <div class="mb-6">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.basic.title') }}</h3>
      <div class="flex items-center gap-3">
        <span class="w-20 font-medium">{{ t('orbien.plugin.basic.enabled') }}：</span>
        <ElSwitch v-model="formData.enabled" @change="handleEnableChange"/>
      </div>
    </div>

    <div class="mb-6">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.header.requestHeaders') }}</h3>
      <div class="border border-gray-200 rounded p-4">
        <ElTable :data="formData.requestRules" style="width: 100%" border>
          <ElTableColumn :label="t('orbien.plugin.header.action')" width="140">
            <template #default="scope">
              <ElSelect
                  v-if="editingId === scope.row.id && editingDirection === HeaderDirection.REQUEST"
                  v-model="scope.row.action"
                  size="small"
                  style="width: 100%"
                  @change="(v: number) => onActionChange(scope.row, v)"
              >
                <ElOption
                    v-for="opt in HEADER_ACTION_OPTIONS"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                />
              </ElSelect>
              <span v-else>{{ getHeaderActionLabel(scope.row.action) }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('orbien.plugin.header.name')" min-width="160">
            <template #default="scope">
              <ElInput
                  v-if="editingId === scope.row.id && editingDirection === HeaderDirection.REQUEST"
                  v-model="scope.row.name"
                  size="small"
                  placeholder="X-Real-IP"
              />
              <span v-else>{{ scope.row.name }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('orbien.plugin.header.value')" min-width="180">
            <template #default="scope">
              <ElInput
                  v-if="editingId === scope.row.id && editingDirection === HeaderDirection.REQUEST"
                  v-model="scope.row.value"
                  size="small"
                  :disabled="scope.row.action === HeaderAction.REMOVE"
                  :placeholder="scope.row.action === HeaderAction.REMOVE ? '—' : '$client_ip'"
              />
              <span v-else>{{ scope.row.action === HeaderAction.REMOVE ? '—' : scope.row.value }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('common.actions')" width="180" fixed="right">
            <template #default="scope">
              <ElSpace size="small">
                <ElButton
                    v-if="editingId === scope.row.id && editingDirection === HeaderDirection.REQUEST"
                    type="primary"
                    size="small"
                    @click="handleSave(scope.row)"
                >
                  {{ t('common.save') }}
                </ElButton>
                <ElButton v-else type="link" size="small" @click="handleEdit(scope.row)">
                  {{ t('common.edit') }}
                </ElButton>
                <ElButton type="link" size="small" @click="handleDelete(scope.row)">
                  <template #icon>
                    <Delete/>
                  </template>
                  {{ t('common.delete') }}
                </ElButton>
              </ElSpace>
            </template>
          </ElTableColumn>
        </ElTable>
        <ElButton type="primary" size="small" class="mt-3" @click="addRule(HeaderDirection.REQUEST)">
          <template #icon>
            <Plus/>
          </template>
          {{ t('orbien.plugin.actions.addRule') }}
        </ElButton>
      </div>
    </div>

    <div class="mb-4">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.header.responseHeaders') }}</h3>
      <div class="border border-gray-200 rounded p-4">
        <ElTable :data="formData.responseRules" style="width: 100%" border>
          <ElTableColumn :label="t('orbien.plugin.header.action')" width="140">
            <template #default="scope">
              <ElSelect
                  v-if="editingId === scope.row.id && editingDirection === HeaderDirection.RESPONSE"
                  v-model="scope.row.action"
                  size="small"
                  style="width: 100%"
                  @change="(v: number) => onActionChange(scope.row, v)"
              >
                <ElOption
                    v-for="opt in HEADER_ACTION_OPTIONS"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                />
              </ElSelect>
              <span v-else>{{ getHeaderActionLabel(scope.row.action) }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('orbien.plugin.header.name')" min-width="160">
            <template #default="scope">
              <ElInput
                  v-if="editingId === scope.row.id && editingDirection === HeaderDirection.RESPONSE"
                  v-model="scope.row.name"
                  size="small"
                  placeholder="X-Content-Type-Options"
              />
              <span v-else>{{ scope.row.name }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('orbien.plugin.header.value')" min-width="180">
            <template #default="scope">
              <ElInput
                  v-if="editingId === scope.row.id && editingDirection === HeaderDirection.RESPONSE"
                  v-model="scope.row.value"
                  size="small"
                  :disabled="scope.row.action === HeaderAction.REMOVE"
                  :placeholder="scope.row.action === HeaderAction.REMOVE ? '—' : 'nosniff'"
              />
              <span v-else>{{ scope.row.action === HeaderAction.REMOVE ? '—' : scope.row.value }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('common.actions')" width="180" fixed="right">
            <template #default="scope">
              <ElSpace size="small">
                <ElButton
                    v-if="editingId === scope.row.id && editingDirection === HeaderDirection.RESPONSE"
                    type="primary"
                    size="small"
                    @click="handleSave(scope.row)"
                >
                  {{ t('common.save') }}
                </ElButton>
                <ElButton v-else type="link" size="small" @click="handleEdit(scope.row)">
                  {{ t('common.edit') }}
                </ElButton>
                <ElButton type="link" size="small" @click="handleDelete(scope.row)">
                  <template #icon>
                    <Delete/>
                  </template>
                  {{ t('common.delete') }}
                </ElButton>
              </ElSpace>
            </template>
          </ElTableColumn>
        </ElTable>
        <ElButton type="primary" size="small" class="mt-3" @click="addRule(HeaderDirection.RESPONSE)">
          <template #icon>
            <Plus/>
          </template>
          {{ t('orbien.plugin.actions.addRule') }}
        </ElButton>
      </div>
    </div>

    <div class="mt-4 text-sm text-gray-500 space-y-1">
      <p class="font-medium text-gray-600">{{ t('orbien.plugin.header.actionGuideTitle') }}</p>
      <ul class="list-disc pl-5 space-y-0.5">
        <li><span class="font-medium text-gray-600">SET</span>：{{ t('orbien.plugin.header.actionSet') }}</li>
        <li><span class="font-medium text-gray-600">ADD</span>：{{ t('orbien.plugin.header.actionAdd') }}</li>
        <li><span class="font-medium text-gray-600">REMOVE</span>：{{ t('orbien.plugin.header.actionRemove') }}</li>
      </ul>
      <p class="pt-1">{{ t('orbien.plugin.header.availableVars') }}：{{ VAR_HINT }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Delete} from '@element-plus/icons-vue'
import {
  fetchGetHeaderRewrite,
  fetchUpdateHeaderRewrite,
  fetchAddHeaderRewriteRule,
  fetchUpdateHeaderRewriteRule,
  fetchDeleteHeaderRewriteRule
} from '@/api/header-rewrite'
import {
  HeaderAction,
  HeaderDirection,
  HEADER_ACTION_OPTIONS,
  getHeaderActionLabel
} from '@/enums/orbien/business'

defineOptions({name: 'HeaderRewritePage'})

const {t} = useI18n()

const props = defineProps<{
  proxyId: string
}>()

const VAR_HINT = '$client_ip $scheme $host $request_id'

const formData = reactive({
  enabled: false,
  requestRules: [] as Api.HeaderRewrite.RuleDTO[],
  responseRules: [] as Api.HeaderRewrite.RuleDTO[]
})

const editingId = ref<number | null>(null)
const editingDirection = ref<number | null>(null)

const resetFormData = () => {
  formData.enabled = false
  formData.requestRules = []
  formData.responseRules = []
  editingId.value = null
  editingDirection.value = null
}

const fetchHeaderRewriteData = async () => {
  const response = await fetchGetHeaderRewrite(props.proxyId)
  if (!response) return
  formData.enabled = response.enabled || false
  formData.requestRules = response.requestRules || []
  formData.responseRules = response.responseRules || []
}

watch(
    () => props.proxyId,
    async (proxyId) => {
      if (!proxyId) return
      resetFormData()
      await fetchHeaderRewriteData()
    },
    {immediate: true}
)

const handleEnableChange = async () => {
  await fetchUpdateHeaderRewrite({
    proxyId: props.proxyId,
    enabled: formData.enabled
  })
}

const onActionChange = (row: Api.HeaderRewrite.RuleDTO, action: number) => {
  row.action = action
  if (action === HeaderAction.REMOVE) {
    row.value = ''
  }
}

const rowsOf = (direction: number) =>
    direction === HeaderDirection.REQUEST ? formData.requestRules : formData.responseRules

const addRule = (direction: number) => {
  rowsOf(direction).push({
    id: 0,
    direction,
    action: HeaderAction.SET,
    name: '',
    value: ''
  })
  editingId.value = 0
  editingDirection.value = direction
}

const handleEdit = (row: Api.HeaderRewrite.RuleDTO) => {
  editingId.value = row.id
  editingDirection.value = row.direction
}

const handleSave = async (row: Api.HeaderRewrite.RuleDTO) => {
  if (!row.name?.trim()) {
    ElMessage.error(t('orbien.plugin.header.nameRequired'))
    return
  }
  if (row.action !== HeaderAction.REMOVE && !row.value?.trim()) {
    ElMessage.error(t('orbien.plugin.header.valueRequired'))
    return
  }
  const payload: Api.HeaderRewrite.RuleAddParam = {
    proxyId: props.proxyId,
    direction: row.direction,
    action: row.action,
    name: row.name.trim(),
    value: row.action === HeaderAction.REMOVE ? null : row.value?.trim()
  }
  if (row.id > 0) {
    await fetchUpdateHeaderRewriteRule({id: row.id, ...payload})
  } else {
    await fetchAddHeaderRewriteRule(payload)
  }
  editingId.value = null
  editingDirection.value = null
  await fetchHeaderRewriteData()
}

const handleDelete = async (row: Api.HeaderRewrite.RuleDTO) => {
  await ElMessageBox.confirm(t('orbien.plugin.deleteConfirm.headerRule'), t('common.warning'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
  if (row.id > 0) {
    await fetchDeleteHeaderRewriteRule(row.id)
    await fetchHeaderRewriteData()
    return
  }
  const rows = rowsOf(row.direction)
  const index = rows.indexOf(row)
  if (index > -1) {
    rows.splice(index, 1)
  }
  editingId.value = null
  editingDirection.value = null
}
</script>
