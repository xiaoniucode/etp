<template>
  <ElDialog
    v-model="dialogVisible"
    title="Basic Auth 配置"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <div class="basic-auth-dialog">
      <!-- 基本配置 -->
      <ElCard class="art-card mb-4">
        <template #header>
          <div class="card-header">
            <span>基本配置</span>
          </div>
        </template>
        <div class="config-section">
          <div class="config-item">
            <span class="label">启用状态：</span>
            <ElSwitch
              v-model="formData.enabled"
              @change="handleEnableChange"
            />
          </div>
        </div>
      </ElCard>

      <!-- 用户列表 -->
      <ElCard class="art-card">
        <template #header>
          <div class="card-header">
            <span>用户列表</span>
          </div>
        </template>
        <div class="users-table">
          <ElTable
            :data="formData.users"
            style="width: 100%"
            border
          >
            <ElTableColumn
              prop="username"
              label="用户名"
              width="200"
            >
              <template #default="scope">
                <ElInput
                  v-if="editingUserId === scope.row.id"
                  v-model="scope.row.username"
                  placeholder="请输入用户名"
                  style="width: 100%"
                />
                <span v-else>{{ scope.row.username }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn
              prop="password"
              label="密码"
              width="200"
            >
              <template #default="scope">
                <ElInput
                  v-if="editingUserId === scope.row.id"
                  v-model="scope.row.password"
                  placeholder="请输入密码"
                  type="password"
                  style="width: 100%"
                />
                <span v-else>{{ '••••••••' }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn
              label="操作"
              width="200"
              fixed="right"
            >
              <template #default="scope">
                <ElSpace size="small">
                  <ElButton
                    v-if="editingUserId === scope.row.id"
                    type="primary"
                    size="small"
                    @click="handleSaveUser(scope.row)"
                  >
                    保存
                  </ElButton>
                  <ElButton
                    v-else
                    type="primary"
                    size="small"
                    @click="handleEditUser(scope.row)"
                  >
                    <template #icon>
                      <Edit />
                    </template>
                    编辑
                  </ElButton>
                  <ElButton
                    type="danger"
                    size="small"
                    @click="handleDeleteUser(scope.row.id)"
                  >
                    <template #icon>
                      <Delete />
                    </template>
                    删除
                  </ElButton>
                </ElSpace>
              </template>
            </ElTableColumn>
          </ElTable>
          <ElButton type="primary" size="small" @click="addUser" style="margin-top: 10px">
            <template #icon>
              <Plus />
            </template>
            新增用户
          </ElButton>
        </div>
        <div v-if="formData.users.length === 0" class="empty-users">
          <ElEmpty description="暂无用户" />
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
    fetchGetBasicAuth,
    fetchUpdateBasicAuth,
    fetchAddBasicAuthUser,
    fetchUpdateBasicAuthUser,
    fetchDeleteBasicAuthUser
  } from '@/api/basic-auth'

  defineOptions({ name: 'BasicAuthDialog' })

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

  // Emits
  const emit = defineEmits(['update:visible', 'close'])

  // 表单数据
  const formData = reactive({
    enabled: false,
    users: [] as Array<{
      id: number
      proxyId: string
      username: string
      password: string
    }>
  })

  // 弹窗状态
  const dialogVisible = ref(false)
  
  // 编辑状态
  const editingUserId = ref<number | null>(null)
  
  // 临时存储编辑前的用户数据，用于取消编辑
  const editingUserBackup = ref<any>(null)

  // 监听 props.visible 变化，同步到本地变量并获取数据
  watch(() => props.visible, async (newVal) => {
    dialogVisible.value = newVal
    if (newVal && props.proxyId) {
      await fetchBasicAuthData()
    }
  })

  // 监听本地 dialogVisible 变化，通知父组件
  watch(dialogVisible, (newVal) => {
    emit('update:visible', newVal)
  })

  // 组件挂载时获取数据
  onMounted(async () => {
    if (props.visible && props.proxyId) {
      await fetchBasicAuthData()
    }
  })

  // 获取 Basic Auth 详情
  const fetchBasicAuthData = async () => {
    try {
      const response = await fetchGetBasicAuth(props.proxyId)
      if (response) {
        formData.enabled = response.enabled || false
        formData.users = response.users || []
      }
    } catch (error) {
      console.error('获取 Basic Auth 详情失败:', error)
      ElMessage.error('获取 Basic Auth 详情失败')
    }
  }

  // 处理启用状态变化
  const handleEnableChange = async () => {
    try {
      await fetchUpdateBasicAuth({
        proxyId: props.proxyId,
        enabled: formData.enabled
      })
    } catch (error) {
      console.error('更新 Basic Auth 状态失败:', error)
      ElMessage.error('更新 Basic Auth 状态失败')
      // 恢复原状态
      formData.enabled = !formData.enabled
    }
  }

  // 添加用户
  const addUser = () => {
    // 添加一行新用户
    formData.users.push({
      id: 0, // 临时ID，后端会生成真实ID
      proxyId: props.proxyId,
      username: '',
      password: ''
    })
    // 自动进入编辑状态
    const newUser = formData.users[formData.users.length - 1]
    handleEditUser(newUser)
  }

  // 开始编辑用户
  const handleEditUser = (user: any) => {
    // 存储编辑前的数据，用于取消编辑
    editingUserBackup.value = { ...user }
    // 设置编辑状态
    editingUserId.value = user.id
  }

  // 保存用户
  const handleSaveUser = async (user: any) => {
    // 验证用户
    if (!user.username) {
      ElMessage.error('请输入用户名')
      return
    }
    if (!user.password) {
      ElMessage.error('请输入密码')
      return
    }
    
    try {
      if (user.id > 0) {
        // 更新现有用户
        await fetchUpdateBasicAuthUser({
          id: user.id,
          username: user.username,
          password: user.password
        })
      } else {
        // 添加新用户
        await fetchAddBasicAuthUser({
          proxyId: props.proxyId,
          username: user.username,
          password: user.password
        })
      }
      // 退出编辑状态
      editingUserId.value = null
      // 重新获取数据
      await fetchBasicAuthData()
    } catch (error) {
      console.error('保存用户失败:', error)
      ElMessage.error('保存用户失败')
      // 恢复编辑前的数据
      if (editingUserBackup.value) {
        Object.assign(user, editingUserBackup.value)
      }
    } finally {
      // 清理备份
      editingUserBackup.value = null
    }
  }

  // 处理删除用户
  const handleDeleteUser = async (id: number) => {
    try {
      await ElMessageBox.confirm('确定要删除此用户吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      if (id > 0) {
        // 删除已保存的用户
        await fetchDeleteBasicAuthUser(id)
        // 重新获取数据
        await fetchBasicAuthData()
      } else {
        // 删除未保存的用户（直接从列表中移除）
        const index = formData.users.findIndex(user => user.id === id)
        if (index > -1) {
          formData.users.splice(index, 1)
          // 退出编辑状态
          editingUserId.value = null
        }
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除用户失败:', error)
      }
    }
  }

  // 处理弹窗关闭
  const handleClose = () => {
    // 取消编辑状态
    if (editingUserId.value !== null) {
      editingUserId.value = null
      editingUserBackup.value = null
    }
    dialogVisible.value = false
    emit('close')
  }
</script>

<style lang="scss" scoped>
  .basic-auth-dialog {
    .config-section {
      display: flex;
      flex-direction: column;
      gap: 16px;
      
      .config-item {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .label {
          width: 80px;
          font-weight: 500;
        }
      }
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .empty-users {
      padding: 40px 0;
      text-align: center;
    }
  }
</style>
