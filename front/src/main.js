import { createApp } from 'vue'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router'
import UserManager from './components/UserManager.vue'
import WorkManager from './components/WorkManager.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: WorkManager
  },
  {
    path: '/users',
    name: 'Users',
    component: UserManager
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(router)
app.mount('#app')
