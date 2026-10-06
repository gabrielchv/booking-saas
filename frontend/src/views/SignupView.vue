<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { apiErrorMessage } from '@/api/client'

const auth = useAuthStore()
const router = useRouter()

const tenantName = ref('')
const fullName = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.signup({
      tenantName: tenantName.value,
      fullName: fullName.value,
      email: email.value,
      password: password.value,
    })
    router.push('/')
  } catch (e) {
    error.value = apiErrorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-card card">
    <h1>Create your tenant</h1>
    <form class="form" @submit.prevent="submit">
      <div class="field">
        <label for="tenantName">Business name</label>
        <input id="tenantName" v-model="tenantName" required />
      </div>
      <div class="field">
        <label for="fullName">Your name</label>
        <input id="fullName" v-model="fullName" required />
      </div>
      <div class="field">
        <label for="email">Email</label>
        <input id="email" v-model="email" type="email" required />
      </div>
      <div class="field">
        <label for="password">Password (min 8 chars)</label>
        <input id="password" v-model="password" type="password" minlength="8" required />
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="loading">{{ loading ? 'Creating…' : 'Create tenant' }}</button>
    </form>
    <p class="muted">Already have an account? <RouterLink to="/login">Sign in</RouterLink></p>
  </div>
</template>

<style scoped>
.auth-card {
  max-width: 440px;
  margin: 4rem auto;
}

h1 {
  margin-top: 0;
}

.muted {
  color: var(--muted);
  font-size: 0.9rem;
}
</style>
