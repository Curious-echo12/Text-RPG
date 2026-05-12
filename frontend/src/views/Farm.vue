<template>
  <div>
    <div class="game-card"><h3>种菜 — 种植等级: {{ store.role?.farmLevel||1 }}</h3>
      <p style="color:var(--muted);font-size:12px">
        种植等级越高，收获数量越多（每3级+1个）。随等级提升可解锁更多菜地（最多9块）。
      </p>
      <p style="color:var(--accent);font-size:12px;margin-top:4px" v-if="farmInfo.nextUnlock">
        {{ farmInfo.nextUnlock }}
      </p>
      <p style="color:var(--green);font-size:12px;margin-top:2px" v-if="farmInfo.farmBonus > 0">
        当前种植加成：每次收获额外 +{{ farmInfo.farmBonus }} 个
      </p>
    </div>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px">
      <div v-for="p in plots" :key="p.index" class="game-card" style="text-align:center">
        <div style="font-size:32px;margin:10px 0">{{ p.status==='EMPTY'?'🟫':p.status==='READY'?'🌾':'🌱' }}</div>
        <p style="color:var(--muted);font-size:13px">菜地 #{{ p.index+1 }}</p>
        <p v-if="p.cropName" style="color:var(--accent);font-size:12px">{{ p.cropName }}</p>
        <p v-if="p.status==='GROWING' && p.remainSec > 0" style="color:var(--accent);font-size:12px">
          剩余 {{ fmtTime(p.remainSec) }}
        </p>
        <p v-if="p.status==='GROWING' && (!p.remainSec || p.remainSec <= 0)" style="color:var(--green);font-size:12px">
          即将成熟...
        </p>
        <p v-if="p.status==='READY'" style="color:var(--green)">已成熟！</p>
        <div style="margin-top:10px">
          <el-select v-if="p.status==='EMPTY'" v-model="p.selCrop" size="small" style="width:140px">
            <el-option v-for="c in crops" :key="c.cropKey" :label="c.name+'('+c.growMinutes+'分钟)'" :value="c.cropKey" />
          </el-select>
          <el-button v-if="p.status==='EMPTY'" size="small" type="primary" @click="plant(p.index, p.selCrop)">种植</el-button>
          <el-button v-if="p.status==='READY'" size="small" type="success" @click="harvest(p.index)">收获</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const store = useUserStore()
const plots = ref([])
const crops = ref([])
const farmInfo = ref({})
let timer = null

async function load() {
  const r = await api.get('/api/farm/plots')
  plots.value = (r.data || []).map(p => ({ ...p, selCrop: p.selCrop || 'wheat' }))
  try {
    const c = await api.get('/api/farm/crops')
    crops.value = c.data.crops || []
    farmInfo.value = c.data
  } catch(e) {}
}

onMounted(() => {
  load()
  // 每秒更新剩余时间
  timer = setInterval(() => {
    for (let p of plots.value) {
      if (p.status === 'GROWING' && p.remainSec > 0) {
        p.remainSec--
        if (p.remainSec <= 0) {
          p.status = 'READY'
        }
      }
    }
  }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })

async function plant(i, cropKey) {
  try { await api.post('/api/farm/plant', {plotIndex: i, cropKey: cropKey || 'wheat'}); ElMessage.success('种植成功'); load() } catch(e){}
}
async function harvest(i) { try { const r = await api.post('/api/farm/harvest', {plotIndex: i}); ElMessage.success(r.data.msg); store.fetchRole(); load() } catch(e){} }
function fmtTime(s) { if(!s || s<=0) return '成熟中'; const m=Math.floor(s/60); const sec=s%60; return m>0?`${m}分${sec}秒`:`${sec}秒` }
</script>
