<template>
  <div class="cron-schedule-picker">
    <div class="picker-row">
      <ElSelect v-model="schedule.type" class="type-select" @change="handleTypeChange">
        <ElOption
          v-for="item in cronScheduleTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </ElSelect>

      <div v-if="schedule.type === 'everyNDays'" class="number-field">
        <ElInputNumber v-model="schedule.interval" :min="1" :max="31" controls-position="right" />
        <span class="number-unit">{{ $t('orbien.cron.unit.days') }}</span>
      </div>

      <ElSelect v-if="schedule.type === 'weekly'" v-model="schedule.dayOfWeek" class="weekday-select">
        <ElOption
          v-for="item in weekdayOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </ElSelect>

      <div v-if="schedule.type === 'monthly'" class="number-field">
        <ElInputNumber v-model="schedule.dayOfMonth" :min="1" :max="31" controls-position="right" />
        <span class="number-unit">{{ $t('orbien.cron.unit.days') }}</span>
      </div>

      <template v-if="showHourMinute">
        <div class="number-field">
          <ElInputNumber v-model="schedule.hour" :min="0" :max="23" controls-position="right" />
          <span class="number-unit">{{ $t('orbien.cron.unit.hours') }}</span>
        </div>
        <div class="number-field">
          <ElInputNumber v-model="schedule.minute" :min="0" :max="59" controls-position="right" />
          <span class="number-unit">{{ $t('orbien.cron.unit.minutes') }}</span>
        </div>
      </template>

      <template v-else-if="schedule.type === 'hourly'">
        <div class="number-field">
          <ElInputNumber v-model="schedule.minute" :min="0" :max="59" controls-position="right" />
          <span class="number-unit">{{ $t('orbien.cron.unit.minutes') }}</span>
        </div>
      </template>

      <template v-else-if="schedule.type === 'everyNHours'">
        <div class="number-field">
          <ElInputNumber v-model="schedule.interval" :min="1" :max="23" controls-position="right" />
          <span class="number-unit">{{ $t('orbien.cron.unit.hours') }}</span>
        </div>
        <div class="number-field">
          <ElInputNumber v-model="schedule.minute" :min="0" :max="59" controls-position="right" />
          <span class="number-unit">{{ $t('orbien.cron.unit.minutes') }}</span>
        </div>
      </template>

      <div v-else-if="schedule.type === 'everyNMinutes'" class="number-field">
        <ElInputNumber v-model="schedule.interval" :min="1" :max="59" controls-position="right" />
        <span class="number-unit">{{ $t('orbien.cron.unit.minutes') }}</span>
      </div>

      <div v-else-if="schedule.type === 'everyNSeconds'" class="number-field">
        <ElInputNumber v-model="schedule.interval" :min="1" :max="59" controls-position="right" />
        <span class="number-unit">{{ $t('orbien.cron.unit.seconds') }}</span>
      </div>
    </div>

    <ElInput
      v-if="schedule.type === 'custom'"
      v-model="schedule.customExpression"
      class="custom-input"
      placeholder="0 0 1 * * ?"
    />
    <div v-if="schedule.type === 'custom'" class="form-tip">{{ $t('orbien.cron.customFormat') }}</div>

    <div class="picker-preview">{{ previewText }}</div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import {
    getCronScheduleTypeOptions,
    getWeekdayOptions,
    buildCronExpression,
    createDefaultSchedule,
    describeCronSchedule,
    parseCronExpression,
    type CronSchedule,
    type CronScheduleType
  } from '@/utils/cron-schedule'

  defineOptions({ name: 'CronSchedulePicker' })

  interface Props {
    modelValue: string
  }

  interface Emits {
    (e: 'update:modelValue', value: string): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { locale } = useI18n()

  const schedule = ref<CronSchedule>(parseCronExpression(props.modelValue))
  const syncing = ref(false)

  const cronScheduleTypeOptions = computed(() => {
    void locale.value
    return getCronScheduleTypeOptions()
  })

  const weekdayOptions = computed(() => {
    void locale.value
    return getWeekdayOptions()
  })

  const showHourMinute = computed(() =>
    ['daily', 'everyNDays', 'weekly', 'monthly'].includes(schedule.value.type)
  )

  const previewText = computed(() => {
    void locale.value
    return describeCronSchedule(schedule.value)
  })

  const handleTypeChange = (type: CronScheduleType) => {
    schedule.value = createDefaultSchedule(type)
  }

  watch(
    () => props.modelValue,
    (value) => {
      if (syncing.value) {
        return
      }
      schedule.value = parseCronExpression(value)
    }
  )

  watch(
    schedule,
    (value) => {
      syncing.value = true
      emit('update:modelValue', buildCronExpression(value))
      queueMicrotask(() => {
        syncing.value = false
      })
    },
    { deep: true }
  )
</script>

<style lang="scss" scoped>
  .cron-schedule-picker {
    width: 100%;
  }

  .picker-row {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }

  .type-select {
    width: 112px;
  }

  .weekday-select {
    width: 96px;
  }

  .number-field {
    display: inline-flex;
    align-items: center;
    overflow: hidden;
    border: 1px solid var(--el-border-color);
    border-radius: var(--el-border-radius-base);
    background: var(--el-fill-color-blank);

    :deep(.el-input-number) {
      width: 88px;

      .el-input__wrapper {
        box-shadow: none;
      }
    }
  }

  .number-unit {
    padding: 0 10px;
    font-size: 13px;
    color: var(--el-text-color-regular);
    white-space: nowrap;
    background: var(--el-fill-color-light);
    border-left: 1px solid var(--el-border-color);
    line-height: 32px;
  }

  .custom-input {
    margin-top: 8px;
  }

  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .picker-preview {
    margin-top: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1.5;
  }
</style>
