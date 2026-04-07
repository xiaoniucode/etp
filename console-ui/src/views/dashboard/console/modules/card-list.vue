<template>
  <ElRow :gutter="20" class="flex">
    <ElCol v-for="(item, index) in dataList" :key="index" :sm="12" :md="6" :lg="6">
      <div class="art-card relative flex flex-col justify-center h-35 px-5 mb-5 max-sm:mb-4">
        <span class="text-g-700 text-sm">{{ item.des }}</span>
        <ArtCountTo class="text-[26px] font-medium mt-2" :target="item.num" :duration="1300" />
        <div
          class="absolute top-0 bottom-0 right-5 m-auto size-12.5 rounded-xl flex-cc bg-theme/10"
        >
          <ArtSvgIcon :icon="item.icon" class="text-xl text-theme" />
        </div>
      </div>
    </ElCol>
  </ElRow>
</template>

<script setup lang="ts">
  interface CardDataItem {
    des: string
    icon: string
    startVal: number
    duration: number
    num: number
  }

  const dataList = reactive<CardDataItem[]>([
    {
      des: '客户端总数',
      icon: 'ri:pie-chart-line',
      startVal: 0,
      duration: 1000,
      num: 9120
    },
    {
      des: '在线客户端数',
      icon: 'ri:group-line',
      startVal: 0,
      duration: 1000,
      num: 182
    },
    {
      des: '代理总数',
      icon: 'ri:fire-line',
      startVal: 0,
      duration: 1000,
      num: 9520
    },
    {
      des: '已启动代理数',
      icon: 'ri:progress-2-line',
      startVal: 0,
      duration: 1000,
      num: 156
    }
  ])
</script>
