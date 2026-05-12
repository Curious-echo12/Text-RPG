<template>
  <div>
    <div class="game-card"><h3>技能 — 技能点: {{ store.role?.skillPoint||0 }}</h3></div>
    <div v-for="s in skills" :key="s.key" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:flex-start">
        <div style="flex:1">
          <div>
            <span style="font-weight:700;color:var(--accent)">{{ s.name }}</span>
            <el-tag size="small" style="margin-left:8px">{{ s.type==='ACTIVE'?'主动':'被动' }}</el-tag>
            <el-tag size="small" type="warning" v-if="s.learned" style="margin-left:4px">Lv.{{ s.level }}</el-tag>
          </div>
          <p style="color:var(--muted);font-size:13px;margin-top:4px">{{ s.desc }}</p>
          <!-- 当前效果 -->
          <p v-if="s.learned" style="color:var(--green);font-size:12px;margin-top:4px">
            当前效果：{{ s.currentEffect }}
          </p>
          <!-- 下一级效果 -->
          <p v-if="s.learned && s.level < s.maxLevel" style="color:var(--accent);font-size:12px;margin-top:2px">
            下一级：{{ s.nextEffect }}
          </p>
        </div>
        <div style="display:flex;flex-direction:column;align-items:flex-end;gap:4px">
          <el-button v-if="!s.learned&&s.canLearn" size="small" type="primary" @click="learn(s.key)">
            学习 ({{ s.goldCost }}金)
          </el-button>
          <el-button v-if="s.learned&&s.level<s.maxLevel" size="small" type="primary" @click="upgrade(s.key)">
            升级 ({{ s.goldCost * s.level }}金)
          </el-button>
          <span v-if="!s.canLearn&&!s.learned" style="color:var(--muted);font-size:12px">Lv.{{ s.unlockLevel }}解锁</span>
          <span v-if="s.learned&&s.level>=s.maxLevel" style="color:var(--green);font-size:12px">已满级</span>
          <span v-if="s.learned" style="color:var(--muted);font-size:11px">需要技能点x1 + 技能书x{{ s.bookCost||0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const store = useUserStore(); const skills = ref([])
async function load() { const r = await api.get('/api/skill/list'); skills.value = r.data }
onMounted(load)
async function learn(k) { try { const r = await api.post('/api/skill/learn',{skillKey:k}); ElMessage.success(r.data.msg); store.fetchRole(); load() } catch(e){} }
async function upgrade(k) { try { const r = await api.post('/api/skill/upgrade',{skillKey:k}); ElMessage.success(r.data.msg); store.fetchRole(); load() } catch(e){} }
</script>
