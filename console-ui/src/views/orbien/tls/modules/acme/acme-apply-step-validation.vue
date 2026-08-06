<template>
  <div class="acme-wizard validation-step">
    <ElRadioGroup
        :model-value="validationMode"
        class="validation-modes"
        @update:model-value="onValidationModeChange"
    >
      <label class="mode-card art-card" :class="{ 'is-active': validationMode === 1 }">
        <ElRadio :value="1"/>
        <div class="mode-card__body">
          <div class="mode-card__title">{{ $t('orbien.tls.acme.validation.manualTitle') }}</div>
          <div class="mode-card__desc">{{ $t('orbien.tls.acme.validation.manualDesc') }}</div>
        </div>
      </label>
      <label class="mode-card art-card" :class="{ 'is-active': validationMode === 2 }">
        <ElRadio :value="2"/>
        <div class="mode-card__body">
          <div class="mode-card__title">
            {{ $t('orbien.tls.acme.validation.cloudTitle') }}
            <ElTag size="small" type="success" effect="plain" class="mode-card__tag">
              {{ $t('orbien.tls.acme.validation.recommended') }}
            </ElTag>
          </div>
          <div class="mode-card__desc">{{ $t('orbien.tls.acme.validation.cloudDesc') }}</div>
        </div>
      </label>
    </ElRadioGroup>

    <div v-if="validationMode === 2" class="dns-credential-block">
      <ElAlert
          type="info"
          :closable="false"
          show-icon
          :title="$t('orbien.tls.acme.validation.cloudHint')"
          class="dns-hint"
      />

      <div v-if="credentialLoading" v-loading="true" class="credential-loading"/>

      <ElAlert v-else-if="!credentialList.length" type="warning" :closable="false" show-icon>
        <template #title>{{ $t('orbien.tls.acme.validation.noCredential') }}</template>
        <div class="empty-credential">
          <span>{{ $t('orbien.tls.acme.validation.addCredentialHint') }}</span>
          <ElButton type="primary" size="small" @click="emit('add-credential')">
            {{ $t('orbien.tls.acme.validation.addDnsCredential') }}
          </ElButton>
        </div>
      </ElAlert>

      <ElForm v-else label-width="88px" class="credential-form">
        <ElFormItem :label="$t('orbien.tls.acme.validation.dnsCredential')" required>
          <div class="credential-row">
            <ElSelect
                :model-value="dnsCredentialId"
                filterable
                :placeholder="$t('orbien.tls.acme.validation.selectCredential')"
                style="flex: 1"
                @update:model-value="emit('update:dnsCredentialId', $event)"
            >
              <ElOption
                  v-for="item in credentialList"
                  :key="item.id"
                  :label="`${item.name}（${item.providerLabel}）`"
                  :value="item.id"
              />
            </ElSelect>
            <ElButton link type="primary" @click="emit('add-credential')">{{
                $t('orbien.tls.acme.validation.add')
              }}
            </ElButton>
          </div>
        </ElFormItem>
      </ElForm>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({name: 'AcmeApplyStepValidation'})

defineProps<{
  validationMode: number
  dnsCredentialId?: number
  credentialList: Api.DnsCredential.CredentialDTO[]
  credentialLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'update:validationMode', value: number): void
  (e: 'update:dnsCredentialId', value?: number): void
  (e: 'add-credential'): void
}>()

const onValidationModeChange = (value: string | number | boolean | undefined) => {
  emit('update:validationMode', Number(value))
}
</script>

<style lang="scss">
@use './acme-apply-shared.scss';
</style>
