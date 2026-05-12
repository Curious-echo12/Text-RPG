import { createRouter, createWebHistory } from 'vue-router'
const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  { path: '/create-role', component: () => import('../views/CreateRole.vue') },
  {
    path: '/', component: () => import('../views/GameLayout.vue'), redirect: '/home',
    children: [
      { path: 'home', component: () => import('../views/Home.vue') },
      { path: 'role', component: () => import('../views/Role.vue') },
      { path: 'skill', component: () => import('../views/Skill.vue') },
      { path: 'bag', component: () => import('../views/Bag.vue') },
      { path: 'mine', component: () => import('../views/Mine.vue') },
      { path: 'farm', component: () => import('../views/Farm.vue') },
      { path: 'forge', component: () => import('../views/Forge.vue') },
      { path: 'shop', component: () => import('../views/Shop.vue') },
      { path: 'pve', component: () => import('../views/Pve.vue') },
      { path: 'pvp', component: () => import('../views/Pvp.vue') },
      { path: 'dungeon', component: () => import('../views/Dungeon.vue') },
      { path: 'rank', component: () => import('../views/Rank.vue') },
      { path: 'task', component: () => import('../views/Task.vue') },
      { path: 'sign', component: () => import('../views/Sign.vue') },
      { path: 'mail', component: () => import('../views/Mail.vue') },
    ]
  },
  { path: '/admin/login', component: () => import('../views/admin/AdminLogin.vue') },
  {
    path: '/admin', component: () => import('../views/admin/AdminLayout.vue'), redirect: '/admin/players',
    children: [
      { path: 'players', component: () => import('../views/admin/PlayerManagement.vue') },
      { path: 'announcements', component: () => import('../views/admin/AnnouncementManagement.vue') },
      { path: 'mail', component: () => import('../views/admin/AdminMail.vue') },
      { path: 'config', component: () => import('../views/admin/GameConfig.vue') },
    ]
  }
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')
  if (to.path.startsWith('/admin')) {
    if (to.path === '/admin/login') next()
    else if (!adminToken) next('/admin/login')
    else next()
  } else if (to.path !== '/login' && to.path !== '/register') {
    if (!token) next('/login')
    else next()
  } else {
    next()
  }
})
export default router
