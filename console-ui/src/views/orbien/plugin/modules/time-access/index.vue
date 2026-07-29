<template>
  <div class="time-access-page">
    <div class="mb-6">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.basic.title') }}</h3>
      <div class="flex flex-col gap-4">
        <div class="flex items-center gap-3">
          <span class="w-20 font-medium">{{ t('orbien.plugin.basic.enabled') }}：</span>
          <ElSwitch v-model="formData.enabled" @change="handleConfigChange" />
        </div>
        <div class="flex items-center gap-3">
          <span class="w-20 font-medium">{{ t('orbien.plugin.basic.controlMode') }}：</span>
          <ElRadioGroup v-model="formData.mode" @change="handleConfigChange">
            <ElRadio :label="AccessControl.ALLOW">{{ t('orbien.plugin.time.allowInWindow') }}</ElRadio>
            <ElRadio :label="AccessControl.DENY">{{ t('orbien.plugin.time.denyInWindow') }}</ElRadio>
          </ElRadioGroup>
        </div>
        <div class="flex items-center gap-3">
          <span class="w-20 font-medium">{{ t('orbien.plugin.basic.timezone') }}：</span>
          <ElSelect
            v-model="formData.timezone"
            filterable
            allow-create
            default-first-option
            style="width: 280px"
            @change="handleConfigChange"
          >
            <ElOption v-for="tz in timezoneOptions" :key="tz" :label="tz" :value="tz" />
          </ElSelect>
        </div>
      </div>
    </div>

    <div class="mb-6">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.time.periodLimit') }}</h3>
      <div class="flex flex-wrap items-center gap-3">
        <ElCheckbox
          :model-value="isAllDaysSelected"
          :indeterminate="isDaysIndeterminate"
          @change="handleSelectAllDays"
        >
          {{ t('orbien.plugin.actions.selectAll') }}
        </ElCheckbox>
        <ElCheckboxGroup v-model="formData.days" @change="handleConfigChange">
          <ElCheckbox v-for="day in weekDays" :key="day.value" :label="day.value">
            {{ day.label }}
          </ElCheckbox>
        </ElCheckboxGroup>
      </div>
    </div>

    <div class="mb-4">
      <div class="flex items-center gap-3 mb-4">
        <h3 class="text-lg font-semibold">{{ t('orbien.plugin.time.timeLimit') }}</h3>
        <ElSwitch v-model="formData.timeEnabled" @change="handleConfigChange" />
      </div>
      <div
        class="border border-gray-200 rounded p-4"
        :class="{ 'opacity-50 pointer-events-none': !formData.timeEnabled }"
      >
        <ElTable :data="formData.windows" style="width: 100%" border>
          <ElTableColumn :label="t('orbien.plugin.time.startTime')" min-width="180">
            <template #default="scope">
              <ElTimePicker
                v-if="editingWindowId === scope.row.id"
                v-model="scope.row.start"
                size="small"
                value-format="HH:mm:ss"
                :placeholder="t('orbien.plugin.time.selectStartTime')"
                style="width: 100%"
              />
              <span v-else>{{ scope.row.start }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('orbien.plugin.time.endTime')" min-width="180">
            <template #default="scope">
              <ElTimePicker
                v-if="editingWindowId === scope.row.id"
                v-model="scope.row.end"
                size="small"
                value-format="HH:mm:ss"
                :placeholder="t('orbien.plugin.time.selectEndTime')"
                style="width: 100%"
              />
              <span v-else>{{ scope.row.end }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('common.actions')" width="180" fixed="right">
            <template #default="scope">
              <ElSpace size="small">
                <ElButton
                  v-if="editingWindowId === scope.row.id"
                  type="primary"
                  size="small"
                  @click="handleSaveWindow(scope.row)"
                >
                  {{ t('common.save') }}
                </ElButton>
                <ElButton v-else type="link" size="small" @click="handleEditWindow(scope.row)">
                  {{ t('common.edit') }}
                </ElButton>
                <ElButton type="link" size="small" @click="handleDeleteWindow(scope.row)">
                  <template #icon>
                    <Delete />
                  </template>
                  {{ t('common.delete') }}
                </ElButton>
              </ElSpace>
            </template>
          </ElTableColumn>
        </ElTable>
        <ElButton type="primary" size="small" class="mt-3" @click="addWindow">
          <template #icon>
            <Plus />
          </template>
          {{ t('orbien.plugin.actions.addWindow') }}
        </ElButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Plus, Delete } from '@element-plus/icons-vue'
  import {
    fetchGetTimeAccess,
    fetchUpdateTimeAccess,
    fetchAddTimeAccessWindow,
    fetchUpdateTimeAccessWindow,
    fetchDeleteTimeAccessWindow
  } from '@/api/time-access'
  import { AccessControl } from '@/enums/orbien/business'

  defineOptions({ name: 'TimeAccessPage' })

  const { t } = useI18n()

  const props = defineProps<{
    proxyId: string
  }>()

  const weekDays = computed(() => [
    { value: 1, label: t('orbien.plugin.time.weekdays.mon') },
    { value: 2, label: t('orbien.plugin.time.weekdays.tue') },
    { value: 3, label: t('orbien.plugin.time.weekdays.wed') },
    { value: 4, label: t('orbien.plugin.time.weekdays.thu') },
    { value: 5, label: t('orbien.plugin.time.weekdays.fri') },
    { value: 6, label: t('orbien.plugin.time.weekdays.sat') },
    { value: 7, label: t('orbien.plugin.time.weekdays.sun') }
  ])

  const timezoneOptions = [
    'Asia/Shanghai',
    'Asia/Hong_Kong',
    'Asia/Tokyo',
    'UTC',
    'America/New_York',
    'Europe/London'
  ]

  const formData = reactive({
    enabled: false,
    mode: AccessControl.ALLOW as number,
    timeEnabled: true,
    timezone: 'Asia/Shanghai',
    days: [] as number[],
    windows: [] as Api.TimeAccess.WindowDTO[]
  })

  const editingWindowId = ref<number | null>(null)

  const isAllDaysSelected = computed(() => formData.days.length === 7)
  const isDaysIndeterminate = computed(() => formData.days.length > 0 && formData.days.length < 7)

  const resetForm = () => {
    formData.enabled = false
    formData.mode = AccessControl.ALLOW
    formData.timeEnabled = true
    formData.timezone = 'Asia/Shanghai'
    formData.days = []
    formData.windows = []
    editingWindowId.value = null
  }

  const fetchData = async () => {
    const response = await fetchGetTimeAccess(props.proxyId)
    if (!response) return
    formData.enabled = !!response.enabled
    formData.mode = response.mode ?? AccessControl.ALLOW
    formData.timeEnabled = response.timeEnabled !== false
    formData.timezone = response.timezone || 'Asia/Shanghai'
    formData.days = [...(response.days || [])]
    formData.windows = (response.windows || []).map((w) => ({ ...w }))
  }

  watch(
    () => props.proxyId,
    async (id) => {
      if (!id) return
      resetForm()
      await fetchData()
    },
    { immediate: true }
  )

  const handleConfigChange = async () => {
    await fetchUpdateTimeAccess({
      proxyId: props.proxyId,
      enabled: formData.enabled,
      mode: formData.mode,
      timeEnabled: formData.timeEnabled,
      timezone: formData.timezone,
      days: [...formData.days]
    })
  }

  const handleSelectAllDays = async (checked: boolean | string | number) => {
    formData.days = checked ? weekDays.value.map((d) => d.value) : []
    await handleConfigChange()
  }

  const addWindow = () => {
    formData.windows.push({
      id: 0,
      start: '',
      end: ''
    })
    editingWindowId.value = 0
  }

  const handleEditWindow = (row: Api.TimeAccess.WindowDTO) => {
    editingWindowId.value = row.id ?? 0
  }

  const handleSaveWindow = async (row: Api.TimeAccess.WindowDTO) => {
    if (!row.start || !row.end) {
      ElMessage.error(t('orbien.plugin.time.timeRequired'))
      return
    }
    if (row.id && row.id > 0) {
      await fetchUpdateTimeAccessWindow({
        id: row.id,
        proxyId: props.proxyId,
        start: row.start,
        end: row.end
      })
    } else {
      await fetchAddTimeAccessWindow({
        proxyId: props.proxyId,
        start: row.start,
        end: row.end
      })
    }
    editingWindowId.value = null
    await fetchData()
  }

  const handleDeleteWindow = async (row: Api.TimeAccess.WindowDTO) => {
    await ElMessageBox.confirm(t('orbien.plugin.deleteConfirm.window'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    if (row.id && row.id > 0) {
      await fetchDeleteTimeAccessWindow(row.id)
      await fetchData()
      return
    }
    const index = formData.windows.indexOf(row)
    if (index > -1) {
      formData.windows.splice(index, 1)
    }
    editingWindowId.value = null
  }
</script>
