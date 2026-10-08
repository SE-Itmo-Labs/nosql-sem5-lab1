<script setup lang="ts">
import FeaturePlaceholder from '~/components/feature/FeaturePlaceholder.vue'
import { getNavigationItem } from '~/config/navigation'

defineOptions({ name: 'TemporaryBlocksPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Временная бронь' })

const feature = getNavigationItem('/blocks')
</script>

<template>
  <FeaturePlaceholder
    :feature="feature"
    state-title="TTL-сценарий подготовлен"
    state-description="Экран будет создавать временную бронь ресурса, показывать обратный отсчёт и фиксировать автоматическое удаление Redis-ключа."
  />
</template>
