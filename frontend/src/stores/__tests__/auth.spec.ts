import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('starts unauthenticated with no stored token', () => {
    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.user).toBeNull()
  })

  it('restores a session from localStorage', () => {
    localStorage.setItem('token', 'abc')
    localStorage.setItem(
      'user',
      JSON.stringify({ id: 1, tenantId: 1, fullName: 'Ana', email: 'a@b.com', role: 'OWNER' }),
    )
    setActivePinia(createPinia())

    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(true)
    expect(auth.user?.role).toBe('OWNER')
  })

  it('logs out and clears storage', () => {
    localStorage.setItem('token', 'abc')
    setActivePinia(createPinia())

    const auth = useAuthStore()
    auth.logout()

    expect(auth.isAuthenticated).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })
})
