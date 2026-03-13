<template>
  <div class="page user-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-row">
          <div>
            <h2>User Center</h2>
            <p>Manage your profile after login.</p>
          </div>
          <div class="actions">
            <el-button @click="fetchProfile">Refresh</el-button>
            <el-button type="danger" plain @click="onLogout">Logout</el-button>
          </div>
        </div>
      </template>

      <div class="avatar-section">
        <el-avatar :size="88" :src="avatarPreviewUrl">
          {{ profile?.nickname?.charAt(0) || profile?.username?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="avatar-actions">
          <el-button :loading="uploadingAvatar" @click="pickAvatar">Upload Avatar</el-button>
          <span class="tip">Only jpg/jpeg/png/webp/gif, max 2MB</span>
        </div>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
          class="hidden-file-input"
          @change="onAvatarSelected"
        />
      </div>

      <el-descriptions :column="1" border class="summary" v-if="profile">
        <el-descriptions-item label="User ID">{{ profile.userId }}</el-descriptions-item>
        <el-descriptions-item label="Username">{{ profile.username }}</el-descriptions-item>
        <el-descriptions-item label="Status">{{ profile.status === 1 ? 'Active' : 'Disabled' }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="profile-form">
        <el-form-item label="Nickname" prop="nickname">
          <el-input v-model="form.nickname" placeholder="Input new nickname" />
        </el-form-item>
        <el-button type="primary" :loading="updating" @click="onUpdateNickname">Save Profile</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { uploadAvatar } from '../../api/modules/file';
import { getMyProfile, updateMyProfile, type UserProfileData } from '../../api/modules/user';
import { useAuthStore } from '../../stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const updating = ref(false);
const uploadingAvatar = ref(false);
const formRef = ref<FormInstance>();
const fileInputRef = ref<HTMLInputElement | null>(null);
const profile = ref<UserProfileData | null>(null);

const form = reactive({
  nickname: ''
});

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080').replace(/\/$/, '');

const avatarPreviewUrl = computed(() => {
  const avatarUrl = profile.value?.avatarUrl;
  if (!avatarUrl) {
    return '';
  }
  if (/^https?:\/\//.test(avatarUrl)) {
    return avatarUrl;
  }
  return `${apiBaseUrl}${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
});

const rules: FormRules = {
  nickname: [
    { required: true, message: 'Please input nickname', trigger: 'blur' },
    { max: 64, message: 'Nickname length must be <= 64', trigger: 'blur' }
  ]
};

async function fetchProfile() {
  loading.value = true;
  try {
    const response = await getMyProfile();
    if (response.code !== 0 || !response.data) {
      ElMessage.error(response.message || 'Failed to load profile');
      return;
    }
    profile.value = response.data;
    form.nickname = response.data.nickname || '';
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to load profile');
  } finally {
    loading.value = false;
  }
}

async function onUpdateNickname() {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }

  updating.value = true;
  try {
    const response = await updateMyProfile({
      nickname: form.nickname.trim(),
      avatarUrl: profile.value?.avatarUrl
    });
    if (response.code !== 0 || !response.data) {
      ElMessage.error(response.message || 'Failed to save profile');
      return;
    }

    profile.value = response.data;
    ElMessage.success('Profile updated');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Failed to save profile');
  } finally {
    updating.value = false;
  }
}

function pickAvatar() {
  fileInputRef.value?.click();
}

async function onAvatarSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file || !profile.value) {
    return;
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('Avatar must be <= 2MB');
    return;
  }

  uploadingAvatar.value = true;
  try {
    const uploadResponse = await uploadAvatar(file);
    if (uploadResponse.code !== 0 || !uploadResponse.data?.url) {
      ElMessage.error(uploadResponse.message || 'Upload failed');
      return;
    }

    const updateResponse = await updateMyProfile({
      nickname: form.nickname.trim() || profile.value.nickname,
      avatarUrl: uploadResponse.data.url
    });

    if (updateResponse.code !== 0 || !updateResponse.data) {
      ElMessage.error(updateResponse.message || 'Failed to update avatar');
      return;
    }

    profile.value = updateResponse.data;
    ElMessage.success('Avatar updated');
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || error?.message || 'Upload failed');
  } finally {
    uploadingAvatar.value = false;
  }
}

async function onLogout() {
  authStore.clearAuth();
  await router.push('/login');
}

onMounted(fetchProfile);
</script>

<style scoped>
.user-page {
  max-width: 760px;
}

.header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.header-row h2 {
  margin: 0;
}

.header-row p {
  margin: 8px 0 0;
  color: #6b7280;
}

.actions {
  display: flex;
  gap: 8px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tip {
  color: #6b7280;
  font-size: 12px;
}

.hidden-file-input {
  display: none;
}

.summary {
  margin-bottom: 20px;
}

.profile-form {
  max-width: 360px;
}
</style>