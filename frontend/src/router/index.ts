import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('@/views/SignupView.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      name: 'appointments',
      component: () => import('@/views/AppointmentsView.vue'),
    },
    {
      path: '/appointments/new',
      name: 'appointment-new',
      component: () => import('@/views/AppointmentCreateView.vue'),
    },
    {
      path: '/services',
      name: 'services',
      component: () => import('@/views/ServicesView.vue'),
      meta: { roles: ['OWNER', 'ADMIN'] },
    },
    {
      path: '/team',
      name: 'team',
      component: () => import('@/views/StaffView.vue'),
      meta: { roles: ['OWNER', 'ADMIN'] },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.guest && auth.isAuthenticated) {
    return { name: 'appointments' }
  }
  if (!to.meta.guest && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  const roles = to.meta.roles as string[] | undefined
  if (roles && auth.user && !roles.includes(auth.user.role)) {
    return { name: 'appointments' }
  }
  return true
})

export default router
