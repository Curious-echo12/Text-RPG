<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>文字江湖</h1>
      <p class="subtitle">纯文字RPG冒险世界</p>

      <!-- 登录表单 -->
      <el-form v-if="!showReset" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名或玩家ID" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" @click="handleLogin" :loading="loading">登录</el-button>
        <div class="auth-link">
          <router-link to="/register">没有账号？去注册</router-link>
          <a href="#" @click.prevent="showReset=true" style="margin-left:20px;color:var(--accent)">忘记密码？</a>
        </div>
      </el-form>

      <!-- 重置密码表单 -->
      <el-form v-else @submit.prevent>
        <p style="color:var(--muted);font-size:13px;margin-bottom:16px">请输入用户名或玩家ID，系统将向绑定邮箱发送验证码</p>
        <el-form-item>
          <el-input v-model="resetForm.account" placeholder="用户名或玩家ID" size="large" />
        </el-form-item>
        <el-form-item>
          <div style="display:flex;gap:8px;width:100%">
            <el-input v-model="resetForm.code" placeholder="6位验证码" size="large" />
            <el-button @click="sendResetCode" :disabled="codeCooldown>0" :loading="sendingCode" size="large">
              {{ codeCooldown>0 ? codeCooldown+'s' : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-input v-model="resetForm.newPassword" type="password" placeholder="新密码（至少6位）" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" @click="handleResetPassword" :loading="loading">重置密码</el-button>
        <div class="auth-link">
          <a href="#" @click.prevent="showReset=false" style="color:var(--accent)">返回登录</a>
        </div>
      </el-form>
    </div>

    <!-- 绑定邮箱提示弹窗 -->
    <el-dialog v-model="showBindPrompt" title="绑定邮箱" width="420px" :close-on-click-modal="false">
      <div style="padding:10px">
        <p style="font-size:15px;margin-bottom:12px">检测到您尚未绑定邮箱</p>
        <p style="color:var(--muted);font-size:13px;line-height:1.8">
          绑定邮箱后可用于：<br/>
          • 忘记密码时通过邮箱验证码重置密码<br/>
          • 接收系统重要通知
        </p>
        <div style="margin-top:16px">
          <el-input v-model="bindEmailForm.email" placeholder="输入邮箱地址" size="large" />
        </div>
        <div style="display:flex;gap:8px;margin-top:10px">
          <el-input v-model="bindEmailForm.code" placeholder="输入验证码" size="large" />
          <el-button @click="sendBindCode" :disabled="bindCooldown>0" :loading="sendingBindCode">
            {{ bindCooldown>0 ? bindCooldown+'s' : '发送验证码' }}
          </el-button>
        </div>
      </div>
      <template #footer>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <el-checkbox v-model="noRemindToday">今日不再提醒</el-checkbox>
          <div>
            <el-button @click="skipBind">稍后再说</el-button>
            <el-button type="primary" @click="doBindEmail">确认绑定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const router = useRouter(); const store = useUserStore(); const loading = ref(false)
const form = reactive({ username: '', password: '' })
const showReset = ref(false)
const resetForm = reactive({ account: '', code: '', newPassword: '' })
const sendingCode = ref(false); const codeCooldown = ref(0)
const showBindPrompt = ref(false)
const bindEmailForm = reactive({ email: '', code: '' })
const sendingBindCode = ref(false); const bindCooldown = ref(0)
const noRemindToday = ref(false)

async function handleLogin() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const res = await api.post('/api/auth/login', { username: form.username.trim(), password: form.password })
    store.setLogin(res.data.token, res.data.username, res.data.playerId, res.data.email)
    await store.fetchRole()
    if (!store.role) { router.push('/create-role'); return }
    // 检查是否需要提示绑定邮箱
    const email = res.data.email
    const today = new Date().toDateString()
    const skipDate = localStorage.getItem('emailBindSkipDate')
    if (!email && skipDate !== today) {
      showBindPrompt.value = true
    }
    router.push('/home')
  } catch(e) {} finally { loading.value = false }
}

async function sendResetCode() {
  if (!resetForm.account) { ElMessage.warning('请输入用户名或玩家ID'); return }
  sendingCode.value = true
  try {
    await api.post('/api/email/sendResetCode', { account: resetForm.account })
    ElMessage.success('验证码已发送至绑定邮箱')
    codeCooldown.value = 60
    const timer = setInterval(() => { codeCooldown.value--; if (codeCooldown.value <= 0) clearInterval(timer) }, 1000)
  } catch(e) {} finally { sendingCode.value = false }
}

async function handleResetPassword() {
  if (!resetForm.account || !resetForm.code || !resetForm.newPassword) { ElMessage.warning('请填写完整信息'); return }
  if (resetForm.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  loading.value = true
  try {
    await api.post('/api/email/resetPassword', { account: resetForm.account, code: resetForm.code, newPassword: resetForm.newPassword })
    ElMessage.success('密码重置成功，请登录')
    showReset.value = false
    resetForm.account = ''; resetForm.code = ''; resetForm.newPassword = ''
  } catch(e) {} finally { loading.value = false }
}

async function sendBindCode() {
  if (!bindEmailForm.email) { ElMessage.warning('请输入邮箱地址'); return }
  sendingBindCode.value = true
  try {
    await api.post('/api/email/sendBindCode', { email: bindEmailForm.email })
    ElMessage.success('验证码已发送')
    bindCooldown.value = 60
    const timer = setInterval(() => { bindCooldown.value--; if (bindCooldown.value <= 0) clearInterval(timer) }, 1000)
  } catch(e) {} finally { sendingBindCode.value = false }
}

async function doBindEmail() {
  if (!bindEmailForm.email || !bindEmailForm.code) { ElMessage.warning('请填写邮箱和验证码'); return }
  try {
    await api.post('/api/email/bind', { email: bindEmailForm.email, code: bindEmailForm.code })
    ElMessage.success('邮箱绑定成功')
    localStorage.setItem('userEmail', bindEmailForm.email)
    showBindPrompt.value = false
  } catch(e) {}
}

function skipBind() {
  if (noRemindToday.value) {
    localStorage.setItem('emailBindSkipDate', new Date().toDateString())
  }
  showBindPrompt.value = false
}
</script>
<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: radial-gradient(ellipse at 30% 50%, #1a1040 0%, transparent 60%), var(--bg); }
.auth-card { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 40px; width: 400px; text-align: center; }
.auth-card h1 { color: var(--accent); font-size: 32px; margin-bottom: 8px; }
.subtitle { color: var(--muted); margin-bottom: 30px; }
.auth-link { margin-top: 16px; text-align: center; }
.auth-link a { color: var(--accent); text-decoration: none; }
</style>
