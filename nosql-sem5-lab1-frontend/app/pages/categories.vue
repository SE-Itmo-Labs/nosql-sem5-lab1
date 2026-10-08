<script setup lang="ts">
import FeaturePlaceholder from '~/components/feature/FeaturePlaceholder.vue'
import { getNavigationItem } from '~/config/navigation'

defineOptions({ name: 'CategoriesPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Категории' })

const feature = getNavigationItem('/categories')
</script>

<template>
  <FeaturePlaceholder
    :feature="feature"
    state-title="Справочник подключён к навигации"
    state-description="Здесь появятся категории и индикаторы DATABASE/CACHE, чтобы первый промах и последующее попадание были видны на защите."
  />
</template>
