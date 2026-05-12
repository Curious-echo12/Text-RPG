<template>
  <div class="auth-page">
    <div class="auth-card" style="width:500px">
      <h1>创建角色</h1>
      <p class="subtitle">选择职业开始冒险</p>
      <el-form-item label="昵称">
        <el-input v-model="name" placeholder="输入昵称（2-12个字符）" size="large" maxlength="12" show-word-limit />
      </el-form-item>
      <div class="job-grid">
        <div v-for="j in jobs" :key="j.key" :class="['job-card',{active:selected===j.key}]" @click="selected=j.key">
          <div class="job-icon">{{ j.icon }}</div>
          <div class="job-name">{{ j.name }}</div>
          <div class="job-desc">{{ j.desc }}</div>
        </div>
      </div>
      <el-button type="primary" size="large" style="width:100%;margin-top:20px" @click="create" :loading="loading">创建角色</el-button>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const router = useRouter(); const store = useUserStore()
const name = ref(''); const selected = ref('WARRIOR'); const loading = ref(false)
const jobs = [
  { key: 'WARRIOR', name: '战士', icon: '⚔️', desc: '高生命高攻击，近战输出' },
  { key: 'MAGE', name: '法师', icon: '🔮', desc: '超高魔法伤害，远程输出' },
  { key: 'ARCHER', name: '射手', icon: '🏹', desc: '高速度高暴击，远程输出' },
  { key: 'PRIEST', name: '牧师', icon: '✝️', desc: '治疗与防御，辅助型' },
  { key: 'MINER', name: '矿工', icon: '⛏️', desc: '挖矿经营加成，生活职业' },
]
async function create() {
  if (!name.value || name.value.trim().length < 2) { ElMessage.warning('昵称至少需要2个字符'); return }
  if (name.value.length > 12) { ElMessage.warning('昵称不能超过12个字符'); return }
  loading.value = true
  try {
    await api.post('/api/role/create', { name: name.value.trim(), job: selected.value })
    await store.fetchRole(); router.push('/home')
  } catch(e) {
    ElMessage.error(e.response?.data?.msg || '创建失败')
  } finally { loading.value = false }
}
</script>
<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: radial-gradient(ellipse at 30% 50%, #1a1040 0%, transparent 60%), var(--bg); }
.auth-card { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 40px; text-align: center; }
.auth-card h1 { color: var(--accent); margin-bottom: 8px; }
.subtitle { color: var(--muted); margin-bottom: 24px; }
.job-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; }
.job-card { background: var(--surface2); border: 2px solid var(--border); border-radius: 10px; padding: 14px 8px; cursor: pointer; transition: all 0.2s; }
.job-card:hover { border-color: var(--accent); }
.job-card.active { border-color: var(--accent); background: rgba(240,192,64,0.1); }
.job-icon { font-size: 28px; margin-bottom: 6px; }
.job-name { font-weight: 700; font-size: 14px; color: var(--accent); }
.job-desc { font-size: 11px; color: var(--muted); margin-top: 4px; }
</style>
