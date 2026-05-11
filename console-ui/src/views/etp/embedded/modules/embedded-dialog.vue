<template>
  <ElDialog
    :model-value="modelValue"
    title="隧道详情"
    width="900px"
    :close-on-click-modal="false"
    @update:model-value="handleClose"
  >
    <div v-if="loading" class="loading-tip">
      <ElLoading :text="'加载中...'" />
    </div>
    <div v-else-if="tunnelData" class="detail-content">
      <!-- 代理基本信息 -->
      <div class="section">
        <div class="section-title">
          <el-icon class="title-icon"><Monitor /></el-icon>
          基本信息
        </div>
        <ElDescriptions :column="3" border :size="'default'">
          <ElDescriptionsItem label="代理ID">{{ tunnelData.proxy.proxyId }}</ElDescriptionsItem>
          <ElDescriptionsItem label="代理名称">{{ tunnelData.proxy.name }}</ElDescriptionsItem>
          <ElDescriptionsItem label="代理协议">
            <ElTag :type="isHttpProtocol ? 'warning' : 'primary'">
              {{ getProtocolText(tunnelData.proxy.protocol) }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="代理状态">
            <ElTag :type="tunnelData.proxy.status === 1 ? 'success' : 'info'">
              {{ tunnelData.proxy.status === 1 ? '开启' : '关闭' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="部署模式">
            <ElTag :type="tunnelData.proxy.deploymentMode === 2 ? 'success' : 'info'">
              {{ tunnelData.proxy.deploymentMode === 2 ? '集群' : '单机' }}
            </ElTag>
          </ElDescriptionsItem>
          <!-- TCP 协议显示监听端口 -->
          <template v-if="isTcpProtocol">
            <ElDescriptionsItem label="远程端口">
              <ElTag type="primary">
                {{ tcpProxy.listenPort }}
              </ElTag>
            </ElDescriptionsItem>
          </template>
          <ElDescriptionsItem v-if="isHttpProtocol" label="远程地址" :span="3">
            <ElSpace wrap>
              <ElTag
                v-for="domain in httpProxy.domains"
                :key="domain"
                type="warning"
                class="domain-tag"
                @click="openDomain(domain)"
              >
                {{ domain }}
              </ElTag>
            </ElSpace>
          </ElDescriptionsItem>
        </ElDescriptions>
      </div>

      <!-- 内网服务 -->
      <div class="section">
        <div class="section-title">
          <el-icon class="title-icon"><OfficeBuilding /></el-icon>
          内网服务
        </div>
        <ElTable
          :data="tunnelData.proxy.targets"
          :size="'default'"
          border
          style="width: 100%; table-layout: fixed"
        >
          <ElTableColumn prop="name" label="服务名称">
            <template #default="{ row }">
              {{ row.name || '-' }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="host" label="主机"></ElTableColumn>
          <ElTableColumn prop="port" label="端口"></ElTableColumn>
          <ElTableColumn prop="weight" label="权重">
            <template #default="{ row }">
              {{ row.weight || '-' }}
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <!-- 带宽配置 -->
      <div class="section">
        <div class="section-title">
          <el-icon class="title-icon"><TrendCharts /></el-icon>
          带宽配置
        </div>
        <ElDescriptions :column="3" border :size="'default'">
          <ElDescriptionsItem label="入站带宽">
            {{ tunnelData.proxy.bandwidth.limitIn || '无' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="出站带宽">
            {{ tunnelData.proxy.bandwidth.limitOut || '无' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="总带宽">
            {{ tunnelData.proxy.bandwidth.limitTotal || '无' }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </div>

      <!-- 传输配置 -->
      <div class="section">
        <div class="section-title">
          <el-icon class="title-icon"><Operation /></el-icon>
          传输配置
        </div>
        <ElDescriptions :column="2" border :size="'default'">
          <ElDescriptionsItem label="TLS加密">
            <ElTag :type="tunnelData.proxy.transport.encrypt ? 'success' : 'info'">
              {{ tunnelData.proxy.transport.encrypt ? '开启' : '关闭' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="隧道类型">
            <ElTag :type="tunnelData.proxy.transport.tunnelType === 1 ? 'info' : 'success'">
              {{ tunnelData.proxy.transport.tunnelType === 1 ? '独立隧道' : '共享隧道' }}
            </ElTag>
          </ElDescriptionsItem>
        </ElDescriptions>
      </div>

      <!-- 系统信息 -->
      <div class="section">
        <div class="section-title">
          <el-icon class="title-icon"><Connection /></el-icon>
          系统信息
        </div>
        <ElDescriptions :column="3" border :size="'default'">
          <ElDescriptionsItem label="会话标识">{{ tunnelData.agent.id }}</ElDescriptionsItem>
          <ElDescriptionsItem label="系统名称">{{ tunnelData.agent.name }}</ElDescriptionsItem>
          <ElDescriptionsItem label="操作系统">{{ tunnelData.agent.os }}</ElDescriptionsItem>
          <ElDescriptionsItem label="系统架构">{{ tunnelData.agent.arch }}</ElDescriptionsItem>
          <ElDescriptionsItem label="版本号">{{ tunnelData.agent.version }}</ElDescriptionsItem>
          <ElDescriptionsItem label="最后活跃时间" :span="3">
            {{ tunnelData.agent.lastActiveTime }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </div>
    </div>
    <div v-else class="empty-tip">暂无数据</div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import {
    ElDialog,
    ElDescriptions,
    ElDescriptionsItem,
    ElTag,
    ElTable,
    ElTableColumn,
    ElSpace,
    ElLoading,
    ElMessage
  } from 'element-plus'
  import {
    Monitor,
    Connection,
    OfficeBuilding,
    Operation,
    TrendCharts
  } from '@element-plus/icons-vue'
  import { fetchGetEmbeddedDetail } from '@/api/embedded'

  defineOptions({ name: 'EmbeddedDialog' })

  const props = defineProps<{
    modelValue: boolean
    proxyId?: string
  }>()

  const emit = defineEmits<{
    'update:modelValue': [value: boolean]
  }>()

  const loading = ref(false)
  const tunnelData = ref<Api.Embedded.TunnelDetailDTO | null>(null)

  const PROTOCOL = {
    TCP: 1,
    HTTP: 2
  }

  const isHttpProtocol = computed(() => tunnelData.value?.proxy.protocol === PROTOCOL.HTTP)
  const isTcpProtocol = computed(() => tunnelData.value?.proxy.protocol === PROTOCOL.TCP)

  const httpProxy = computed(() => tunnelData.value?.proxy as Api.Embedded.HttpProxyDTO)
  const tcpProxy = computed(() => tunnelData.value?.proxy as Api.Embedded.TcpProxyDTO)

  const getProtocolText = (protocol: number) => {
    return protocol === PROTOCOL.HTTP ? 'HTTP' : 'TCP'
  }

  const openDomain = (domain: string) => {
    let url = `http://${domain}`
    if (tunnelData.value?.httpProxyPort && tunnelData.value.httpProxyPort !== 80) {
      url += `:${tunnelData.value.httpProxyPort}`
    }
    window.open(url, '_blank')
  }

  const fetchData = async () => {
    if (!props.proxyId) {
      tunnelData.value = null
      return
    }

    loading.value = true
    try {
      const result = await fetchGetEmbeddedDetail(props.proxyId)
      tunnelData.value = result
    } catch (error) {
      console.error('获取隧道详情失败:', error)
      tunnelData.value = null
      ElMessage.error('获取隧道详情失败')
    } finally {
      loading.value = false
    }
  }

  const handleClose = (value: boolean) => {
    if (!value) {
      tunnelData.value = null
    }
    emit('update:modelValue', value)
  }

  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) {
        fetchData()
      }
    }
  )
</script>

<style lang="scss" scoped>
  .detail-content {
    padding: 0;
  }

  .section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .section-title {
    display: flex;
    align-items: center;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 4px solid #409eff;

    .title-icon {
      margin-right: 8px;
      color: #409eff;
    }
  }

  .domain-tag {
    cursor: pointer;
  }

  .empty-tip {
    text-align: center;
    color: #999;
    padding: 40px 0;
  }

  .loading-tip {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 40px 0;
  }
</style>
