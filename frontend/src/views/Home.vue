<!-- frontend/src/views/Home.vue -->
<template>
    <div v-if="store.role">
        <!-- 公告模块 -->
        <div class="game-card" v-if="announcements.length">
            <h3>📢 公告</h3>
            <div v-for="(a,idx) in announcements" :key="idx" class="announce-item">
                <div style="display:flex;justify-content:space-between;align-items:center">
                    <h4 style="margin:0;color:var(--accent)">{{ a.title }}</h4>
                    <span style="color:var(--muted);font-size:12px;flex-shrink:0;margin-left:12px">{{ a.time }}</span>
                </div>
                <p style="color:var(--text-secondary);font-size:13px;margin:6px 0 0;line-height:1.8;white-space:pre-wrap">{{ a.content }}</p>
            </div>
        </div>

        <div class="game-card">
            <h3>欢迎来到文字江湖</h3>
            <p style="color:var(--muted);line-height:1.8">
                少侠 <b style="color:var(--accent)">{{ store.role.name }}</b>，
                当前等级 <b style="color:var(--green)">Lv.{{ store.role.level }}</b>，
                战力 <b style="color:var(--accent)">{{ store.role.fightPower }}</b>
            </p>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
            <div class="game-card">
                <h3>角色概况</h3>
                <div class="info-row"><span>职业</span><span>{{ jobName(store.role.job) }}</span></div>
                <div class="info-row"><span>等级</span><span>Lv.{{ store.role.level }}</span></div>
                <div class="info-row"><span>生命</span><span>{{ store.role.hp }}/{{ store.role.maxHp }}</span></div>
                <div class="info-row"><span>攻击</span><span style="color:var(--red)">{{ store.role.attack }}</span></div>
                <div class="info-row"><span>防御</span><span style="color:var(--blue)">{{ store.role.defense }}</span></div>
                <div class="info-row"><span>战力</span><span style="color:var(--accent);font-weight:700">{{ store.role.fightPower }}</span></div>
                <div class="info-row"><span>金币</span><span>🪙 {{ store.role.gold }}</span></div>
                <div class="info-row"><span>钻石</span><span style="color:var(--blue)">💎 {{ store.role.diamond }}</span></div>
            </div>
            <div class="game-card">
                <h3>快捷入口</h3>
                <div style="display:flex;flex-wrap:wrap;gap:8px">
                    <el-button v-for="q in quick" :key="q.path" @click="$router.push(q.path)" size="small">
                        {{ q.icon }} {{ q.name }}
                    </el-button>
                </div>
                <h3 style="margin-top:20px">今日状态</h3>
                <div class="info-row"><span>体力</span><span>⚡ {{ store.role.energy }}/{{ store.role.maxEnergy }}</span></div>
                <div class="info-row"><span>精力</span><span>🔮 {{ store.role.spirit }}/{{ store.role.maxSpirit }}</span></div>
                <div class="info-row"><span>技能点</span><span style="color:var(--accent)">{{ store.role.skillPoint }}</span></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'

const store = useUserStore()
const announcements = ref([])

function jobName(j) { return {WARRIOR:'战士',MAGE:'法师',ARCHER:'射手',PRIEST:'牧师',MINER:'矿工'}[j]||j }

const quick = [
    {path:'/pve',name:'冒险',icon:'⚔️'},{path:'/mine',name:'挖矿',icon:'⛏️'},
    {path:'/farm',name:'种菜',icon:'🌾'},{path:'/forge',name:'锻造',icon:'🔨'},
    {path:'/pvp',name:'竞技',icon:'🏟️'},{path:'/sign',name:'签到',icon:'✅'},
    {path:'/task',name:'任务',icon:'📋'},{path:'/dungeon',name:'副本',icon:'🏰'},
    {path:'/shop',name:'商店',icon:'🏪'}
]

async function loadAnnouncements() {
    try {
        const r = await api.get('/api/game/announcements')
        announcements.value = r.data.list || []
    } catch(e) {}
}

onMounted(loadAnnouncements)
</script>

<style scoped>
.info-row{display:flex;justify-content:space-between;padding:7px 0;border-bottom:1px solid var(--border);font-size:14px}
.info-row span:first-child{color:var(--muted)}
.announce-item{
    padding:12px;margin-bottom:10px;border-radius:8px;
    background:var(--surface2);border-left:3px solid var(--accent);
}
.announce-item:last-child{margin-bottom:0}
</style>
