<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="admin-logo">🛡️ 管理后台</div>
      <nav class="admin-nav">
        <router-link to="/admin/players" class="nav-item" active-class="active">👥 玩家管理</router-link>
        <router-link to="/admin/announcements" class="nav-item" active-class="active">📢 公告管理</router-link>
        <router-link to="/admin/mail" class="nav-item" active-class="active">📧 发送邮件</router-link>
        <router-link to="/admin/config" class="nav-item" active-class="active">⚙️ 游戏配置</router-link>
      </nav>
      <div class="admin-footer">
        <span>{{ adminName }}</span>
        <el-button text size="small" @click="logout" style="color:var(--muted)">退出</el-button>
      </div>
    </aside>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>
<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const adminName = computed(() => localStorage.getItem('adminUsername') || '管理员')
function logout() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminUsername')
  router.push('/admin/login')
}
</script>
<style scoped>
.admin-layout { display: flex; min-height: 100vh; background: var(--bg); }
.admin-sidebar { width: 200px; background: var(--surface); border-right: 1px solid var(--border); display: flex; flex-direction: column; position: fixed; height: 100vh; }
.admin-logo { padding: 20px; font-size: 18px; font-weight: 700; color: var(--accent); text-align: center; border-bottom: 1px solid var(--border); }
.admin-nav { flex: 1; padding: 12px; }
.nav-item { display: block; padding: 12px 16px; border-radius: 8px; color: var(--muted); text-decoration: none; font-size: 14px; margin-bottom: 4px; transition: all 0.15s; }
.nav-item:hover { background: var(--surface2); color: var(--text); }
.nav-item.active { background: rgba(240,192,64,0.12); color: var(--accent); font-weight: 700; }
.admin-footer { padding: 12px 16px; border-top: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; font-size: 13px; color: var(--muted); }
.admin-main { flex: 1; margin-left: 200px; padding: 24px; max-width: 1200px; }
</style>
