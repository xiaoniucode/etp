<template>
  <ElDialog v-model="dialogVisible" title="客户端详情" width="60%" align-center>
    <div v-if="clientData" class="client-detail">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem label="客户端名称">{{ clientData.name }}</ElDescriptionsItem>
        <ElDescriptionsItem label="访问令牌">{{ clientData.token }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="getStatusType(clientData.isOnline)">
            {{ getStatusText(clientData.isOnline) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="类型">{{
          clientData.agentType === 1 ? 'BINARY' : 'SESSION'
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="操作系统">{{ clientData.os }}</ElDescriptionsItem>
        <ElDescriptionsItem label="架构">{{ clientData.arch }}</ElDescriptionsItem>
        <ElDescriptionsItem label="版本">{{ clientData.version }}</ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">{{
          formatDate(clientData.createdAt)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="更新时间">{{
          formatDate(clientData.updatedAt)
        }}</ElDescriptionsItem>
      </ElDescriptions>
    </div>
    <div v-else class="loading-state">
      <ElSkeleton :rows="10" animated />
    </div>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">关闭</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, watch, nextTick } from 'vue'
  import { ElTag, ElDescriptions, ElSkeleton, ElButton } from 'element-plus'

  interface ClientData {
    id: string
    token: string
    isOnline: boolean
    name: string
    os: string
    arch: string
    version: string
    agentType: number
    createdAt: string
    updatedAt: string
  }

  interface Props {
    visible: boolean
    clientData?: ClientData
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  // 客户端数据
  const clientData = computed(() => props.clientData)

  /**
   * 获取状态类型
   */
  const getStatusType = (isOnline: boolean) => {
    return isOnline ? 'success' : 'info'
  }

  /**
   * 获取状态文本
   */
  const getStatusText = (isOnline: boolean) => {
    return isOnline ? '在线' : '离线'
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
          // 可以在这里添加初始化逻辑
        })
      }
    }
  )
</script>

<style scoped>
  .client-detail {
    margin-top: 20px;
  }

  .loading-state {
    margin-top: 20px;
  }
</style>
