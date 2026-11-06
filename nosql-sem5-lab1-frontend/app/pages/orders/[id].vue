<script setup lang="ts">
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import type { CinemaOrder } from '~/types/api'
import { formatPrice, formatScreeningDate, formatScreeningTime } from '~/utils/cinemaFormatters'

defineOptions({ name: 'OrderDetailsPage' })
definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Заказ' })

const route = useRoute()
const api = useApi()
const order = ref<CinemaOrder | null>(null)
const loading = ref(true)
const error = ref('')
const cancelling = ref(false)
const notificationRefreshVersion = useState('notification-refresh-version', () => 0)
const orderId = Number(route.params.id)

onMounted(async () => {
  try { order.value = await api.orders.getById(orderId) }
  catch (cause) { error.value = cause instanceof Error ? cause.message : 'Не удалось загрузить заказ' }
  finally { loading.value = false }
})

const cancel = async () => {
  if (!order.value || order.value.status === 'CANCELLED') return
  cancelling.value = true
  error.value = ''
  try {
    order.value = await api.orders.cancel(order.value.id)
    notificationRefreshVersion.value += 1
  }
  catch (cause) { error.value = cause instanceof Error ? cause.message : 'Не удалось отменить заказ' }
  finally { cancelling.value = false }
}
</script>

<template>
  <div>
    <NuxtLink class="back-link" to="/orders">← Все заказы</NuxtLink>
    <StatePanel v-if="loading" kind="loading" icon="◌" title="Загружаем заказ" description="Получаем билеты и позиции кинобара." />
    <StatePanel v-else-if="error || !order" kind="error" icon="!" title="Заказ недоступен" :description="error || 'Заказ не найден'" />
    <template v-else>
      <PageHeader :eyebrow="order.status === 'CANCELLED' ? 'Отменён' : 'Подтверждён'" :title="`Заказ №${order.id}`" :description="new Date(order.confirmedAt).toLocaleString('ru-RU')">
        <template v-if="order.status === 'CONFIRMED'" #actions><button class="btn btn-secondary" type="button" :disabled="cancelling" @click="cancel">{{ cancelling ? 'Отменяем…' : 'Отменить заказ' }}</button></template>
      </PageHeader>
      <p v-if="error" class="page-error" role="alert">{{ error }}</p>
      <section class="card details">
        <h2>Билеты</h2>
        <div v-for="ticket in order.tickets" :key="ticket.id" class="line">
          <div class="ticket-info">
            <strong>{{ ticket.movieTitle }}</strong>
            <span>{{ formatScreeningDate(ticket.startsAt) }} · {{ formatScreeningTime(ticket.startsAt) }} · {{ ticket.auditoriumName }}</span>
            <span>Ряд {{ ticket.rowNumber }}, место {{ ticket.seatNumber }}</span>
          </div>
          <strong>{{ formatPrice(ticket.price) }}</strong>
        </div>
      </section>
      <section v-if="order.products.length" class="card details"><h2>Кинобар</h2><div v-for="item in order.products" :key="item.productId" class="line"><span>{{ item.name }} × {{ item.quantity }}</span><strong>{{ formatPrice(item.lineTotal) }}</strong></div></section>
      <section class="card total"><span>Итого</span><strong>{{ formatPrice(order.totalAmount) }}</strong></section>
    </template>
  </div>
</template>

<style scoped>
.back-link{display:inline-block;margin-bottom:20px;color:var(--color-text-secondary);font-size:13px;text-decoration:none}.details{margin-bottom:14px;padding:22px}.details h2{margin:0 0 14px;font-size:18px}.line,.total{display:flex;justify-content:space-between;gap:16px}.line{padding:11px 0;border-top:1px solid var(--color-border-soft)}.total{padding:22px;font-size:20px}.total span{color:var(--color-text-muted)}
.ticket-info { display: flex; flex-direction: column; gap: 6px; }
.page-error{margin:0 0 14px;color:var(--color-error)}
</style>
