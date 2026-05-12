<template>
    <div>
        <div class="game-card"><h3>背包</h3></div>

        <div class="game-card" v-if="equips.length">
            <h3>已穿戴装备</h3>
            <div v-for="e in equips" :key="e.id" class="item-row">
                <div style="flex:1">
                    <span :class="'quality-'+e.quality">{{ e.itemName||names[e.itemKey]||e.itemKey }}</span>
                    <span style="color:var(--accent);margin-left:6px">+{{ e.strengthenLevel }}</span>
                    <span style="color:var(--muted);font-size:12px;margin-left:8px">
            {{ partName(e.part) }}
            <template v-if="parseAttr(e.baseAttrJson)">
              | 攻击+{{ parseAttr(e.baseAttrJson).atk }}
              防御+{{ parseAttr(e.baseAttrJson).def }}
              生命+{{ parseAttr(e.baseAttrJson).hp }}
            </template>
          </span>
                </div>
                <el-button size="small" type="warning" @click="doUnequip(e.id)">卸下</el-button>
            </div>
        </div>

        <div class="game-card">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
                <h3 style="margin:0">物品列表</h3>
                <div style="display:flex;gap:8px;align-items:center">
                    <el-select v-model="filterType" size="small" style="width:120px">
                        <el-option label="全部" value="ALL" />
                        <el-option label="装备" value="EQUIPMENT" />
                        <el-option label="消耗品" value="CONSUMABLE" />
                        <el-option label="材料" value="MATERIAL" />
                    </el-select>
                    <span style="color:var(--muted);font-size:12px">共 {{ filteredItems.length }} 件</span>
                </div>
            </div>
            <div v-for="item in pagedItems" :key="item.id" class="item-row">
                <div style="flex:1">
                    <span :class="item.extraJson?'quality-'+getQ(item):''">{{ item.itemName||names[item.itemKey]||item.itemKey }}</span>
                    <span v-if="!item.extraJson" style="color:var(--muted);margin-left:8px">x{{ item.count }}</span>
                    <template v-if="item.extraJson">
                        <span style="color:var(--accent);margin-left:6px">+{{ getSL(item) }}</span>
                        <span style="color:var(--muted);font-size:12px;margin-left:8px">
              {{ partName(getPart(item)) }}
              | 攻击+{{ getAttr(item,'atk') }}
              防御+{{ getAttr(item,'def') }}
              生命+{{ getAttr(item,'hp') }}
            </span>
                    </template>
                    <p style="color:var(--muted);font-size:11px;margin-top:2px">{{ item.itemDesc||getDesc(item.itemKey) }}</p>
                </div>
                <div style="display:flex;gap:6px;align-items:center">
                    <el-button v-if="isUsable(item)" size="small" @click="confirmUse(item)">使用</el-button>
                    <el-button v-if="item.extraJson" size="small" type="primary" @click="doEquip(item.id)">穿戴</el-button>
                    <template v-if="item.extraJson">
                        <el-button size="small" type="danger" @click="confirmSellEquipment(item)">
                            出售({{ getSellPrice(item) }}金)
                        </el-button>
                    </template>
                    <template v-else>
                        <el-input-number v-model="item.sellCount" :min="1" :max="item.count" size="small" style="width:80px" />
                        <el-button size="small" type="danger" @click="confirmSell(item)">
                            出售({{ getSellPrice(item) * (item.sellCount||1) }}金)
                        </el-button>
                    </template>
                </div>
            </div>
            <p v-if="!filteredItems.length" style="color:var(--muted);text-align:center;padding:20px">暂无物品</p>
            <!-- 分页 -->
            <div v-if="totalPages > 1" style="display:flex;justify-content:center;align-items:center;gap:12px;margin-top:16px">
                <el-button size="small" :disabled="currentPage<=1" @click="currentPage--">上一页</el-button>
                <span style="color:var(--muted);font-size:12px">{{ currentPage }} / {{ totalPages }}</span>
                <el-button size="small" :disabled="currentPage>=totalPages" @click="currentPage++">下一页</el-button>
            </div>
        </div>

        <!-- 使用物品弹窗 -->
        <el-dialog v-model="showUseConfirm" title="使用物品" width="400px">
            <div v-if="useTarget">
                <p>物品：<b>{{ useTarget.itemName||names[useTarget.itemKey]||useTarget.itemKey }}</b></p>
                <p style="color:var(--muted);font-size:12px;margin-top:4px">{{ useTarget.itemDesc||getDesc(useTarget.itemKey) }}</p>
                <template v-if="useTarget.itemKey==='energy_potion'">
                    <p style="color:var(--accent);font-size:13px;margin-top:12px">
                        使用后体力将回满
                    </p>
                </template>
                <template v-else>
                    <div style="margin-top:12px;display:flex;align-items:center;gap:8px">
                        <span>使用数量：</span>
                        <el-input-number v-model="useCount" :min="1" :max="maxUseCount" size="small" />
                        <span style="color:var(--muted);font-size:12px">（最多{{ maxUseCount }}个）</span>
                    </div>
                    <p style="color:var(--accent);font-size:13px;margin-top:8px">
                        预计恢复：{{ useCount * 20 }} 点精力
                    </p>
                </template>
            </div>
            <template #footer>
                <el-button @click="showUseConfirm=false">取消</el-button>
                <el-button type="primary" @click="doUse">确定使用</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="showSellConfirm" title="出售物品" width="400px">
            <div v-if="sellTarget" style="padding:10px;background:var(--surface2);border-radius:8px">
                <p>物品：{{ sellTarget.itemName||names[sellTarget.itemKey]||sellTarget.itemKey }}</p>
                <p v-if="sellTarget.extraJson" style="color:var(--muted);font-size:12px">装备出售返还基础售价金币</p>
                <p v-else>数量：{{ sellTarget.sellCount }} 个</p>
                <p>获得：<span style="color:var(--accent);font-weight:700">{{ getSellPrice(sellTarget)*(sellTarget.sellCount||1) }}</span> 金币</p>
            </div>
            <template #footer>
                <el-button @click="showSellConfirm=false">取消</el-button>
                <el-button type="danger" @click="doSell">确认出售</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const store = useUserStore()
const items = ref([])
const equips = ref([])
const showUseConfirm = ref(false)
const useTarget = ref(null)
const useCount = ref(1)
const showSellConfirm = ref(false)
const sellTarget = ref(null)

// 分页和筛选
const filterType = ref('ALL')
const currentPage = ref(1)
const pageSize = 10

// 动态获取物品配置
const gameConfig = ref({})
const names = computed(() => {
    const n = {}
    if (gameConfig.value.items) {
        for (const [k, v] of Object.entries(gameConfig.value.items)) n[k] = v.name
    }
    return n
})
const descs = computed(() => {
    const d = {}
    if (gameConfig.value.items) {
        for (const [k, v] of Object.entries(gameConfig.value.items)) d[k] = v.desc
    }
    return d
})
const sellPrices = computed(() => {
    const s = {}
    if (gameConfig.value.items) {
        for (const [k, v] of Object.entries(gameConfig.value.items)) s[k] = v.sellPrice
    }
    return s
})

// 筛选后的物品
const filteredItems = computed(() => {
    if (filterType.value === 'ALL') return items.value
    return items.value.filter(i => {
        const itemType = i.itemType || ''
        if (filterType.value === 'EQUIPMENT') return !!i.extraJson
        if (filterType.value === 'CONSUMABLE') return itemType === 'CONSUMABLE'
        if (filterType.value === 'MATERIAL') return itemType === 'MATERIAL'
        return true
    })
})

// 分页后的物品
const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / pageSize)))
const pagedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    return filteredItems.value.slice(start, start + pageSize)
})

// 筛选变化时重置页码
watch(filterType, () => { currentPage.value = 1 })

const maxUseCount = computed(() => {
    if (!useTarget.value) return 1
    const role = store.role
    if (!role) return useTarget.value.count
    if (useTarget.value.itemKey === 'mana_potion') {
        const spiritDiff = role.maxSpirit - role.spirit
        const maxBySpirit = Math.ceil(spiritDiff / 20)
        return Math.max(1, Math.min(useTarget.value.count, maxBySpirit))
    }
    return useTarget.value.count
})

async function load() {
    const r = await api.get('/api/bag/list')
    items.value = (r.data.items || []).map(i => ({ ...i, sellCount: 1 }))
    equips.value = r.data.equips || []
    try { const c = await api.get('/api/game/config'); gameConfig.value = c.data || {} } catch(e) {}
}
onMounted(load)
function getQ(i) { try { return JSON.parse(i.extraJson).quality } catch(e) { return 'WHITE' } }
function getSL(i) { try { return JSON.parse(i.extraJson).strengthenLevel || 0 } catch(e) { return 0 } }
function getPart(i) { try { return JSON.parse(i.extraJson).part } catch(e) { return '' } }
function getAttr(i, key) { try { const a = JSON.parse(JSON.parse(i.extraJson).baseAttr); return a[key] || 0 } catch(e) { return 0 } }
function parseAttr(json) { try { return typeof json === 'string' ? JSON.parse(json) : json } catch(e) { return null } }
function partName(p) { return {WEAPON:'武器',HELMET:'头盔',ARMOR:'衣服',PANTS:'裤子',SHOES:'鞋子',NECKLACE:'项链',RING:'戒指'}[p]||p }
function isUsable(item) { return (item.itemKey === 'mana_potion' || item.itemKey === 'energy_potion') && !item.extraJson }
function getSellPrice(item) { return sellPrices.value[item.itemKey] || 1 }
function getDesc(key) { return descs.value[key] || '' }
function confirmUse(item) { useTarget.value = item; useCount.value = 1; showUseConfirm.value = true }
function confirmSellEquipment(item) { sellTarget.value = { ...item, sellCount: 1 }; showSellConfirm.value = true }
async function doUse() {
    if (!useTarget.value) return
    try {
        const count = useTarget.value.itemKey === 'energy_potion' ? 1 : useCount.value
        const r = await api.post('/api/bag/use', { bagId: useTarget.value.id, useCount: count })
        ElMessage.success(r.data.msg); store.fetchRole(); load()
    } catch(e){}
    showUseConfirm.value = false; useTarget.value = null; useCount.value = 1
}
function confirmSell(item) { sellTarget.value = item; showSellConfirm.value = true }
async function doSell() {
    if (!sellTarget.value) return
    try {
        const count = sellTarget.value.extraJson ? 1 : (sellTarget.value.sellCount||1)
        const r = await api.post('/api/bag/sell',{bagId:sellTarget.value.id,count})
        ElMessage.success(r.data.msg); store.fetchRole(); load()
    } catch(e){}
    showSellConfirm.value = false; sellTarget.value = null
}
async function doEquip(id) { try { const r = await api.post('/api/bag/equip',{bagId:id}); ElMessage.success(r.data.msg); store.fetchRole(); load() } catch(e){} }
async function doUnequip(equipId) { try { const r = await api.post('/api/bag/unequip',{equipId}); ElMessage.success(r.data.msg); store.fetchRole(); load() } catch(e){} }
</script>
<style scoped>
.item-row{display:flex;align-items:center;justify-content:space-between;padding:10px 0;border-bottom:1px solid var(--border)}
</style>
