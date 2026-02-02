import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '../components/Layout/index.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/login/index.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../views/dashboard/index.vue'),
                meta: { title: '控制台', icon: 'DataLine' }
            },
            // 用户管理
            {
                path: 'user/list',
                name: 'UserList',
                component: () => import('../views/user/list.vue'),
                meta: { title: '用户列表', icon: 'User' }
            },
            // 内容管理
            {
                path: 'goods/list',
                name: 'GoodsList',
                component: () => import('../views/goods/list.vue'),
                meta: { title: '商品管理', icon: 'Goods' }
            },
            {
                path: 'post/list',
                name: 'PostList',
                component: () => import('../views/post/list.vue'),
                meta: { title: '广场动态', icon: 'ChatLineSquare' }
            },
            {
                path: 'lost-found/list',
                name: 'LostFoundList',
                component: () => import('../views/lostFound/list.vue'),
                meta: { title: '失物招领', icon: 'Search' }
            },
            {
                path: 'part-time/list',
                name: 'PartTimeList',
                component: () => import('../views/content/part-time/index.vue'),
                meta: { title: '兼职管理', icon: 'Suitcase' }
            },
            // 系统功能
            {
                path: 'system/report',
                name: 'ReportManage',
                component: () => import('../views/system/report/index.vue'),
                meta: { title: '举报管理', icon: 'Warning' }
            },
            {
                path: 'system/message',
                name: 'SystemMessage',
                component: () => import('../views/system/message/index.vue'),
                meta: { title: '系统消息', icon: 'Message' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('admin_token')
    if (to.path !== '/login' && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router
