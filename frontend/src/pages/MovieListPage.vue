<template>
  <section class="page">
    <div class="page-heading">
      <h1>电影</h1>
      <p>浏览本地片单，选择一部电影查看详情。</p>
    </div>
    <el-skeleton v-if="loading" :rows="8" animated />
    <el-alert v-else-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
    <div v-else class="movie-grid">
      <MovieCard v-for="movie in movies" :key="movie.id" :movie="movie" @select="openMovie" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MovieCard from '../components/MovieCard.vue'
import { listMovies } from '../api/movie'
import type { Movie } from '../types/domain'

const router = useRouter()
const movies = ref<Movie[]>([])
const loading = ref(true)
const errorMessage = ref('')

onMounted(async () => {
  try {
    movies.value = await listMovies()
  } catch {
    errorMessage.value = '电影列表加载失败'
  } finally {
    loading.value = false
  }
})

function openMovie(id: number) {
  void router.push(`/movies/${id}`)
}
</script>
