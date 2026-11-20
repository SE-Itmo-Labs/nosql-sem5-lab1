<script setup lang="ts">
import ScreeningList from '~/components/catalog/ScreeningList.vue'
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'

defineOptions({ name: 'MovieDetailsPage' })
definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Фильм' })

const route = useRoute()
const movieId = Number(route.params.id)
const { error, isLoading, loadMovie, movie, screenings } = useCatalog()

onMounted(() => loadMovie(movieId))
</script>

<template>
  <div>
    <NuxtLink class="back-link" to="/movies">← Вернуться к афише</NuxtLink>
    <StatePanel v-if="isLoading" kind="loading" icon="◌" title="Загружаем фильм" description="Получаем карточку и доступные сеансы." />
    <StatePanel v-else-if="error || !movie" kind="error" icon="!" title="Фильм недоступен" :description="error || 'Фильм не найден'">
      <template #actions><button class="btn btn-secondary" type="button" @click="loadMovie(movieId)">Повторить</button></template>
    </StatePanel>
    <template v-else>
      <PageHeader eyebrow="Карточка фильма" :title="movie.title" :description="movie.description" />
      <section class="movie-facts card">
        <div><span>Длительность</span><strong>{{ movie.durationMinutes }} минут</strong></div>
        <div><span>Возраст</span><strong>{{ movie.ageRating }}+</strong></div>
        <div><span>Статус</span><strong>{{ movie.status === 'COMING_SOON' ? 'Скоро' : 'В прокате' }}</strong></div>
      </section>
      <div class="section-title">
        <div><p>Расписание</p><h3>Выберите сеанс</h3></div>
        <span>{{ screenings.length }} доступно</span>
      </div>
      <ScreeningList v-if="screenings.length" :screenings="screenings" />
      <StatePanel v-else kind="empty" icon="◷" title="Сеансов пока нет" description="Расписание этого фильма ещё не опубликовано." />
    </template>
  </div>
</template>

<style scoped>
.back-link { display: inline-block; margin-bottom: 20px; color: var(--color-text-secondary); font-size: 13px; text-decoration: none; }
.back-link:hover { color: var(--color-primary-hover); }
.movie-facts { display: grid; grid-template-columns: repeat(3, 1fr); margin-bottom: 30px; }
.movie-facts div { display: flex; flex-direction: column; gap: 6px; padding: 4px 20px; border-right: 1px solid var(--color-border-soft); }
.movie-facts div:last-child { border: 0; }
.movie-facts span { color: var(--color-text-muted); font-size: 11px; font-weight: 700; text-transform: uppercase; }
.section-title { display: flex; align-items: flex-end; justify-content: space-between; margin: 0 0 14px; }
.section-title p, .section-title h3 { margin: 0; }
.section-title p { color: var(--color-primary-hover); font-size: 11px; font-weight: 750; text-transform: uppercase; }
.section-title h3 { margin-top: 4px; font-size: 22px; }
.section-title > span { color: var(--color-text-muted); font-size: 12px; }
@media (max-width: 620px) { .movie-facts { grid-template-columns: 1fr; gap: 14px; } .movie-facts div { border-right: 0; border-bottom: 1px solid var(--color-border-soft); } }
</style>
