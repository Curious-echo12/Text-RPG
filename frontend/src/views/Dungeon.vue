<!-- frontend/src/views/Dungeon.vue -->
<template>
    <div>
        <div class="game-card"><h3>副本</h3>
            <p style="color:var(--muted);font-size:12px">副本通关后有概率获得矿石和技能书奖励</p>
        </div>
        <div v-for="d in dungeons" :key="d.key" class="game-card">
            <div style="display:flex;justify-content:space-between;align-items:flex-start">
                <div style="flex:1">
                    <h3>{{ d.name }}</h3>
                    <p style="color:var(--muted);font-size:12px;margin-top:2px">{{ d.desc }}</p>
                    <p style="color:var(--muted);font-size:12px;margin-top:4px">
                        Lv.{{ d.levelReq }} | 体力:{{ d.energyCost }} | 剩余:{{ d.remainCount }}/{{ d.dailyLimit }}
                    </p>
                    <!-- Boss信息 -->
                    <p style="color:var(--accent);font-size:12px;margin-top:4px" v-if="d.boss">
                        Boss: {{ d.boss.name }} Lv.{{ d.boss.level }} HP:{{ d.boss.hp }} ATK:{{ d.boss.atk }}
                    </p>
                    <!-- 掉落概率 -->
                    <div v-if="d.drops && d.drops.length" style="display:flex;flex-wrap:wrap;gap:6px;margin-top:6px">
                        <el-tag v-for="drop in d.drops" :key="drop.key" size="small" type="warning">
                            {{ drop.name }} {{ drop.rate }}%
                        </el-tag>
                    </div>
                </div>
                <el-button type="primary" @click="confirmDungeon(d)" :disabled="d.remainCount<=0" style="flex-shrink:0">
                    {{ d.remainCount<=0?'今日已用完':'进入' }}
                </el-button>
            </div>
        </div>

        <el-divider />
        <div class="game-card">
            <h3>秘境探险</h3>
            <p style="color:var(--muted);font-size:13px">消耗10精力，随机遭遇事件（战斗、宝箱、奇遇、陷阱）</p>
            <el-button type="primary" style="margin-top:10px" @click="showSecretConfirm=true">探索秘境</el-button>
            <div v-if="sr" style="margin-top:12px">
                <p style="color:var(--accent)">{{ sr.eventType }}: {{ sr.desc }}</p>
                <p v-if="sr.msg" style="color:var(--green)">{{ sr.msg }}</p>
                <div v-if="sr.battle" class="battle-log" style="margin-top:8px;max-height:150px">
                    <div v-for="(l,i) in sr.battle.log" :key="i">{{ l }}</div>
                    <p :style="{color:sr.battle.win?'var(--green)':'var(--red)'}">
                        {{ sr.battle.win?'战斗胜利！':'战斗失败...' }}
                    </p>
                </div>
            </div>
        </div>

        <!-- 副本确认弹窗 -->
        <el-dialog v-model="showDungeonConfirm" title="确认进入副本" width="400px">
            <div v-if="dungeonTarget">
                <p>进入 <b style="color:var(--accent)">{{ dungeonTarget.name }}</b></p>
                <p style="color:var(--muted);font-size:13px;margin-top:4px">{{ dungeonTarget.desc }}</p>
                <p style="color:var(--muted);font-size:13px;margin-top:8px">消耗 {{ dungeonTarget.energyCost }} 点体力</p>
                <div v-if="dungeonTarget.drops && dungeonTarget.drops.length" style="margin-top:8px">
                    <p style="color:var(--muted);font-size:12px">通关奖励概率：</p>
                    <el-tag v-for="drop in dungeonTarget.drops" :key="drop.key" size="small" type="warning" style="margin:2px">
                        {{ drop.name }} {{ drop.rate }}%
                    </el-tag>
                </div>
            </div>
            <template #footer>
                <el-button @click="showDungeonConfirm=false">取消</el-button>
                <el-button type="primary" @click="doEnterDungeon">进入</el-button>
            </template>
        </el-dialog>

        <!-- 秘境确认弹窗 -->
        <el-dialog v-model="showSecretConfirm" title="确认探索秘境" width="360px">
            <p>消耗 <b>10</b> 点精力进入秘境</p>
            <p style="color:var(--muted);font-size:13px;margin-top:8px">可能遇到战斗、宝箱、奇遇或陷阱</p>
            <template #footer>
                <el-button @click="showSecretConfirm=false">取消</el-button>
                <el-button type="primary" @click="doExplore">探索</el-button>
            </template>
        </el-dialog>

        <!-- 副本战斗结果 -->
        <el-dialog v-model="showBattle" title="副本战斗" width="600px">
            <div class="battle-log">
                <div v-for="(l,i) in bd.log" :key="i">{{ l }}</div>
            </div>
            <div style="text-align:center;margin-top:16px">
                <h2 :style="{color:bd.win?'var(--green)':'var(--red)'}">
                    {{ bd.win?'胜利！':'战败' }}
                </h2>
                <p v-if="bd.win" style="color:var(--accent)">
                    经验+{{ bd.expReward }} 金币+{{ bd.goldReward }}
                </p>
                <p v-if="bd.dropMsg" style="color:var(--green);font-size:13px;margin-top:6px">{{ bd.dropMsg }}</p>
                <p style="color:var(--green);font-size:13px;margin-top:8px">生命已恢复满</p>
            </div>
            <template #footer><el-button type="primary" @click="showBattle=false">确定</el-button></template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'

const store = useUserStore()
const dungeons = ref([])
const showDungeonConfirm = ref(false)
const dungeonTarget = ref(null)
const showSecretConfirm = ref(false)
const showBattle = ref(false)
const bd = ref({log:[],win:false})
const sr = ref(null)

async function load() { const r = await api.get('/api/dungeon/list'); dungeons.value = r.data }
onMounted(load)

function confirmDungeon(d) {
    dungeonTarget.value = d
    showDungeonConfirm.value = true
}

async function doEnterDungeon() {
    if (!dungeonTarget.value) return
    showDungeonConfirm.value = false
    try {
        const r = await api.post('/api/dungeon/enter', { dungeonKey: dungeonTarget.value.key })
        bd.value = r.data
        showBattle.value = true
        store.fetchRole()
        load()
    } catch(e) {}
    dungeonTarget.value = null
}

async function doExplore() {
    showSecretConfirm.value = false
    try {
        const r = await api.post('/api/dungeon/secret')
        sr.value = r.data
        store.fetchRole()
    } catch(e) {}
}
</script>
