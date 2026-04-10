<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增 HTTP 代理' : '编辑 HTTP 代理'"
    width="900px"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="140px">
      <ElFormItem label="客户端" prop="agentId">
        <ElSelect
          v-model="formData.agentId"
          placeholder="请选择客户端"
          :disabled="dialogType === 'edit'"
          style="width: 200px"
        >
          <ElOption v-for="agent in agents" :key="agent.id" :label="agent.name" :value="agent.id" />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="代理名称" prop="name">
        <ElInput v-model="formData.name" placeholder="请输入代理名称" />
      </ElFormItem>

      <ElFormItem label="域名类型" prop="domainType">
        <ElRadioGroup v-model="formData.domainType">
          <ElRadio label="0">自动</ElRadio>
          <ElRadio label="1">子域名</ElRadio>
          <ElRadio label="2">自定义域名</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem v-if="formData.domainType !== '0'" prop="domains">
        <ElInput
          v-model="formData.domains"
          type="textarea"
          :rows="3"
          placeholder="请输入域名，多个域名用换行分隔"
        />
      </ElFormItem>
      <ElFormItem label="隧道类型" prop="tunnelType">
        <ElRadioGroup v-model="formData.tunnelType">
          <ElRadio label="1">多路复用</ElRadio>
          <ElRadio label="0">独立隧道</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem label="TLS加密" prop="encrypt">
        <ElSwitch v-model="formData.encrypt" />
      </ElFormItem>

      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio label="1">开启</ElRadio>
          <ElRadio label="0">关闭</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElRow :gutter="20">
        <ElCol :span="8">
          <ElFormItem label="总带宽限制" prop="limitTotal">
            <ElInput v-model="formData.limitTotal" placeholder="如 5M" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="入站带宽限制" prop="limitIn">
            <ElInput v-model="formData.limitIn" placeholder="如 5M" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="出站带宽限制" prop="limitOut">
            <ElInput v-model="formData.limitOut" placeholder="如 5M" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="部署模式" prop="deployMode">
        <ElRadioGroup v-model="formData.deployMode">
          <ElRadio label="single">单节点</ElRadio>
          <ElRadio label="cluster">集群</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <!-- 单节点模式 -->
      <ElFormItem v-if="formData.deployMode === 'single'" label="服务地址" :required="true">
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElInput v-model="formData.singleHost" placeholder="请输入主机" />
          </ElCol>
          <ElCol :span="12">
            <ElInput v-model.number="formData.singlePort" type="number" placeholder="请输入端口" />
          </ElCol>
        </ElRow>
      </ElFormItem>

      <!-- 集群模式 -->
      <ElFormItem
        v-if="formData.deployMode === 'cluster'"
        label="负载均衡策略"
        prop="loadBalanceStrategy"
        :required="formData.deployMode === 'cluster'"
      >
        <ElSelect
          v-model="formData.loadBalanceStrategy"
          placeholder="请选择负载均衡策略"
          style="width: 220px"
        >
          <ElOption label="轮询 (roundrobin)" value="1" />
          <ElOption label="权重 (weight)" value="2" />
          <ElOption label="随机 (random)" value="3" />
          <ElOption label="最少连接 (leastconn)" value="4" />
        </ElSelect>
      </ElFormItem>

      <ElFormItem v-if="formData.deployMode === 'cluster'" label="服务列表" prop="targets">
        <div class="targets-table">
          <ElTable :data="formData.targets" style="width: 100%">
            <ElTableColumn v-if="dialogType === 'edit'" prop="id" label="ID" width="100">
              <template #default="scope">
                <ElInput v-model="scope.row.id" disabled style="width: 100%" />
              </template>
            </ElTableColumn>
            <ElTableColumn prop="name" label="服务名称" min-width="150">
              <template #default="scope">
                <ElInput
                  v-model="scope.row.name"
                  placeholder="请输入服务名称"
                  style="width: 100%"
                />
              </template>
            </ElTableColumn>
            <ElTableColumn prop="host" label="主机" min-width="180">
              <template #default="scope">
                <ElInput v-model="scope.row.host" placeholder="请输入主机" style="width: 100%" />
              </template>
            </ElTableColumn>
            <ElTableColumn prop="port" label="端口" width="100">
              <template #default="scope">
                <ElInput
                  v-model.number="scope.row.port"
                  type="number"
                  placeholder="请输入端口"
                  style="width: 100%"
                />
              </template>
            </ElTableColumn>
            <ElTableColumn prop="weight" label="权重" width="100">
              <template #default="scope">
                <ElInput
                  v-model.number="scope.row.weight"
                  type="number"
                  placeholder="请输入权重"
                  style="width: 100%"
                />
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="80" align="center">
              <template #default="scope">
                <ElButton type="danger" size="small" @click="removeTarget(scope.$index)"
                  >删除</ElButton
                >
              </template>
            </ElTableColumn>
          </ElTable>
          <ElButton type="primary" size="small" @click="addTarget" style="margin-top: 10px"
            >添加服务</ElButton
          >
        </div>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, computed, reactive, watch, nextTick } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { fetchGetAgentListAll } from '@/api/agent'
  import {
    fetchCreateHttpProxy,
    fetchUpdateHttpProxy,
    fetchGetHttpProxyById
  } from '@/api/http-proxy'

  interface Props {
    visible: boolean
    type: string
    proxyData?: Partial<Api.HttpProxy.HttpProxyDTO>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  const formRef = ref<FormInstance>()

  interface Target {
    id?: string
    host: string
    port: number
    weight: number
    name: string
  }

  const formData = reactive({
    agentId: '',
    name: '',
    status: '1',
    domainType: '0',
    domains: '',
    encrypt: false,
    tunnelType: '1',
    deployMode: 'single',
    singleHost: '127.0.0.1',
    singlePort: 80,
    targets: [] as Target[],
    limitTotal: '',
    limitIn: '',
    limitOut: '',
    loadBalanceStrategy: '1'
  })

  const agents = ref([])

  const rules: FormRules = {
    agentId: [{ required: true, message: '请选择客户端', trigger: 'change' }],
    name: [{ required: true, message: '请输入代理名称', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
    domainType: [{ required: true, message: '请选择域名类型', trigger: 'change' }],
    domains: [{ required: formData.domainType !== '0', message: '请输入域名', trigger: 'blur' }],
    encrypt: [{ required: true, message: '请选择是否开启TLS加密', trigger: 'change' }],
    tunnelType: [{ required: true, message: '请选择隧道类型', trigger: 'change' }],
    deployMode: [{ required: true, message: '请选择部署模式', trigger: 'change' }],
    singleHost: [
      { required: true, message: '请输入主机', trigger: 'blur' },
      { required: formData.deployMode === 'single', message: '请输入主机', trigger: 'blur' }
    ],
    singlePort: [
      { required: true, message: '请输入端口', trigger: 'blur' },
      { required: formData.deployMode === 'single', message: '请输入端口', trigger: 'blur' },
      { type: 'number', message: '端口必须是数字', trigger: 'blur' },
      { min: 1, max: 65535, message: '端口必须在 1-65535 之间', trigger: 'blur' }
    ],
    targets: [
      {
        validator: (rule: any, value: Target[], callback: any) => {
          if (formData.deployMode === 'cluster' && value.length === 0) {
            callback(new Error('请至少添加一个服务'))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ],
    loadBalanceStrategy: [
      {
        validator: (rule: any, value: string, callback: any) => {
          if (formData.deployMode === 'cluster' && formData.targets.length >= 2 && !value) {
            callback(new Error('服务列表数量在2个及以上时，必须选择负载均衡策略'))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ]
  }

  const fetchAgents = async () => {
    try {
      // 调用后端接口获取客户端列表
      const agentsList = await fetchGetAgentListAll()
      agents.value = agentsList || []
    } catch (error) {
      console.error('获取客户端列表失败:', error)
      ElMessage.error('获取客户端列表失败')
    }
  }

  const addTarget = () => {
    formData.targets.push({ host: '127.0.0.1', port: 80, weight: 1, name: '' })
  }

  const removeTarget = (index: number) => {
    formData.targets.splice(index, 1)
  }

  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.proxyData && props.proxyData.id

    if (isEdit) {
      try {
        // 从服务端获取真实的代理详情数据
        const proxyDetail = await fetchGetHttpProxyById(props.proxyData!.id!)

        Object.assign(formData, {
          agentId: proxyDetail.agentId || '',
          name: proxyDetail.name || '',
          status: proxyDetail.status?.toString() || '1',
          domainType: proxyDetail.domainType?.toString() || '0',
          domains: proxyDetail.domain ? proxyDetail.domain.join('\n') : '',
          encrypt: proxyDetail.transport?.encrypt || false,
          tunnelType: proxyDetail.tunnelType?.toString() || '1',
          deployMode: proxyDetail.deploymentMode === 1 ? 'single' : 'cluster',
          singleHost:
            proxyDetail.targets && proxyDetail.targets.length > 0
              ? proxyDetail.targets[0].host || '127.0.0.1'
              : '127.0.0.1',
          singlePort:
            proxyDetail.targets && proxyDetail.targets.length > 0
              ? proxyDetail.targets[0].port || 80
              : 80,
          targets: proxyDetail.targets
            ? proxyDetail.targets.map((t: any) => ({
                id: t.id || '',
                host: t.host || '',
                port: t.port || 80,
                weight: t.weight || 1,
                name: t.name || ''
              }))
            : [],
          limitTotal: proxyDetail.bandwidth
            ? proxyDetail.bandwidth.uploadLimit?.toString() || ''
            : '',
          limitIn: proxyDetail.bandwidth
            ? proxyDetail.bandwidth.downloadLimit?.toString() || ''
            : '',
          limitOut: '',
          loadBalanceStrategy: proxyDetail.loadBalance?.strategy?.toString() || '1'
        })
      } catch (error) {
        console.error('获取代理详情失败:', error)
        ElMessage.error('获取代理详情失败，请稍后重试')

        // 失败时使用props.proxyData作为 fallback
        const row = props.proxyData
        Object.assign(formData, {
          agentId: row ? row.agentId || '' : '',
          name: row ? row.name || '' : '',
          status: row ? row.status?.toString() || '1' : '1',
          domainType: row ? row.domainType?.toString() || '0' : '0',
          domains: row && row.domains ? row.domains.join('\n') : '',
          encrypt: row ? row.encrypt || false : false,
          tunnelType: row ? row.tunnelType?.toString() || '1' : '1',
          deployMode: 'single',
          singleHost: '127.0.0.1',
          singlePort: 80,
          targets:
            row && row.targets
              ? row.targets.map((t: any) => ({
                  id: t.id || '',
                  host: t.host || '',
                  port: t.port || 80,
                  weight: t.weight || 1,
                  name: t.name || ''
                }))
              : [],
          limitTotal: row && row.bandwidth ? row.bandwidth.uploadLimit?.toString() || '' : '',
          limitIn: row && row.bandwidth ? row.bandwidth.downloadLimit?.toString() || '' : '',
          limitOut: '',
          loadBalanceStrategy: '1'
        })
      }
    } else {
      // 新增模式，使用默认值
      Object.assign(formData, {
        agentId: '',
        name: '',
        status: '1',
        domainType: '0',
        domains: '',
        encrypt: false,
        tunnelType: '1',
        deployMode: 'single',
        singleHost: '127.0.0.1',
        singlePort: 80,
        targets: [],
        limitTotal: '',
        limitIn: '',
        limitOut: '',
        loadBalanceStrategy: '1'
      })
    }

    // 如果没有目标地址，添加一个默认的
    if (formData.targets.length === 0) {
      addTarget()
    }
  }

  watch(
    () => [props.visible, props.type, props.proxyData],
    async ([visible]) => {
      if (visible) {
        await fetchAgents()
        await initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  // 监听服务列表数量变化，触发负载均衡策略验证
  watch(
    () => formData.targets.length,
    () => {
      formRef.value?.validateField('loadBalanceStrategy')
    }
  )

  // 监听部署模式变化，触发相关字段验证
  watch(
    () => formData.deployMode,
    () => {
      formRef.value?.clearValidate()
      formRef.value?.validateField('singleHost')
      formRef.value?.validateField('singlePort')
      formRef.value?.validateField('targets')
      formRef.value?.validateField('loadBalanceStrategy')
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 处理域名，将换行分隔的域名转换为数组
          const domainsArray = formData.domains.split('\n').filter((domain) => domain.trim())

          // 根据部署模式生成targets数据
          let targets = []
          if (formData.deployMode === 'single') {
            // 单节点模式，生成一个目标服务
            targets = [
              {
                name: formData.name, // 服务名称与代理名称保持一致
                host: formData.singleHost,
                port: formData.singlePort,
                weight: 1 // 权重默认为1
              }
            ]
          } else {
            // 集群模式，使用现有targets数据
            targets = formData.targets
          }

          if (dialogType.value === 'add') {
            // 构建创建请求数据
            const requestData = {
              agentId: formData.agentId,
              name: formData.name,
              status: parseInt(formData.status),
              domainType: parseInt(formData.domainType),
              domains: domainsArray,
              deploymentMode: formData.deployMode === 'single' ? 1 : 0, // 1: STANDALONE, 0: CLUSTER
              targets: targets,
              bandwidth: {
                limitTotal: formData.limitTotal,
                limitIn: formData.limitIn,
                limitOut: formData.limitOut
              },
              loadBalance:
                formData.deployMode === 'cluster'
                  ? {
                      strategy: parseInt(formData.loadBalanceStrategy)
                    }
                  : undefined,
              transport: {
                tunnelType: parseInt(formData.tunnelType),
                encrypt: formData.encrypt
              }
            }

            console.log('创建HTTP代理数据:', requestData)
            await fetchCreateHttpProxy(requestData)
          } else {
            // 构建更新请求数据
            const requestData = {
              id: props.proxyData?.id,
              name: formData.name,
              status: parseInt(formData.status),
              domainType: parseInt(formData.domainType),
              domains: domainsArray,
              encrypt: formData.encrypt,
              targets: targets,
              tunnelType: parseInt(formData.tunnelType)
            }

            console.log('更新HTTP代理数据:', requestData)
            await fetchUpdateHttpProxy(requestData)
          }

          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('提交失败:', error)
        }
      }
    })
  }
</script>

<style scoped>
  .targets-table {
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 10px;
  }
</style>
