<template>
  <section class="auth-page">
    <div class="auth-panel">
      <h1>Mini Douban</h1>
      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>
      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model.trim="username" autocomplete="username" maxlength="32" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
        <el-button class="full-button" type="primary" native-type="submit" :loading="submitting">
          {{ mode === 'login' ? '登录' : '注册并登录' }}
        </el-button>
      </el-form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { AxiosError } from 'axios'
import { useUserStore } from '../stores/user'
import type { ApiError } from '../types/domain'

const router = useRouter()
const userStore = useUserStore()
const mode = ref<'login' | 'register'>('login')
const username = ref('')
const password = ref('')
const submitting = ref(false)
const errorMessage = ref('')

async function submit() {
  errorMessage.value = ''
  submitting.value = true
  try {
    if (mode.value === 'login') {
      await userStore.login(username.value, password.value)
    } else {
      await userStore.register(username.value, password.value)
    }
    void router.push('/movies')
  } catch (error) {
    const axiosError = error as AxiosError<ApiError>
    errorMessage.value = axiosError.response?.data.message || '操作失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>
