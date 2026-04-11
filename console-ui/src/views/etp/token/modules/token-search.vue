<template>
  <div class="token-search">
    <ArtSearchBar
      ref="searchBarRef"
      v-model="formData"
      :items="formItems"
      :rules="rules"
      @reset="handleReset"
      @search="handleSearch"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'

  defineOptions({ name: 'TokenSearch' })

  interface Props {
    modelValue: {
      keyword?: string
    }
  }

  interface Emits {
    (e: 'update:modelValue', value: { keyword?: string }): void
    (e: 'search', params: { keyword?: string }): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 表单数据双向绑定
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 校验规则
  const rules = {}

  // 表单配置
  const formItems = computed(() => [
    {
      label: '关键词',
      key: 'keyword',
      type: 'input',
      placeholder: '请输入令牌名称或令牌值',
      clearable: true
    }
  ])

  // 事件
  function handleReset() {
    console.log('重置表单')
    emit('reset')
  }

  async function handleSearch(params: { keyword?: string }) {
    await searchBarRef.value.validate()
    emit('search', params)
    console.log('表单数据', params)
  }
</script>

<style scoped>
  .token-search {
    margin-bottom: 20px;
  }
</style>
