<script setup lang="ts">
import type { HoldReplicationResponse } from '~/types/api'

const props = defineProps<{ holdId: string }>()
const api = useApi()
const result = ref<HoldReplicationResponse | null>(null)
const loading = ref(false)
const error = ref('')

const labels: Record<HoldReplicationResponse['nodes'][number]['status'], string> = {
  PRESENT: 'Найдено',
  MATCH: 'Совпадает с primary',
  MISSING: 'Не найдено',
  DIFFERENT: 'Отличается от primary',
  UNAVAILABLE: 'Узел недоступен',
}

const inspect = async () => {
  loading.value = true
  error.value = ''
  result.value = null
  try {
    result.value = await api.consistency.inspectHold(props.holdId)
  }
  catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'Не удалось проверить удержание'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="card replication-check" aria-label="Проверка репликации удержания">
    <h2>Исследование репликации</h2>
    <p>Сравните текущее удержание на основном Redis и репликах до подтверждения заказа.</p>
    <button class="btn btn-secondary" type="button" :disabled="loading" @click="inspect">
      {{ loading ? 'Проверяем…' : 'Проверить репликацию удержания' }}
    </button>
    <p v-if="error" class="check-error" role="alert">{{ error }}</p>
    <div v-if="result" aria-live="polite">
      <ul class="node-list">
        <li v-for="node in result.nodes" :key="node.node">
          <strong>{{ node.node }} ({{ node.role }})</strong>
          <span>{{ labels[node.status] }}<template v-if="node.ttlSeconds !== null && node.ttlSeconds >= 0"> · TTL {{ node.ttlSeconds }} с</template></span>
        </li>
      </ul>
      <p>Результат показывает состояние при проверке; совпадение не измеряет задержку репликации.</p>
    </div>
  </section>
</template>

<style scoped>
.replication-check { margin-bottom: 16px; padding: 22px; }
.replication-check h2 { margin: 0 0 8px; font-size: 20px; }
.replication-check p { color: var(--color-text-secondary); font-size: 13px; }
.node-list { display: grid; gap: 8px; margin: 16px 0; padding: 0; list-style: none; }
.node-list li { display: flex; flex-wrap: wrap; gap: 8px 20px; justify-content: space-between; }
.check-error { color: var(--color-error) !important; }
</style>
