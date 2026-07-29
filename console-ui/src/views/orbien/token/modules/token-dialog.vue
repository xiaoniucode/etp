<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? $t('orbien.token.add') : $t('orbien.token.edit')"
    width="500px"
    align-center
  >
    <div v-if="loading" class="loading-state">
      <ElSkeleton :rows="5" animated />
    </div>
    <ElForm v-else ref="formRef" :model="formData" :rules="rules" label-width="120px" :show-message="false">
      <ElFormItem v-if="dialogType === 'edit'" :label="$t('orbien.token.tokenValue')">
        <ElInput v-model="formData.token" disabled />
      </ElFormItem>
      <ElFormItem :label="$t('orbien.token.tokenName')" prop="name">
        <ElInput v-model="formData.name" :placeholder="$t('orbien.token.namePlaceholder')" />
      </ElFormItem>
      <ElFormItem v-if="dialogType === 'edit'" :label="$t('common.description')">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          :placeholder="$t('orbien.common.descPlaceholder')"
          maxlength="500"
          show-word-limit
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="loading">{{ $t('common.submit') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, watch, nextTick } from 'vue'
  import { useI18n } from 'vue-i18n'
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
    (e: 'create-success', token: string): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const { t } = useI18n()

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
    remark: ''
  })

  const rules = computed<FormRules>(() => ({
    name: [{ required: true, message: t('orbien.token.nameRequired'), trigger: 'blur' }]
  }))

  const initFormData = async () => {
    if (props.type === 'add') {
      Object.assign(formData, {
        id: undefined,
        token: '',
        name: '',
        remark: ''
      })
    } else if (props.type === 'edit' && props.tokenId) {
      loading.value = true
      try {
        const data = await fetchGetTokenById(props.tokenId)
        Object.assign(formData, {
          id: data.id,
          token: data.token || '',
          name: data.name || '',
          remark: data.remark || ''
        })
      } catch (error) {
        console.error('获取令牌详情失败:', error)
        ElMessage.error(t('orbien.token.fetchDetailFailed'))
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
            const result = await fetchCreateToken({
              name: formData.name
            })
            dialogVisible.value = false
            emit('create-success', result.token)
          } else {
            if (formData.id) {
              await fetchUpdateToken({
                id: formData.id,
                name: formData.name,
                remark: formData.remark
              })
              ElMessage.success(t('common.success.update'))
              dialogVisible.value = false
            }
          }
          emit('submit')
        } catch (error) {
          console.error('操作失败:', error)
        }
      }
    })
  }
</script>
