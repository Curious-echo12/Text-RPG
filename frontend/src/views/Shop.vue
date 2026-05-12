<template>
    <div>
        <div class="game-card">
            <h3>商店</h3>
            <p style="color:var(--muted);font-size:13px">金币: {{ store.role?.gold }} | 钻石: {{ store.role?.diamond }}</p>
        </div>

        <div class="game-card">
            <h3>💎 钻石兑换金币</h3>
            <p style="color:var(--muted);font-size:13px;margin-bottom:10px">兑换比例：1 钻石 = {{ exchangeRate }} 金币</p>
            <div style="display:flex;align-items:center;gap:12px">
                <el-input-number v-model="exchangeAmount" :min="1" :max="store.role?.diamond||0" style="width:120px" />
                <span style="color:var(--muted)">钻石</span>
                <span style="color:var(--accent)">→ {{ exchangeAmount * exchangeRate }} 金币</span>
                <el-button type="primary" @click="confirmExchange">兑换</el-button>
            </div>
        </div>

        <div v-for="item in items" :key="item.itemKey" class="game-card">
            <div style="display:flex;justify-content:space-between;align-items:center">
                <div>
                    <span style="font-weight:700">{{ item.itemName }}</span>
                    <p style="color:var(--muted);font-size:12px;margin-top:4px">{{ item.desc }}</p>
                </div>
                <div style="display:flex;align-items:center;gap:12px">
          <span :style="{color:item.currency==='DIAMOND'?'var(--blue)':'var(--accent)'}">
            {{ item.currency==='DIAMOND'?'💎':'🪙' }} {{ item.price }}
          </span>
                    <el-input-number v-model="item.buyCount" :min="1" :max="99" size="small" style="width:80px" />
                    <el-button size="small" type="primary" @click="confirmBuy(item)">购买</el-button>
                </div>
            </div>
        </div>

        <el-dialog v-model="showBuyConfirm" title="确认购买" width="360px">
            <div v-if="buyTarget">
                <p>购买 <b>{{ buyTarget.itemName }}</b> x{{ buyTarget.buyCount }}</p>
                <p>总价: <span style="color:var(--accent)">{{ buyTarget.currency==='DIAMOND'?'💎':'🪙' }} {{ buyTarget.price*buyTarget.buyCount }}</span></p>
            </div>
            <template #footer>
                <el-button @click="showBuyConfirm=false">取消</el-button>
                <el-button type="primary" @click="doBuy">确认</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="showExchangeConfirm" title="确认兑换" width="360px">
            <p>{{ exchangeAmount }} 💎 → {{ exchangeAmount * exchangeRate }} 🪙</p>
            <template #footer>
                <el-button @click="showExchangeConfirm=false">取消</el-button>
                <el-button type="primary" @click="doExchange">确认兑换</el-button>
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
const items = ref([])
const exchangeRate = ref(100)
const exchangeAmount = ref(1)
const showBuyConfirm = ref(false)
const buyTarget = ref(null)
const showExchangeConfirm = ref(false)
async function load() {
    const r = await api.get('/api/shop/list')
    items.value = (r.data || []).map(i => ({ ...i, buyCount: 1 }))
    try { const er = await api.get('/api/shop/exchangeRate'); exchangeRate.value = er.data.rate } catch(e){}
}
onMounted(load)
function confirmBuy(item) { buyTarget.value = item; showBuyConfirm.value = true }
async function doBuy() {
    if(!buyTarget.value) return
    try{const r=await api.post('/api/shop/buy',{itemKey:buyTarget.value.itemKey,count:buyTarget.value.buyCount});ElMessage.success(r.data.msg);store.fetchRole()}catch(e){}
    showBuyConfirm.value=false;buyTarget.value=null
}
function confirmExchange() { showExchangeConfirm.value = true }
async function doExchange() {
    try{const r=await api.post('/api/shop/exchange',{amount:exchangeAmount.value});ElMessage.success(r.data.msg);store.fetchRole();exchangeAmount.value=1}catch(e){}
    showExchangeConfirm.value=false
}
</script>
