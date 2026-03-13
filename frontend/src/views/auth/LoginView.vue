<template>
  <div class="page login-page">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>账号登录</h2>
          <p>校园二手交易平台</p>
        </div>
      </template>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item v-if="activeTab === 'register'" label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="可选，不填默认同用户名" />
        </el-form-item>

        <el-button type="primary" :loading="loading" class="submit-button" @click="onSubmit">
          {{ activeTab === 'login' ? '登录' : '注册并登录' }}
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { login, register } from '../../api/modules/auth';
import { useAuthStore } from '../../stores/auth';

type AuthTab = 'login' | 'register';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeTab = ref<AuthTab>('login');
const loading = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  username: '',
  password: '',
  nickname: ''
});

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度需在3到32之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度需在6到64之间', trigger: 'blur' }
  ],
  nickname: [{ max: 64, message: '昵称长度最多64', trigger: 'blur' }]
};

async function onSubmit() {
  if (!formRef.value) {
    return;
  }

  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }

  loading.value = true;
  try {
    const response =
      activeTab.value === 'login'
        ? await login({ username: form.username.trim(), password: form.password })
        : await register({ username: form.username.trim(), password: form.password, nickname: form.nickname.trim() });

    if (response.code !== 0 || !response.data?.token) {
      ElMessage.error(response.message || '操作失败');
      return;
    }

    authStore.setAuth(response.data.token, response.data.userId, response.data.username);
    ElMessage.success(activeTab.value === 'login' ? '登录成功' : '注册成功，已自动登录');
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/me';
    await router.push(redirect);
  } catch (error: any) {
    const message = error?.response?.data?.message || error?.message || '请求失败';
    ElMessage.error(message);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #f2f7ff 0%, #edf2ff 50%, #f7fafc 100%);
}

.login-card {
  width: 420px;
}

.card-header h2 {
  margin: 0;
}

.card-header p {
  margin: 8px 0 0;
  color: #6b7280;
}

.submit-button {
  width: 100%;
}
</style>
