import { createRouter, createWebHistory } from 'vue-router'
import MovieListPage from '../pages/MovieListPage.vue'
import MovieDetailPage from '../pages/MovieDetailPage.vue'
import LoginPage from '../pages/LoginPage.vue'
import NotFoundPage from '../pages/NotFoundPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/movies' },
    { path: '/login', component: LoginPage },
    { path: '/movies', component: MovieListPage },
    { path: '/movies/:id', component: MovieDetailPage },
    { path: '/:pathMatch(.*)*', component: NotFoundPage },
  ],
})

export default router
