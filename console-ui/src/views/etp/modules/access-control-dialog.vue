<template>
  <ElDialog
    v-model="dialogVisible"
    title="IP访问控制"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <div class="access-control-dialog">
      <!-- 基本配置 -->
      <div class="mb-6">
        <h3 class="text-lg font-semibold mb-4">基本配置</h3>
        <div class="flex flex-col gap-4">
          <div class="flex items-center gap-3">
            <span class="w-20 font-medium">启用状态：</span>
            <ElSwitch v-model="formData.enabled" @change="handleEnableChange" />
          </div>
          <div class="flex items-center gap-3">
            <span class="w-20 font-medium">控制模式：</span>
            <ElRadioGroup v-model="formData.mode" @change="handleModeChange">
              <ElRadio :label="1">白名单（只允许指定IP访问）</ElRadio>
              <ElRadio :label="0">黑名单（禁止指定IP访问）</ElRadio>
            </ElRadioGroup>
          </div>
        </div>
      </div>

      <!-- 规则列表 -->
      <div>
        <h3 class="text-lg font-semibold mb-4">访问规则</h3>
        <div class="border border-gray-200 rounded p-4">
          <ElTable :data="formData.rules" style="width: 100%" border>
            <ElTableColumn prop="cidr" label="IP地址段 (例如：192.168.1.0/24)" width="300">
              <template #default="scope">
                <ElInput
                  v-if="editingRuleId === scope.row.id"
                  v-model="scope.row.cidr"
                  placeholder="请输入IP地址段，例如：192.168.1.0/24"
                  style="width: 100%"
                />
                <span v-else>{{ scope.row.cidr }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="ruleType" label="规则类型" width="150">
              <template #default="scope">
                <ElRadioGroup v-if="editingRuleId === scope.row.id" v-model="scope.row.ruleType">
                  <ElRadio :label="1">放行</ElRadio>
                  <ElRadio :label="0">禁止</ElRadio>
                </ElRadioGroup>
                <ElTag v-else :type="scope.row.ruleType === 1 ? 'success' : 'danger'">
                  {{ scope.row.ruleType === 1 ? '放行' : '禁止' }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="240" fixed="right">
              <template #default="scope">
                <ElSpace size="small">
                  <ElButton
                    v-if="editingRuleId === scope.row.id"
                    type="primary"
                    size="small"
                    @click="handleSaveRule(scope.row)"
                  >
                    保存
                  </ElButton>
                  <ElButton v-else type="primary" size="small" @click="handleEditRule(scope.row)">
                    <template #icon>
                      <Edit />
                    </template>
                    编辑
                  </ElButton>
                  <ElButton type="danger" size="small" @click="handleDeleteRule(scope.row.id)">
                    <template #icon>
                      <Delete />
                    </template>
                    删除
                  </ElButton>
                </ElSpace>
              </template>
            </ElTableColumn>
          </ElTable>
          <ElButton type="primary" size="small" @click="addRule" class="mt-3">
            <template #icon>
              <Plus />
            </template>
            新增规则
          </ElButton>
        </div>
      </div>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Plus, Edit, Delete } from '@element-plus/icons-vue'
  import {
    fetchGetAccessControl,
    fetchUpdateAccessControl,
    fetchAddAccessControlRule,
    fetchUpdateAccessControlRule,
    fetchDeleteAccessControlRule
  } from '@/api/access-control'

  defineOptions({ name: 'AccessControlDialog' })

  // Props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false
    },
    proxyId: {
      type: String,
      required: true
    }
  })

  const emit = defineEmits(['update:visible', 'close'])

  const formData = reactive({
    enabled: false,
    mode: 1,
    rules: [] as Array<{
      id: number
      proxyId: string
      cidr: string
      ruleType: number
    }>
  })

  const dialogVisible = ref(false)

  const editingRuleId = ref<number | null>(null)

  // 临时存储编辑前的规则数据，用于取消编辑
  const editingRuleBackup = ref<any>(null)

  // 监听 props.visible 变化，同步到本地变量并获取数据
  watch(
    () => props.visible,
    async (newVal) => {
      dialogVisible.value = newVal
      if (newVal && props.proxyId) {
        resetFormData()
        await fetchAccessControlData()
      }
    }
  )

  // 监听本地 dialogVisible 变化，通知父组件
  watch(dialogVisible, (newVal) => {
    emit('update:visible', newVal)
  })

  const resetFormData = () => {
    formData.enabled = false
    formData.mode = 1
    formData.rules = []
    editingRuleId.value = null
    editingRuleBackup.value = null
  }

  // 组件挂载时获取数据
  onMounted(async () => {
    if (props.visible && props.proxyId) {
      resetFormData()
      await fetchAccessControlData()
    }
  })

  // 获取访问控制详情
  const fetchAccessControlData = async () => {
    const response = await fetchGetAccessControl(props.proxyId)
    if (response) {
      formData.enabled = response.enabled || false
      formData.mode = response.mode !== undefined ? response.mode : 1
      formData.rules = response.rules || []
    }
  }

  // 处理启用状态变化
  const handleEnableChange = async () => {
    await updateAccessControlConfig()
  }

  // 处理控制模式变化
  const handleModeChange = async () => {
    await updateAccessControlConfig()
  }

  // 更新访问控制配置
  const updateAccessControlConfig = async () => {
    await fetchUpdateAccessControl({
      proxyId: props.proxyId,
      enabled: formData.enabled,
      mode: formData.mode
    })
  }

  // 添加规则
  const addRule = () => {
    formData.rules.push({
      id: 0, // 临时ID，后端会生成真实ID
      proxyId: props.proxyId,
      cidr: '',
      ruleType: 1
    })
    // 自动进入编辑状态
    const newRule = formData.rules[formData.rules.length - 1]
    handleEditRule(newRule)
  }

  // 开始编辑规则
  const handleEditRule = (rule: any) => {
    // 存储编辑前的数据，用于取消编辑
    editingRuleBackup.value = { ...rule }
    // 设置编辑状态
    editingRuleId.value = rule.id
  }

  const handleSaveRule = async (rule: any) => {
    if (!rule.cidr) {
      ElMessage.error('请输入IP地址段')
      return
    }

    if (rule.id > 0) {
      await fetchUpdateAccessControlRule({
        id: rule.id,
        cidr: rule.cidr,
        ruleType: rule.ruleType
      })
    } else {
      await fetchAddAccessControlRule({
        proxyId: props.proxyId,
        cidr: rule.cidr,
        ruleType: rule.ruleType
      })
    }
    editingRuleId.value = null
    await fetchAccessControlData()
    editingRuleBackup.value = null
  }

  // 处理删除规则
  const handleDeleteRule = async (id: number) => {
    await ElMessageBox.confirm('确定要删除此规则吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (id > 0) {
      await fetchDeleteAccessControlRule(id)
      await fetchAccessControlData()
    } else {
      const index = formData.rules.findIndex((rule) => rule.id === id)
      if (index > -1) {
        formData.rules.splice(index, 1)
        editingRuleId.value = null
      }
    }
  }

  // 处理弹窗关闭
  const handleClose = () => {
    resetFormData()
    dialogVisible.value = false
    emit('close')
  }
</script>
