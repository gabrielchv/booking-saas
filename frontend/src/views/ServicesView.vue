<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api, { apiErrorMessage } from '@/api/client'
import type { Offering, OfferingPayload } from '@/api/types'

const offerings = ref<Offering[]>([])
const loading = ref(true)
const error = ref('')

const editingId = ref<number | null>(null)
const name = ref('')
const description = ref('')
const durationMinutes = ref(30)
const price = ref(0)
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get<Offering[]>('/services')
    offerings.value = data
  } catch (e) {
    error.value = apiErrorMessage(e)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  name.value = ''
  description.value = ''
  durationMinutes.value = 30
  price.value = 0
}

function edit(offering: Offering) {
  editingId.value = offering.id
  name.value = offering.name
  description.value = offering.description ?? ''
  durationMinutes.value = offering.durationMinutes
  price.value = offering.price
}

async function save() {
  error.value = ''
  saving.value = true
  const payload: OfferingPayload = {
    name: name.value,
    description: description.value || null,
    durationMinutes: durationMinutes.value,
    price: price.value,
  }
  try {
    if (editingId.value) {
      await api.put(`/services/${editingId.value}`, payload)
    } else {
      await api.post('/services', payload)
    }
    resetForm()
    await load()
  } catch (e) {
    error.value = apiErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function remove(offering: Offering) {
  error.value = ''
  try {
    await api.delete(`/services/${offering.id}`)
    await load()
  } catch (e) {
    error.value = apiErrorMessage(e)
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h1>Services</h1>
    <p v-if="error" class="error">{{ error }}</p>

    <div class="card">
      <h2>{{ editingId ? 'Edit service' : 'Add service' }}</h2>
      <form class="form" @submit.prevent="save">
        <div class="field">
          <label for="name">Name</label>
          <input id="name" v-model="name" required />
        </div>
        <div class="field">
          <label for="description">Description</label>
          <textarea id="description" v-model="description" rows="2"></textarea>
        </div>
        <div class="field">
          <label for="duration">Duration (minutes)</label>
          <input id="duration" v-model.number="durationMinutes" type="number" min="1" required />
        </div>
        <div class="field">
          <label for="price">Price</label>
          <input id="price" v-model.number="price" type="number" min="0" step="0.01" required />
        </div>
        <div>
          <button type="submit" :disabled="saving">{{ saving ? 'Saving…' : 'Save' }}</button>
          <button v-if="editingId" type="button" class="secondary" @click="resetForm">Cancel</button>
        </div>
      </form>
    </div>

    <div class="card" style="margin-top: 1.5rem">
      <p v-if="loading">Loading…</p>
      <p v-else-if="offerings.length === 0">No services yet.</p>
      <table v-else>
        <thead>
          <tr>
            <th>Name</th>
            <th>Duration</th>
            <th>Price</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in offerings" :key="o.id">
            <td>{{ o.name }}</td>
            <td>{{ o.durationMinutes }} min</td>
            <td>{{ o.price }}</td>
            <td>
              <button class="secondary" @click="edit(o)">Edit</button>
              <button class="danger" @click="remove(o)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
