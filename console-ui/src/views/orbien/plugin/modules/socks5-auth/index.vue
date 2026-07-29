<template>
  <div class="socks5-auth-page">
    <div class="mb-6">
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.basic.title') }}</h3>
      <div class="flex flex-col gap-4">
        <div class="flex items-center gap-3">
          <span class="w-20 font-medium">{{ t('orbien.plugin.basic.enabled') }}：</span>
          <ElSwitch v-model="formData.enabled" @change="handleEnableChange"/>
        </div>
      </div>
    </div>

    <div>
      <h3 class="text-lg font-semibold mb-4">{{ t('orbien.plugin.socks5Auth.userList') }}</h3>
      <div class="border border-gray-200 rounded p-4">
        <ElTable :data="formData.users" style="width: 100%" border>
          <ElTableColumn prop="username" :label="t('orbien.plugin.socks5Auth.username')" width="200">
            <template #default="scope">
              <ElInput
                  v-if="editingUserId === scope.row.id"
                  v-model="scope.row.username"
                  size="small"
                  :placeholder="t('orbien.plugin.socks5Auth.usernamePlaceholder')"
                  style="width: 100%"
              />
              <span v-else>{{ scope.row.username }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="password" :label="t('orbien.plugin.socks5Auth.password')" width="200">
            <template #default="scope">
              <ElInput
                  v-if="editingUserId === scope.row.id"
                  v-model="scope.row.password"
                  size="small"
                  :placeholder="t('orbien.plugin.socks5Auth.passwordPlaceholder')"
                  type="password"
                  style="width: 100%"
              />
              <span v-else>{{ '••••••••' }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="t('common.actions')" width="200" fixed="right">
            <template #default="scope">
              <ElSpace size="small">
                <ElButton
                    v-if="editingUserId === scope.row.id"
                    type="primary"
                    size="small"
                    @click="handleSaveUser(scope.row)"
                >
                  {{ t('common.save') }}
                </ElButton>
                <ElButton v-else type="link" size="small" @click="handleEditUser(scope.row)">
                  {{ t('common.edit') }}
                </ElButton>
                <ElButton type="link" size="small" @click="handleDeleteUser(scope.row.id)">
                  <template #icon>
                    <Delete/>
                  </template>
                  {{ t('common.delete') }}
                </ElButton>
              </ElSpace>
            </template>
          </ElTableColumn>
        </ElTable>
        <ElButton type="primary" size="small" @click="addUser" class="mt-3">
          <template #icon>
            <Plus/>
          </template>
          {{ t('orbien.plugin.actions.addUser') }}
        </ElButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Delete} from '@element-plus/icons-vue'
import {
  fetchGetSocks5Auth,
  fetchUpdateSocks5Auth,
  fetchAddSocks5AuthUser,
  fetchUpdateSocks5AuthUser,
  fetchDeleteSocks5AuthUser
} from '@/api/socks5-auth'

defineOptions({name: 'Socks5AuthPage'})

const {t} = useI18n()

const props = defineProps<{
  proxyId: string
}>()

const formData = reactive({
  enabled: false,
  users: [] as Array<{
    id: number
    proxyId: string
    username: string
    password: string
  }>
})

const editingUserId = ref<number | null>(null)
const editingUserBackup = ref<any>(null)

const resetFormData = () => {
  formData.enabled = false
  formData.users = []
  editingUserId.value = null
  editingUserBackup.value = null
}

const fetchSocks5AuthData = async () => {
  const response = await fetchGetSocks5Auth(props.proxyId)
  if (response) {
    formData.enabled = response.enabled || false
    formData.users = response.users || []
  }
}

watch(
    () => props.proxyId,
    async (proxyId) => {
      if (!proxyId) return
      resetFormData()
      await fetchSocks5AuthData()
    },
    {immediate: true}
)

const handleEnableChange = async () => {
  await fetchUpdateSocks5Auth({
    proxyId: props.proxyId,
    enabled: formData.enabled
  })
}

const addUser = () => {
  formData.users.push({
    id: 0,
    proxyId: props.proxyId,
    username: '',
    password: ''
  })
  const newUser = formData.users[formData.users.length - 1]
  handleEditUser(newUser)
}

const handleEditUser = (user: any) => {
  editingUserBackup.value = {...user}
  editingUserId.value = user.id
}

const handleSaveUser = async (user: any) => {
  if (!user.username) {
    ElMessage.error(t('orbien.plugin.socks5Auth.usernameRequired'))
    return
  }
  if (!user.password) {
    ElMessage.error(t('orbien.plugin.socks5Auth.passwordRequired'))
    return
  }

  if (user.id > 0) {
    await fetchUpdateSocks5AuthUser({
      id: user.id,
      proxyId: props.proxyId,
      username: user.username,
      password: user.password
    })
  } else {
    await fetchAddSocks5AuthUser({
      proxyId: props.proxyId,
      username: user.username,
      password: user.password
    })
  }
  editingUserId.value = null
  await fetchSocks5AuthData()
  editingUserBackup.value = null
}

const handleDeleteUser = async (id: number) => {
  await ElMessageBox.confirm(t('orbien.plugin.deleteConfirm.user'), t('common.warning'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })

  if (id > 0) {
    await fetchDeleteSocks5AuthUser(id)
    await fetchSocks5AuthData()
  } else {
    const index = formData.users.findIndex((user) => user.id === id)
    if (index > -1) {
      formData.users.splice(index, 1)
      editingUserId.value = null
    }
  }
}
</script>
