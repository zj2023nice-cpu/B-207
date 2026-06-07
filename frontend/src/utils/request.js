import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000,
    withCredentials: true
})

service.interceptors.request.use(
    config => {
        return config
    },
    error => {
        console.error('请求错误', error)
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || 'Error')
            if (res.message && (res.message.includes('未登录') || res.message.includes('登录已过期') || res.message.includes('会话已失效'))) {
                localStorage.removeItem('user')
                router.push('/login')
            }
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            return res
        }
    },
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('user')
            ElMessage.error('登录已过期，请重新登录')
            router.push('/login')
            return Promise.reject(error)
        }
        ElMessage.error(error.message)
        return Promise.reject(error)
    }
)

export default service
