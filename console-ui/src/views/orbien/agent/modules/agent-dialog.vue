<template>
  <ElDialog v-model="dialogVisible" :title="$t('orbien.agent.detailTitle')" width="60%" align-center>
    <div v-if="localClientData" class="mt-5">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem :label="$t('orbien.common.clientId')">{{ localClientData.id }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.clientName')">{{ localClientData.name }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.accessToken')">{{ localClientData.token }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.status')">
          <ElTag :type="getStatusType(localClientData.isOnline)">
            {{ getStatusText(localClientData.isOnline) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.type')">
          <ElTag :type="getAgentTypeTag(localClientData.agentType).type">
            {{ getAgentTypeTag(localClientData.agentType).text }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.os')">{{ localClientData.os }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.arch')">{{ localClientData.arch }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.version')">{{ localClientData.version }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.sourceIp')">{{ localClientData.sourceIp || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('orbien.common.lastActiveTime')">{{
            formatDate(localClientData.lastActiveTime)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.createTime')">{{
            formatDate(localClientData.createdAt)
          }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.updateTime')">{{
            formatDate(localClientData.updatedAt)
          }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </div>
    <div v-else class="mt-5">
      <ElSkeleton :rows="10" animated/>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">{{ $t('common.close') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {ref, computed, watch, nextTick} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElTag, ElDescriptions, ElSkeleton, ElButton, ElMessage} from 'element-plus'
import {fetchGetAgentById} from '@/api/agent'
import {getAgentTypeTag} from '@/enums/orbien/business'

interface Props {
  visible: boolean
  clientData?: Api.Agent.AgentDTO
}

interface Emits {
  (e: 'update:visible', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { t } = useI18n()

// 对话框显示控制
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 客户端数据
const localClientData = ref<Api.Agent.AgentDTO | null>(null)
const loading = ref(false)

/**
 * 获取客户端详情
 */
const fetchClientDetail = async () => {
  if (!props.clientData?.id) return

  loading.value = true
  try {
    const data = await fetchGetAgentById(props.clientData.id)
    localClientData.value = data
  } catch (error) {
    console.error('获取客户端详情失败:', error)
    ElMessage.error(t('orbien.agent.fetchDetailFailed'))
  } finally {
    loading.value = false
  }
}

/**
 * 获取状态类型
 */
const getStatusType = (isOnline: boolean) => {
  return isOnline ? 'primary' : 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = (isOnline: boolean) => {
  return isOnline ? t('common.online') : t('common.offline')
}

/**
 * 格式化日期
 */
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

/**
 * 监听对话框状态变化
 */
watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        nextTick(() => {
          fetchClientDetail()
        })
      }
    }
)

/**
 * 监听客户端数据变化
 */
watch(
    () => props.clientData,
    () => {
      if (props.visible) {
        fetchClientDetail()
      }
    }
)
</script>
