<template>
  <div class="acme-wizard dns-verify-step">
    <ElAlert
        v-if="validationMode === 1"
        type="warning"
        :closable="false"
        show-icon
        :title="$t('orbien.tls.acme.dnsVerify.manualHint')"
        class="mb-4"
    />
    <ElAlert
        v-else
        type="success"
        :closable="false"
        show-icon
        :title="$t('orbien.tls.acme.dnsVerify.autoHint')"
        class="mb-4"
    />

    <div v-if="orderResult?.challenges?.length">
      <div v-for="item in orderResult.challenges" :key="item.id" class="challenge-card">
        <div class="challenge-domain">{{ item.domain }}</div>
        <div v-if="item.dnsZone" class="field-row zone-tip">{{ $t('orbien.tls.acme.dnsVerify.dnsZone', { zone: item.dnsZone }) }}</div>
        <div class="field-row">
          <span>{{ $t('orbien.tls.acme.dnsVerify.hostRecord') }}</span>
          <code>{{ item.hostRecord || item.recordName }}</code>
          <ElButton link type="primary" @click="emit('copy', item.hostRecord || item.recordName)">{{ $t('common.copy') }}</ElButton>
        </div>
        <div class="field-row">
          <span>{{ $t('orbien.tls.acme.dnsVerify.recordType') }}</span>
          <code>TXT</code>
        </div>
        <div class="field-row">
          <span>{{ $t('orbien.tls.acme.dnsVerify.recordValue') }}</span>
          <code class="record-value">{{ item.recordValue }}</code>
          <ElButton link type="primary" @click="emit('copy', item.recordValue)">{{ $t('common.copy') }}</ElButton>
        </div>
      </div>
    </div>

    <div v-if="orderResult" class="order-status">
      {{ $t('orbien.tls.acme.dnsVerify.currentStatus') }}
      <ElTag size="small" :type="resolveAcmeOrderStatusTagType(orderResult.status)">
        {{ orderResult.statusLabel }}
      </ElTag>
    </div>
    <div v-if="orderResult?.errorMessage" class="error-text">{{ orderResult.errorMessage }}</div>
  </div>
</template>

<script setup lang="ts">
import {resolveAcmeOrderStatusTagType} from '@/utils/ui/status-tag'

defineOptions({name: 'AcmeApplyStepDnsVerify'})

defineProps<{
  validationMode: number
  orderResult: Api.AcmeOrder.OrderDTO | null
}>()

const emit = defineEmits<{
  (e: 'copy', text: string): void
}>()
</script>

<style lang="scss">
@use './acme-apply-shared.scss';
</style>
