<template>
  <div class="domain-page art-full-height">
    <div class="art-card p-5 mb-5 max-sm:mb-4 shrink-0">
      <div class="mb-5">
        <h4 class="m-0 text-lg font-semibold">{{ $t('orbien.domain.resourcesTitle') }}</h4>
        <p class="m-0 mt-1 text-sm text-g-500">
          {{ $t('orbien.domain.resourcesDesc') }}
        </p>
      </div>

      <div class="flex gap-10 max-md:flex-col max-md:gap-5">
        <div
          v-for="item in overviewItems"
          :key="item.key"
          class="flex-1 min-w-0 cursor-pointer rounded-lg p-4 transition-colors"
          :class="activeView === item.key ? 'bg-theme/10 ring-1 ring-theme' : 'hover:bg-g-100'"
          @click="activeView = item.key"
        >
          <div class="mb-1 text-sm text-g-600">{{ item.label }}</div>
          <div class="flex items-center gap-3">
            <ArtCountTo class="text-3xl font-semibold leading-none" :target="item.count" :duration="600" />
            <ArtSvgIcon :icon="item.icon" class="ml-auto text-2xl" :class="item.iconClass" />
          </div>
          <div class="mt-2 text-xs text-g-500">{{ item.hint }}</div>
        </div>
      </div>
    </div>

    <ElCard class="art-table-card">
      <DomainList v-if="activeView === 'pool'" @change="loadSummary"/>
      <UsedDomainList v-else @change="loadSummary"/>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, computed, onMounted} from 'vue'
import {useI18n} from 'vue-i18n'
import ArtCountTo from '@/components/core/text-effect/art-count-to/index.vue'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import DomainList from './modules/domain-list.vue'
import UsedDomainList from './modules/used-domain-list.vue'
import {fetchGetDomainListByPage, fetchGetUsedDomainListByPage} from '@/api/domain'

defineOptions({name: 'DomainManagement'})

type DomainView = 'pool' | 'allocated'

const {t} = useI18n()
const activeView = ref<DomainView>('pool')

const summary = reactive({
  baseCount: 0,
  usedCount: 0
})

const overviewItems = computed(() => [
  {
    key: 'pool' as DomainView,
    label: t('orbien.domain.poolLabel'),
    hint: t('orbien.domain.poolHint'),
    icon: 'ri:global-line',
    iconClass: 'text-theme',
    count: summary.baseCount
  },
  {
    key: 'allocated' as DomainView,
    label: t('orbien.domain.allocatedLabel'),
    hint: t('orbien.domain.allocatedHint'),
    icon: 'ri:link',
    iconClass: 'text-g-600',
    count: summary.usedCount
  }
])

const loadSummary = async () => {
  try {
    const [baseRes, usedRes] = await Promise.all([
      fetchGetDomainListByPage({current: 1, size: 1}),
      fetchGetUsedDomainListByPage({current: 1, size: 1})
    ])
    summary.baseCount = baseRes.total ?? 0
    summary.usedCount = usedRes.total ?? 0
  } catch {
    // 概览统计失败时不阻断列表展示
  }
}

onMounted(() => {
  loadSummary()
})
</script>
