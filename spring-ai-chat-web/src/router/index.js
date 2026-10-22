import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ChatBotView from '../views/ChatBotView.vue'
import GameView from '../views/GameView.vue'
import ServiceView from '../views/ServiceView.vue'
import PdfView from '../views/PdfView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/chatbot', name: 'chatbot', component: ChatBotView },
    { path: '/game', name: 'game', component: GameView },
    { path: '/service', name: 'service', component: ServiceView },
    { path: '/pdf', name: 'pdf', component: PdfView }
  ],
  scrollBehavior: () => ({ top: 0 })
})

export default router
