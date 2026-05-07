import { http } from './http'
import type { LoginResponse, User } from '../types/domain'

export async function register(username: string, password: string) {
  const response = await http.post<User>('/api/auth/register', { username, password })
  return response.data
}

export async function login(username: string, password: string) {
  const response = await http.post<LoginResponse>('/api/auth/login', { username, password })
  return response.data
}

export async function fetchMe() {
  const response = await http.get<User>('/api/me')
  return response.data
}
