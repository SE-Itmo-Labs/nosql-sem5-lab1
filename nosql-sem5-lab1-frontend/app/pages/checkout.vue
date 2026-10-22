<script setup lang="ts">
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'
import type { CinemaOrder, Product, ScreeningSeats, SeatHold } from '~/types/api'
import { formatPrice, formatScreeningDate, formatScreeningTime } from '~/utils/cinemaFormatters'

defineOptions({ name: 'CheckoutPage' })
definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Оформление заказа' })

const route = useRoute()
const api = useApi()
const hold = ref<SeatHold | null>(null)
const screening = ref<ScreeningSeats | null>(null)
const products = ref<Product[]>([])
const quantities = reactive<Record<number, number>>({})
const order = ref<CinemaOrder | null>(null)
const idempotencyKey = ref('')
const notificationRefreshVersion = useState('notification-refresh-version', () => 0)
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const now = ref(Date.now())
const holdId = computed(() => String(route.query.holdId ?? ''))
const remainingSeconds = computed(() => hold.value
  ? Math.max(0, Math.ceil((Date.parse(hold.value.expiresAt) - now.value) / 1000))
  : 0)
const ticketTotal = computed(() => (
  (screening.value?.basePrice ?? 0) * (hold.value?.seatIds.length ?? 0)
))
const productsTotal = computed(() => products.value.reduce(
  (sum, product) => sum + product.price * (quantities[product.id] ?? 0),
  0,
))
const total = computed(() => ticketTotal.value + productsTotal.value)
const countdown = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60).toString().padStart(2, '0')
  const seconds = (remainingSeconds.value % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
})

let timer: ReturnType<typeof setInterval> | undefined

const load = async () => {
  if (!holdId.value) {
    error.value = 'Идентификатор удержания не указан'
    loading.value = false
    return
  }
  try {
    hold.value = await api.seatHolds.getById(holdId.value)
    ;[screening.value, products.value] = await Promise.all([
      api.catalog.getScreeningSeats(hold.value.screeningId),
      api.products.getAvailable(),
    ])
  }
  catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'Не удалось открыть оформление'
  }
  finally {
    loading.value = false
  }
}

const changeQuantity = (productId: number, delta: number) => {
  quantities[productId] = Math.max(0, Math.min(10, (quantities[productId] ?? 0) + delta))
}

const confirm = async () => {
  if (!hold.value || remainingSeconds.value <= 0) return
  submitting.value = true
  error.value = ''
  try {
    order.value = await api.orders.create({
      holdId: hold.value.holdId,
      idempotencyKey: idempotencyKey.value,
      products: Object.entries(quantities)
        .filter(([, quantity]) => quantity > 0)
        .map(([productId, quantity]) => ({ productId: Number(productId), quantity })),
    })
    notificationRefreshVersion.value += 1
  }
  catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'Не удалось подтвердить заказ'
  }
  finally {
    submitting.value = false
  }
}

const release = async () => {
  if (!hold.value) return
  await api.seatHolds.release(hold.value.holdId)
  await navigateTo(`/screenings/${hold.value.screeningId}`)
}

onMounted(() => {
  idempotencyKey.value = crypto.randomUUID()
  load()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<template>
  <div>
    <PageHeader eyebrow="Оформление" title="Подтверждение заказа" description="Проверьте места и при желании добавьте товары кинобара." />
    <StatePanel v-if="loading" kind="loading" icon="◌" title="Проверяем удержание" description="Загружаем актуальное состояние заказа." />
    <StatePanel v-else-if="error && !hold" kind="error" icon="!" title="Оформление недоступно" :description="error" />
    <section v-else-if="order" class="card checkout-card success-card">
      <span class="success-icon">✓</span>
      <div><h2>Заказ №{{ order.id }} подтверждён</h2><p>Билеты сохранены. Системное уведомление появится в личном кабинете. Итоговая сумма — {{ formatPrice(order.totalAmount) }}.</p></div>
      <div class="success-actions"><NuxtLink class="btn btn-primary" :to="`/orders/${order.id}`">Открыть заказ</NuxtLink><NuxtLink class="btn btn-secondary" to="/notifications">Уведомления</NuxtLink></div>
    </section>
    <template v-else-if="hold">
      <p v-if="error" class="page-error" role="alert">{{ error }}</p>
      <section class="card checkout-card">
        <div class="hold-heading"><div><span>Места удерживаются</span><strong>{{ countdown }}</strong></div><button class="btn btn-secondary" type="button" @click="release">Освободить</button></div>
        <p v-if="remainingSeconds === 0" class="page-error">Время удержания истекло. <NuxtLink :to="`/screenings/${hold.screeningId}`">Выберите места снова</NuxtLink>.</p>
        <div v-if="screening" class="screening-summary">
          <h2>{{ screening.movieTitle }}</h2>
          <p>{{ formatScreeningDate(screening.startsAt) }} · {{ formatScreeningTime(screening.startsAt) }} · {{ screening.auditorium.name }}</p>
          <p>{{ screening.seats.filter(seat => hold.seatIds.includes(seat.id)).map(seat => `Ряд ${seat.rowNumber}, место ${seat.seatNumber}`).join('; ') }}</p>
        </div>
        <div class="summary-row"><span>Билеты · {{ hold.seatIds.length }}</span><strong>{{ formatPrice(ticketTotal) }}</strong></div>
      </section>
      <section class="card checkout-card">
        <h2>Кинобар</h2>
        <div v-if="!products.length" class="empty-products">Доступных товаров сейчас нет.</div>
        <div v-for="product in products" :key="product.id" class="product-row">
          <div><strong>{{ product.name }}</strong><span>{{ formatPrice(product.price) }}</span></div>
          <div class="quantity"><button type="button" @click="changeQuantity(product.id, -1)">−</button><span>{{ quantities[product.id] ?? 0 }}</span><button type="button" @click="changeQuantity(product.id, 1)">+</button></div>
        </div>
      </section>
      <section class="card total-card"><div><span>Итого</span><strong>{{ formatPrice(total) }}</strong></div><button class="btn btn-primary" type="button" :disabled="submitting || remainingSeconds === 0" @click="confirm">{{ submitting ? 'Подтверждаем…' : 'Подтвердить заказ' }}</button></section>
    </template>
  </div>
</template>

<style scoped>
.screening-summary { margin-top: 18px; }
.screening-summary h2 { margin-bottom: 8px; }
.checkout-card { margin-bottom: 16px; padding: 22px; }
.checkout-card h2 { margin: 0 0 18px; font-size: 20px; }
.hold-heading,.summary-row,.product-row,.total-card { display:flex; align-items:center; justify-content:space-between; gap:16px; }
.hold-heading { padding-bottom:18px; border-bottom:1px solid var(--color-border-soft); }
.hold-heading div,.product-row div:first-child,.total-card div { display:flex; flex-direction:column; gap:5px; }
.hold-heading span,.summary-row span,.product-row span,.total-card span { color:var(--color-text-muted); font-size:12px; }
.hold-heading strong { color:var(--color-primary); font-size:28px; }
.summary-row { padding-top:18px; }
.product-row { padding:14px 0; border-top:1px solid var(--color-border-soft); }
.quantity { display:flex; align-items:center; gap:12px; }
.quantity button { width:32px; height:32px; border:1px solid var(--color-border); border-radius:8px; background:var(--color-surface); cursor:pointer; }
.total-card { padding:22px; }
.total-card strong { font-size:24px; }
.page-error { margin:0 0 14px; color:var(--color-error); }
.success-card { display:flex; align-items:center; gap:18px; }
.success-card h2,.success-card p { margin:0; }
.success-card p { margin-top:6px; color:var(--color-text-secondary); }
.success-card div { flex:1; }
.success-actions { display:flex; flex:0 0 auto!important; gap:8px; }
.success-icon { display:grid; width:48px; height:48px; place-items:center; border-radius:50%; background:var(--color-success-soft); color:var(--color-success); font-size:24px; }
@media(max-width:640px){.total-card,.success-card{align-items:stretch;flex-direction:column}.product-row{align-items:flex-start}}
</style>
