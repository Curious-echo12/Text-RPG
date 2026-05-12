<!-- frontend/src/views/Forge.vue -->
<template>
    <div>
        <div class="game-card">
            <h3>锻造 — 等级: {{ store.role?.forgeLevel||1 }}</h3>
            <p style="color:var(--muted);font-size:12px">锻造等级越高，出高品质概率越大。同一件装备可重复锻造。</p>
        </div>

        <div v-for="r in recipes" :key="r.resultKey" class="game-card">
            <div style="display:flex;justify-content:space-between;align-items:flex-start">
                <div style="flex:1">
                    <h3 style="margin-bottom:8px">
                        {{ r.resultName }}（{{ partName(r.part) }}）
                    </h3>
                    <p style="font-size:12px;color:var(--muted);margin-bottom:8px">金币: {{ r.goldCost }}</p>

                    <!-- 材料 -->
                    <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:10px">
                        <div v-for="m in r.materials" :key="m.key"
                             style="display:flex;align-items:center;gap:4px;padding:4px 8px;border-radius:6px;font-size:12px"
                             :style="{background:m.enough?'rgba(74,222,128,0.1)':'rgba(248,113,113,0.1)',
                       border:'1px solid '+(m.enough?'var(--green)':'var(--red)')}">
                            <span>{{ m.name }}</span>
                            <span :style="{color:m.enough?'var(--green)':'var(--red)'}">{{ m.have }}/{{ m.need }}</span>
                        </div>
                    </div>

                    <!-- 品质概率 + 属性预览 -->
                    <div style="background:var(--surface2);border-radius:8px;padding:10px;font-size:12px">
                        <p style="color:var(--muted);margin-bottom:6px">品质概率与属性预览（锻造等级加成后）：</p>
                        <div v-for="(attr, q) in r.preview" :key="q" style="display:flex;align-items:center;gap:8px;padding:3px 0">
                            <span :style="{color:qualityColor(q),fontWeight:600,width:'50px'}">{{ qualityName(q) }}</span>
                            <span style="color:var(--muted);width:50px">{{ r.adjustedPercent?.[q] ?? 0 }}%</span>
                            <span style="color:var(--text-secondary)">
                攻击+{{ attr.atk }} 防御+{{ attr.def }} 生命+{{ attr.hp }}
              </span>
                        </div>
                    </div>

                    <!-- 出售返还说明 -->
                    <p style="color:var(--muted);font-size:11px;margin-top:6px">{{ r.sellRefundDesc }}</p>
                    <!-- 红色品质说明 -->
                    <p style="color:var(--red);font-size:11px;margin-top:4px" v-if="r.redDesc">{{ r.redDesc }}</p>
                </div>
                <div style="display:flex;flex-direction:column;gap:6px;margin-left:16px;flex-shrink:0">
                    <el-button type="primary" @click="confirmForge(r)" :disabled="!r.canForge">
                        {{ r.canForge ? '锻造' : '材料不足' }}
                    </el-button>
                </div>
            </div>
        </div>

        <!-- 已拥有装备（可出售返还材料） -->
        <el-divider />
        <div class="game-card">
            <h3>装备管理</h3>
            <p style="color:var(--muted);font-size:12px;margin-bottom:10px">背包中的锻造装备可出售返还50%矿石材料</p>
            <div v-for="item in forgableEquips" :key="item.id" class="item-row">
                <div style="flex:1">
                    <span :class="'quality-'+getQ(item)">{{ names[item.itemKey]||item.itemKey }}</span>
                    <span style="color:var(--accent);margin-left:6px">+{{ getSL(item) }}</span>
                    <span style="color:var(--muted);font-size:12px;margin-left:8px">
            {{ partName(getPart(item)) }}
            | 攻击+{{ getAttr(item,'atk') }}
            防御+{{ getAttr(item,'def') }}
            生命+{{ getAttr(item,'hp') }}
          </span>
                </div>
                <div style="display:flex;gap:6px">
                    <el-button size="small" type="primary" @click="doEquip(item.id)">穿戴</el-button>
                    <el-button size="small" type="warning" @click="confirmSellEquip(item)">出售返还材料</el-button>
                </div>
            </div>
            <p v-if="!forgableEquips.length" style="color:var(--muted);text-align:center;padding:10px">背包中没有锻造装备</p>
        </div>

        <!-- 强化 -->
        <el-divider />
        <div class="game-card">
            <h3>装备强化</h3>
            <p style="color:var(--muted);font-size:12px;margin-bottom:10px">+1~+10 失败不降级 | +11~+20 失败降1级 | 强化需消耗对应矿石</p>
            <div v-for="item in strengthenable" :key="item.id" class="item-row">
                <div style="flex:1">
                    <span :class="'quality-'+getQ(item)">{{ names[item.itemKey]||item.itemKey }}</span>
                    <span style="color:var(--accent);margin-left:8px">+{{ getSL(item) }}</span>
                    <template v-if="getSL(item)<20">
            <span style="color:var(--muted);font-size:12px;margin-left:8px">
              → 需要: 金币{{ (getSL(item)+1)*80 }} 强化石x{{ getSL(item)+1 }}
              {{ oreName(getSL(item)+1) }}x{{ getSL(item)+1 }} | 成功率: {{ rateStr(getSL(item)+1) }}%
            </span>
                    </template>
                </div>
                <el-button size="small" type="warning" @click="confirmStrengthen(item)" :disabled="getSL(item)>=20">
                    {{ getSL(item)>=20 ? '已满' : '强化' }}
                </el-button>
            </div>
            <p v-if="!strengthenable.length" style="color:var(--muted);text-align:center;padding:10px">背包中没有可强化的装备</p>
        </div>

        <el-dialog v-model="showForgeConfirm" title="确认锻造" width="400px">
            <div v-if="forgeTarget">
                <p>锻造 <b style="color:var(--accent)">{{ forgeTarget.resultName }}</b></p>
                <p>消耗金币: {{ forgeTarget.goldCost }}</p>
                <p v-for="m in forgeTarget.materials" :key="m.key">{{ m.name }} x{{ m.need }}</p>
            </div>
            <template #footer>
                <el-button @click="showForgeConfirm=false">取消</el-button>
                <el-button type="primary" @click="doForge">确认锻造</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="showStrengthenConfirm" title="确认强化" width="400px">
            <div v-if="strengthenTarget">
                <p>强化 <b>{{ names[strengthenTarget.itemKey]||strengthenTarget.itemKey }}</b>
                    +{{ getSL(strengthenTarget) }} → +{{ getSL(strengthenTarget)+1 }}</p>
                <div style="margin:10px 0;padding:10px;background:var(--surface2);border-radius:8px">
                    <p>成功率: <b style="color:var(--accent)">{{ rateStr(getSL(strengthenTarget)+1) }}%</b></p>
                    <p>消耗金币: {{ (getSL(strengthenTarget)+1)*80 }}</p>
                    <p>消耗强化石: {{ getSL(strengthenTarget)+1 }} 个</p>
                    <p>消耗{{ oreName(getSL(strengthenTarget)+1) }}: {{ getSL(strengthenTarget)+1 }} 个</p>
                </div>
            </div>
            <template #footer>
                <el-button @click="showStrengthenConfirm=false">取消</el-button>
                <el-button type="warning" @click="doStrengthen">确认强化</el-button>
            </template>
        </el-dialog>

        <!-- 出售装备确认 -->
        <el-dialog v-model="showSellEquipConfirm" title="出售装备" width="400px">
            <div v-if="sellEquipTarget">
                <p>出售 <b :class="'quality-'+getQ(sellEquipTarget)">{{ names[sellEquipTarget.itemKey]||sellEquipTarget.itemKey }}</b></p>
                <p style="color:var(--accent);margin-top:8px">将返还以下50%矿石材料：</p>
                <div v-if="getSellRefund(sellEquipTarget).length" style="margin-top:6px">
                    <p v-for="r in getSellRefund(sellEquipTarget)" :key="r.key" style="color:var(--green);font-size:13px">
                        {{ r.name }} x{{ r.count }}
                    </p>
                </div>
                <p v-else style="color:var(--muted);font-size:13px">无材料返还</p>
                <p style="color:var(--red);font-size:12px;margin-top:8px">注意：装备出售后无法恢复！</p>
            </div>
            <template #footer>
                <el-button @click="showSellEquipConfirm=false">取消</el-button>
                <el-button type="warning" @click="doSellEquip">确认出售</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const store = useUserStore()
const recipes = ref([])
const bagItems = ref([])
const showForgeConfirm = ref(false)
const forgeTarget = ref(null)
const showStrengthenConfirm = ref(false)
const strengthenTarget = ref(null)
const showSellEquipConfirm = ref(false)
const sellEquipTarget = ref(null)
const gameConfig = ref({})
const names = computed(() => {
    const n = { strengthen_stone:'强化石' }
    if (gameConfig.value.items) {
        for (const [k, v] of Object.entries(gameConfig.value.items)) n[k] = v.name
    }
    return n
})
// 锻造配方材料映射（从配置获取）
const recipeMaterials = computed(() => {
    const m = {}
    if (gameConfig.value.forgeRecipes) {
        for (const r of gameConfig.value.forgeRecipes) m[r.resultKey] = r.materials
    }
    return m
})
async function load() {
    const r1 = await api.get('/api/forge/recipes'); recipes.value = r1.data
    const r2 = await api.get('/api/bag/list'); bagItems.value = r2.data.items || []
    try { const c = await api.get('/api/game/config'); gameConfig.value = c.data || {} } catch(e) {}
}
onMounted(load)
const forgableEquips = computed(() => bagItems.value.filter(i => i.extraJson))
const strengthenable = computed(() => bagItems.value.filter(i => i.extraJson))
function getQ(i) { try { return JSON.parse(i.extraJson).quality } catch(e) { return 'WHITE' } }
function getSL(i) { try { return JSON.parse(i.extraJson).strengthenLevel || 0 } catch(e) { return 0 } }
function getPart(i) { try { return JSON.parse(i.extraJson).part } catch(e) { return '' } }
function getAttr(i, key) { try { const a = JSON.parse(JSON.parse(i.extraJson).baseAttr); return a[key] || 0 } catch(e) { return 0 } }
function partName(p) { return {WEAPON:'武器',HELMET:'头盔',ARMOR:'衣服',PANTS:'裤子',SHOES:'鞋子',NECKLACE:'项链',RING:'戒指'}[p]||p }
function oreName(lvl) { if(lvl<=5)return'铁矿石';if(lvl<=10)return'铜矿石';if(lvl<=15)return'银矿石';return'金矿石' }
function rateStr(lvl) { if(lvl<=5)return 90;if(lvl<=10)return 70;if(lvl<=15)return 40;return 20 }

function qualityColor(q) {
    return {WHITE:'#9ca3af',GREEN:'#4ade80',BLUE:'#60a5fa',PURPLE:'#a855f7',ORANGE:'#f59e0b',RED:'#ef4444'}[q]||'#9ca3af'
}
function qualityName(q) {
    return {WHITE:'白色',GREEN:'绿色',BLUE:'蓝色',PURPLE:'紫色',ORANGE:'橙色',RED:'红色'}[q]||q
}

// 计算出售返还
function getSellRefund(item) {
    const mats = recipeMaterials.value[item.itemKey]
    if (!mats) return []
    return Object.entries(mats).map(([key, count]) => ({
        key, name: names[key] || key, count: Math.floor(count / 2)
    })).filter(r => r.count > 0)
}

function confirmForge(recipe) { forgeTarget.value = recipe; showForgeConfirm.value = true }
async function doForge() {
    if(!forgeTarget.value) return
    try{const r=await api.post('/api/forge/do',{recipeKey:forgeTarget.value.resultKey});ElMessage.success(r.data.msg);store.fetchRole();load()}catch(e){}
    showForgeConfirm.value=false;forgeTarget.value=null
}
function confirmStrengthen(item) { strengthenTarget.value = item; showStrengthenConfirm.value = true }
async function doStrengthen() {
    if(!strengthenTarget.value) return
    try{const r=await api.post('/api/forge/strengthen',{equipId:strengthenTarget.value.id});ElMessage({message:r.data.msg,type:r.data.success?'success':'warning'});store.fetchRole();load()}catch(e){}
    showStrengthenConfirm.value=false;strengthenTarget.value=null
}
function confirmSellEquip(item) { sellEquipTarget.value = item; showSellEquipConfirm.value = true }
async function doSellEquip() {
    if(!sellEquipTarget.value) return
    try{const r=await api.post('/api/forge/sell',{bagId:sellEquipTarget.value.id});ElMessage.success(r.data.msg);store.fetchRole();load()}catch(e){}
    showSellEquipConfirm.value=false;sellEquipTarget.value=null
}
async function doEquip(id) { try { const r = await api.post('/api/bag/equip',{bagId:id}); ElMessage.success(r.data.msg); store.fetchRole(); load() } catch(e){} }
</script>
<style scoped>.item-row{display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border)}</style>
