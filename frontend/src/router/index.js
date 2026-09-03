import { createRouter, createWebHashHistory } from 'vue-router'
import MODULES, { homePathOf } from '../modules'
import { getUser, isLogin } from '../util'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },

  // 消费者端（公开）
  { path: '/consumer', name: 'consumer', component: () => import('../views/consumer/ConsumerHome.vue') },
  { path: '/consumer/trace', name: 'consumer-trace', component: () => import('../views/consumer/ConsumerTrace.vue') },

  // 管理端
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { admin: true },
    children: [
      { path: 'node', name: 'admin-node', component: () => import('../views/admin/AdminNode.vue') },
      { path: 'stats', name: 'admin-stats', component: () => import('../views/admin/AdminStats.vue') }
    ]
  },

  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

// 四类流通企业：复用 NodeLayout 与通用页面组件，差异取 meta.module 配置
Object.values(MODULES).forEach((mod) => {
  const children = [
    { path: 'home', component: () => import('../views/node/NodeHome.vue') },
    { path: 'batch/create', component: () => import('../views/node/BatchForm.vue'), props: { mode: 'create' } },
    { path: 'batch/list', component: () => import('../views/node/BatchList.vue') },
    { path: 'batch/detail/:id', component: () => import('../views/node/BatchDetail.vue') },
    { path: 'batch/edit/:id', component: () => import('../views/node/BatchForm.vue'), props: { mode: 'edit' } }
  ]
  if (mod.hasConfirm) {
    children.push({ path: 'confirm', component: () => import('../views/node/ConfirmBatch.vue') })
  }
  routes.push({
    path: '/' + mod.key,
    component: () => import('../views/node/NodeLayout.vue'),
    meta: { module: mod.key, requiresAuth: true, nodeTypes: [mod.nodeType] },
    children
  })
})

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  // 公开页面：登录/消费者
  const publicPaths = ['/login', '/consumer', '/consumer/trace']
  if (publicPaths.some((p) => to.path.startsWith(p))) return true

  if (!isLogin()) return '/login'

  const user = getUser()
  if (!user) return '/login'

  // 管理端：仅管理员
  if (to.matched.some((r) => r.meta.admin)) {
    return user.type === 'ADMIN' ? true : homePathOf(user)
  }

  // 节点端：校验角色
  const nodeRoute = to.matched.find((r) => r.meta.nodeTypes)
  if (nodeRoute) {
    if (user.type === 'ADMIN') return '/admin/node'
    const allowed = (nodeRoute.meta.nodeTypes || []).includes(user.nodeType)
    return allowed ? true : homePathOf(user)
  }

  return homePathOf(user)
})

export default router
