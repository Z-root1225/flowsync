import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import HomeView from '../views/HomeView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  // 捕获未定义路由重定向到首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由拦截导航守卫
router.beforeEach((to, from, next) => {
  const userStr = sessionStorage.getItem('user')
  const isLoggedIn = !!userStr

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (!to.meta.requiresAuth && isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    next('/')
  } else {
    next()
  }
})

export default router
