<template>
  <div>
    <div class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <h3>公告管理</h3>
        <el-button type="primary" @click="openCreate">发布公告</el-button>
      </div>
    </div>

    <div v-for="a in announcements" :key="a.id" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:flex-start">
        <div style="flex:1">
          <div style="display:flex;align-items:center;gap:8px">
            <h4 style="margin:0;color:var(--accent)">{{ a.title }}</h4>
            <el-tag :type="a.isActive?'success':'info'" size="small">{{ a.isActive?'显示中':'已隐藏' }}</el-tag>
          </div>
          <p style="color:var(--muted);font-size:13px;margin-top:6px;white-space:pre-line">{{ a.content }}</p>
          <p style="color:var(--muted);font-size:11px;margin-top:4px">发布时间: {{ a.createTime }}</p>
        </div>
        <div style="display:flex;gap:6px;flex-shrink:0;margin-left:12px">
          <el-button size="small" @click="toggleActive(a)">{{ a.isActive?'隐藏':'显示' }}</el-button>
          <el-button size="small" type="warning" @click="openEdit(a)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAnnouncement(a)">删除</el-button>
        </div>
      </div>
    </div>
    <p v-if="!announcements.length" style="color:var(--muted);text-align:center;padding:30px">暂无公告</p>

    <!-- 创建/编辑公告弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit?'编辑公告':'发布公告'" width="600px">
      <el-form label-width="60px">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="公告标题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" placeholder="公告内容" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog=false">取消</el-button>
        <el-button type="primary" @click="saveAnnouncement">{{ isEdit?'保存':'发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
const announcements = ref([]); const showDialog = ref(false); const isEdit = ref(false)
const form = ref({ id:null, title:'', content:'' })
const adminHeaders = { headers: { Authorization: 'Bearer ' + localStorage.getItem('adminToken') } }
async function load() { const r = await api.get('/api/announcement/all', adminHeaders); announcements.value = r.data || [] }
onMounted(load)
function openCreate() { form.value = { id:null, title:'', content:'' }; isEdit.value = false; showDialog.value = true }
function openEdit(a) { form.value = { id:a.id, title:a.title, content:a.content }; isEdit.value = true; showDialog.value = true }
async function saveAnnouncement() {
  if (!form.value.title || !form.value.content) { ElMessage.warning('请填写标题和内容'); return }
  if (isEdit.value) { await api.post('/api/announcement/update', form.value, adminHeaders) }
  else { await api.post('/api/announcement/create', form.value, adminHeaders) }
  ElMessage.success(isEdit.value?'更新成功':'发布成功'); showDialog.value = false; load()
}
async function toggleActive(a) { await api.post('/api/announcement/toggle', { id: a.id }, adminHeaders); load() }
async function deleteAnnouncement(a) {
  await ElMessageBox.confirm('确定删除公告 "'+a.title+'" ？', '确认删除', { type: 'warning' })
  await api.post('/api/announcement/delete', { id: a.id }, adminHeaders); ElMessage.success('已删除'); load()
}
</script>
