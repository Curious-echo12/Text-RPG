<template>
  <div>
    <div class="game-card">
      <h3>玩家管理</h3>
      <div style="display:flex;gap:12px;margin-top:12px;flex-wrap:wrap">
        <el-input v-model="keyword" placeholder="搜索用户名/ID/邮箱" style="width:250px" @keyup.enter="loadPlayers" />
        <el-select v-model="filterJob" clearable placeholder="职业筛选" style="width:120px">
          <el-option label="战士" value="WARRIOR" /><el-option label="法师" value="MAGE" />
          <el-option label="射手" value="ARCHER" /><el-option label="牧师" value="PRIEST" />
          <el-option label="矿工" value="MINER" />
        </el-select>
        <el-input-number v-model="filterMinLevel" :min="1" placeholder="最低等级" style="width:120px" size="default" clearable />
        <el-input-number v-model="filterMaxLevel" :min="1" placeholder="最高等级" style="width:120px" size="default" clearable />
        <el-button type="primary" @click="loadPlayers">搜索</el-button>
      </div>
    </div>

    <div class="game-card">
      <el-table :data="players" style="width:100%" size="small" max-height="500">
        <el-table-column prop="playerId" label="玩家ID" width="90" />
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="roleName" label="角色名" width="90" />
        <el-table-column prop="roleJobName" label="职业" width="70" />
        <el-table-column prop="roleLevel" label="等级" width="60" />
        <el-table-column prop="fightPower" label="战力" width="80" />
        <el-table-column prop="gold" label="金币" width="80" />
        <el-table-column prop="diamond" label="钻石" width="60" />
        <el-table-column prop="email" label="邮箱" width="150" />
        <el-table-column prop="createTime" label="注册时间" width="150" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="warning" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" type="info" @click="deletePlayer(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px;display:flex;justify-content:center">
        <el-pagination :total="total" :page-size="pageSize" v-model:current-page="currentPage"
          layout="prev, pager, next" @current-change="loadPlayers" />
      </div>
    </div>

    <!-- 编辑玩家弹窗 -->
    <el-dialog v-model="showEdit" title="编辑玩家信息" width="500px">
      <div v-if="editTarget">
        <p style="margin-bottom:12px">玩家: <b>{{ editTarget.username }}</b> (ID: {{ editTarget.playerId }})</p>
        <el-form label-width="80px">
          <el-form-item label="金币"><el-input-number v-model="editForm.gold" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="钻石"><el-input-number v-model="editForm.diamond" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="等级"><el-input-number v-model="editForm.level" :min="1" :max="999" style="width:100%" /></el-form-item>
          <el-form-item label="经验"><el-input-number v-model="editForm.exp" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="体力"><el-input-number v-model="editForm.energy" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="精力"><el-input-number v-model="editForm.spirit" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="技能点"><el-input-number v-model="editForm.skillPoint" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="PVP积分"><el-input-number v-model="editForm.pvpScore" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="攻击力"><el-input-number v-model="editForm.attack" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="防御力"><el-input-number v-model="editForm.defense" :min="0" style="width:100%" /></el-form-item>
          <el-form-item label="挖矿等级"><el-input-number v-model="editForm.mineLevel" :min="1" style="width:100%" /></el-form-item>
          <el-form-item label="种植等级"><el-input-number v-model="editForm.farmLevel" :min="1" style="width:100%" /></el-form-item>
          <el-form-item label="锻造等级"><el-input-number v-model="editForm.forgeLevel" :min="1" style="width:100%" /></el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showEdit=false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 玩家详情弹窗 -->
    <el-dialog v-model="showDetail" title="玩家详情" width="700px">
      <div v-if="detail" style="max-height:600px;overflow-y:auto">
        <el-tabs>
          <el-tab-pane label="基本信息">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="玩家ID">{{ detail.playerId }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ detail.email || '未绑定' }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ detail.createTime }}</el-descriptions-item>
              <el-descriptions-item label="角色名">{{ detail.roleName }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ detail.roleJobName }}</el-descriptions-item>
              <el-descriptions-item label="等级">Lv.{{ detail.roleLevel }}</el-descriptions-item>
              <el-descriptions-item label="经验">{{ detail.exp }}</el-descriptions-item>
              <el-descriptions-item label="战力">{{ detail.fightPower }}</el-descriptions-item>
              <el-descriptions-item label="转生次数">{{ detail.rebornCount }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="战斗属性">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="生命">{{ detail.hp }}/{{ detail.maxHp }}</el-descriptions-item>
              <el-descriptions-item label="攻击">{{ detail.attack }}</el-descriptions-item>
              <el-descriptions-item label="防御">{{ detail.defense }}</el-descriptions-item>
              <el-descriptions-item label="速度">{{ detail.speed }}</el-descriptions-item>
              <el-descriptions-item label="暴击率">{{ (detail.critRate*100).toFixed(0) }}%</el-descriptions-item>
              <el-descriptions-item label="暴击伤害">{{ (detail.critDmg*100).toFixed(0) }}%</el-descriptions-item>
              <el-descriptions-item label="体力">{{ detail.energy }}/{{ detail.maxEnergy }}</el-descriptions-item>
              <el-descriptions-item label="精力">{{ detail.spirit }}/{{ detail.maxSpirit }}</el-descriptions-item>
              <el-descriptions-item label="技能点">{{ detail.skillPoint }}</el-descriptions-item>
              <el-descriptions-item label="PVP积分">{{ detail.pvpScore }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="货币资源">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="金币">{{ detail.gold }}</el-descriptions-item>
              <el-descriptions-item label="钻石">{{ detail.diamond }}</el-descriptions-item>
              <el-descriptions-item label="挖矿等级">Lv.{{ detail.mineLevel }} ({{ detail.mineExp }}exp)</el-descriptions-item>
              <el-descriptions-item label="种植等级">Lv.{{ detail.farmLevel }} ({{ detail.farmExp }}exp)</el-descriptions-item>
              <el-descriptions-item label="锻造等级">Lv.{{ detail.forgeLevel }} ({{ detail.forgeExp }}exp)</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="已穿戴装备">
            <div v-if="detail.equips && detail.equips.length">
              <div v-for="e in detail.equips" :key="e.itemKey" style="padding:8px 0;border-bottom:1px solid var(--border)">
                <span :style="{color:qualityColor(e.quality)}">{{ e.name }}</span>
                <span style="color:var(--accent);margin-left:6px">+{{ e.strengthenLevel }}</span>
                <span style="color:var(--muted);font-size:12px;margin-left:8px">{{ e.part }} | {{ e.baseAttrJson }}</span>
              </div>
            </div>
            <p v-else style="color:var(--muted);text-align:center;padding:20px">暂无装备</p>
          </el-tab-pane>
          <el-tab-pane label="背包物品">
            <div v-if="detail.bagItems && detail.bagItems.length" style="max-height:300px;overflow-y:auto">
              <div v-for="b in detail.bagItems" :key="b.id" style="padding:6px 0;border-bottom:1px solid var(--border);font-size:13px">
                <span>{{ b.name }}</span>
                <span v-if="b.count>1" style="color:var(--muted);margin-left:6px">x{{ b.count }}</span>
                <span v-if="b.extraJson" style="color:var(--accent);font-size:11px;margin-left:8px">[装备]</span>
                <span style="color:var(--muted);font-size:11px;margin-left:8px">{{ b.itemKey }}</span>
              </div>
            </div>
            <p v-else style="color:var(--muted);text-align:center;padding:20px">背包为空</p>
          </el-tab-pane>
          <el-tab-pane label="已学技能">
            <div v-if="detail.skills && detail.skills.length">
              <div v-for="s in detail.skills" :key="s.skillKey" style="padding:8px 0;border-bottom:1px solid var(--border)">
                <span style="font-weight:700;color:var(--accent)">{{ s.name }}</span>
                <el-tag size="small" style="margin-left:6px">{{ s.type==='ACTIVE'?'主动':'被动' }}</el-tag>
                <el-tag size="small" type="warning" style="margin-left:4px">Lv.{{ s.level }}</el-tag>
                <p style="color:var(--muted);font-size:12px;margin-top:4px">{{ s.desc }}</p>
              </div>
            </div>
            <p v-else style="color:var(--muted);text-align:center;padding:20px">暂无技能</p>
          </el-tab-pane>
          <el-tab-pane label="签到/PVP">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="最后签到">{{ detail.lastSignDate || '无' }}</el-descriptions-item>
              <el-descriptions-item label="连续签到">{{ detail.consecutiveDays }}天</el-descriptions-item>
              <el-descriptions-item label="PVP积分">{{ detail.pvpScore }}</el-descriptions-item>
              <el-descriptions-item label="PVP星级">{{ detail.pvpStar }}</el-descriptions-item>
            </el-descriptions>
            <p style="margin-top:12px;font-weight:700">最近PVP记录：</p>
            <div v-if="detail.pvpRecords && detail.pvpRecords.length" style="max-height:200px;overflow-y:auto">
              <div v-for="r in detail.pvpRecords" :key="r.id" style="font-size:12px;padding:4px 0;border-bottom:1px solid var(--border)">
                <span :style="{color:r.result==='WIN'?'var(--green)':'var(--red)'}">{{ r.result==='WIN'?'胜':'败' }}</span>
                vs {{ r.defenderName }}
                <span :style="{color:r.scoreChange>0?'var(--green)':'var(--red)'}"> {{ r.scoreChange>0?'+':'' }}{{ r.scoreChange }}分</span>
              </div>
            </div>
            <p v-else style="color:var(--muted)">暂无PVP记录</p>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="showDetail=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
const players = ref([]); const total = ref(0); const currentPage = ref(1); const pageSize = 20
const keyword = ref(''); const filterJob = ref(''); const filterMinLevel = ref(null); const filterMaxLevel = ref(null)
const showEdit = ref(false); const editTarget = ref(null)
const showDetail = ref(false); const detail = ref(null)
const editForm = ref({ gold:0, diamond:0, level:1, exp:0, energy:0, spirit:0, skillPoint:0, pvpScore:0, attack:0, defense:0, mineLevel:1, farmLevel:1, forgeLevel:1 })
const adminHeaders = { headers: { Authorization: 'Bearer ' + localStorage.getItem('adminToken') } }

function qualityColor(q) { return {WHITE:'#9ca3af',GREEN:'#4ade80',BLUE:'#60a5fa',PURPLE:'#a855f7',ORANGE:'#f59e0b',RED:'#ef4444'}[q]||'#9ca3af' }

async function loadPlayers() {
  const params = { page: currentPage.value-1, size: pageSize, keyword: keyword.value }
  if (filterJob.value) params.job = filterJob.value
  if (filterMinLevel.value) params.minLevel = filterMinLevel.value
  if (filterMaxLevel.value) params.maxLevel = filterMaxLevel.value
  const r = await api.get('/api/admin/players', { params, ...adminHeaders })
  players.value = r.data.list || []; total.value = r.data.total || 0
}

async function viewDetail(row) {
  const r = await api.get('/api/admin/players/detail', { params: { userId: row.id }, ...adminHeaders })
  detail.value = r.data; showDetail.value = true
}

function openEdit(row) {
  editTarget.value = row
  editForm.value = { gold:row.gold||0, diamond:row.diamond||0, level:row.roleLevel||1, exp:row.exp||0, energy:row.energy||0, spirit:row.spirit||0, skillPoint:row.skillPoint||0, pvpScore:row.pvpScore||0, attack:row.attack||0, defense:row.defense||0, mineLevel:row.mineLevel||1, farmLevel:row.farmLevel||1, forgeLevel:row.forgeLevel||1 }
  showEdit.value = true
}

async function saveEdit() {
  await api.post('/api/admin/players/modify', { userId: editTarget.value.id, ...editForm.value }, adminHeaders)
  ElMessage.success('修改成功'); showEdit.value = false; loadPlayers()
}

async function resetPwd(row) {
  await ElMessageBox.confirm('确定将玩家 ' + row.username + ' 的密码重置为默认密码 qaz123 ？', '重置密码', { type: 'warning' })
  const r = await api.post('/api/admin/players/resetPassword', { userId: row.id }, adminHeaders)
  ElMessage.success(r.data.msg)
  if (r.data.emailSent) ElMessage.info('已发送通知邮件至 ' + r.data.email)
}

async function deletePlayer(row) {
  await ElMessageBox.confirm('确定删除玩家 ' + row.username + ' ？此操作不可恢复！', '删除玩家', { type: 'error' })
  await api.post('/api/admin/players/delete', { userId: row.id }, adminHeaders)
  ElMessage.success('玩家已删除'); loadPlayers()
}

onMounted(loadPlayers)
</script>
