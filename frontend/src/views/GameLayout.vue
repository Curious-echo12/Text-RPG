<!-- frontend/src/views/GameLayout.vue -->
<template>
    <div class="game-layout">
        <aside class="sidebar">
            <div class="logo">⚔️ 文字江湖</div>
            <nav class="nav-menu">
                <router-link v-for="item in menu" :key="item.path" :to="item.path"
                             :class="['nav-item',{active:$route.path===item.path}]"
                             @click="onMenuClick(item.path)">
                    <span class="nav-icon">{{ item.icon }}</span>
                    <span class="nav-text">{{ item.name }}</span>
                    <span v-if="item.path==='/mail' && hasUnreadMail" class="mail-badge">NEW</span>
                </router-link>
            </nav>
            <div class="sidebar-footer">
                <span class="user-name">{{ store.username }}</span>
                <el-button text size="small" @click="logout" style="color:var(--muted)">退出</el-button>
            </div>
        </aside>
        <div class="main-area">
            <header class="top-bar" v-if="store.role">
                <div class="bar-info">
                    <span class="role-name">{{ store.role.name }}</span>
                    <span class="role-job">{{ jobName(store.role.job) }}</span>
                    <span class="role-level">Lv.{{ store.role.level }}</span>
                </div>
                <div class="bar-resources">
                    <!-- 生命值 -->
                    <div class="res-item" title="生命值">
                        <span>❤️</span>
                        <div class="stat-bar" style="width:80px">
                            <div class="stat-bar-fill" :style="{width:hpPct+'%',background:'var(--hp)'}"></div>
                        </div>
                        <span>{{ store.role.hp }}/{{ store.role.maxHp }}</span>
                    </div>
                    <!-- 体力 -->
                    <el-tooltip placement="bottom" :disabled="store.recover.energyRecoverSec < 0">
                        <template #content>
                            <span>下一点体力恢复：{{ store.recover.energyRecoverSec }}秒</span>
                        </template>
                        <div class="res-item" style="cursor:default">
                            <span>⚡</span>
                            <span>{{ store.role.energy }}/{{ store.role.maxEnergy }}</span>
                        </div>
                    </el-tooltip>
                    <!-- 精力 -->
                    <el-tooltip placement="bottom" :disabled="store.recover.spiritRecoverSec < 0">
                        <template #content>
                            <span>下一点精力恢复：{{ store.recover.spiritRecoverSec }}秒</span>
                        </template>
                        <div class="res-item" style="cursor:default">
                            <span>🔮</span>
                            <span>{{ store.role.spirit }}/{{ store.role.maxSpirit }}</span>
                        </div>
                    </el-tooltip>
                    <div class="res-item" title="钻石"><span>💎</span><span>{{ store.role.diamond }}</span></div>
                    <div class="res-item" title="金币"><span>🪙</span><span>{{ store.role.gold }}</span></div>
                </div>
            </header>
            <main class="content">
                <router-view v-slot="{ Component }">
                    <transition name="fade" mode="out-in">
                        <component :is="Component" />
                    </transition>
                </router-view>
            </main>
            <!-- ICP备案信息 -->
            <footer class="site-footer">
                <span>© 2026 文字江湖</span>
                <span class="divider">|</span>
                <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">粤ICP备XXXXXXXX号</a>
                <span class="divider">|</span>
                <a href="https://www.beian.gov.cn/" target="_blank" rel="noopener">粤公网安备XXXXXXXXXXXXXXX号</a>
            </footer>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import api from '../utils/request'

const store = useUserStore()
const router = useRouter()
const hasUnreadMail = ref(false)
let refreshTimer = null
let countdownTimer = null
let mailCheckTimer = null

async function checkUnreadMail() {
    try {
        const r = await api.get('/api/mail/hasUnread')
        hasUnreadMail.value = r.data?.hasUnread || false
    } catch(e) {}
}

function onMenuClick(path) {
    if (path === '/mail') {
        hasUnreadMail.value = false
    }
}

onMounted(() => {
    store.fetchRole()
    checkUnreadMail()
    refreshTimer = setInterval(() => { store.fetchRole() }, 15000)
    mailCheckTimer = setInterval(() => { checkUnreadMail() }, 30000)
    countdownTimer = setInterval(() => {
        if (store.recover.energyRecoverSec > 0) store.recover.energyRecoverSec--
        if (store.recover.spiritRecoverSec > 0) store.recover.spiritRecoverSec--
    }, 1000)
})
onUnmounted(() => {
    if (refreshTimer) clearInterval(refreshTimer)
    if (countdownTimer) clearInterval(countdownTimer)
    if (mailCheckTimer) clearInterval(mailCheckTimer)
})

const hpPct = computed(() => store.role ? (store.role.hp / store.role.maxHp * 100) : 0)
const menu = [
    {path:'/home',name:'主页',icon:'🏠'},{path:'/role',name:'角色',icon:'👤'},
    {path:'/skill',name:'技能',icon:'📖'},{path:'/bag',name:'背包',icon:'🎒'},
    {path:'/mine',name:'挖矿',icon:'⛏️'},{path:'/farm',name:'种菜',icon:'🌾'},
    {path:'/forge',name:'锻造',icon:'🔨'},{path:'/shop',name:'商店',icon:'🏪'},
    {path:'/pve',name:'冒险',icon:'⚔️'},{path:'/dungeon',name:'副本',icon:'🏰'},
    {path:'/pvp',name:'竞技',icon:'🏟️'},{path:'/rank',name:'排行',icon:'🏆'},
    {path:'/task',name:'任务',icon:'📋'},{path:'/sign',name:'签到',icon:'✅'},
    {path:'/mail',name:'邮件',icon:'📧'}
]
function jobName(j) { return {WARRIOR:'战士',MAGE:'法师',ARCHER:'射手',PRIEST:'牧师',MINER:'矿工'}[j]||j }
function logout() { store.logout(); router.push('/login') }
</script>

<style scoped>
.game-layout{display:flex;min-height:100vh}
.sidebar{width:170px;background:var(--surface);border-right:1px solid var(--border);display:flex;flex-direction:column;position:fixed;height:100vh;z-index:10}
.logo{padding:18px;font-size:16px;font-weight:700;color:var(--accent);text-align:center;border-bottom:1px solid var(--border)}
.nav-menu{flex:1;overflow-y:auto;padding:6px}
.nav-item{display:flex;align-items:center;gap:8px;padding:9px 12px;border-radius:8px;color:var(--muted);text-decoration:none;font-size:13px;transition:all 0.15s;margin-bottom:1px;position:relative}
.nav-item:hover{background:var(--surface2);color:var(--text)}
.nav-item.active{background:rgba(240,192,64,0.12);color:var(--accent);font-weight:700}
.nav-icon{font-size:15px;width:20px;text-align:center}
.mail-badge{position:absolute;right:8px;top:50%;transform:translateY(-50%);background:var(--red);color:#fff;font-size:9px;font-weight:700;padding:1px 5px;border-radius:8px;animation:pulse 1.5s infinite}
@keyframes pulse{0%,100%{opacity:1}50%{opacity:0.6}}
.sidebar-footer{padding:10px;border-top:1px solid var(--border);display:flex;align-items:center;justify-content:space-between}
.user-name{font-size:12px;color:var(--muted)}
.main-area{flex:1;margin-left:170px;display:flex;flex-direction:column;min-height:100vh}
.top-bar{background:var(--surface);border-bottom:1px solid var(--border);padding:8px 20px;display:flex;align-items:center;justify-content:space-between;position:sticky;top:0;z-index:5}
.bar-info{display:flex;align-items:center;gap:10px}
.role-name{font-weight:700;color:var(--accent)}
.role-job{font-size:11px;color:var(--muted);background:var(--surface2);padding:2px 6px;border-radius:4px}
.role-level{font-size:12px;color:var(--green);font-weight:700}
.bar-resources{display:flex;gap:14px;align-items:center;font-size:12px}
.res-item{display:flex;align-items:center;gap:4px}
.content{flex:1;padding:20px;max-width:960px}
.site-footer{padding:16px 20px;text-align:center;font-size:11px;color:var(--muted);border-top:1px solid var(--border);line-height:2}
.site-footer a{color:var(--muted);text-decoration:none}
.site-footer a:hover{color:var(--accent)}
.site-footer .divider{margin:0 8px;opacity:0.4}
</style>
