<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增访问令牌' : '编辑访问令牌'"
    width="500px"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <ElFormItem v-if="dialogType === 'edit'" label="令牌值">
        <ElInput v-model="formData.token" disabled />
      </ElFormItem>
      <ElFormItem label="令牌名称" prop="name">
        <ElInput v-model="formData.name" placeholder="请输入令牌名称" />
      </ElFormItem>
      <ElFormItem label="最大设备数" prop="maxDevice">
        <ElInputNumber v-model="formData.maxDevice" :min="1" :max="1000" />
      </ElFormItem>
      <ElFormItem label="设备超时时间" prop="deviceTimeout">
        <ElInputNumber v-model="formData.deviceTimeout" :min="1" :max="365" />
        <span class="ml-2 text-xs text-gray-500">天</span>
      </ElFormItem>
      <ElFormItem label="最大连接数" prop="maxConnection">
        <ElInputNumber v-model="formData.maxConnection" :min="1" :max="10000" />
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
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchCreateToken, fetchUpdateToken } from '@/api/token'
  import { ElMessage } from 'element-plus'

  interface Props {
    visible: boolean
    type: string
    tokenData?: Partial<Api.AccessToken.AccessTokenDTO>
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

  const formData = reactive({
    id: undefined as number | undefined,
    token: '',
    name: '',
    maxDevice: 3,
    deviceTimeout: 30,
    maxConnection: 3
  })

  const rules: FormRules = {
    name: [
      { required: true, message: '请输入令牌名称', trigger: 'blur' }
    ],
    maxDevice: [
      { required: true, message: '请输入最大设备数', trigger: 'blur' }
    ],
    deviceTimeout: [
      { required: true, message: '请输入设备超时时间', trigger: 'blur' }
    ],
    maxConnection: [
      { required: true, message: '请输入最大连接数', trigger: 'blur' }
    ]
  }

  const initFormData = () => {
    const isEdit = props.type === 'edit' && props.tokenData
    const row = props.tokenData

    Object.assign(formData, {
      id: isEdit && row ? row.id : undefined,
      token: isEdit && row ? row.token || '' : '',
      name: isEdit && row ? row.name || '' : '',
      maxDevice: isEdit && row ? row.max_device || 3 : 3,
      deviceTimeout: isEdit && row ? row.device_timeout || 30 : 30,
      maxConnection: isEdit && row ? row.max_connection || 3 : 3
    })
  }

  watch(
    () => [props.visible, props.type, props.tokenData],
    ([visible]) => {
      if (visible) {
        initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          if (dialogType.value === 'add') {
            await fetchCreateToken({
              name: formData.name,
              maxDevice: formData.maxDevice,
              deviceTimeout: formData.deviceTimeout,
              maxConnection: formData.maxConnection
            })
            ElMessage.success('创建成功')
          } else {
            if (formData.id) {
              await fetchUpdateToken(formData.id, {
                name: formData.name,
                maxDevice: formData.maxDevice,
                deviceTimeout: formData.deviceTimeout,
                maxConnection: formData.maxConnection
              })
              ElMessage.success('更新成功')
            }
          }
          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('操作失败:', error)
        }
      }
    })
  }
</script>
