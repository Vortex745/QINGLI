import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 5000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 在这里可以统一加上 token
        const token = localStorage.getItem('admin_token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        // 这里根据后端返回的状态码进行处理
        if (res.code === 401) {
            ElMessage.error('登录状态已过期，请重新登录')
            localStorage.removeItem('admin_token')
            localStorage.removeItem('admin_user')
            // Add a small delay/check to avoid loop if already on login page
            if (window.location.hash.indexOf('#/login') === -1) {
                setTimeout(() => {
                    window.location.href = '#/login'
                }, 1000)
            }
            return Promise.reject(new Error('Unauthorized'))
        }
        if (res.code !== 200 && res.code !== 0) {
            ElMessage.error(res.msg || 'Error')
            return Promise.reject(new Error(res.msg || 'Error'))
        } else {
            return res.data
        }
    },
    error => {
        // Handle HTTP errors
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录认证失败，请重新登录')
            localStorage.removeItem('admin_token')
            localStorage.removeItem('admin_user')
            setTimeout(() => {
                window.location.href = '#/login'
            }, 1000)
        } else {
            ElMessage.error(error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default service
