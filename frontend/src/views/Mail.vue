<template>
  <div>
    <div class="game-card"><h3>邮件</h3></div>
    <div v-for="m in mails" :key="m.id" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:flex-start">
        <div style="flex:1">
          <span style="font-weight:700" :style="{color:m.isRead?'var(--muted)':'var(--accent)'}">{{ m.title }}</span>
          <p style="color:var(--text-secondary);font-size:13px;margin-top:6px;white-space:pre-line;line-height:1.6">{{ m.content }}</p>
          <!-- 附件信息 -->
          <div v-if="m.hasAttachment && m.items && m.items.length" style="margin-top:8px">
            <p style="font-size:12px;color:var(--muted);margin-bottom:4px">附件：</p>
            <div style="display:flex;flex-wrap:wrap;gap:6px">
              <div v-for="item in m.items" :key="item.key" class="mail-item-tag"
                   :class="item.isEquip ? 'equip-tag' : 'normal-tag'">
                <template v-if="item.isEquip">
                  <span :style="{color:qualityColor(item.quality)}">{{ item.name }}</span>
                  <span style="font-size:10px;color:var(--muted);margin-left:4px">{{ item.qualityName }}</span>
                  <span v-if="item.atk" style="font-size:10px;color:var(--muted);margin-left:4px">
                    攻+{{ item.atk }} 防+{{ item.def }} 血+{{ item.hp }}
                  </span>
                </template>
                <template v-else>
                  <span>{{ item.name }} x{{ item.count }}</span>
                </template>
              </div>
            </div>
          </div>
          <p style="color:var(--muted);font-size:11px;margin-top:6px">{{ m.createTime }}</p>
        </div>
        <div style="display:flex;gap:6px;align-items:center;flex-shrink:0;margin-left:12px">
          <el-button v-if="m.hasAttachment&&!m.isClaimed" size="small" type="success" @click="claim(m.id)">领取附件</el-button>
          <el-tag v-if="m.isClaimed" type="info" size="small">已领取</el-tag>
          <el-button size="small" type="danger" @click="confirmDelete(m)">删除</el-button>
        </div>
      </div>
    </div>
    <p v-if="!mails.length" style="color:var(--muted);text-align:center;padding:40px">暂无邮件</p>

    <!-- 删除确认弹窗 -->
    <el-dialog v-model="showDeleteConfirm" title="确认删除" width="360px">
      <p>确定删除邮件 <b style="color:var(--accent)">{{ deleteTarget?.title }}</b> ？</p>
      <p style="color:var(--red);font-size:13px;margin-top:8px">删除后无法恢复，是否确认？</p>
      <template #footer>
        <el-button @click="showDeleteConfirm=false">取消</el-button>
        <el-button type="danger" @click="doDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import api from '../utils/request'
import { ElMessage } from 'element-plus'
const store = useUserStore(); const mails = ref([])
const showDeleteConfirm = ref(false)
const deleteTarget = ref(null)

function qualityColor(q) {
  return {WHITE:'#9ca3af',GREEN:'#4ade80',BLUE:'#60a5fa',PURPLE:'#a855f7',ORANGE:'#f59e0b',RED:'#ef4444'}[q]||'#9ca3af'
}

async function load() { const r = await api.get('/api/mail/list'); mails.value = r.data }
onMounted(load)
async function claim(id) {
  try {
    const r = await api.post('/api/mail/claim',{mailId:id})
    ElMessage.success(r.data.msg)
    store.fetchRole(); load()
  } catch(e){}
}
function confirmDelete(m) { deleteTarget.value = m; showDeleteConfirm.value = true }
async function doDelete() {
  if(!deleteTarget.value) return
  try { const r = await api.post('/api/mail/delete',{mailId:deleteTarget.value.id}); ElMessage.success(r.data.msg || '删除成功'); load() } catch(e){}
  showDeleteConfirm.value = false; deleteTarget.value = null
}
</script>
<style scoped>
.mail-item-tag{display:inline-flex;align-items:center;padding:3px 8px;border-radius:6px;font-size:12px}
.normal-tag{background:var(--surface2);color:var(--accent);border:1px solid var(--border)}
.equip-tag{background:rgba(168,85,247,0.1);border:1px solid rgba(168,85,247,0.3)}
</style>
