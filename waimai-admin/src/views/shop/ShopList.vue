<template>
  <div>
    <h2>商家管理</h2>
    <a-table :columns="columns" :data-source="data" :loading="loading" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getShopList } from '@/api/shop'

const loading = ref(false)
const data = ref([])

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id' },
  { title: '商家名称', dataIndex: 'name', key: 'name' },
  { title: '分类', dataIndex: 'categoryName', key: 'categoryName' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

onMounted(() => {
  // TODO: 加载商家列表
})
</script>
