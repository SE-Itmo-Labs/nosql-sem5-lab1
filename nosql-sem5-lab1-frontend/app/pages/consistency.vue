<script setup lang="ts">
import FeaturePlaceholder from '~/components/feature/FeaturePlaceholder.vue'
import { getNavigationItem } from '~/config/navigation'

defineOptions({ name: 'ConsistencyResearchPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Согласованность' })

const feature = getNavigationItem('/consistency')
</script>

<template>
  <FeaturePlaceholder
    :feature="feature"
    state-title="Эксперимент primary/replica подготовлен"
    state-description="Экран сравнит немедленное чтение реплики с режимом WAIT_FOR_REPLICA и покажет значения, подтверждения и длительности операций."
  />
</template>
