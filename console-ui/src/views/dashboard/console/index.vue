<!-- 工作台页面 -->
<template>
  <div>
    <CardList></CardList>

    <ElRow :gutter="20">
      <ElCol :sm="24" :md="12" :lg="10">
        <ProxyTypeChart />
      </ElCol>
      <ElCol :sm="24" :md="12" :lg="14">
        <SalesOverview />
      </ElCol>
    </ElRow>
    <AppConfig />
  </div>
</template>

<script setup lang="ts">
  import CardList from './modules/card-list.vue'
  import ProxyTypeChart from './modules/proxy-type-chart.vue'
  import SalesOverview from './modules/sales-overview.vue'
  import AppConfig from './modules/app-config.vue'
  defineOptions({ name: 'Console' })
</script>
