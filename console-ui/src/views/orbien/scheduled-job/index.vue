<template>
  <div class="scheduled-job-page art-full-height">
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData"/>

      <ArtTable
          :loading="loading"
          :data="data"
          :columns="columns"
          :show-pagination="false"
      />
    </ElCard>

    <JobConfigDrawer
        v-model:visible="configVisible"
        :job-code="currentJobCode"
        @saved="refreshData"
    />
    <JobLogDrawer
        v-model:visible="logVisible"
        :job-code="currentJobCode"
        :job-name="currentJobName"
    />
  </div>
</template>

<script setup lang="ts">
import {h, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage, ElMessageBox, ElSwitch, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {
  fetchRunScheduledJob,
  fetchScheduledJobList,
  fetchUpdateScheduledJobEnabled
} from '@/api/scheduled-job'
import {describeCronExpression} from '@/utils/cron-schedule'
import {resolveBinaryOutcomeTagType} from '@/utils/ui/status-tag'
import JobConfigDrawer from './modules/job-config-drawer.vue'
import JobLogDrawer from './modules/job-log-drawer.vue'

defineOptions({name: 'ScheduledJobManagement'})

const {t} = useI18n()

const ACME_RENEW_JOB_CODE = 'ACME_RENEW'

const configVisible = ref(false)
const logVisible = ref(false)
const currentJobCode = ref<string | null>(null)
const currentJobName = ref('')
const togglingCode = ref<string | null>(null)

const openConfig = (row: Api.ScheduledJob.JobDTO) => {
  currentJobCode.value = row.jobCode
  configVisible.value = true
}

const openLogs = (row: Api.ScheduledJob.JobDTO) => {
  currentJobCode.value = row.jobCode
  currentJobName.value = row.jobName
  logVisible.value = true
}

const handleEnabledChange = async (row: Api.ScheduledJob.JobDTO, enabled: boolean) => {
  if (!enabled && row.jobCode === ACME_RENEW_JOB_CODE) {
    try {
      await ElMessageBox.confirm(
          t('orbien.scheduledJob.confirm.disableAcmeMessage'),
          t('orbien.scheduledJob.confirm.disableAcmeTitle', {name: row.jobName}),
          {
            confirmButtonText: t('orbien.scheduledJob.actions.disable'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          }
      )
    } catch {
      return
    }
  }

  togglingCode.value = row.jobCode
  try {
    await fetchUpdateScheduledJobEnabled(row.jobCode, enabled)
    row.enabled = enabled
    if (!enabled) {
      row.nextRunAt = undefined
    }
    ElMessage.success(
        enabled ? t('orbien.scheduledJob.message.enabled') : t('orbien.scheduledJob.message.disabled')
    )
    refreshData()
  } catch {
    row.enabled = !enabled
  } finally {
    togglingCode.value = null
  }
}

const handleRun = async (row: Api.ScheduledJob.JobDTO) => {
  try {
    await ElMessageBox.confirm(
        t('orbien.scheduledJob.confirm.runMessage', {name: row.jobName}),
        t('orbien.scheduledJob.confirm.runTitle'),
        {
          confirmButtonText: t('orbien.scheduledJob.actions.run'),
          cancelButtonText: t('common.cancel'),
          type: 'info'
        }
    )
    await fetchRunScheduledJob(row.jobCode)
    ElMessage.success(t('orbien.scheduledJob.message.triggered'))
    refreshData()
  } catch (error) {
    if (error === 'cancel') return
  }
}

const {
  columns,
  columnChecks,
  data,
  loading,
  refreshData
} = useTable({
  core: {
    apiFn: fetchScheduledJobList,
    columnsFactory: () => [
      {
        prop: 'jobName',
        label: t('orbien.scheduledJob.columns.jobName'),
        minWidth: 160
      },
      {
        prop: 'enabled',
        label: t('orbien.scheduledJob.columns.status'),
        width: 130,
        formatter: (row: Api.ScheduledJob.JobDTO) =>
            h('div', {class: 'status-cell'}, [
              h(
                  'span',
                  {
                    class: ['status-text', row.enabled ? 'is-active' : '']
                  },
                  row.enabled
                      ? t('orbien.scheduledJob.status.active')
                      : t('orbien.scheduledJob.status.disabled')
              ),
              h(ElSwitch, {
                modelValue: row.enabled,
                size: 'small',
                loading: togglingCode.value === row.jobCode,
                'onUpdate:modelValue': (enabled: string | number | boolean) =>
                    handleEnabledChange(row, Boolean(enabled))
              })
            ])
      },
      {
        prop: 'cronExpression',
        label: t('orbien.scheduledJob.columns.schedule'),
        minWidth: 200,
        showOverflowTooltip: true,
        formatter: (row: Api.ScheduledJob.JobDTO) => describeCronExpression(row.cronExpression)
      },
      {
        prop: 'lastRunAt',
        label: t('orbien.scheduledJob.columns.lastRun'),
        width: 170
      },
      {
        prop: 'lastRunStatusLabel',
        label: t('orbien.scheduledJob.columns.result'),
        width: 100,
        formatter: (row: Api.ScheduledJob.JobDTO) => {
          if (!row.lastRunStatusLabel) {
            return ''
          }
          return h(
              ElTag,
              {type: resolveBinaryOutcomeTagType(row.lastRunStatus), size: 'small'},
              () => row.lastRunStatusLabel
          )
        }
      },
      {
        prop: 'nextRunAt',
        label: t('orbien.scheduledJob.columns.nextRun'),
        width: 170,
        formatter: (row: Api.ScheduledJob.JobDTO) => (row.enabled ? row.nextRunAt || '' : '')
      },
      {
        prop: 'operation',
        label: t('orbien.scheduledJob.columns.operation'),
        width: 150,
        fixed: 'right',
        formatter: (row: Api.ScheduledJob.JobDTO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.scheduledJob.actions.config'),
                onClick: () => openConfig(row)
              }),
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.scheduledJob.actions.run'),
                onClick: () => handleRun(row)
              }),
              h(ArtButtonTable, {
                type: 'link',
                text: t('orbien.scheduledJob.actions.logs'),
                onClick: () => openLogs(row)
              })
            ])
      }
    ]
  }
})
</script>

<style lang="scss" scoped>
:deep(.status-cell) {
  display: inline-flex;
  gap: 10px;
  align-items: center;
}

:deep(.status-text) {
  min-width: 28px;
  font-size: 13px;
  color: var(--el-text-color-secondary);

  &.is-active {
    color: var(--el-text-color-primary);
  }
}
</style>
