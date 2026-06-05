import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000
})

service.interceptors.request.use(
    config => {
        const userStr = localStorage.getItem('user')
        if (userStr) {
            try {
                const user = JSON.parse(userStr)
                if (user.id) {
                    config.headers['X-User-Id'] = user.id
                }
                if (user.username) {
                    config.headers['X-Username'] = user.username
                }
            } catch (e) {
                console.error('解析用户信息失败', e)
            }
        }
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
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            return res
        }
    },
    error => {
        ElMessage.error(error.message)
        return Promise.reject(error)
    }
)

export default service
