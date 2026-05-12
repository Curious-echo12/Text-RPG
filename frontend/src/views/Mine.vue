<!-- frontend/src/views/Mine.vue -->
<template>
    <div>
        <div class="game-card">
            <h3>挖矿 — 矿工等级: {{ info.mineLevel||1 }}</h3>
            <p style="color:var(--muted);font-size:13px">
                熟练度: {{ info.mineExp||0 }}/{{ info.mineNextExp||100 }} | 体力: {{ store.role?.energy }}/{{ store.role?.maxEnergy }}
            </p>
            <div style="margin-top:6px;font-size:12px">
                <p style="color:var(--accent)">⛏️ 矿工等级加成：每5级额外+1个矿石产出，当前+{{ info.mineBonusCount||0 }}个</p>
                <p style="color:var(--green);margin-top:2px">矿工职业专属：每次挖矿额外+1个矿石产出</p>
                <p style="color:var(--muted);margin-top:2px">
                    矿工等级提升方式：每次挖矿获得10点熟练度。等级越高，每次产出矿石数量越多。
                </p>
            </div>
        </div>
        <div v-for="(mine,idx) in mines" :key="idx" class="game-card">
            <div style="display:flex;justify-content:space-between;align-items:flex-start">
                <div style="flex:1">
                    <h3 style="margin-bottom:6px">
                        {{ mine.name }}
                        <span style="color:var(--muted);font-size:12px;font-weight:400;margin-left:8px">
              冷却 {{ mine.cooldown }}秒
            </span>
                    </h3>
                    <p style="color:var(--muted);font-size:12px;margin-bottom:4px">{{ mine.desc }}</p>
                    <p style="color:var(--muted);font-size:12px;margin-bottom:8px">
                        消耗体力: {{ mine.energyCost }}
                    </p>
                    <!-- 矿石概率 -->
                    <div style="display:flex;flex-wrap:wrap;gap:6px">
            <span v-for="d in mine.drops" :key="d.key"
                  style="padding:3px 8px;border-radius:4px;font-size:11px;background:var(--surface2)">
              {{ d.name }}
              <span :style="{color: d.rate >= 20 ? 'var(--green)' : d.rate >= 10 ? 'var(--accent)' : 'var(--red)'}">
                {{ d.rate }}%
              </span>
            </span>
                    </div>
                </div>
                <el-button type="primary" @click="dig(idx)" :disabled="(mine.cooldownRemain||0)>0"
                           style="margin-left:16px;flex-shrink:0">
                    {{ (mine.cooldownRemain||0)>0?'冷却 '+mine.cooldownRemain+'s':'挖矿' }}
                </el-button>
            </div>
        </div>
        <div class="game-card" v-if="lastResult">
            <h3>挖矿结果</h3>
            <p style="color:var(--green)">{{ lastResult.msg }}</p>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
const store = useUserStore()
const info = ref({})
const mines = ref([])
const lastResult = ref(null)
let timer = null
async function loadInfo() {
    const res = await api.get('/api/mine/info')
    info.value = res.data
    mines.value = res.data.mines || []
}
onMounted(() => {
    loadInfo()
    timer = setInterval(() => {
        for (let m of mines.value) { if (m.cooldownRemain && m.cooldownRemain > 0) m.cooldownRemain-- }
    }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
async function dig(idx) {
    try { const res = await api.post('/api/mine/dig', { mineIndex: idx }); lastResult.value = res.data; store.fetchRole(); loadInfo() } catch(e) {}
}
</script>
