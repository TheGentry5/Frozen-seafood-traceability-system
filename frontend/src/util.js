import axios from 'axios'

const TOKEN_KEY = 'fs_token'
const USER_KEY = 'fs_user'

/* ---------------- 轻量 toast ---------------- */
export function toast(msg, type = 'info') {
  const box = document.createElement('div')
  box.className = `toast toast-${type}`
  box.textContent = msg
  document.body.appendChild(box)
  setTimeout(() => box.classList.add('toast-hide'), 2000)
  setTimeout(() => box.remove(), 2400)
}

/* ---------------- 本地存储 ---------------- */
export function setAuth(token, user) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY)) || null
  } catch (e) {
    return null
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function isLogin() {
  return !!getToken()
}

/* ---------------- axios 实例 ---------------- */
const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers['X-Token'] = token
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data || {}
    if (res.code === 200) return res.data
    if (res.code === 401) {
      clearAuth()
      toast(res.msg || '登录已失效，请重新登录', 'warn')
      window.location.hash = '#/login'
      return Promise.reject(new Error(res.msg))
    }
    toast(res.msg || '请求失败', 'error')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    toast(error.message || '网络异常，请稍后再试', 'error')
    return Promise.reject(error)
  }
)

export default request
