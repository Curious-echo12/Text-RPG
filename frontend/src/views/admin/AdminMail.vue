<template>
  <div>
    <div class="game-card">
      <h3>发送系统邮件</h3>
    </div>

    <div class="game-card">
      <el-form label-width="80px">
        <el-form-item label="邮件标题">
          <el-input v-model="form.title" placeholder="输入邮件标题" />
        </el-form-item>
        <el-form-item label="邮件内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="输入邮件内容" />
        </el-form-item>

        <el-divider>附件物品（可选）</el-divider>
        <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:12px">
          <div v-for="(item, idx) in form.itemList" :key="idx" style="display:flex;align-items:center;gap:4px">
            <el-select v-model="item.key" placeholder="物品" style="width:140px" size="small">
              <el-option v-for="k in itemOptions" :key="k.key" :label="k.name" :value="k.key" />
            </el-select>
            <el-input-number v-model="item.count" :min="1" size="small" style="width:90px" />
            <el-button size="small" type="danger" @click="form.itemList.splice(idx,1)">删除</el-button>
          </div>
          <el-button size="small" @click="form.itemList.push({key:'gold',count:100})">+ 添加物品</el-button>
        </div>

        <el-divider>附件装备（可选，随邮件一起发送）</el-divider>
        <div style="display:flex;gap:12px;margin-bottom:12px;flex-wrap:wrap;align-items:center">
          <el-select v-model="form.equipItem" placeholder="选择装备" clearable style="width:160px" size="small">
            <el-option v-for="k in equipOptions" :key="k.key" :label="k.name" :value="k.key" />
          </el-select>
          <el-select v-model="form.equipQuality" placeholder="品质" style="width:120px" size="small">
            <el-option v-for="q in qualityOptions" :key="q.key" :label="q.name" :value="q.key" />
          </el-select>
          <span v-if="form.equipItem" style="color:var(--muted);font-size:12px">
            预览：{{ equipPreview }}
          </span>
        </div>
        <p style="color:var(--muted);font-size:11px;margin-bottom:12px">装备将作为邮件附件，玩家领取后进入背包</p>

        <el-divider>发送对象</el-divider>
        <el-form-item label="发送方式">
          <el-radio-group v-model="form.sendMode">
            <el-radio label="all">全选所有玩家</el-radio>
            <el-radio label="select">指定玩家</el-radio>
            <el-radio label="filter">条件筛选</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.sendMode==='select'" label="选择玩家">
          <el-select v-model="form.userIds" multiple filterable placeholder="搜索并选择玩家" style="width:100%">
            <el-option v-for="p in playerOptions" :key="p.userId"
              :label="p.playerId+' - '+(p.roleName||p.username)+' Lv.'+(p.level||0)" :value="p.userId" />
          </el-select>
        </el-form-item>

        <template v-if="form.sendMode==='filter'">
          <el-form-item label="职业">
            <el-select v-model="form.filterJob" clearable placeholder="不限职业" style="width:200px">
              <el-option label="战士" value="WARRIOR" /><el-option label="法师" value="MAGE" />
              <el-option label="射手" value="ARCHER" /><el-option label="牧师" value="PRIEST" />
              <el-option label="矿工" value="MINER" />
            </el-select>
          </el-form-item>
          <el-form-item label="等级范围">
            <div style="display:flex;gap:8px;align-items:center">
              <el-input-number v-model="form.filterMinLevel" :min="1" :max="999" placeholder="最低" style="width:120px" size="small" />
              <span>~</span>
              <el-input-number v-model="form.filterMaxLevel" :min="1" :max="999" placeholder="最高" style="width:120px" size="small" />
            </div>
          </el-form-item>
          <p style="color:var(--accent);font-size:12px;margin-bottom:12px">
            将发送给{{ filterDesc }}
          </p>
        </template>
      </el-form>

      <div style="text-align:right;margin-top:16px">
        <el-button type="primary" size="large" @click="sendMail" :loading="sending">
          发送邮件
        </el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
const sending = ref(false); const playerOptions = ref([])
const adminHeaders = { headers: { Authorization: 'Bearer ' + localStorage.getItem('adminToken') } }
const form = ref({
  title:'', content:'', itemList:[], sendMode:'all', userIds:[],
  equipItem:'', equipQuality:'WHITE',
  filterJob:'', filterMinLevel:null, filterMaxLevel:null
})

const itemOptions = [
  { key:'gold', name:'金币' }, { key:'diamond', name:'钻石' },
  { key:'mana_potion', name:'精力药水' }, { key:'strengthen_stone', name:'强化石' },
  { key:'skill_book', name:'技能书' }, { key:'iron_ore', name:'铁矿石' },
  { key:'copper_ore', name:'铜矿石' }, { key:'silver_ore', name:'银矿石' },
  { key:'gold_ore', name:'金矿石' }, { key:'mithril_ore', name:'秘银矿石' },
  { key:'adamantite_ore', name:'精金矿石' }, { key:'star_ore', name:'星辰矿石' }
]

const equipOptions = [
  { key:'sword', name:'铁剑' }, { key:'helmet', name:'银盔' },
  { key:'armor', name:'金甲' }, { key:'pants', name:'秘银护腿' },
  { key:'boots', name:'精金战靴' }, { key:'necklace', name:'星辰项链' },
  { key:'ring', name:'星辰戒指' }
]

const qualityOptions = [
  { key:'WHITE', name:'白色·普通' }, { key:'GREEN', name:'绿色·优秀' },
  { key:'BLUE', name:'蓝色·稀有' }, { key:'PURPLE', name:'紫色·史诗' },
  { key:'ORANGE', name:'橙色·传说' }, { key:'RED', name:'红色·神话' }
]

const qualityMult = { WHITE:1, GREEN:1.15, BLUE:1.35, PURPLE:1.6, ORANGE:1.9, RED:2.3 }
const equipBase = { sword:[12,3,18], helmet:[3,8,35], armor:[5,12,55], pants:[3,10,40], boots:[4,5,18], necklace:[10,3,30], ring:[12,2,18] }

const equipPreview = computed(() => {
  if (!form.value.equipItem) return ''
  const base = equipBase[form.value.equipItem]
  if (!base) return ''
  const mult = qualityMult[form.value.equipQuality] || 1
  return `攻击+${Math.floor(base[0]*mult)} 防御+${Math.floor(base[1]*mult)} 生命+${Math.floor(base[2]*mult)}`
})

const filterDesc = computed(() => {
  const parts = []
  if (form.value.filterJob) {
    const names = {WARRIOR:'战士',MAGE:'法师',ARCHER:'射手',PRIEST:'牧师',MINER:'矿工'}
    parts.push(names[form.value.filterJob] || form.value.filterJob)
  }
  if (form.value.filterMinLevel) parts.push('Lv.'+form.value.filterMinLevel+'以上')
  if (form.value.filterMaxLevel) parts.push('Lv.'+form.value.filterMaxLevel+'以下')
  return parts.length ? parts.join('、') + '的玩家' : '所有玩家'
})

async function loadPlayers() { const r = await api.get('/api/admin/players/options', adminHeaders); playerOptions.value = r.data || [] }
onMounted(loadPlayers)

async function sendMail() {
  if (!form.value.title || !form.value.content) { ElMessage.warning('请填写标题和内容'); return }
  if (form.value.sendMode==='select' && form.value.userIds.length===0) { ElMessage.warning('请选择发送对象'); return }

  const items = {}
  for (const item of form.value.itemList) { if (item.key && item.count > 0) items[item.key] = item.count }

  const params = {
    title: form.value.title,
    content: form.value.content,
    items,
    sendToAll: form.value.sendMode === 'all',
    userIds: form.value.sendMode === 'select' ? form.value.userIds : [],
    equipItem: form.value.equipItem || null,
    equipQuality: form.value.equipQuality,
    filterJob: form.value.sendMode === 'filter' ? form.value.filterJob : null,
    filterMinLevel: form.value.sendMode === 'filter' ? form.value.filterMinLevel : null,
    filterMaxLevel: form.value.sendMode === 'filter' ? form.value.filterMaxLevel : null
  }

  const confirmMsg = form.value.sendMode === 'all' ? '确定发送给所有玩家？' :
    form.value.sendMode === 'select' ? '确定发送给选中的 '+form.value.userIds.length+' 位玩家？' :
    '确定发送给' + filterDesc.value + '？'

  await ElMessageBox.confirm(confirmMsg, '确认发送', { type: 'warning' })
  sending.value = true
  try {
    const r = await api.post('/api/admin/mail/send', params, adminHeaders)
    ElMessage.success(r.data.msg)
    form.value = { title:'', content:'', itemList:[], sendMode:'all', userIds:[], equipItem:'', equipQuality:'WHITE', filterJob:'', filterMinLevel:null, filterMaxLevel:null }
  } catch(e) {} finally { sending.value = false }
}
</script>
