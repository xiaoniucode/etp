<template>
  <ElCard class="art-search-card">
    <ElForm
      :model="searchForm"
      :inline="true"
      @keyup.enter="handleSearch"
    >
      <ElFormItem label="关键词">
        <ElInput
          v-model="searchForm.keyword"
          placeholder="请输入代理名称"
          clearable
          style="width: 200px"
        />
      </ElFormItem>
      
      <ElFormItem>
        <ElSpace>
          <ElButton type="primary" @click="handleSearch">搜索</ElButton>
          <ElButton @click="handleReset">重置</ElButton>
        </ElSpace>
      </ElFormItem>
    </ElForm>
  </ElCard>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'

  defineOptions({ name: 'TcpSearch' })

  const props = defineProps<{
    modelValue: {
      keyword?: string
    }
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: { keyword?: string }): void
    (e: 'search', value: { keyword?: string }): void
    (e: 'reset'): void
  }>()

  const searchForm = ref({ ...props.modelValue })

  watch(
    () => props.modelValue,
    (newValue) => {
      searchForm.value = { ...newValue }
    },
    { deep: true }
  )

  const handleSearch = () => {
    emit('update:modelValue', searchForm.value)
    emit('search', searchForm.value)
  }

  const handleReset = () => {
    searchForm.value = {}
    emit('update:modelValue', searchForm.value)
    emit('reset')
  }
</script>

<style lang="scss" scoped>
  .art-search-card {
    margin-bottom: 20px;
  }
</style>