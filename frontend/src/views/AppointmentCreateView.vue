<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api, { apiErrorMessage } from '@/api/client'
import type { Offering, User } from '@/api/types'

const router = useRouter()

const offerings = ref<Offering[]>([])
const staff = ref<User[]>([])
const serviceId = ref<number | null>(null)
const staffId = ref<number | null>(null)
const customerName = ref('')
const customerEmail = ref('')
const customerPhone = ref('')
const startAt = ref('')
const endAt = ref('')
const error = ref('')
const loading = ref(false)

async function loadOptions() {
  try {
    const [services, users] = await Promise.all([
      api.get<Offering[]>('/services'),
      api.get<User[]>('/users'),
    ])
    offerings.value = services.data
    staff.value = users.data
    if (users.data.length === 1) {
      staffId.value = users.data[0]!.id
    }
  } catch (e) {
    error.value = apiErrorMessage(e)
  }
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await api.post('/appointments', {
      serviceId: serviceId.value,
      staffId: staffId.value,
      customerName: customerName.value,
      customerEmail: customerEmail.value || undefined,
      customerPhone: customerPhone.value || undefined,
      startAt: new Date(startAt.value).toISOString(),
      endAt: new Date(endAt.value).toISOString(),
    })
    router.push('/')
  } catch (e) {
    error.value = apiErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadOptions)
</script>

<template>
  <div>
    <h1>New appointment</h1>
    <form class="form" @submit.prevent="submit">
      <div class="field">
        <label for="service">Service</label>
        <select id="service" v-model="serviceId" required>
          <option disabled :value="null">Select a service</option>
          <option v-for="o in offerings" :key="o.id" :value="o.id">
            {{ o.name }} ({{ o.durationMinutes }} min)
          </option>
        </select>
      </div>
      <div class="field">
        <label for="staff">Staff member</label>
        <select id="staff" v-model="staffId" required>
          <option disabled :value="null">Select staff</option>
          <option v-for="u in staff" :key="u.id" :value="u.id">{{ u.fullName }}</option>
        </select>
      </div>
      <div class="field">
        <label for="customerName">Customer name</label>
        <input id="customerName" v-model="customerName" required />
      </div>
      <div class="field">
        <label for="customerEmail">Customer email</label>
        <input id="customerEmail" v-model="customerEmail" type="email" />
      </div>
      <div class="field">
        <label for="customerPhone">Customer phone</label>
        <input id="customerPhone" v-model="customerPhone" />
      </div>
      <div class="field">
        <label for="startAt">Starts at</label>
        <input id="startAt" v-model="startAt" type="datetime-local" required />
      </div>
      <div class="field">
        <label for="endAt">Ends at</label>
        <input id="endAt" v-model="endAt" type="datetime-local" required />
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="loading || !serviceId || !staffId">
        {{ loading ? 'Booking…' : 'Book' }}
      </button>
    </form>
  </div>
</template>
