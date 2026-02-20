<template>
  <div>
    <h2>{{ isEdit ? '编辑商家' : '新增商家' }}</h2>
    <a-form :model="form" @submit="handleSubmit">
      <!-- TODO: 实现表单 -->
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isEdit = ref(!!route.params.id)
const form = ref({})

const handleSubmit = () => {
  // TODO: 实现提交逻辑
}
</script>
