<script setup lang="ts">
const router = useRouter()
</script>

<template>
  <main class="page">
    <button type="button" @click="router.back()">
      Назад
    </button>

    <h1>Блокировки</h1>
    <p>Раздел блокировок пока находится в разработке.</p>
  </main>
</template>