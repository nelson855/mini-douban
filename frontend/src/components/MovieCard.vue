<template>
  <article class="movie-card" @click="$emit('select', movie.id)">
    <img class="poster" :src="poster" :alt="movie.title" />
    <div class="movie-card-body">
      <h2>{{ movie.title }}</h2>
      <p class="meta">{{ movie.director || '未知导演' }} · {{ movie.releaseYear || '未知年份' }}</p>
      <div class="score-row">
        <el-rate
          :model-value="movie.averageScore ? movie.averageScore / 2 : 0"
          disabled
          allow-half
          :max="5"
          size="small"
        />
        <span>{{ scoreText }}</span>
      </div>
      <p class="synopsis">{{ movie.synopsis || '暂无简介' }}</p>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Movie } from '../types/domain'

const props = defineProps<{
  movie: Movie
}>()

defineEmits<{
  select: [id: number]
}>()

const poster = computed(() => props.movie.posterUrl || '/vite.svg')
const scoreText = computed(() => {
  if (props.movie.averageScore === null) {
    return '暂无评分'
  }
  return `${props.movie.averageScore.toFixed(1)} / ${props.movie.ratingCount} 人`
})
</script>
