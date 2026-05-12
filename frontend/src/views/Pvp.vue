<!-- frontend/src/views/Pvp.vue -->
<template>
    <div>
        <div class="game-card">
            <h3>竞技场 — {{ jobName(store.role?.job) }}</h3>
            <div style="display:flex;gap:20px;margin-top:8px">
                <span>积分:<b style="color:var(--accent)">{{ myRank.score||store.role?.pvpScore||0 }}</b></span>
                <span>段位:<b style="color:var(--accent)">{{ myRank.rankName||'青铜' }}</b></span>
                <span>本职业排名:<b style="color:var(--accent)">第{{ myRank.rank||'?' }}名</b></span>
                <span>精力:{{ store.role?.spirit }}/{{ store.role?.maxSpirit }}</span>
            </div>
            <p style="color:var(--muted);font-size:12px;margin-top:6px">
                竞技场仅匹配同职业、PVP积分相差不超过15%的对手，保证公平竞技
            </p>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
            <div class="game-card">
                <h3>匹配对手</h3>
                <div v-if="opponent">
                    <p>
                        对手: <b>{{ opponent.opponentName }}</b>
                        <span style="color:var(--muted);font-size:12px">
              Lv.{{ opponent.opponentLevel }}
              {{ opponent.opponentJobName }}
              积分:{{ opponent.opponentScore||0 }}
            </span>
                    </p>
                    <div style="display:flex;gap:8px;margin-top:10px">
                        <el-button type="primary" @click="showPvpConfirm=true">开始战斗</el-button>
                        <el-button @click="match" :loading="matching">重新匹配</el-button>
                    </div>
                </div>
                <div v-else>
                    <el-button type="primary" @click="match" :loading="matching">匹配同职业对手</el-button>
                </div>
            </div>

            <div class="game-card">
                <h3>战斗记录</h3>
                <div v-for="r in records" :key="r.id" class="item-row" style="font-size:13px">
          <span :style="{color:r.result==='WIN'?'var(--green)':'var(--red)',width:'40px'}">
            {{ r.result==='WIN'?'胜':'败' }}
          </span>
                    <span style="flex:1">vs {{ r.defenderName }}</span>
                    <span :style="{color:r.scoreChange>0?'var(--green)':'var(--red)'}">
            {{ r.scoreChange>0?'+':'' }}{{ r.scoreChange }}分
          </span>
                    <span style="color:var(--muted);font-size:11px;width:130px;text-align:right">{{ r.createTime }}</span>
                </div>
                <p v-if="!records.length" style="color:var(--muted);text-align:center;padding:10px">暂无记录</p>
                <!-- 分页 -->
                <div v-if="recordTotal > recordSize" style="display:flex;justify-content:center;gap:12px;margin-top:10px;align-items:center">
                    <el-button size="small" :disabled="recordPage<=0" @click="recordPage--;loadRecords()">上一页</el-button>
                    <span style="color:var(--muted);font-size:12px">{{ recordPage+1 }} / {{ Math.ceil(recordTotal/recordSize) }}</span>
                    <el-button size="small" :disabled="(recordPage+1)*recordSize>=recordTotal" @click="recordPage++;loadRecords()">下一页</el-button>
                </div>
            </div>
        </div>

        <!-- PVP排行 -->
        <div class="game-card">
            <h3>竞技排行</h3>
            <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:12px">
                <el-button v-for="j in jobs" :key="j.key"
                           :type="pvpViewJob===j.key?'primary':''"
                           size="small" @click="switchPvpJob(j.key)">
                    {{ j.icon }} {{ j.name }}
                    <el-tag v-if="j.key===store.role?.job" size="small" type="warning" style="margin-left:2px">本职业</el-tag>
                </el-button>
            </div>
            <div v-for="r in pvpRankList" :key="r.rank" class="item-row">
        <span style="width:40px;text-align:center;font-weight:700"
              :style="{color: r.rank<=3 ? 'var(--accent)' : 'var(--muted)'}">
          {{ r.rank<=3 ? ['🥇','🥈','🥉'][r.rank-1] : '#'+r.rank }}
        </span>
                <span style="flex:1;font-weight:600">{{ r.name }}</span>
                <span style="color:var(--muted);font-size:12px;margin-right:12px">Lv.{{ r.level }}</span>
                <span style="color:var(--accent);font-weight:700;width:60px;text-align:right">{{ r.pvpScore }}</span>
                <span style="color:var(--muted);font-size:12px;margin-left:8px;width:50px">{{ r.pvpRank }}</span>
            </div>
            <div v-if="!pvpRankList.length" style="text-align:center;padding:30px">
                <p style="color:var(--muted);font-size:15px;margin-bottom:8px">
                    {{ jobName(pvpViewJob) }}暂无竞技排行数据
                </p>
            </div>
        </div>

        <el-dialog v-model="showPvpConfirm" title="确认竞技" width="360px">
            <p>即将挑战 <b style="color:var(--accent)">{{ opponent?.opponentName }}</b></p>
            <p style="color:var(--muted);font-size:13px;margin-top:4px">
                {{ opponent?.opponentJobName }} Lv.{{ opponent?.opponentLevel }} 积分:{{ opponent?.opponentScore||0 }}
            </p>
            <p style="color:var(--muted);font-size:13px;margin-top:8px">消耗 5 点精力</p>
            <template #footer>
                <el-button @click="showPvpConfirm=false">取消</el-button>
                <el-button type="primary" @click="doFight">开战！</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="showBattle" title="PVP战斗" width="600px">
            <div class="battle-log">
                <div v-for="(l,i) in bd.log" :key="i">{{ l }}</div>
            </div>
            <div style="text-align:center;margin-top:16px">
                <h2 :style="{color:bd.win?'var(--green)':'var(--red)'}">
                    {{ bd.win?'胜利！':'战败' }}
                </h2>
                <p>积分变化:<b :style="{color:bd.scoreChange>0?'var(--green)':'var(--red)'}">
                    {{ bd.scoreChange>0?'+':'' }}{{ bd.scoreChange }}
                </b> 段位:{{ bd.pvpRank }}</p>
                <p style="color:var(--green);font-size:13px;margin-top:8px">战斗后生命已恢复满</p>
            </div>
            <template #footer>
                <el-button @click="showBattle=false;match();loadAll()">再次挑战</el-button>
                <el-button type="primary" @click="showBattle=false;loadAll()">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const store = useUserStore()
const opponent = ref(null)
const matching = ref(false)
const records = ref([])
const recordTotal = ref(0)
const recordPage = ref(0)
const recordSize = 10
const pvpRankList = ref([])
const myRank = ref({})
const showPvpConfirm = ref(false)
const showBattle = ref(false)
const bd = ref({log:[],win:false})
const pvpViewJob = ref(store.role?.job || 'WARRIOR')

const jobs = [
    { key:'WARRIOR', name:'战士', icon:'🗡️' },
    { key:'MAGE',    name:'法师', icon:'🔮' },
    { key:'ARCHER',  name:'射手', icon:'🏹' },
    { key:'PRIEST',  name:'牧师', icon:'✝️' },
    { key:'MINER',   name:'矿工', icon:'⛏️' }
]

function jobName(j) { return {WARRIOR:'战士',MAGE:'法师',ARCHER:'射手',PRIEST:'牧师',MINER:'矿工'}[j]||j }

async function loadPvpRank() {
    try {
        const r = await api.get('/api/pvp/rank', { params: { size: 20, job: pvpViewJob.value } })
        pvpRankList.value = r.data.list || []
        myRank.value = r.data.myRank || {}
    } catch(e) {}
}

async function loadRecords() {
    try {
        const r = await api.get('/api/pvp/records', { params: { page: recordPage.value, size: recordSize } })
        records.value = r.data.list || []
        recordTotal.value = r.data.total || 0
    } catch(e) {}
}

async function loadAll() {
    await loadRecords()
    await loadPvpRank()
}
onMounted(loadAll)

function switchPvpJob(job) { pvpViewJob.value = job; loadPvpRank() }

async function match() {
    matching.value = true
    opponent.value = null
    try { const r = await api.post('/api/pvp/match'); opponent.value = r.data }
    catch(e) { ElMessage.error(e.response?.data?.msg || '匹配失败') }
    finally { matching.value = false }
}

async function doFight() {
    if(!opponent.value) return
    showPvpConfirm.value = false
    try {
        const r = await api.post('/api/pvp/fight', { opponentUserId: opponent.value.opponentUserId })
        bd.value = r.data
        showBattle.value = true
        opponent.value = null
        store.fetchRole()
    } catch(e) {}
}
</script>

<style scoped>
.item-row{display:flex;gap:12px;padding:8px 0;border-bottom:1px solid var(--border);font-size:14px;align-items:center}
</style>
