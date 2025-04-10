import { createRouter, createWebHistory } from 'vue-router'
import TransactionList from '../views/TransactionList.vue'

const routes = [
  {
    path: '/',
    name: 'TransactionList',
    component: TransactionList
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 