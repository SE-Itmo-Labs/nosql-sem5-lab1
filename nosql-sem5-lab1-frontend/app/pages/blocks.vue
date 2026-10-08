<script setup lang="ts">
import TemporaryBlockForm from '~/components/blocks/TemporaryBlockForm.vue'
import TemporaryBlockLookup from '~/components/blocks/TemporaryBlockLookup.vue'
import TemporaryBlockStatus from '~/components/blocks/TemporaryBlockStatus.vue'
import PageHeader from '~/components/ui/PageHeader.vue'

defineOptions({ name: 'TemporaryBlocksPage' })

definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Временная бронь' })

const {
  actionError,
  block,
  checkBlock,
  createBlock,
  feedback,
  isChecking,
  isCreating,
  isReleasing,
  lookupKey,
  releaseBlock,
  remainingTtl,
  status,
  user,
} = useTemporaryBlocks()
</script>

<template>
  <div>
    <PageHeader
      eyebrow="Redis TTL"
      title="Временная бронь ресурса"
      description="Создайте ключ с ограниченным временем жизни, наблюдайте обратный отсчёт и подтвердите автоматическое удаление после окончания TTL."
    />

    <TemporaryBlockLookup v-model="lookupKey" :checking="isChecking" @check="checkBlock" />

    <p v-if="feedback" class="page-message page-message--success" role="status">{{ feedback }}</p>
    <p v-if="actionError" class="page-message page-message--error" role="alert">{{ actionError }}</p>

    <div class="blocks-layout">
      <TemporaryBlockForm
        :username="user?.username ?? '—'"
        :submitting="isCreating"
        @submit="createBlock"
      />
      <TemporaryBlockStatus
        :block="block"
        :status="status"
        :remaining-ttl="remainingTtl"
        :checking="isChecking"
        :releasing="isReleasing"
        @release="releaseBlock"
        @recheck="checkBlock"
      />
    </div>
  </div>
</template>

<style scoped>
.blocks-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 0.8fr);
  align-items: start;
  gap: 18px;
}

.page-message {
  margin: 0 0 14px;
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.page-message--success { background: var(--color-success-soft); color: var(--color-success); }
.page-message--error { background: var(--color-error-bg); color: var(--color-error); }

@media (max-width: 840px) {
  .blocks-layout { grid-template-columns: 1fr; }
}
</style>
