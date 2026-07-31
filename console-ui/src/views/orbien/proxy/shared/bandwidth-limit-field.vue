<template>
  <ElFormItem :label="t('orbien.proxy.bandwidthLimit')" prop="bandwidth">
    <div class="bandwidth-limit-field">
      <ElInputNumber
          v-model="bandwidth"
          :placeholder="t('orbien.proxy.noBandwidthLimit')"
          :controls="false"
          :min="1"
          :precision="0"
          class="bandwidth-input"
      />
      <span class="bandwidth-unit">Mbps</span>
    </div>
  </ElFormItem>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type {BandwidthMbps} from './bandwidth-limit'

defineOptions({name: 'BandwidthLimitField'})

const { t } = useI18n()

const bandwidth = defineModel<BandwidthMbps>()
</script>

<style scoped lang="scss">
.bandwidth-limit-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bandwidth-input {
  width: 200px;
}

.bandwidth-unit {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
