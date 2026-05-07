<template>
  <header class="app-header">
    <RouterLink class="brand" to="/movies">Mini Douban</RouterLink>
    <nav class="nav-actions">
      <template v-if="userStore.isAuthenticated">
        <span class="username">{{ userStore.user?.username }}</span>
        <el-button plain @click="logout">退出</el-button>
      </template>
      <RouterLink v-else to="/login">
        <el-button type="primary">登录</el-button>
      </RouterLink>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  void router.push('/movies')
}
</script>
