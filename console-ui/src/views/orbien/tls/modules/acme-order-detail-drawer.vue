<template>
  <ElDrawer v-model="drawerVisible" :title="$t('orbien.tls.acme.order.detail.title')" size="560px" destroy-on-close>
    <div v-loading="loading">
      <ElDescriptions :column="1" border>
        <ElDescriptionsItem :label="$t('orbien.tls.acme.order.detail.orderNo')">{{ detail?.orderNo }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.tls.acme.order.detail.status')">
          <ElTag size="small" :type="resolveAcmeOrderStatusTagType(detail?.status)">{{
              detail?.statusLabel
            }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.tls.acme.order.detail.domains')">{{ detail?.domains?.join(', ') }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.tls.acme.order.detail.validationMode')">
          {{ detail?.validationMode === 2 ? $t('orbien.tls.acme.order.detail.cloudAuto') : $t('orbien.tls.acme.order.detail.manualDns') }}
        </ElDescriptionsItem>
        <ElDescriptionsItem v-if="detail?.validationMode === 2" :label="$t('orbien.tls.acme.order.detail.renewal')">
          {{ $t('orbien.tls.acme.order.detail.autoRenewalSupported') }}
        </ElDescriptionsItem>
        <ElDescriptionsItem v-if="detail?.certId" :label="$t('orbien.tls.acme.order.detail.certId')">{{
            detail.certId
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem v-if="detail?.errorMessage" :label="$t('orbien.tls.acme.order.detail.errorMessage')">
          <span class="text-danger">{{ detail.errorMessage }}</span>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.tls.acme.order.detail.createdAt')">{{ detail?.createdAt }}</ElDescriptionsItem>
      </ElDescriptions>

      <div v-if="detail?.challenges?.length" class="challenge-section">
        <div class="section-title">{{ $t('orbien.tls.acme.order.detail.dnsTxtRecords') }}</div>
        <div v-for="item in detail.challenges" :key="item.id" class="challenge-card">
          <div class="challenge-domain">{{ item.domain }}</div>
          <div v-if="item.dnsZone" class="zone-tip">{{ $t('orbien.tls.acme.dnsVerify.dnsZone', { zone: item.dnsZone }) }}</div>
          <div class="challenge-row">
            <span class="label">{{ $t('orbien.tls.acme.dnsVerify.hostRecord') }}</span>
            <ElInput :model-value="item.hostRecord || item.recordName" readonly>
              <template #append>
                <ElButton @click="copyText(item.hostRecord || item.recordName)">{{ $t('common.copy') }}</ElButton>
              </template>
            </ElInput>
          </div>
          <div class="challenge-row">
            <span class="label">{{ $t('orbien.tls.acme.dnsVerify.recordValue') }}</span>
            <ElInput :model-value="item.recordValue" readonly>
              <template #append>
                <ElButton @click="copyText(item.recordValue)">{{ $t('common.copy') }}</ElButton>
              </template>
            </ElInput>
          </div>
          <ElTag size="small" :type="resolveAcmeChallengeStatusTagType(item.status)">
            {{ item.statusLabel || $t('orbien.tls.acme.order.detail.pendingVerify') }}
          </ElTag>
        </div>
      </div>

      <div v-if="showActions" class="drawer-actions">
        <ElButton v-if="canVerify" type="primary" :loading="actionLoading" @click="handleVerify">
          {{ $t('orbien.tls.acme.wizard.startVerify') }}
        </ElButton>
        <ElButton v-if="canRetry" :loading="actionLoading" @click="handleRetry">{{ $t('orbien.tls.acme.order.actions.retry') }}</ElButton>
        <ElButton v-if="canCancel" :loading="actionLoading" @click="handleCancel"
        >{{ $t('orbien.tls.acme.order.detail.cancelApplication') }}
        </ElButton
        >
      </div>
    </div>
  </ElDrawer>
</template>

<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage} from 'element-plus'
import {
  fetchAcmeOrderDetail,
  fetchCancelAcmeOrder,
  fetchRetryAcmeOrder,
  fetchVerifyAcmeOrder
} from '@/api/acme-order'
import {resolveAcmeChallengeStatusTagType, resolveAcmeOrderStatusTagType} from '@/utils/ui/status-tag'

defineOptions({name: 'AcmeOrderDetailDrawer'})

const {t} = useI18n()

interface Props {
  visible: boolean
  orderId?: number | null
}

interface Emits {
  (e: 'update:visible', value: boolean): void

  (e: 'changed'): void
}

const props = withDefaults(defineProps<Props>(), {orderId: null})
const emit = defineEmits<Emits>()

const drawerVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const loading = ref(false)
const actionLoading = ref(false)
const detail = ref<Api.AcmeOrder.OrderDTO | null>(null)

const terminalStatuses = [5, 6, 7]
const canVerify = computed(
    () => detail.value && [1, 2].includes(detail.value.status) && detail.value.validationMode === 1
)
const canRetry = computed(() => detail.value?.status === 6)
const canCancel = computed(() => detail.value && !terminalStatuses.includes(detail.value.status))
const showActions = computed(() => canVerify.value || canRetry.value || canCancel.value)

const loadDetail = async () => {
  if (!props.orderId) return
  loading.value = true
  try {
    detail.value = await fetchAcmeOrderDetail(props.orderId)
  } finally {
    loading.value = false
  }
}

watch(
    () => [props.visible, props.orderId],
    ([visible]) => {
      if (visible) loadDetail()
    }
)

const copyText = async (text: string) => {
  await navigator.clipboard.writeText(text)
  ElMessage.success(t('common.copied'))
}

const handleVerify = async () => {
  if (!props.orderId) return
  actionLoading.value = true
  try {
    await fetchVerifyAcmeOrder(props.orderId)
    ElMessage.success(t('orbien.tls.acme.order.detail.verifyStartedRefresh'))
    emit('changed')
    await loadDetail()
  } finally {
    actionLoading.value = false
  }
}

const handleRetry = async () => {
  if (!props.orderId) return
  actionLoading.value = true
  try {
    await fetchRetryAcmeOrder(props.orderId)
    ElMessage.success(t('orbien.tls.acme.order.detail.retrySubmitted'))
    emit('changed')
    await loadDetail()
  } finally {
    actionLoading.value = false
  }
}

const handleCancel = async () => {
  if (!props.orderId) return
  actionLoading.value = true
  try {
    await fetchCancelAcmeOrder(props.orderId)
    ElMessage.success(t('orbien.tls.acme.order.messages.cancelled'))
    emit('changed')
    await loadDetail()
  } finally {
    actionLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.challenge-section {
  margin-top: 20px;
}

.section-title {
  margin-bottom: 12px;
  font-weight: 600;
}

.challenge-card {
  padding: 12px;
  margin-bottom: 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.challenge-domain {
  margin-bottom: 8px;
  font-weight: 500;
}

.zone-tip {
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.challenge-row {
  margin-bottom: 8px;

  .label {
    display: block;
    margin-bottom: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.drawer-actions {
  display: flex;
  gap: 8px;
  margin-top: 20px;
}

.text-danger {
  color: var(--el-color-danger);
}
</style>
