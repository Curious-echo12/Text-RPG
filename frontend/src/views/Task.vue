<template>
  <div>
    <div class="game-card">
      <h3>每日任务</h3>
      <div style="text-align:center;padding:40px 20px">
        <p style="font-size:48px;margin-bottom:16px">🔧</p>
        <p style="color:var(--accent);font-size:18px;font-weight:700;margin-bottom:8px">任务系统维护中</p>
        <p style="color:var(--muted);font-size:14px">每日任务和成就系统正在优化升级，暂不可用</p>
        <p style="color:var(--muted);font-size:13px;margin-top:12px">请通过冒险、挖矿、种菜、锻造、竞技等玩法获取奖励</p>
      </div>
    </div>
  </div>
</template>
<script setup>
import { useUserStore } from '../store/user'
const store = useUserStore()
</script>
