<template>
  <section class="page">
    <el-skeleton v-if="loading" :rows="10" animated />
    <el-alert v-else-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
    <article v-else-if="movie" class="movie-detail">
      <img class="detail-poster" :src="movie.posterUrl || '/vite.svg'" :alt="movie.title" />
      <div class="detail-body">
        <el-button class="back-button" text @click="router.push('/movies')">返回</el-button>
        <h1>{{ movie.title }}</h1>
        <p class="meta">{{ movie.director || '未知导演' }} · {{ movie.releaseYear || '未知年份' }}</p>
        <div class="score-row detail-score">
          <el-rate :model-value="movie.averageScore ? movie.averageScore / 2 : 0" disabled allow-half />
          <span>{{ averageText }}</span>
        </div>
        <p class="detail-synopsis">{{ movie.synopsis || '暂无简介' }}</p>
        <div class="rating-panel">
          <span class="rating-label">我的评分</span>
          <el-rate v-model="selectedScore" :max="5" @change="submitRating" />
          <span v-if="saving" class="saving">保存中</span>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { getMovie } from '../api/movie'
import { rateMovie } from '../api/rating'
import { useUserStore } from '../stores/user'
import type { ApiError, MovieDetail } from '../types/domain'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const movie = ref<MovieDetail | null>(null)
const loading = ref(true)
const saving = ref(false)
const selectedScore = ref(0)
const errorMessage = ref('')

const movieId = computed(() => Number(route.params.id))
const averageText = computed(() => {
  if (!movie.value || movie.value.averageScore === null) {
    return '暂无评分'
  }
  return `${movie.value.averageScore.toFixed(1)} / ${movie.value.ratingCount} 人评分`
})

onMounted(loadMovie)

async function loadMovie() {
  loading.value = true
  errorMessage.value = ''
  try {
    movie.value = await getMovie(movieId.value)
    selectedScore.value = movie.value.myScore || 0
  } catch (error) {
    const axiosError = error as AxiosError<ApiError>
    errorMessage.value = axiosError.response?.data.message || '电影详情加载失败'
  } finally {
    loading.value = false
  }
}

async function submitRating(score: number) {
  if (!userStore.isAuthenticated) {
    selectedScore.value = movie.value?.myScore || 0
    void router.push('/login')
    return
  }

  saving.value = true
  try {
    const result = await rateMovie(movieId.value, score)
    if (movie.value) {
      movie.value = {
        ...movie.value,
        myScore: result.myScore,
        averageScore: result.averageScore,
        ratingCount: result.ratingCount,
      }
    }
    selectedScore.value = result.myScore
    ElMessage.success('评分已保存')
  } catch (error) {
    const axiosError = error as AxiosError<ApiError>
    ElMessage.error(axiosError.response?.data.message || '评分保存失败')
  } finally {
    saving.value = false
  }
}
</script>
