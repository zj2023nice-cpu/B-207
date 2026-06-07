import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/index.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/',
        component: Layout,
        redirect: '/elderly',
        children: [
            {
                path: 'elderly',
                name: 'Elderly',
                component: () => import('../views/Elderly.vue'),
                meta: { title: '老人管理' }
            },
            {
                path: 'health',
                name: 'Health',
                component: () => import('../views/Health.vue'),
                meta: { title: '健康监测' }
            },
            {
                path: 'warning',
                name: 'Warning',
                component: () => import('../views/Warning.vue'),
                meta: { title: '健康预警' }
            },
            {
                path: 'threshold',
                name: 'Threshold',
                component: () => import('../views/Threshold.vue'),
                meta: { title: '阈值配置' }
            },
            {
                path: 'notification',
                name: 'Notification',
                component: () => import('../views/Notification.vue'),
                meta: { title: '通知消息' }
            },
            {
                path: 'elderly-tag',
                name: 'ElderlyTag',
                component: () => import('../views/ElderlyTag.vue'),
                meta: { title: '标签管理' }
            },
            {
                path: 'operation-log',
                name: 'OperationLog',
                component: () => import('../views/OperationLog.vue'),
                meta: { title: '操作日志' }
            },
            {
                path: 'nursing-observation',
                name: 'NursingObservation',
                component: () => import('../views/NursingObservation.vue'),
                meta: { title: '护理观察记录' }
            },
            {
                path: 'visitor-visit',
                name: 'VisitorVisit',
                component: () => import('../views/VisitorVisit.vue'),
                meta: { title: '访客来访登记' }
            },
            {
                path: 'shift-handover',
                name: 'ShiftHandover',
                component: () => import('../views/ShiftHandover.vue'),
                meta: { title: '交接班记录' }
            },
            {
                path: 'system-announcement',
                name: 'SystemAnnouncement',
                component: () => import('../views/SystemAnnouncement.vue'),
                meta: { title: '系统公告' }
            },
            {
                path: 'system-announcement-admin',
                name: 'SystemAnnouncementAdmin',
                component: () => import('../views/SystemAnnouncementAdmin.vue'),
                meta: { title: '公告管理' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userStr = localStorage.getItem('user')
    if (to.path !== '/login' && !userStr) {
        next('/login')
        return
    }
    if (to.path.startsWith('/system-announcement-admin')) {
        const user = JSON.parse(userStr)
        if (!user.role || user.role.toLowerCase() !== 'admin') {
            next('/system-announcement')
            return
        }
    }
    next()
})

export default router
