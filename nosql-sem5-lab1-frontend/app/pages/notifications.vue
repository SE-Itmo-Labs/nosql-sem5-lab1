<script setup lang="ts">
import FeaturePlaceholder from '~/components/feature/FeaturePlaceholder.vue'
import { getNavigationItem } from '~/config/navigation'

defineOptions({ name: 'NotificationsPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Уведомления' })

const feature = getNavigationItem('/notifications')
</script>

<template>
  <FeaturePlaceholder
    :feature="feature"
    state-title="Каркас раздела готов"
    state-description="Следующий функциональный этап добавит список, фильтры, статистику и форму отправки уведомления через ApiClient."
  />
</template>
