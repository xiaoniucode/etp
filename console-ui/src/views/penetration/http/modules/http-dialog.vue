<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增 HTTP 代理' : '编辑 HTTP 代理'"
    width="500px"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <ElFormItem label="代理名称" prop="name">
        <ElInput v-model="formData.name" placeholder="请输入代理名称" />
      </ElFormItem>
      <ElFormItem label="域名" prop="domains">
        <ElInput v-model="formData.domains" placeholder="请输入域名，多个域名用逗号分隔" />
      </ElFormItem>
      <ElFormItem label="目标地址" prop="targets">
        <ElInput v-model="formData.targets" placeholder="请输入目标地址，格式：host:port/path" />
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElSelect v-model="formData.status">
          <ElOption label="运行中" value="1" />
          <ElOption label="已停止" value="0" />
        </ElSelect>
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

  const formData = reactive({
    name: '',
    domains: '',
    targets: '',
    status: '1'
  })

  const rules: FormRules = {
    name: [
      { required: true, message: '请输入代理名称', trigger: 'blur' }
    ],
    domains: [
      { required: true, message: '请输入域名', trigger: 'blur' }
    ],
    targets: [
      { required: true, message: '请输入目标地址', trigger: 'blur' }
    ],
    status: [
      { required: true, message: '请选择状态', trigger: 'blur' }
    ]
  }

  const initFormData = () => {
    const isEdit = props.type === 'edit' && props.proxyData
    const row = props.proxyData

    Object.assign(formData, {
      name: isEdit && row ? row.name || '' : '',
      domains: isEdit && row && row.domains ? row.domains.join(', ') : '',
      targets: isEdit && row && row.targets ? row.targets.map(t => `${t.targetHost}:${t.targetPort}${t.targetPath || ''}`).join(', ') : '',
      status: isEdit && row ? row.status?.toString() || '1' : '1'
    })
  }

  watch(
    () => [props.visible, props.type, props.proxyData],
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
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('提交失败:', error)
          ElMessage.error('提交失败')
        }
      }
    })
  }
</script>
