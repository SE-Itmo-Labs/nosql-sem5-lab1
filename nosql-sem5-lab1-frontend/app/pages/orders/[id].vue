<script setup lang="ts">
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import type { CinemaOrder } from '~/types/api'
import { formatPrice } from '~/utils/cinemaFormatters'

defineOptions({ name: 'OrderDetailsPage' })
definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Заказ' })

const route = useRoute()
const api = useApi()
const order = ref<CinemaOrder | null>(null)
const loading = ref(true)
const error = ref('')
const orderId = Number(route.params.id)

onMounted(async () => {
  try { order.value = await api.orders.getById(orderId) }
  catch (cause) { error.value = cause instanceof Error ? cause.message : 'Не удалось загрузить заказ' }
  finally { loading.value = false }
})
</script>

<template>
  <div>
    <NuxtLink class="back-link" to="/orders">← Все заказы</NuxtLink>
    <StatePanel v-if="loading" kind="loading" icon="◌" title="Загружаем заказ" description="Получаем билеты и позиции кинобара." />
    <StatePanel v-else-if="error || !order" kind="error" icon="!" title="Заказ недоступен" :description="error || 'Заказ не найден'" />
    <template v-else>
      <PageHeader eyebrow="Подтверждён" :title="`Заказ №${order.id}`" :description="new Date(order.confirmedAt).toLocaleString('ru-RU')" />
      <section class="card details"><h2>Билеты</h2><div v-for="ticket in order.tickets" :key="ticket.id" class="line"><span>Ряд {{ ticket.rowNumber }}, место {{ ticket.seatNumber }}</span><strong>{{ formatPrice(ticket.price) }}</strong></div></section>
      <section v-if="order.products.length" class="card details"><h2>Кинобар</h2><div v-for="item in order.products" :key="item.productId" class="line"><span>{{ item.name }} × {{ item.quantity }}</span><strong>{{ formatPrice(item.lineTotal) }}</strong></div></section>
      <section class="card total"><span>Итого</span><strong>{{ formatPrice(order.totalAmount) }}</strong></section>
    </template>
  </div>
</template>

<style scoped>
.back-link{display:inline-block;margin-bottom:20px;color:var(--color-text-secondary);font-size:13px;text-decoration:none}.details{margin-bottom:14px;padding:22px}.details h2{margin:0 0 14px;font-size:18px}.line,.total{display:flex;justify-content:space-between;gap:16px}.line{padding:11px 0;border-top:1px solid var(--color-border-soft)}.total{padding:22px;font-size:20px}.total span{color:var(--color-text-muted)}
</style>
