<script setup lang="ts">
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import type { CinemaOrder } from '~/types/api'
import { formatPrice } from '~/utils/cinemaFormatters'

defineOptions({ name: 'OrdersPage' })
definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Мои заказы' })

const api = useApi()
const orders = ref<CinemaOrder[]>([])
const loading = ref(true)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try { orders.value = await api.orders.getAll() }
  catch (cause) { error.value = cause instanceof Error ? cause.message : 'Не удалось загрузить заказы' }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <div>
    <PageHeader eyebrow="Личный кабинет" title="Мои заказы" description="Подтверждённые билеты и покупки в кинобаре." />
    <StatePanel v-if="loading" kind="loading" icon="◌" title="Загружаем заказы" description="Получаем историю оформлений." />
    <StatePanel v-else-if="error" kind="error" icon="!" title="История недоступна" :description="error"><template #actions><button class="btn btn-secondary" type="button" @click="load">Повторить</button></template></StatePanel>
    <StatePanel v-else-if="!orders.length" kind="empty" icon="◇" title="Заказов пока нет" description="Выберите фильм и места в афише."><template #actions><NuxtLink class="btn btn-primary" to="/movies">Открыть афишу</NuxtLink></template></StatePanel>
    <div v-else class="order-list">
      <NuxtLink v-for="order in orders" :key="order.id" class="card order-card" :to="`/orders/${order.id}`">
        <div><span>Заказ №{{ order.id }}</span><strong>{{ order.tickets.length }} билета · {{ formatPrice(order.totalAmount) }}</strong></div>
        <time>{{ new Date(order.confirmedAt).toLocaleString('ru-RU') }}</time>
      </NuxtLink>
    </div>
  </div>
</template>

<style scoped>
.order-list{display:grid;gap:12px}.order-card{display:flex;align-items:center;justify-content:space-between;padding:20px;text-decoration:none;color:inherit}.order-card div{display:flex;flex-direction:column;gap:6px}.order-card span,.order-card time{color:var(--color-text-muted);font-size:12px}.order-card strong{font-size:16px}
</style>
