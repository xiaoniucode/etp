<template>
  <div class="health-check-page" v-loading="loading">
    <section class="setting-section">
      <div class="section-header">
        <h3 class="section-title">{{ t('orbien.plugin.basic.title') }}</h3>
        <p class="section-desc">{{ t('orbien.plugin.health.desc') }}</p>
      </div>
      <div class="setting-item">
        <div class="setting-meta">
          <span class="setting-name">{{ t('orbien.plugin.basic.enabled') }}</span>
        </div>
        <ElSwitch v-model="form.enabled" @change="handleEnableChange" />
      </div>
    </section>

    <section class="setting-section">
      <div class="setting-list">
        <div class="setting-item">
          <div class="setting-meta">
            <span class="setting-name">{{ t('orbien.plugin.health.checkType') }}</span>
            <span class="setting-hint">{{ t('orbien.plugin.health.checkTypeHint') }}</span>
          </div>
          <ElRadioGroup v-model="form.type">
            <ElRadio :value="HealthCheckType.TCP">TCP</ElRadio>
            <ElRadio :value="HealthCheckType.HTTP">HTTP</ElRadio>
          </ElRadioGroup>
        </div>

        <div v-for="field in numberFields" :key="field.key" class="setting-item">
          <div class="setting-meta">
            <span class="setting-name">{{ field.label }}</span>
            <span class="setting-hint">{{ field.hint }}</span>
          </div>
          <div class="number-input-wrap">
            <ElInputNumber
              v-model="form[field.key]"
              :controls="false"
              :min="1"
              :precision="0"
              class="number-input"
            />
            <span class="number-unit">{{ field.unit }}</span>
          </div>
        </div>

        <div v-if="form.type === HealthCheckType.HTTP" class="setting-item">
          <div class="setting-meta">
            <span class="setting-name">{{ t('orbien.plugin.health.checkPath') }}</span>
            <span class="setting-hint">{{ t('orbien.plugin.health.checkPathHint') }}</span>
          </div>
          <ElInput v-model="form.path" placeholder="/health" class="path-input" />
        </div>
      </div>
    </section>

    <div class="form-actions">
      <ElButton type="primary" :loading="saving" @click="handleSave">{{ t('orbien.plugin.basic.saveConfig') }}</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage } from 'element-plus'
  import {
    fetchGetHealthCheck,
    fetchSaveHealthCheck,
    fetchUpdateHealthCheckStatus
  } from '@/api/health-check'
  import type { ProxyConfigProtocol } from '../../menus'
  import { HealthCheckType, ProtocolType } from '@/enums/orbien/business'

  defineOptions({ name: 'HealthCheckPage' })

  const { t } = useI18n()

  type NumberFieldKey = 'interval' | 'timeout' | 'maxFailed'

  const numberFields = computed(() => [
    {
      key: 'interval' as NumberFieldKey,
      label: t('orbien.plugin.health.interval'),
      hint: t('orbien.plugin.health.intervalHint'),
      unit: t('orbien.plugin.health.unitSecond')
    },
    {
      key: 'timeout' as NumberFieldKey,
      label: t('orbien.plugin.health.timeout'),
      hint: t('orbien.plugin.health.timeoutHint'),
      unit: t('orbien.plugin.health.unitSecond')
    },
    {
      key: 'maxFailed' as NumberFieldKey,
      label: t('orbien.plugin.health.maxFailed'),
      hint: t('orbien.plugin.health.maxFailedHint'),
      unit: t('orbien.plugin.health.unitTimes')
    }
  ])

  const props = defineProps<{
    proxyId: string
    protocol: ProxyConfigProtocol
  }>()

  const loading = ref(false)
  const saving = ref(false)
  const toggling = ref(false)

  const form = reactive({
    enabled: false,
    type: HealthCheckType.TCP,
    interval: 10,
    timeout: 8,
    maxFailed: 3,
    path: '/health'
  })

  const getDefaultType = () =>
    props.protocol === ProtocolType.TCP || props.protocol === ProtocolType.UDP
      ? HealthCheckType.TCP
      : HealthCheckType.HTTP

  const fillForm = (data: Api.HealthCheck.HealthCheckDTO) => {
    form.enabled = data.enabled ?? false
    form.type = data.type ?? getDefaultType()
    form.interval = data.interval ?? 10
    form.timeout = data.timeout ?? 8
    form.maxFailed = data.maxFailed ?? 3
    form.path = data.path || '/health'
  }

  const loadData = async () => {
    loading.value = true
    try {
      const data = await fetchGetHealthCheck(props.proxyId)
      fillForm(data)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => [props.proxyId, props.protocol] as const,
    ([proxyId]) => {
      if (proxyId) loadData()
    },
    { immediate: true }
  )

  const handleEnableChange = async () => {
    if (toggling.value) return
    toggling.value = true
    try {
      await fetchUpdateHealthCheckStatus({
        proxyId: props.proxyId,
        enabled: form.enabled
      })
    } catch {
      form.enabled = !form.enabled
    } finally {
      toggling.value = false
    }
  }

  const handleSave = async () => {
    if (form.type === HealthCheckType.HTTP && !form.path.trim()) {
      ElMessage.warning(t('orbien.plugin.health.pathRequired'))
      return
    }

    saving.value = true
    try {
      await fetchSaveHealthCheck({
        proxyId: props.proxyId,
        type: form.type,
        interval: form.interval,
        timeout: form.timeout,
        maxFailed: form.maxFailed,
        path: form.type === HealthCheckType.HTTP ? form.path.trim() : undefined
      })
      await loadData()
    } finally {
      saving.value = false
    }
  }
</script>

<style scoped lang="scss">
  .health-check-page {
    max-width: 640px;
  }

  .setting-section {
    padding: 16px;
    margin-bottom: 16px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    background: var(--el-fill-color-blank);
  }

  .section-header {
    margin-bottom: 16px;
  }

  .section-title {
    margin: 0 0 4px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .section-desc {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
  }

  .setting-item {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 16px 0;
    border-top: 1px solid var(--el-border-color-lighter);

    &:first-child {
      padding-top: 0;
      border-top: none;
    }

    &:last-child {
      padding-bottom: 0;
    }
  }

  .setting-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .setting-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .setting-hint {
    font-size: 12px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
  }

  .number-input,
  .path-input {
    width: 240px;
  }

  .number-input-wrap {
    display: flex;
    align-items: center;
    gap: 8px;
    width: fit-content;
  }

  .number-unit {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .form-actions {
    padding-top: 4px;
  }
</style>
