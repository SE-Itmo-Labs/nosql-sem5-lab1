<script setup lang="ts">
defineOptions({ name: 'TemporaryBlockLookupForm' })

defineProps<{
  modelValue: string
  checking: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'check': []
}>()
</script>

<template>
  <form class="lookup card" @submit.prevent="emit('check')">
    <div class="lookup-copy">
      <span>GET /api/blocks/{resourceKey}</span>
      <strong>Проверить существующий ключ</strong>
    </div>
    <input
      class="field-input"
      type="text"
      :value="modelValue"
      placeholder="Ключ ресурса"
      aria-label="Ключ ресурса"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    >
    <button class="btn btn-secondary" type="submit" :disabled="checking">
      {{ checking ? 'Проверяем…' : 'Проверить' }}
    </button>
  </form>
</template>

<style scoped>
.lookup {
  display: grid;
  grid-template-columns: auto minmax(200px, 1fr) auto;
  align-items: end;
  gap: 14px;
  margin-bottom: 18px;
  padding: 16px 18px;
}

.lookup-copy span,
.lookup-copy strong {
  display: block;
}

.lookup-copy span {
  color: var(--color-text-muted);
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 9px;
}

.lookup-copy strong {
  margin-top: 4px;
  font-size: 13px;
}

@media (max-width: 720px) {
  .lookup { grid-template-columns: 1fr; align-items: stretch; }
}
</style>
