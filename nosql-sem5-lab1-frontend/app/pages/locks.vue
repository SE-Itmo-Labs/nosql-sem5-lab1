<script setup lang="ts">
import FeaturePlaceholder from '~/components/feature/FeaturePlaceholder.vue'
import { getNavigationItem } from '~/config/navigation'

defineOptions({ name: 'DistributedLocksPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Распределённый lock' })

const feature = getNavigationItem('/locks')
</script>

<template>
  <FeaturePlaceholder
    :feature="feature"
    state-title="Атомарный сценарий подготовлен"
    state-description="Здесь будут две конкурирующие попытки SET NX EX и безопасное освобождение блокировки только по токену владельца."
  />
</template>
