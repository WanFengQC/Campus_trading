<template>
  <div class="page">
    <h1>Campus Trading</h1>
    <p>Project baseline is ready. You can enter user center after login.</p>

    <div class="action-row">
      <router-link v-if="!authStore.isLoggedIn" to="/login">
        <el-button type="primary">Login / Register</el-button>
      </router-link>

      <router-link v-else to="/me">
        <el-button type="primary">Go to User Center</el-button>
      </router-link>

      <el-button v-if="authStore.isLoggedIn" plain @click="onLogout">Logout</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';

const router = useRouter();
const authStore = useAuthStore();

async function onLogout() {
  authStore.clearAuth();
  await router.push('/login');
}
</script>

<style scoped>
.action-row {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
</style>
