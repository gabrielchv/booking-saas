<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppNav from '@/components/AppNav.vue'

const auth = useAuthStore()
</script>

<template>
  <AppNav v-if="auth.isAuthenticated" />
  <main>
    <RouterView />
  </main>
</template>
