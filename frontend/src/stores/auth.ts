import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

export const AUTH_TOKEN_KEY = 'campus_trading_token';
const AUTH_USER_ID_KEY = 'campus_trading_user_id';
const AUTH_USERNAME_KEY = 'campus_trading_username';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(AUTH_TOKEN_KEY) || '');
  const userId = ref<number | null>(Number(localStorage.getItem(AUTH_USER_ID_KEY)) || null);
  const username = ref<string>(localStorage.getItem(AUTH_USERNAME_KEY) || '');

  const isLoggedIn = computed(() => !!token.value);

  function setAuth(nextToken: string, nextUserId: number, nextUsername: string) {
    token.value = nextToken;
    userId.value = nextUserId;
    username.value = nextUsername;
    localStorage.setItem(AUTH_TOKEN_KEY, nextToken);
    localStorage.setItem(AUTH_USER_ID_KEY, String(nextUserId));
    localStorage.setItem(AUTH_USERNAME_KEY, nextUsername);
  }

  function clearAuth() {
    token.value = '';
    userId.value = null;
    username.value = '';
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_ID_KEY);
    localStorage.removeItem(AUTH_USERNAME_KEY);
  }

  return { token, userId, username, isLoggedIn, setAuth, clearAuth };
});
