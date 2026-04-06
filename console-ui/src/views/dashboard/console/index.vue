<!-- 工作台页面 -->
<template>
  <div>
    <CardList></CardList>

    <ElRow :gutter="20">
      <ElCol :sm="24" :md="12" :lg="10">
        <ActiveUser />
      </ElCol>
      <ElCol :sm="24" :md="12" :lg="14">
        <SalesOverview />
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import CardList from './modules/card-list.vue'
  import ActiveUser from './modules/active-user.vue'
  import SalesOverview from './modules/sales-overview.vue'
  defineOptions({ name: 'Console' })
</script>
