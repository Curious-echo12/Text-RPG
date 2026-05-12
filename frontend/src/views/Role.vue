<!-- frontend/src/views/Role.vue -->
<template>
    <div v-if="role">
        <div class="game-card">
            <h3>{{ role.name }} — {{ detail.jobName || jobName(role.job) }}</h3>
            <div style="display:flex;gap:20px;margin-top:6px;font-size:13px">
                <span style="color:var(--muted)">玩家ID: <b style="color:var(--accent);letter-spacing:2px">{{ playerId }}</b></span>
                <span style="color:var(--muted)">邮箱: <b v-if="userEmail" style="color:var(--green)">{{ userEmail }} (已绑定)</b>
                    <el-button v-else size="small" type="primary" link @click="showBindEmail=true">绑定邮箱</el-button>
                </span>
                <span v-if="userEmail">
                    <el-button size="small" type="warning" link @click="openChangeEmail">更换邮箱</el-button>
                    <el-button size="small" type="danger" link @click="showResetPwd=true" style="margin-left:8px">修改密码</el-button>
                </span>
                <span v-else>
                    <el-button size="small" type="danger" link @click="showResetPwd=true">修改密码</el-button>
                </span>
            </div>
            <p style="color:var(--muted);font-size:12px;margin-top:4px">鼠标悬浮各项属性可查看详细加成信息</p>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:12px">
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p>通过冒险战斗获取经验值升级</p>
                            <p>每次升级获得1个技能点，全属性提升</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>等级</span><span>Lv.{{ role.level }}</span></div>
                </el-tooltip>

                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p>经验来源：冒险战斗、副本战斗、秘境探索</p>
                            <p>升级所需经验随等级递增</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>经验</span><span>{{ role.exp }}/{{ levelExp }}</span></div>
                </el-tooltip>

                <!-- 最大生命 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>决定战斗中可承受的总伤害量</p>
                            <p><b>来源详情：</b></p>
                            <p>　职业基础: {{ detail.maxHp?.base }}</p>
                            <p>　等级成长: +{{ detail.maxHp?.levelGrowth }}</p>
                            <p>　装备加成: +{{ detail.maxHp?.equip }}</p>
                            <p v-if="detail.rebornCount > 0">　转生倍率: x{{ detail.maxHp?.rebornMult }}</p>
                            <p><b>获取渠道：</b>升级、穿戴装备、强化装备、转生、被动技能</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>最大生命</span><span>{{ role.maxHp }}</span></div>
                </el-tooltip>

                <!-- 攻击 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>决定战斗中造成的伤害高低</p>
                            <p><b>来源详情：</b></p>
                            <p>　职业基础: {{ detail.attack?.base }}</p>
                            <p>　等级成长: +{{ detail.attack?.levelGrowth }}</p>
                            <p>　装备加成: +{{ detail.attack?.equip }}</p>
                            <p v-if="detail.rebornCount > 0">　转生倍率: x{{ detail.attack?.rebornMult }}</p>
                            <p><b>获取渠道：</b>升级、穿戴武器/饰品、强化装备、转生、被动技能</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>攻击</span><span style="color:var(--red)">{{ role.attack }}</span></div>
                </el-tooltip>

                <!-- 防御 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>减少受到的伤害</p>
                            <p><b>来源详情：</b></p>
                            <p>　职业基础: {{ detail.defense?.base }}</p>
                            <p>　等级成长: +{{ detail.defense?.levelGrowth }}</p>
                            <p>　装备加成: +{{ detail.defense?.equip }}</p>
                            <p v-if="detail.rebornCount > 0">　转生倍率: x{{ detail.defense?.rebornMult }}</p>
                            <p><b>获取渠道：</b>升级、穿戴防具、强化装备、转生、被动技能</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>防御</span><span style="color:var(--blue)">{{ role.defense }}</span></div>
                </el-tooltip>

                <!-- 速度 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>速度高的一方先手攻击</p>
                            <p><b>来源详情：</b></p>
                            <p>　职业基础: {{ detail.speed?.base }}</p>
                            <p>　等级成长: +{{ detail.speed?.levelGrowth }}</p>
                            <p><b>获取渠道：</b>升级（射手职业成长最高）、被动技能</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>速度</span><span style="color:var(--green)">{{ role.speed }}</span></div>
                </el-tooltip>

                <!-- 暴击率 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>攻击时触发暴击的概率</p>
                            <p><b>当前值：</b>{{ (role.critRate*100).toFixed(0) }}%</p>
                            <p><b>获取渠道：</b>初始5%</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>暴击率</span><span>{{ (role.critRate*100).toFixed(0) }}%</span></div>
                </el-tooltip>

                <!-- 暴击伤害 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>触发暴击时的伤害倍率</p>
                            <p><b>当前值：</b>{{ (role.critDmg*100).toFixed(0) }}%</p>
                            <p><b>获取渠道：</b>初始150%</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>暴击伤害</span><span>{{ (role.critDmg*100).toFixed(0) }}%</span></div>
                </el-tooltip>

                <!-- 战力 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>综合评估角色战斗能力，影响PVP匹配</p>
                            <p><b>来源：</b>由生命、攻击、防御、速度、暴击率、暴击伤害综合计算</p>
                            <p><b>提升方式：</b>升级、穿戴装备、强化装备、学习被动技能</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>战力</span><span style="color:var(--accent);font-weight:700">{{ role.fightPower }}</span></div>
                </el-tooltip>

                <!-- 技能点 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>学习和升级技能</p>
                            <p><b>获取渠道：</b>每次升级获得1点</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>技能点</span><span style="color:var(--accent)">{{ role.skillPoint }}</span></div>
                </el-tooltip>

                <!-- 金币 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>购买物品、学习技能、锻造装备、强化装备</p>
                            <p><b>获取渠道：</b>战斗掉落、出售物品、签到、钻石兑换</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>金币</span><span style="color:var(--accent)">{{ role.gold }}</span></div>
                </el-tooltip>

                <!-- 钻石 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>兑换金币（1钻石=100金币）</p>
                            <p><b>获取渠道：</b>签到（每7天）、秘境探索、新手礼包</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>钻石</span><span style="color:var(--accent)">{{ role.diamond }}</span></div>
                </el-tooltip>

                <!-- 体力 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>冒险（5点）、副本（15~40点）、挖矿（5~50点）</p>
                            <p><b>恢复：</b>每60秒自动恢复1点</p>
                            <p><b>获取渠道：</b>自动恢复、精力药水、矿工职业技能提升上限</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>体力</span><span>{{ role.energy }}/{{ role.maxEnergy }}</span></div>
                </el-tooltip>

                <!-- 精力 -->
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p><b>用途：</b>PVP（5点）、秘境探索（10点）</p>
                            <p><b>恢复：</b>每60秒自动恢复1点</p>
                            <p><b>获取渠道：</b>自动恢复、精力药水</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>精力</span><span>{{ role.spirit }}/{{ role.maxSpirit }}</span></div>
                </el-tooltip>
            </div>
        </div>

        <!-- 装备加成明细 -->
        <div class="game-card" v-if="detail.equips && detail.equips.length">
            <h3>装备加成明细</h3>
            <div v-for="e in detail.equips" :key="e.name" class="equip-detail-row">
                <span :class="'quality-'+(e.quality||'WHITE')">{{ e.name }}</span>
                <span style="color:var(--accent);margin-left:4px" v-if="e.strengthenLevel">+{{ e.strengthenLevel }}</span>
                <span style="color:var(--muted);font-size:12px;margin-left:8px">
          攻击+{{ e.atk||0 }} 防御+{{ e.def||0 }} 生命+{{ e.hp||0 }}
        </span>
            </div>
        </div>

        <div class="game-card">
            <h3>生活技能</h3>
            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px">
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p>每次挖矿获得10点经验</p>
                            <p>每5级额外+1个矿石产出</p>
                            <p>矿工职业额外+1个产出</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>挖矿 Lv.{{ role.mineLevel }}</span><span>{{ role.mineExp }}/{{ role.mineLevel*100 }}</span></div>
                </el-tooltip>
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p>每次收获获得10点经验</p>
                            <p>每3级额外+1个作物产出</p>
                            <p>5/10/15/20/30/40级解锁新菜地</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>种植 Lv.{{ role.farmLevel }}</span><span>{{ role.farmExp }}/{{ role.farmLevel*100 }}</span></div>
                </el-tooltip>
                <el-tooltip placement="top" :show-after="200">
                    <template #content>
                        <div style="font-size:12px;line-height:2;padding:4px 0">
                            <p>每次锻造获得15点经验</p>
                            <p>锻造等级越高，出高品质概率越大</p>
                            <p>影响绿色~红色品质的额外权重</p>
                        </div>
                    </template>
                    <div class="info-row hoverable"><span>锻造 Lv.{{ role.forgeLevel }}</span><span>{{ role.forgeExp }}/{{ role.forgeLevel*100 }}</span></div>
                </el-tooltip>
            </div>
            <div style="margin-top:8px;font-size:12px;color:var(--muted)">
                <p v-if="detail.mineBonusDesc">⛏️ {{ detail.mineBonusDesc }}</p>
                <p v-if="detail.farmBonusDesc">🌾 {{ detail.farmBonusDesc }}</p>
                <p>🔨 锻造等级越高，出高品质装备概率越大</p>
            </div>
        </div>
        <!-- 邮箱绑定弹窗 -->
        <el-dialog v-model="showBindEmail" title="绑定邮箱" width="420px">
            <p style="color:var(--muted);font-size:13px;margin-bottom:12px">绑定邮箱后可用于找回密码。需要先发送验证码到您的邮箱。</p>
            <el-form label-width="80px">
                <el-form-item label="邮箱地址">
                    <el-input v-model="bindForm.email" placeholder="输入邮箱地址" />
                </el-form-item>
                <el-form-item label="验证码">
                    <div style="display:flex;gap:8px">
                        <el-input v-model="bindForm.code" placeholder="输入6位验证码" />
                        <el-button @click="sendCode" :disabled="codeCooldown>0" :loading="sendingCode">
                            {{ codeCooldown>0 ? codeCooldown+'s' : '发送验证码' }}
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showBindEmail=false">取消</el-button>
                <el-button type="primary" @click="bindEmail">确认绑定</el-button>
            </template>
        </el-dialog>

        <!-- 更换邮箱弹窗 -->
        <el-dialog v-model="showChangeEmail" title="更换绑定邮箱" width="420px">
            <p style="color:var(--muted);font-size:13px;margin-bottom:12px">当前邮箱：{{ userEmail }}</p>
            <el-form label-width="80px">
                <el-form-item label="新邮箱">
                    <el-input v-model="changeEmailForm.email" placeholder="输入新邮箱地址" />
                </el-form-item>
                <el-form-item label="验证码">
                    <div style="display:flex;gap:8px">
                        <el-input v-model="changeEmailForm.code" placeholder="输入6位验证码" />
                        <el-button @click="sendChangeCode" :disabled="changeCooldown>0" :loading="sendingChangeCode">
                            {{ changeCooldown>0 ? changeCooldown+'s' : '发送验证码' }}
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showChangeEmail=false">取消</el-button>
                <el-button type="primary" @click="doChangeEmail">确认更换</el-button>
            </template>
        </el-dialog>

        <!-- 修改密码弹窗 -->
        <el-dialog v-model="showResetPwd" title="修改密码" width="420px">
            <el-form label-width="80px">
                <el-form-item label="原密码">
                    <el-input v-model="pwdForm.oldPassword" type="password" placeholder="输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码">
                    <el-input v-model="pwdForm.newPassword" type="password" placeholder="输入新密码（至少6位）" />
                </el-form-item>
                <el-form-item label="确认密码">
                    <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showResetPwd=false">取消</el-button>
                <el-button type="primary" @click="doChangePassword">确认修改</el-button>
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
const role = computed(() => store.role)
const detail = ref({})
const playerId = ref(localStorage.getItem('playerId') || '未知')
const userEmail = ref(localStorage.getItem('userEmail') || '')
const levelExp = computed(() => { const l = role.value?.level||1; return l*120+l*l*8 })
function jobName(j) { return {WARRIOR:'战士',MAGE:'法师',ARCHER:'射手',PRIEST:'牧师',MINER:'矿工'}[j]||j }

const showBindEmail = ref(false)
const bindForm = ref({ email: '', code: '' })
const sendingCode = ref(false)
const codeCooldown = ref(0)

const showChangeEmail = ref(false)
const changeEmailForm = ref({ email: '', code: '' })
const sendingChangeCode = ref(false)
const changeCooldown = ref(0)

const showResetPwd = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

async function loadDetail() {
    try {
        const r = await api.get('/api/role/detail')
        detail.value = r.data.detail || {}
        if (r.data.playerId) { playerId.value = r.data.playerId; localStorage.setItem('playerId', r.data.playerId) }
        if (r.data.email) { userEmail.value = r.data.email; localStorage.setItem('userEmail', r.data.email) }
    } catch(e) {}
}

async function sendCode() {
    if (!bindForm.value.email) { ElMessage.warning('请输入邮箱地址'); return }
    sendingCode.value = true
    try {
        await api.post('/api/email/sendBindCode', { email: bindForm.value.email })
        ElMessage.success('验证码已发送')
        codeCooldown.value = 60
        const timer = setInterval(() => { codeCooldown.value--; if (codeCooldown.value <= 0) clearInterval(timer) }, 1000)
    } catch(e) {} finally { sendingCode.value = false }
}

async function bindEmail() {
    if (!bindForm.value.email || !bindForm.value.code) { ElMessage.warning('请填写邮箱和验证码'); return }
    try {
        const r = await api.post('/api/email/bind', { email: bindForm.value.email, code: bindForm.value.code })
        userEmail.value = bindForm.value.email
        localStorage.setItem('userEmail', bindForm.value.email)
        ElMessage.success('邮箱绑定成功')
        showBindEmail.value = false
        bindForm.value = { email: '', code: '' }
    } catch(e) {}
}

function openChangeEmail() {
    changeEmailForm.value = { email: '', code: '' }
    showChangeEmail.value = true
}

async function sendChangeCode() {
    if (!changeEmailForm.value.email) { ElMessage.warning('请输入新邮箱地址'); return }
    sendingChangeCode.value = true
    try {
        await api.post('/api/email/sendChangeCode', { email: changeEmailForm.value.email })
        ElMessage.success('验证码已发送')
        changeCooldown.value = 60
        const timer = setInterval(() => { changeCooldown.value--; if (changeCooldown.value <= 0) clearInterval(timer) }, 1000)
    } catch(e) {} finally { sendingChangeCode.value = false }
}

async function doChangeEmail() {
    if (!changeEmailForm.value.email || !changeEmailForm.value.code) { ElMessage.warning('请填写邮箱和验证码'); return }
    try {
        await api.post('/api/email/change', { email: changeEmailForm.value.email, code: changeEmailForm.value.code })
        userEmail.value = changeEmailForm.value.email
        localStorage.setItem('userEmail', changeEmailForm.value.email)
        ElMessage.success('邮箱更换成功')
        showChangeEmail.value = false
    } catch(e) {}
}

async function doChangePassword() {
    if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) { ElMessage.warning('请填写完整信息'); return }
    if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) { ElMessage.error('两次密码不一致'); return }
    if (pwdForm.value.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
    try {
        await api.post('/api/email/changePassword', { oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
        ElMessage.success('密码修改成功')
        showResetPwd.value = false
        pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } catch(e) {}
}

onMounted(loadDetail)
</script>

<style scoped>
.info-row{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--border);font-size:14px}
.info-row span:first-child{color:var(--muted)}
.hoverable{cursor:default;border-radius:6px;padding:8px 6px;margin:-1px -6px;border-bottom:none}
.hoverable:hover{background:rgba(240,192,64,0.08)}
.equip-detail-row{display:flex;align-items:center;padding:6px 0;border-bottom:1px solid var(--border);font-size:13px}
</style>
