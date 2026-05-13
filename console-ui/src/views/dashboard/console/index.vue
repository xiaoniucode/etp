<!-- 工作台页面 -->
<template>
  <div>
    <ServerStatusCards></ServerStatusCards>
    <CardList></CardList>
    <ElRow :gutter="20">
      <ElCol :sm="24" :md="24" :lg="24">
        <SalesOverview />
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :sm="24" :md="12" :lg="14">
        <ProxyTypeChart />
      </ElCol>
      <ElCol :sm="24" :md="12" :lg="10">
        <AppConfig />
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import ServerStatusCards from './modules/server-status-cards.vue'
  import CardList from './modules/card-list.vue'
  import ProxyTypeChart from './modules/proxy-type-chart.vue'
  import SalesOverview from './modules/sales-overview.vue'
  import AppConfig from './modules/app-config.vue'
  defineOptions({ name: 'Console' })
</script>
