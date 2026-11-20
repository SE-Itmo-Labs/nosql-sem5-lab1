<script setup lang="ts">
import type { NotificationStats } from '~/types/api'

defineOptions({ name: 'NotificationStatsCards' })

const props = defineProps<{
  stats: NotificationStats
}>()

/** Количество категорий считается по полной агрегации, а не по текущей странице. */
const categoryCount = computed(() => Object.values(props.stats.byCategory)
  .filter(count => count > 0).length)
</script>

<template>
  <section aria-label="Сводка по всем вашим уведомлениям">
    <dl class="tw:mt-0 tw:mb-5 tw:grid tw:grid-cols-1 tw:gap-3 tw:min-[431px]:grid-cols-2 tw:min-[781px]:grid-cols-4 tw:[&_dt]:text-xs tw:[&_dt]:font-medium tw:[&_dt]:text-(--color-text-secondary) tw:[&_dd]:m-0 tw:[&_dd]:mt-2 tw:[&_dd]:text-[26px] tw:[&_dd]:font-semibold tw:[&_dd]:tabular-nums">
      <div class="card tw:px-5! tw:py-4! tw:shadow-none!">
        <dt>Всего в истории</dt>
        <dd>{{ stats.total }}</dd>
      </div>
      <div class="card tw:px-5! tw:py-4! tw:shadow-none!">
        <dt>Новые</dt>
        <dd>{{ stats.sent }}</dd>
      </div>
      <div class="card tw:px-5! tw:py-4! tw:shadow-none!">
        <dt>Прочитанные</dt>
        <dd>{{ stats.read }}</dd>
      </div>
      <div class="card tw:px-5! tw:py-4! tw:shadow-none!">
        <dt>Категорий в истории</dt>
        <dd>{{ categoryCount }}</dd>
      </div>
    </dl>
  </section>
</template>
