<script setup lang="ts">
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import type { NavigationItem } from '~/config/navigation'

defineProps<{
  feature: NavigationItem
  stateTitle: string
  stateDescription: string
}>()
</script>

<template>
  <div>
    <PageHeader
      :title="feature.label"
      :description="feature.description"
      :eyebrow="feature.shortLabel"
    />

    <!-- Единая заглушка не дает временным страницам расходиться по разметке. -->
    <StatePanel
      kind="info"
      :icon="feature.icon"
      :title="stateTitle"
      :description="stateDescription"
    >
      <template v-if="$slots.actions" #actions>
        <slot name="actions" />
      </template>
    </StatePanel>
  </div>
</template>
