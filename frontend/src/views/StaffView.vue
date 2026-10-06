<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api, { apiErrorMessage } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import type { Role, User } from '@/api/types'

const auth = useAuthStore()
const users = ref<User[]>([])
const loading = ref(true)
const error = ref('')

const fullName = ref('')
const email = ref('')
const password = ref('')
const role = ref<Role>('STAFF')
const saving = ref(false)

const isOwner = () => auth.user?.role === 'OWNER'

async function load() {
  loading.value = true
  try {
    const { data } = await api.get<User[]>('/users')
    users.value = data
  } catch (e) {
    error.value = apiErrorMessage(e)
  } finally {
    loading.value = false
  }
}

async function add() {
  error.value = ''
  saving.value = true
  try {
    await api.post('/users', {
      fullName: fullName.value,
      email: email.value,
      password: password.value,
      role: role.value,
    })
    fullName.value = ''
    email.value = ''
    password.value = ''
    role.value = 'STAFF'
    await load()
  } catch (e) {
    error.value = apiErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function changeRole(user: User, newRole: Role) {
  error.value = ''
  try {
    await api.patch(`/users/${user.id}/role`, { role: newRole })
    await load()
  } catch (e) {
    error.value = apiErrorMessage(e)
  }
}

async function remove(user: User) {
  error.value = ''
  try {
    await api.delete(`/users/${user.id}`)
    await load()
  } catch (e) {
    error.value = apiErrorMessage(e)
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h1>Team</h1>
    <p v-if="error" class="error">{{ error }}</p>

    <div class="card">
      <h2>Add member</h2>
      <form class="form" @submit.prevent="add">
        <div class="field">
          <label for="fullName">Name</label>
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
        <div class="field">
          <label for="role">Role</label>
          <select id="role" v-model="role">
            <option value="STAFF">Staff</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>
        <div>
          <button type="submit" :disabled="saving">{{ saving ? 'Adding…' : 'Add member' }}</button>
        </div>
      </form>
    </div>

    <div class="card" style="margin-top: 1.5rem">
      <p v-if="loading">Loading…</p>
      <table v-else>
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.fullName }}</td>
            <td>{{ u.email }}</td>
            <td><span class="badge">{{ u.role }}</span></td>
            <td>
              <template v-if="isOwner() && u.role !== 'OWNER'">
                <button v-if="u.role === 'STAFF'" class="secondary" @click="changeRole(u, 'ADMIN')">Make admin</button>
                <button v-if="u.role === 'ADMIN'" class="secondary" @click="changeRole(u, 'STAFF')">Make staff</button>
                <button class="danger" @click="remove(u)">Remove</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
