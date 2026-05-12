<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>文字江湖</h1>
      <p class="subtitle">注册新账号</p>

      <div style="background:var(--surface2);border-radius:8px;padding:12px;margin-bottom:16px;text-align:left;font-size:12px;color:var(--muted);line-height:2">
        <p style="color:var(--accent);font-weight:700;margin-bottom:4px">注册要求：</p>
        <p>• 用户名：至少3个字符或汉字，最多20个字符</p>
        <p>• 密码：至少6位，且必须包含至少6位数字</p>
        <p>• 注册成功后将生成唯一6位玩家ID，可用于登录</p>
      </div>

      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirm" type="password" placeholder="确认密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" @click="handleRegister" :loading="loading">注册</el-button>
        <div class="auth-link"><router-link to="/login">已有账号？去登录</router-link></div>
      </el-form>

      <el-dialog v-model="showSuccess" title="注册成功" width="400px" :close-on-click-modal="false">
        <div style="text-align:center;padding:10px">
          <p style="font-size:16px;margin-bottom:12px">恭喜注册成功！</p>
          <p style="color:var(--muted);margin-bottom:8px">您的唯一玩家ID为：</p>
          <p style="font-size:28px;font-weight:700;color:var(--accent);letter-spacing:4px;margin:12px 0">{{ newPlayerId }}</p>
          <p style="color:var(--muted);font-size:12px">请牢记此ID，可用于登录游戏</p>
        </div>
        <template #footer>
          <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../utils/request'
const router = useRouter(); const loading = ref(false)
const showSuccess = ref(false); const newPlayerId = ref('')
const form = reactive({ username: '', password: '', confirm: '' })
async function handleRegister() {
  if (!form.username || form.username.trim().length < 3) { ElMessage.warning('用户名至少需要3个字符'); return }
  if (form.username.length > 20) { ElMessage.warning('用户名不能超过20个字符'); return }
  if (!form.password || form.password.length < 6) { ElMessage.warning('密码至少6位'); return }
  if (!form.password.match(/\d{6,}/)) { ElMessage.warning('密码至少需要包含6位数字'); return }
  if (form.password !== form.confirm) { ElMessage.error('两次密码不一致'); return }
  loading.value = true
  try {
    const res = await api.post('/api/auth/register', { username: form.username.trim(), password: form.password })
    newPlayerId.value = res.data.playerId || ''
    showSuccess.value = true
  } catch(e) {} finally { loading.value = false }
}
</script>
<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: radial-gradient(ellipse at 30% 50%, #1a1040 0%, transparent 60%), var(--bg); }
.auth-card { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 40px; width: 400px; text-align: center; }
.auth-card h1 { color: var(--accent); font-size: 32px; margin-bottom: 8px; }
.subtitle { color: var(--muted); margin-bottom: 30px; }
.auth-link { margin-top: 16px; }
.auth-link a { color: var(--accent); text-decoration: none; }
</style>
