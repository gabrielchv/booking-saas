import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/api/client'
import type { AuthResponse, AuthUser, LoginPayload, SignupPayload } from '@/api/types'

function loadUser(): AuthUser | null {
  const raw = localStorage.getItem('user')
  return raw ? (JSON.parse(raw) as AuthUser) : null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<AuthUser | null>(loadUser())

  const isAuthenticated = computed(() => token.value !== null)

  async function login(payload: LoginPayload) {
    const { data } = await api.post<AuthResponse>('/auth/login', payload)
    setSession(data)
  }

  async function signup(payload: SignupPayload) {
    const { data } = await api.post<AuthResponse>('/auth/signup', payload)
    setSession(data)
  }

  function setSession(data: AuthResponse) {
    token.value = data.token
    user.value = {
      id: data.id,
      tenantId: data.tenantId,
      fullName: data.fullName,
      email: data.email,
      role: data.role,
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isAuthenticated, login, signup, logout }
})
