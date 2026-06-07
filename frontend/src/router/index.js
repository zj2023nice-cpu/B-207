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
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../views/Dashboard.vue'),
                meta: { title: '总览' }
            },
            {
                path: 'workbench',
                name: 'Workbench',
                component: () => import('../views/Workbench.vue'),
                meta: { title: '待办工作台' }
            },
            {
                path: 'elderly',
                name: 'Elderly',
                component: () => import('../views/Elderly.vue'),
                meta: { title: '老人管理' }
            },
            {
                path: 'elderly-follow',
                name: 'ElderlyFollow',
                component: () => import('../views/ElderlyFollow.vue'),
                meta: { title: '重点关注' }
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
                path: 'notification-subscription',
                name: 'NotificationSubscription',
                component: () => import('../views/NotificationSubscription.vue'),
                meta: { title: '订阅规则' }
            },
            {
                path: 'notification-preference',
                name: 'NotificationPreference',
                component: () => import('../views/NotificationPreference.vue'),
                meta: { title: '消息偏好' }
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
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/Profile.vue'),
                meta: { title: '个人资料与安全设置' }
            },
            {
                path: 'export-task',
                name: 'ExportTask',
                component: () => import('../views/ExportTask.vue'),
                meta: { title: '数据导出任务中心' }
            },
            {
                path: 'search',
                name: 'Search',
                component: () => import('../views/Search.vue'),
                meta: { title: '全局搜索' }
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
    next()
})

export default router
