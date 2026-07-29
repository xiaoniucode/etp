<template>
  <ElInput v-model.number="port" type="number" :placeholder="t('orbien.proxy.localPort')">
    <template #prepend>
      <ElSelect
          v-model="presetPort"
          :placeholder="t('orbien.proxy.commonPorts')"
          clearable
          :style="{ width: selectWidth }"
          @change="onPresetChange"
      >
        <ElOption
            v-for="item in presets"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </ElSelect>
    </template>
  </ElInput>
</template>

<script setup lang="ts">
import {ref, watch} from 'vue'
import { useI18n } from 'vue-i18n'
import {matchPortPreset, type PortPresetOption} from './port-presets'

defineOptions({name: 'LocalPortInput'})

const { t } = useI18n()

const props = withDefaults(
    defineProps<{
      presets: PortPresetOption[]
      selectWidth?: string
    }>(),
    {
      selectWidth: '115px'
    }
)

const port = defineModel<number | undefined>()

/**
 * 仅当端口命中预设时才在下拉中展示选中项
 * */
const presetPort = ref<number | undefined>()

const syncPresetFromPort = () => {
  presetPort.value = matchPortPreset(port.value, props.presets)
}

watch(
    port,
    () => {
      syncPresetFromPort()
    },
    {immediate: true}
)

const onPresetChange = (value: number | undefined) => {
  if (value != null) {
    port.value = value
  }
}
</script>
