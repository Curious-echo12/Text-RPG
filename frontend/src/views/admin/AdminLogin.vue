<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>🛡️ 管理后台</h1>
      <p class="subtitle">文字江湖管理系统</p>
      <el-form @submit.prevent>
        <el-form-item><el-input v-model="form.username" placeholder="管理员账号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" /></el-form-item>
        <el-button type="primary" size="large" style="width:100%" @click="handleLogin" :loading="loading">登录</el-button>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../utils/request'
import { ElMessage } from 'element-plus'
const router = useRouter(); const loading = ref(false)
const form = reactive({ username: '', password: '' })
async function handleLogin() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const res = await api.post('/api/admin/login', form)
    localStorage.setItem('adminToken', res.data.token)
    localStorage.setItem('adminUsername', res.data.username)
    router.push('/admin/players')
  } catch(e) {} finally { loading.value = false }
}
</script>
<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: radial-gradient(ellipse at 30% 50%, #1a1040 0%, transparent 60%), var(--bg); }
.auth-card { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 40px; width: 380px; text-align: center; }
.auth-card h1 { color: var(--accent); font-size: 28px; margin-bottom: 8px; }
.subtitle { color: var(--muted); margin-bottom: 30px; }
</style>
