<template>
  <section v-if="selectedDomains.length || extraDomainText.trim()" class="section-block">
    <div class="section-title">{{ $t('orbien.tls.acme.section.thisApplication') }}</div>
    <div class="selected-domains">
      <ElTag
          v-for="item in selectedDomains"
          :key="item.proxyDomainId"
          type="primary"
          effect="plain"
          class="domain-tag"
      >
        {{ item.fullDomain }}
      </ElTag>
      <ElTag
          v-for="domain in extraDomains"
          :key="`extra-${domain}`"
          type="warning"
          effect="plain"
          class="domain-tag"
      >
        {{ domain }}
      </ElTag>
    </div>
  </section>

  <ElCollapse v-model="advancedPanels" class="advanced-collapse">
    <ElCollapseItem :title="$t('orbien.tls.acme.section.extraDomains')" name="extra">
      <ElInput
          :model-value="extraDomainText"
          type="textarea"
          :rows="3"
          placeholder="*.example.com"
          @update:model-value="emit('update:extraDomainText', $event)"
      />
    </ElCollapseItem>
  </ElCollapse>
</template>

<script setup lang="ts">
import {ref} from 'vue'

defineOptions({name: 'AcmeApplyDomainSummary'})

defineProps<{
  selectedDomains: Api.AcmeOrder.HttpsProxyDomainOption[]
  extraDomains: string[]
  extraDomainText: string
}>()

const emit = defineEmits<{
  (e: 'update:extraDomainText', value: string): void
}>()

const advancedPanels = ref<string[]>([])
</script>

<style lang="scss">
@use './acme-apply-shared.scss';
</style>
