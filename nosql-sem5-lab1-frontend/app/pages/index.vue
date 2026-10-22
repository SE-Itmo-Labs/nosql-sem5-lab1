<script setup lang="ts">
defineOptions({ name: 'CinemaHomePage' })
definePageMeta({ middleware: 'auth' })

await navigateTo('/movies', { replace: true })
</script>

<template>
  <div />
</template>
