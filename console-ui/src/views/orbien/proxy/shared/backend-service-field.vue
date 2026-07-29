<template>
  <ElFormItem :label="t('orbien.proxy.backend')" :prop="clusterMode ? undefined : hostProp">
    <div v-if="clusterMode" class="cluster-backend">
      <span class="cluster-backend__summary">{{ t('orbien.proxy.clusterSummary', { n: targets.length }) }}</span>
      <ElButton link type="primary" class="cluster-backend__link" @click="emit('open-cluster')">
        {{ t('orbien.proxy.goClusterConfig') }}
      </ElButton>
    </div>
    <slot v-else/>
  </ElFormItem>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineOptions({name: 'BackendServiceField'})

const { t } = useI18n()

withDefaults(
    defineProps<{
      clusterMode: boolean
      targets?: Api.Proxy.TargetDTO[]
      hostProp?: string
    }>(),
    {
      targets: () => [],
      hostProp: 'localHost'
    }
)

const emit = defineEmits<{
  (e: 'open-cluster'): void
}>()
</script>

<style scoped lang="scss">
.cluster-backend {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  min-height: 32px;
  padding: 8px 12px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
}

.cluster-backend__summary {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.cluster-backend__link {
  flex-shrink: 0;
  padding: 0;
  font-size: 13px;
}
</style>
