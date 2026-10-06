<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <header class="nav">
    <nav>
      <RouterLink to="/">Appointments</RouterLink>
      <RouterLink v-if="auth.user && auth.user.role !== 'STAFF'" to="/services">Services</RouterLink>
      <RouterLink v-if="auth.user && auth.user.role !== 'STAFF'" to="/team">Team</RouterLink>
    </nav>
    <div class="right">
      <span class="who" v-if="auth.user">{{ auth.user.fullName }} · {{ auth.user.role }}</span>
      <button class="secondary" @click="logout">Log out</button>
    </div>
  </header>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.5rem;
  background: var(--card);
  border-bottom: 1px solid var(--border);
}

nav {
  display: flex;
  gap: 1.25rem;
}

nav a {
  color: var(--text);
  font-weight: 500;
}

nav a.router-link-exact-active {
  color: var(--primary);
}

.right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.who {
  color: var(--muted);
  font-size: 0.9rem;
}
</style>
