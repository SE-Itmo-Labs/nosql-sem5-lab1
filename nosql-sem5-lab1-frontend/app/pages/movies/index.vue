<script setup lang="ts">
import MovieCard from '~/components/catalog/MovieCard.vue'
import PageHeader from '~/components/ui/PageHeader.vue'
import StatePanel from '~/components/ui/StatePanel.vue'

defineOptions({ name: 'MoviesPage' })
definePageMeta({ layout: 'dashboard', middleware: 'auth', title: 'Афиша' })

const { error, isLoading, loadMovies, movies } = useCatalog()
onMounted(loadMovies)
</script>

<template>
  <div>
    <PageHeader
      eyebrow="Кинотеатр"
      title="Афиша"
      description="Выберите фильм, подходящий сеанс и места в зале."
    />

    <StatePanel v-if="isLoading" kind="loading" icon="◌" title="Загружаем афишу" description="Получаем актуальные фильмы кинотеатра." />
    <StatePanel v-else-if="error" kind="error" icon="!" title="Афиша недоступна" :description="error">
      <template #actions><button class="btn btn-secondary" type="button" @click="loadMovies">Повторить</button></template>
    </StatePanel>
    <StatePanel v-else-if="!movies.length" kind="empty" icon="○" title="Показов пока нет" description="Новые фильмы появятся здесь после публикации расписания." />
    <section v-else class="movie-grid" aria-label="Фильмы в прокате">
      <MovieCard v-for="movie in movies" :key="movie.id" :movie="movie" />
    </section>
  </div>
</template>

<style scoped>
.movie-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; }
@media (max-width: 940px) { .movie-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 620px) { .movie-grid { grid-template-columns: 1fr; } }
</style>
