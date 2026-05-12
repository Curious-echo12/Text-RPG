<!-- frontend/src/views/Pve.vue -->
<template>
    <div>
        <div class="game-card">
            <h3>冒险 — 体力: {{ store.role?.energy }}/{{ store.role?.maxEnergy }}</h3>
        </div>
        <div v-for="ch in chapters" :key="ch.chapter" class="game-card">
            <h3 :style="{color:ch.unlocked?'var(--accent)':'var(--muted)'}">
                第{{ ch.chapter }}章 · {{ ch.name }}
                <span style="font-size:12px;margin-left:8px">需要Lv.{{ ch.levelReq }}</span>
            </h3>
            <div v-if="ch.unlocked" style="margin-top:10px">
                <div v-for="(s,idx) in ch.stages" :key="idx" class="stage-row">
                    <div>
                        <span style="font-weight:600">{{ s.name }}</span>
                        <span style="color:var(--muted);font-size:12px;margin-left:8px">
              Lv.{{ s.level }} | HP:{{ s.hp }} | ATK:{{ s.atk }} | 经验:{{ s.exp }} | 金币:{{ s.gold }}
            </span>
                    </div>
                    <el-button size="small" type="primary" @click="confirmFight(ch.chapter,idx,s)">
                        挑战
                    </el-button>
                </div>
            </div>
            <p v-else style="color:var(--muted);font-size:13px">🔒 等级不足</p>
        </div>

        <!-- 战斗确认弹窗 -->
        <el-dialog v-model="showConfirm" title="确认战斗" width="360px">
            <p>即将挑战 <b style="color:var(--accent)">{{ fightTarget?.name }}</b></p>
            <p style="color:var(--muted);font-size:13px;margin-top:8px">消耗 5 点体力</p>
            <template #footer>
                <el-button @click="showConfirm=false">取消</el-button>
                <el-button type="primary" @click="doFight">开战！</el-button>
            </template>
        </el-dialog>

        <!-- 战斗结果弹窗 -->
        <el-dialog v-model="showBattle" title="战斗结果" width="600px" :close-on-click-modal="false">
            <div class="battle-log">
                <div v-for="(l,i) in bd.log" :key="i">
                    <span :style="{color: getLineColor(l)}">{{ l }}</span>
                </div>
            </div>
            <div style="text-align:center;margin-top:16px">
                <h2 :style="{color:bd.win?'var(--green)':'var(--red)'}">
                    {{ bd.win?'🎉 胜利！':'💀 战败...' }}
                </h2>
                <p v-if="bd.win" style="color:var(--accent)">
                    经验 +{{ bd.expReward }} | 金币 +{{ bd.goldReward }}
                    <span v-if="bd.drops?.length"> | 掉落: {{ bd.drops.join(', ') }}</span>
                </p>
                <p style="color:var(--green);font-size:13px;margin-top:8px">⚔️ 战斗结束，生命已恢复满</p>
            </div>
            <template #footer><el-button type="primary" @click="showBattle=false">确定</el-button></template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
const store = useUserStore()
const chapters = ref([])
const showConfirm = ref(false)
const fightTarget = ref(null)
const fightChapter = ref(0)
const fightStage = ref(0)
const showBattle = ref(false)
const bd = ref({log:[],win:false})

async function load() { const r = await api.get('/api/pve/chapters'); chapters.value = r.data }
onMounted(load)

function confirmFight(chapter, stage, monster) {
    fightChapter.value = chapter
    fightStage.value = stage
    fightTarget.value = monster
    showConfirm.value = true
}

async function doFight() {
    showConfirm.value = false
    try {
        const r = await api.post('/api/pve/fight', {
            chapter: fightChapter.value,
            stage: fightStage.value
        })
        bd.value = r.data
        showBattle.value = true
        store.fetchRole()
    } catch(e) {}
}

function getLineColor(line) {
    if (line.startsWith('===')) return 'var(--accent)'
    if (line.includes('【你】')) return 'var(--green)'
    if (line.includes('恢复')) return 'var(--blue)'
    return 'var(--red)'
}
</script>

<style scoped>
.stage-row{display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border)}
</style>
