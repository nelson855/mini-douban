import { defineStore, createPinia } from 'pinia'
import { fetchMe, login as loginApi, register as registerApi } from '../api/auth'
import type { User } from '../types/domain'

const TOKEN_KEY = 'mini-douban-token'
const USER_KEY = 'mini-douban-user'

function readStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as User
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const pinia = createPinia()

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY),
    user: readStoredUser(),
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user),
  },
  actions: {
    setSession(token: string, user: User) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    async login(username: string, password: string) {
      const result = await loginApi(username, password)
      this.setSession(result.token, result.user)
    },
    async register(username: string, password: string) {
      await registerApi(username, password)
      await this.login(username, password)
    },
    async fetchMe() {
      if (!this.token) {
        return
      }
      this.user = await fetchMe()
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
  },
})
