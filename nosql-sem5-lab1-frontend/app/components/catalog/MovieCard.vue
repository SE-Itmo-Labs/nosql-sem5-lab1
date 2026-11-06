<script setup lang="ts">
import type { Movie } from '~/types/api'

defineProps<{ movie: Movie }>()
</script>

<template>
  <NuxtLink class="movie-card" :to="`/movies/${movie.id}`">
    <div class="movie-art" :class="`movie-art--${movie.id % 3}`" aria-hidden="true">
      <span>{{ movie.ageRating }}+</span>
      <strong>{{ movie.title.slice(0, 1) }}</strong>
    </div>
    <div class="movie-copy">
      <div class="movie-meta">
        <span>{{ movie.durationMinutes }} мин</span>
        <span>{{ movie.status === 'COMING_SOON' ? 'Скоро' : 'В прокате' }}</span>
      </div>
      <h3>{{ movie.title }}</h3>
      <p>{{ movie.description }}</p>
      <span class="movie-link">Сеансы <span aria-hidden="true">→</span></span>
    </div>
  </NuxtLink>
</template>

<style scoped>
.movie-card { display: grid; overflow: hidden; border: 1px solid var(--color-border-soft); border-radius: var(--radius-lg); background: var(--color-surface); box-shadow: var(--shadow-sm); text-decoration: none; transition: transform .18s ease, box-shadow .18s ease; }
.movie-card:hover { box-shadow: var(--shadow-md); transform: translateY(-3px); }
.movie-art { position: relative; display: grid; min-height: 210px; place-items: center; background: linear-gradient(145deg, #172033, #ea580c); color: #fff; }
.movie-art--1 { background: linear-gradient(145deg, #172554, #2563eb 58%, #93c5fd); }
.movie-art--2 { background: linear-gradient(145deg, #3f1d58, #7c3aed 58%, #f0abfc); }
.movie-art strong { font-family: Georgia, serif; font-size: 76px; opacity: .78; }
.movie-art > span { position: absolute; top: 16px; right: 16px; padding: 5px 9px; border: 1px solid rgb(255 255 255 / 30%); border-radius: 999px; background: rgb(17 24 39 / 35%); font-size: 12px; font-weight: 800; }
.movie-copy { padding: 20px; }
.movie-meta { display: flex; justify-content: space-between; color: var(--color-text-muted); font-size: 11px; font-weight: 700; text-transform: uppercase; }
.movie-copy h3 { margin: 10px 0 8px; font-size: 20px; }
.movie-copy p { min-height: 66px; margin: 0; color: var(--color-text-secondary); font-size: 13px; line-height: 1.65; }
.movie-link { display: inline-flex; gap: 8px; margin-top: 17px; color: var(--color-primary-hover); font-size: 13px; font-weight: 750; }
</style>
