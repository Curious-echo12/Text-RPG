<!-- frontend/src/views/Rank.vue -->
<template>
    <div>
        <div class="game-card">
            <h3>🏆 排行榜</h3>
        </div>

        <!-- 职业切换 -->
        <div class="game-card" style="padding:12px">
            <div style="display:flex;gap:8px;flex-wrap:wrap">
                <el-button v-for="j in jobs" :key="j.key"
                           :type="currentJob===j.key?'primary':''"
                           size="small" @click="switchJob(j.key)">
                    {{ j.icon }} {{ j.name }}
                    <span style="font-size:11px;margin-left:4px;color:var(--muted)">
            ({{ jobCounts[j.key]||0 }}人)
          </span>
                    <el-tag v-if="j.key===myJob" size="small" type="warning" style="margin-left:4px">本职业</el-tag>
                </el-button>
            </div>
        </div>

        <!-- 排行类型切换 -->
        <div class="game-card" style="padding:12px">
            <div style="display:flex;gap:8px;flex-wrap:wrap">
                <el-button v-for="t in rankTypes" :key="t.key"
                           :type="currentType===t.key?'primary':''"
                           size="small" @click="switchType(t.key)">
                    {{ t.icon }} {{ t.name }}
                </el-button>
            </div>
        </div>

        <!-- 我的排名 -->
        <div class="game-card" v-if="myRank.rank" style="border-left:3px solid var(--accent)">
            <div style="display:flex;align-items:center;gap:16px">
                <span style="color:var(--accent);font-weight:700;font-size:18px">#{{ myRank.rank }}</span>
                <div>
                    <p style="font-weight:600">{{ myRank.name }}
                        <span style="color:var(--muted);font-size:12px;margin-left:6px">{{ myRank.jobName }}</span>
                    </p>
                </div>
                <span style="margin-left:auto;color:var(--accent);font-weight:700;font-size:18px">
          {{ myRank.value }}
        </span>
            </div>
            <p v-if="currentJob!==myJob" style="color:var(--muted);font-size:12px;margin-top:4px">
                以上为你的本职业排名，当前正在查看{{ jobName(currentJob) }}排行
            </p>
        </div>

        <!-- 排行列表 -->
        <div class="game-card">
            <h3 style="margin-bottom:12px">
                {{ jobName(currentJob) }} · {{ typeName(currentType) }}排行
                <span style="color:var(--muted);font-size:12px;font-weight:400;margin-left:8px">
          共{{ rankList.length }}人
        </span>
            </h3>
            <div v-for="r in rankList" :key="r.rank" class="rank-row">
        <span class="rank-num" :style="{color: r.rank<=3 ? 'var(--accent)' : 'var(--muted)'}">
          {{ r.rank<=3 ? ['🥇','🥈','🥉'][r.rank-1] : '#'+r.rank }}
        </span>
                <div style="flex:1">
                    <span style="font-weight:600">{{ r.name }}</span>
                    <span style="color:var(--muted);font-size:12px;margin-left:8px">Lv.{{ r.level }}</span>
                    <el-tag v-if="r.name===myRank.name && currentJob===myJob" size="small" type="warning" style="margin-left:8px">我</el-tag>
                </div>
                <span style="color:var(--accent);font-weight:700;font-size:16px">{{ r.value }}</span>
            </div>
            <div v-if="!rankList.length" style="text-align:center;padding:30px">
                <p style="color:var(--muted);font-size:15px;margin-bottom:8px">
                    {{ jobName(currentJob) }}暂无排行数据
                </p>
                <p style="color:var(--muted);font-size:12px">
                    当前该职业共有 {{ jobCounts[currentJob]||0 }} 名玩家
                    <span v-if="(jobCounts[currentJob]||0)===0">，还没有人选择{{ jobName(currentJob) }}职业</span>
                </p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'

const store = useUserStore()
const myJob = ref(store.role?.job || 'WARRIOR')
const currentJob = ref(store.role?.job || 'WARRIOR')
const currentType = ref('fight')
const rankList = ref([])
const myRank = ref({})
const jobCounts = ref({})

const jobs = [
    { key:'WARRIOR', name:'战士', icon:'🗡️' },
    { key:'MAGE',    name:'法师', icon:'🔮' },
    { key:'ARCHER',  name:'射手', icon:'🏹' },
    { key:'PRIEST',  name:'牧师', icon:'✝️' },
    { key:'MINER',   name:'矿工', icon:'⛏️' }
]

const rankTypes = [
    { key:'fight', name:'战力', icon:'⚔️' },
    { key:'level', name:'等级', icon:'📊' },
    { key:'mine',  name:'挖矿', icon:'⛏️' },
    { key:'farm',  name:'种植', icon:'🌾' },
    { key:'forge', name:'锻造', icon:'🔨' }
]

function jobName(j) { return {WARRIOR:'战士',MAGE:'法师',ARCHER:'射手',PRIEST:'牧师',MINER:'矿工'}[j]||j }
function typeName(t) { return {fight:'战力',level:'等级',mine:'挖矿',farm:'种植',forge:'锻造'}[t]||t }

async function loadRank() {
    try {
        const r = await api.get('/api/rank/list', {
            params: { type: currentType.value, job: currentJob.value, page: 0, size: 50 }
        })
        rankList.value = r.data.list || []
        myRank.value = r.data.myRank || {}
        if (r.data.jobCounts) jobCounts.value = r.data.jobCounts
    } catch(e) {
        console.error('排行加载失败', e)
    }
}

function switchJob(job) {
    currentJob.value = job
    loadRank()
}

function switchType(type) {
    currentType.value = type
    loadRank()
}

onMounted(loadRank)
</script>

<style scoped>
.rank-row {
    display:flex; align-items:center; gap:12px;
    padding:10px 0; border-bottom:1px solid var(--border);
}
.rank-num {
    width:40px; text-align:center; font-weight:700; font-size:15px;
}
</style>
