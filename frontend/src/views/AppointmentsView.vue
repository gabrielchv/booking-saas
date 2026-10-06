<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import api, { apiErrorMessage } from '@/api/client'
import type { Appointment } from '@/api/types'

const appointments = ref<Appointment[]>([])
const loading = ref(true)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<Appointment[]>('/appointments')
    appointments.value = data
  } catch (e) {
    error.value = apiErrorMessage(e)
  } finally {
    loading.value = false
  }
}

async function setStatus(appointment: Appointment, status: string) {
  error.value = ''
  try {
    await api.patch(`/appointments/${appointment.id}/status`, { status })
    await load()
  } catch (e) {
    error.value = apiErrorMessage(e)
  }
}

function fmt(iso: string) {
  return new Date(iso).toLocaleString()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h1>Appointments</h1>
      <RouterLink to="/appointments/new"><button>New appointment</button></RouterLink>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="card">
      <p v-if="loading">Loading…</p>
      <p v-else-if="appointments.length === 0">No appointments yet.</p>
      <table v-else>
        <thead>
          <tr>
            <th>Customer</th>
            <th>Service</th>
            <th>Staff</th>
            <th>When</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in appointments" :key="a.id">
            <td>{{ a.customerName }}</td>
            <td>{{ a.serviceName }}</td>
            <td>{{ a.staffName }}</td>
            <td>{{ fmt(a.startAt) }}</td>
            <td><span class="badge" :class="a.status.toLowerCase()">{{ a.status }}</span></td>
            <td>
              <button v-if="a.status === 'SCHEDULED'" class="secondary" @click="setStatus(a, 'CONFIRMED')">Confirm</button>
              <button v-if="a.status === 'CONFIRMED'" class="secondary" @click="setStatus(a, 'COMPLETED')">Complete</button>
              <button v-if="a.status === 'SCHEDULED' || a.status === 'CONFIRMED'" class="danger" @click="setStatus(a, 'CANCELLED')">Cancel</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
