// frontend/src/store/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(null)
  const recover = ref({ energyRecoverSec: -1, spiritRecoverSec: -1 })

  function setLogin(t, u, pid, em) {
    token.value = t
    username.value = u
    localStorage.setItem('token', t)
    localStorage.setItem('username', u)
    if (pid) localStorage.setItem('playerId', pid)
    if (em) localStorage.setItem('userEmail', em)
  }

  function logout() {
    token.value = ''
    username.value = ''
    role.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('playerId')
    localStorage.removeItem('userEmail')
  }

  async function fetchRole() {
    try {
      const res = await api.get('/api/role/info')
      if (res.code === 200 && res.data) {
        role.value = res.data.role || res.data
        if (res.data.recover) {
          recover.value = res.data.recover
        }
      }
    } catch (e) {}
  }

  return { token, username, role, recover, setLogin, logout, fetchRole }
})
