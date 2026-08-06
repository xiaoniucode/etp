<template>
  <ElDrawer
      v-model="drawerVisible"
      :title="drawerTitle"
      size="760px"
      destroy-on-close
  >
    <ArtTableHeader :loading="loading" @refresh="refreshData">
      <template #left>
        <ElButton @click="handleBatchDelete" :disabled="selectedRows.length === 0" v-ripple>
          {{ $t('orbien.scheduledJob.actions.batchDelete') }}
        </ElButton>
      </template>
    </ArtTableHeader>

    <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
    />
  </ElDrawer>
</template>

<script setup lang="ts">
import {computed, h, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {fetchDeleteScheduledJobLogs, fetchScheduledJobLogs} from '@/api/scheduled-job'
import {resolveBinaryOutcomeTagType} from '@/utils/ui/status-tag'

defineOptions({name: 'JobLogDrawer'})

interface Props {
  visible: boolean
  jobCode?: string | null
  jobName?: string
}

interface Emits {
  (e: 'update:visible', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const {t} = useI18n()

const selectedRows = ref<Api.ScheduledJob.JobLogDTO[]>([])

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const drawerTitle = computed(() =>
    t('orbien.scheduledJob.log.title', {name: props.jobName || ''})
)

const handleSelectionChange = (selection: Api.ScheduledJob.JobLogDTO[]) => {
  selectedRows.value = selection
}

const deleteLogs = async (rows: Api.ScheduledJob.JobLogDTO[]) => {
  if (!props.jobCode || rows.length === 0) {
    return
  }
  const ids = rows.map((row) => row.id)
  await fetchDeleteScheduledJobLogs(props.jobCode, ids)
  ElMessage.success(t('orbien.scheduledJob.message.deleteSuccess'))
  selectedRows.value = []
  refreshData()
}

const handleDelete = async (row: Api.ScheduledJob.JobLogDTO) => {
  try {
    await ElMessageBox.confirm(
        t('orbien.scheduledJob.confirm.deleteLogMessage'),
        t('orbien.scheduledJob.confirm.deleteLogTitle'),
        {
          confirmButtonText: t('orbien.scheduledJob.actions.delete'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
    )
    await deleteLogs([row])
  } catch (error) {
    if (error === 'cancel') return
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t('orbien.scheduledJob.message.selectLogs'))
    return
  }
  try {
    await ElMessageBox.confirm(
        t('orbien.scheduledJob.confirm.batchDeleteMessage', {
          count: selectedRows.value.length
        }),
        t('orbien.scheduledJob.confirm.batchDeleteTitle'),
        {
          confirmButtonText: t('orbien.scheduledJob.actions.delete'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
    )
    await deleteLogs(selectedRows.value)
  } catch (error) {
    if (error === 'cancel') return
  }
}

const {
  columns,
  data,
  loading,
  pagination,
  handleSizeChange,
  handleCurrentChange,
  refreshData,
  resetSearchParams
} = useTable({
  core: {
    apiFn: (params: Api.Common.CommonSearchParams) => {
      if (!props.jobCode) {
        return Promise.resolve({
          records: [],
          total: 0,
          current: params.current ?? 1,
          size: params.size ?? 10
        })
      }
      return fetchScheduledJobLogs(props.jobCode, params)
    },
    apiParams: {
      current: 1,
      size: 10
    },
    immediate: false,
    columnsFactory: () => [
      {type: 'selection'},
      {
        prop: 'startedAt',
        label: t('orbien.scheduledJob.log.columns.startedAt'),
        width: 170
      },
      {
        prop: 'finishedAt',
        label: t('orbien.scheduledJob.log.columns.finishedAt'),
        width: 170
      },
      {
        prop: 'triggerTypeLabel',
        label: t('orbien.scheduledJob.log.columns.triggerType'),
        width: 90
      },
      {
        prop: 'statusLabel',
        label: t('orbien.scheduledJob.log.columns.status'),
        width: 80,
        formatter: (row: Api.ScheduledJob.JobLogDTO) =>
            h(
                ElTag,
                {size: 'small', type: resolveBinaryOutcomeTagType(row.status)},
                () => row.statusLabel
            )
      },
      {
        prop: 'affectedCount',
        label: t('orbien.scheduledJob.log.columns.affectedCount'),
        width: 90
      },

      {
        prop: 'message',
        label: t('orbien.scheduledJob.log.columns.message'),
        minWidth: 140,
        showOverflowTooltip: true
      },
      {
        prop: 'operation',
        label: t('orbien.scheduledJob.log.columns.operation'),
        width: 80,
        fixed: 'right',
        formatter: (row: Api.ScheduledJob.JobLogDTO) =>
            h(ArtButtonTable, {
              type: 'link',
              text: t('orbien.scheduledJob.actions.delete'),
              onClick: () => handleDelete(row)
            })
      }
    ]
  }
})

watch(
    () => [props.visible, props.jobCode],
    ([visible, jobCode]) => {
      if (visible && jobCode) {
        selectedRows.value = []
        resetSearchParams()
        refreshData()
      }
    }
)
</script>

<style lang="scss" scoped>
:deep(.art-table) {
  padding: 0;
}
</style>
