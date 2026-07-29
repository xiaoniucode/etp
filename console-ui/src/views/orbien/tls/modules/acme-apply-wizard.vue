<template>
  <ElDialog
      v-model="dialogVisible"
      :title="$t('orbien.tls.acme.wizard.title')"
      width="760px"
      align-center
      destroy-on-close
      :close-on-click-modal="false"
  >
    <ElSteps :active="currentStep" finish-status="success" align-center class="acme-wizard wizard-steps">
      <ElStep :title="$t('orbien.tls.acme.wizard.steps.domain')"/>
      <ElStep :title="$t('orbien.tls.acme.wizard.steps.validation')"/>
      <ElStep :title="$t('orbien.tls.acme.wizard.steps.dnsVerify')"/>
    </ElSteps>

    <div class="acme-wizard wizard-body" v-loading="submitting" :element-loading-text="$t('orbien.tls.acme.wizard.submitting')">
      <AcmeApplyStepDomainGlobal
          v-show="currentStep === 0"
          ref="domainStepRef"
          :visible="dialogVisible"
          v-model:cert-brand="certBrand"
          v-model:selected-domains="selectedDomains"
          v-model:extra-domain-text="extraDomainText"
          :parsed-extra-domains="parsedExtraDomains"
      />

      <AcmeApplyStepValidation
          v-show="currentStep === 1"
          v-model:validation-mode="validationMode"
          v-model:dns-credential-id="dnsCredentialId"
          :credential-list="credentialList"
          :credential-loading="credentialLoading"
          @add-credential="openDnsDialog"
      />

      <AcmeApplyStepDnsVerify
          v-show="currentStep === 2"
          :validation-mode="validationMode"
          :order-result="orderResult"
          @copy="copyText"
      />
    </div>

    <template #footer>
      <ElButton v-if="currentStep > 0 && currentStep < 2" :disabled="submitting" @click="currentStep--">{{ $t('orbien.tls.acme.wizard.prev') }}
      </ElButton>
      <ElButton v-if="currentStep < 1" type="primary" @click="handleNext">{{ $t('orbien.tls.acme.wizard.next') }}</ElButton>
      <ElButton v-else-if="currentStep === 1" type="primary" :loading="submitting" @click="handleSubmit">{{ $t('orbien.tls.acme.wizard.submit') }}
      </ElButton>
      <ElButton
          v-else-if="validationMode === 1 && canManualVerify"
          type="primary"
          :loading="verifying"
          @click="handleVerify"
      >
        {{ $t('orbien.tls.acme.wizard.startVerify') }}
      </ElButton>
      <ElButton v-else-if="currentStep === 2" :loading="refreshing" @click="refreshOrder">{{ $t('orbien.tls.acme.wizard.refreshStatus') }}</ElButton>
    </template>
  </ElDialog>

  <DnsCredentialDialog v-model:visible="dnsDialogVisible" @submit="handleDnsCredentialSaved"/>
</template>

<script setup lang="ts">
import {computed, ref, toRef} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage} from 'element-plus'
import DnsCredentialDialog from './dns-credential-dialog.vue'
import AcmeApplyStepDomainGlobal from './acme/acme-apply-step-domain-global.vue'
import AcmeApplyStepValidation from './acme/acme-apply-step-validation.vue'
import AcmeApplyStepDnsVerify from './acme/acme-apply-step-dns-verify.vue'
import {useAcmeApplyWizard} from './acme/use-acme-apply-wizard'

defineOptions({name: 'AcmeApplyWizard'})

const {t} = useI18n()

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const domainStepRef = ref<InstanceType<typeof AcmeApplyStepDomainGlobal>>()

const {
  currentStep,
  certBrand,
  selectedDomains,
  extraDomainText,
  parsedExtraDomains,
  validationMode,
  dnsCredentialId,
  credentialList,
  credentialLoading,
  dnsDialogVisible,
  submitting,
  verifying,
  refreshing,
  orderResult,
  canManualVerify,
  openDnsDialog,
  handleDnsCredentialSaved,
  validateDomainStep,
  handleSubmit,
  refreshOrder,
  handleVerify,
  copyText
} = useAcmeApplyWizard({
  visible: toRef(props, 'visible'),
  onSuccess: () => emit('success')
})

const handleNext = () => {
  if (domainStepRef.value && !domainStepRef.value.validate()) {
    ElMessage.warning(t('orbien.tls.acme.wizard.selectHttpsProxy'))
    return
  }
  if (!validateDomainStep()) return
  currentStep.value++
}
</script>

<style lang="scss">
@use './acme/acme-apply-shared.scss';
</style>
