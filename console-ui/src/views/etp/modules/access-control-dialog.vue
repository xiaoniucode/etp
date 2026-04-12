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
      <ElCard class="art-card mb-4">
        <template #header>
          <div class="flex justify-between items-center">
            <span>基本配置</span>
          </div>
        </template>
        <div class="flex flex-col gap-4">
          <div class="flex items-center gap-3">
            <span class="w-20 font-medium">启用状态：</span>
            <ElSwitch v-model="formData.enabled" @change="handleEnableChange" />
          </div>
          <div class="flex items-center gap-3">
            <span class="w-20 font-medium">控制模式：</span>
            <ElRadioGroup v-model="formData.mode" @change="handleModeChange">
              <ElRadio :label="1">白名单（只允许指定IP访问）</ElRadio>
              <ElRadio :label="0">黑名单（拒绝指定IP访问）</ElRadio>
            </ElRadioGroup>
          </div>
        </div>
      </ElCard>

      <!-- 规则列表 -->
      <ElCard class="art-card">
        <template #header>
          <div class="flex justify-between items-center">
            <span>访问规则</span>
          </div>
        </template>
        <div class="border border-gray-200 rounded p-4">
          <ElTable :data="formData.rules" style="width: 100%" border>
            <ElTableColumn prop="cidr" label="IP地址段" width="300">
              <template #default="scope">
                <ElInput
                  v-if="editingRuleId === scope.row.id"
                  v-model="scope.row.cidr"
                  placeholder="请输入IP地址段"
                  style="width: 100%"
                />
                <span v-else>{{ scope.row.cidr }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="ruleType" label="规则类型" width="150">
              <template #default="scope">
                <ElRadioGroup v-if="editingRuleId === scope.row.id" v-model="scope.row.ruleType">
                  <ElRadio :label="1">允许</ElRadio>
                  <ElRadio :label="0">拒绝</ElRadio>
                </ElRadioGroup>
                <ElTag v-else :type="scope.row.ruleType === 1 ? 'success' : 'danger'">
                  {{ scope.row.ruleType === 1 ? '允许' : '拒绝' }}
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
        <div v-if="formData.rules.length === 0" class="py-10 text-center">
          <ElEmpty description="暂无规则" />
        </div>
      </ElCard>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, onMounted } from 'vue'
  import { ElMessage, ElMessageBox, ElEmpty } from 'element-plus'
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
        await fetchAccessControlData()
      }
    }
  )

  // 监听本地 dialogVisible 变化，通知父组件
  watch(dialogVisible, (newVal) => {
    emit('update:visible', newVal)
  })

  // 组件挂载时获取数据
  onMounted(async () => {
    if (props.visible && props.proxyId) {
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
    // 验证规则
    if (!rule.cidr) {
      ElMessage.error('请输入IP地址段')
      return
    }

    try {
      if (rule.id > 0) {
        // 更新现有规则
        await fetchUpdateAccessControlRule({
          id: rule.id,
          cidr: rule.cidr,
          ruleType: rule.ruleType
        })
      } else {
        // 添加新规则
        await fetchAddAccessControlRule({
          proxyId: props.proxyId,
          cidr: rule.cidr,
          ruleType: rule.ruleType
        })
      }
      // 退出编辑状态
      editingRuleId.value = null
      // 重新获取数据
      await fetchAccessControlData()
    } catch (error) {
      console.error('保存规则失败:', error)
      ElMessage.error('保存规则失败')
      // 恢复编辑前的数据
      if (editingRuleBackup.value) {
        Object.assign(rule, editingRuleBackup.value)
      }
    } finally {
      // 清理备份
      editingRuleBackup.value = null
    }
  }

  // 处理删除规则
  const handleDeleteRule = async (id: number) => {
    try {
      await ElMessageBox.confirm('确定要删除此规则吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      if (id > 0) {
        // 删除已保存的规则
        await fetchDeleteAccessControlRule(id)
        // 重新获取数据
        await fetchAccessControlData()
      } else {
        // 删除未保存的规则（直接从列表中移除）
        const index = formData.rules.findIndex((rule) => rule.id === id)
        if (index > -1) {
          formData.rules.splice(index, 1)
          // 退出编辑状态
          editingRuleId.value = null
        }
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除规则失败:', error)
      }
    }
  }

  // 处理弹窗关闭
  const handleClose = () => {
    // 取消编辑状态
    if (editingRuleId.value !== null) {
      editingRuleId.value = null
      editingRuleBackup.value = null
    }
    dialogVisible.value = false
    emit('close')
  }
</script>


