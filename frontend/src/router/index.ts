import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: () => import('../views/home/HomeView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/auth/LoginView.vue') },
  { path: '/me', name: 'me', component: () => import('../views/user/UserCenterView.vue'), meta: { requiresAuth: true } },
  { path: '/goods', name: 'goods-list', component: () => import('../views/goods/GoodsListView.vue') },
  { path: '/goods/:id', name: 'goods-detail', component: () => import('../views/goods/GoodsDetailView.vue') },
  { path: '/chat', name: 'chat', component: () => import('../views/chat/ChatView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'orders', component: () => import('../views/order/OrderView.vue'), meta: { requiresAuth: true } },
  { path: '/admin', name: 'admin', component: () => import('../views/admin/AdminDashboardView.vue'), meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }

  if (to.path === '/login' && authStore.isLoggedIn) {
    return '/me';
  }

  return true;
});

export default router;
