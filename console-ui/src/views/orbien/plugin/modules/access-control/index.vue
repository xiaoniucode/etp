<template>
  <div class="access-control-page">
    <div class="mb-6">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.basic.title') }}</h3>
      <div class="flex flex-col gap-4">
        <div class="flex items-center gap-3">
          <span class="w-20 font-medium">{{ t('orbien.plugin.basic.enabled') }}：</span>
          <ElSwitch v-model="formData.enabled" @change="handleEnableChange" />
        </div>
        <div class="flex items-center gap-3">
          <span class="w-20 font-medium">{{ t('orbien.plugin.basic.controlMode') }}：</span>
          <ElRadioGroup v-model="formData.mode" @change="handleModeChange">
            <ElRadio :label="AccessControl.ALLOW">{{ t('orbien.plugin.access.allowWhitelist') }}</ElRadio>
            <ElRadio :label="AccessControl.DENY">{{ t('orbien.plugin.access.denyBlacklist') }}</ElRadio>
          </ElRadioGroup>
        </div>
      </div>
    </div>

    <div>
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.access.rulesTitle') }}</h3>
      <div class="border border-gray-200 rounded p-4">
        <ElTable :data="formData.rules" style="width: 100%" border>
          <ElTableColumn prop="cidr" :label="t('orbien.plugin.access.cidrColumn')" width="300">
            <template #default="scope">
              <ElInput
                v-if="editingRuleId === scope.row.id"
                v-model="scope.row.cidr"
                size="small"
                :placeholder="t('orbien.plugin.access.cidrPlaceholder')"
                style="width: 100%"
              />
              <span v-else>{{ scope.row.cidr }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="ruleType" :label="t('orbien.plugin.access.ruleType')" width="150">
            <template #default="scope">
              <ElRadioGroup v-if="editingRuleId === scope.row.id" v-model="scope.row.ruleType" size="small">
                <ElRadio :label="AccessControl.ALLOW">{{ t('orbien.plugin.access.allow') }}</ElRadio>
                <ElRadio :label="AccessControl.DENY">{{ t('orbien.plugin.access.deny') }}</ElRadio>
              </ElRadioGroup>
              <ElTag v-else size="small" :type="scope.row.ruleType === AccessControl.ALLOW ? 'primary' : 'danger'">
                {{ scope.row.ruleType === AccessControl.ALLOW ? t('orbien.plugin.access.allow') : t('orbien.plugin.access.deny') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('common.actions')" width="240" fixed="right">
            <template #default="scope">
              <ElSpace size="small">
                <ElButton
                  v-if="editingRuleId === scope.row.id"
                  type="primary"
                  size="small"
                  @click="handleSaveRule(scope.row)"
                >
                  {{ t('common.save') }}
                </ElButton>
                <ElButton v-else type="link" size="small" @click="handleEditRule(scope.row)">
                  {{ t('common.edit') }}
                </ElButton>
                <ElButton type="link" size="small" @click="handleDeleteRule(scope.row.id)">
                  <template #icon>
                    <Delete />
                  </template>
                  {{ t('common.delete') }}
                </ElButton>
              </ElSpace>
            </template>
          </ElTableColumn>
        </ElTable>
        <ElButton type="primary" size="small" @click="addRule" class="mt-3">
          <template #icon>
            <Plus />
          </template>
          {{ t('orbien.plugin.actions.addRule') }}
        </ElButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Plus, Delete } from '@element-plus/icons-vue'
  import {
    fetchGetAccessControl,
    fetchUpdateAccessControl,
    fetchAddAccessControlRule,
    fetchUpdateAccessControlRule,
    fetchDeleteAccessControlRule
  } from '@/api/access-control'
  import { AccessControl } from '@/enums/orbien/business'

  defineOptions({ name: 'AccessControlPage' })

  const { t } = useI18n()

  const props = defineProps<{
    proxyId: string
  }>()

  const formData = reactive({
    enabled: false,
    mode: AccessControl.ALLOW,
    rules: [] as Array<{
      id: number
      proxyId: string
      cidr: string
      ruleType: number
    }>
  })

  const editingRuleId = ref<number | null>(null)
  const editingRuleBackup = ref<any>(null)

  const resetFormData = () => {
    formData.enabled = false
    formData.mode = AccessControl.ALLOW
    formData.rules = []
    editingRuleId.value = null
    editingRuleBackup.value = null
  }

  const fetchAccessControlData = async () => {
    const response = await fetchGetAccessControl(props.proxyId)
    if (response) {
      formData.enabled = response.enabled || false
      formData.mode = response.mode !== undefined ? response.mode : AccessControl.ALLOW
      formData.rules = response.rules || []
    }
  }

  watch(
    () => props.proxyId,
    async (proxyId) => {
      if (!proxyId) return
      resetFormData()
      await fetchAccessControlData()
    },
    { immediate: true }
  )

  const handleEnableChange = async () => {
    await updateAccessControlConfig()
  }

  const handleModeChange = async () => {
    await updateAccessControlConfig()
  }

  const updateAccessControlConfig = async () => {
    await fetchUpdateAccessControl({
      proxyId: props.proxyId,
      enabled: formData.enabled,
      mode: formData.mode
    })
  }

  const addRule = () => {
    formData.rules.push({
      id: 0,
      proxyId: props.proxyId,
      cidr: '',
      ruleType: AccessControl.ALLOW
    })
    const newRule = formData.rules[formData.rules.length - 1]
    handleEditRule(newRule)
  }

  const handleEditRule = (rule: any) => {
    editingRuleBackup.value = { ...rule }
    editingRuleId.value = rule.id
  }

  const handleSaveRule = async (rule: any) => {
    if (!rule.cidr) {
      ElMessage.error(t('orbien.plugin.access.cidrRequired'))
      return
    }

    if (rule.id > 0) {
      await fetchUpdateAccessControlRule({
        id: rule.id,
        cidr: rule.cidr,
        ruleType: rule.ruleType
      })
    } else {
      await fetchAddAccessControlRule({
        proxyId: props.proxyId,
        cidr: rule.cidr,
        ruleType: rule.ruleType
      })
    }
    editingRuleId.value = null
    await fetchAccessControlData()
    editingRuleBackup.value = null
  }

  const handleDeleteRule = async (id: number) => {
    await ElMessageBox.confirm(t('orbien.plugin.deleteConfirm.rule'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    if (id > 0) {
      await fetchDeleteAccessControlRule(id)
      await fetchAccessControlData()
    } else {
      const index = formData.rules.findIndex((rule) => rule.id === id)
      if (index > -1) {
        formData.rules.splice(index, 1)
        editingRuleId.value = null
      }
    }
  }
</script>
