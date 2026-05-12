<template>
    <div>
        <div class="game-card">
            <h3>每日签到</h3>
            <div style="margin-top:12px">
                <p style="color:var(--muted)">连续签到: <b style="color:var(--accent)">{{ info.consecutiveDays }}</b> 天</p>
                <div style="margin-top:10px;padding:12px;background:var(--surface2);border-radius:8px">
                    <p style="color:var(--muted);font-size:13px">签到奖励预览：</p>
                    <p style="margin-top:6px">
            <span v-if="info.isDiamond" style="color:var(--blue)">
              💎 {{ info.rewardMin }} ~ {{ info.rewardMax }} 钻石
            </span>
                        <span v-else style="color:var(--accent)">
              🪙 {{ info.rewardMin }} ~ {{ info.rewardMax }} 金币
            </span>
                        <span style="color:var(--muted);font-size:12px;margin-left:8px">（随机获得）</span>
                    </p>
                    <p style="margin-top:4px;color:var(--blue);font-size:13px">
                        💎 每日额外赠送 5~10 钻石
                    </p>
                </div>
                <el-button type="primary" style="margin-top:16px" :disabled="info.signedToday" @click="doSign" size="large">
                    {{ info.signedToday ? '✅ 今日已签到' : '签到领取奖励' }}
                </el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const store = useUserStore()
const info = ref({ signedToday: false, consecutiveDays: 0, rewardMin: 15, rewardMax: 30, isDiamond: false })
async function load() { const r = await api.get('/api/sign/info'); info.value = r.data }
onMounted(load)
async function doSign() {
    try { const r = await api.post('/api/sign/do'); ElMessage.success(r.data.msg); store.fetchRole(); load() } catch(e) {}
}
</script>
