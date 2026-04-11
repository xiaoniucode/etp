<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增访问令牌' : '编辑访问令牌'"
    width="500px"
    align-center
  >
    <div v-if="loading" class="loading-state">
      <ElSkeleton :rows="5" animated />
    </div>
    <ElForm v-else ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <ElFormItem v-if="dialogType === 'edit'" label="令牌值">
        <ElInput v-model="formData.token" disabled />
      </ElFormItem>
      <ElFormItem label="令牌名称" prop="name">
        <ElInput v-model="formData.name" placeholder="请输入令牌名称" />
      </ElFormItem>
      <ElFormItem label="最大设备数" prop="maxDevice">
        <ElInputNumber v-model="formData.maxDevice" :min="1" :max="1000" />
      </ElFormItem>
      <ElFormItem label="最大连接数" prop="maxConnection">
        <ElInputNumber v-model="formData.maxConnection" :min="1" :max="10000" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="loading">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, watch, nextTick } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchCreateToken, fetchUpdateToken, fetchGetTokenById } from '@/api/token'
  import { ElMessage } from 'element-plus'

  interface Props {
    visible: boolean
    type: string
    tokenId?: number
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
  const loading = ref(false)

  const formRef = ref<FormInstance>()

  const formData = reactive({
    id: undefined as number | undefined,
    token: '',
    name: '',
    maxDevice: 3,
    maxConnection: 3
  })

  const rules: FormRules = {
    name: [{ required: true, message: '请输入令牌名称', trigger: 'blur' }],
    maxDevice: [{ required: true, message: '请输入最大设备数', trigger: 'blur' }],
    maxConnection: [{ required: true, message: '请输入最大连接数', trigger: 'blur' }]
  }

  const initFormData = async () => {
    if (props.type === 'add') {
      // 重置表单
      Object.assign(formData, {
        id: undefined,
        token: '',
        name: '',
        maxDevice: 3,
        maxConnection: 3
      })
    } else if (props.type === 'edit' && props.tokenId) {
      // 根据 ID 获取详情
      loading.value = true
      try {
        const data = await fetchGetTokenById(props.tokenId)
        Object.assign(formData, {
          id: data.id,
          token: data.token || '',
          name: data.name || '',
          maxDevice: data.maxDevice || 3,
          maxConnection: data.maxConnection || 3
        })
      } catch (error) {
        console.error('获取令牌详情失败:', error)
        ElMessage.error('获取令牌详情失败')
      } finally {
        loading.value = false
      }
    }
  }

  watch(
    () => [props.visible, props.type, props.tokenId],
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
              maxConnection: formData.maxConnection
            })
            ElMessage.success('创建成功')
          } else {
            if (formData.id) {
              await fetchUpdateToken(formData.id, {
                name: formData.name,
                maxDevice: formData.maxDevice,
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
