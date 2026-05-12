import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
const api = axios.create({ baseURL: '' })
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})
api.interceptors.response.use(
  res => { const d = res.data; if (d.code !== 200) { ElMessage.error(d.msg || '请求失败'); return Promise.reject(d) }; return d },
  err => { if (err.response?.status === 401 || err.response?.status === 403) { localStorage.removeItem('token'); router.push('/login') }; ElMessage.error(err.response?.data?.msg || '网络错误'); return Promise.reject(err) }
)
export default api
