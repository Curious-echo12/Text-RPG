<template>
  <div>
    <div class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <h3>游戏配置管理</h3>
        <el-button type="warning" @click="reloadAll">重新加载所有配置</el-button>
      </div>
      <p style="color:var(--muted);font-size:12px;margin-top:4px">所有修改点击保存后即时生效，无需重启服务器</p>
    </div>

    <div class="game-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="职业属性" name="JOB_BASE" />
        <el-tab-pane label="职业成长" name="JOB_GROWTH" />
        <el-tab-pane label="物品管理" name="ITEMS" />
        <el-tab-pane label="矿洞配置" name="MINES" />
        <el-tab-pane label="冒险章节" name="CHAPTERS" />
        <el-tab-pane label="副本配置" name="DUNGEONS" />
        <el-tab-pane label="锻造配方" name="FORGE_RECIPES" />
        <el-tab-pane label="品质倍率" name="QUALITY_MULT" />
        <el-tab-pane label="装备属性" name="QUALITY_ATTR_BASE" />
        <el-tab-pane label="商店物品" name="SHOP_ITEMS" />
        <el-tab-pane label="技能配置" name="SKILLS" />
        <el-tab-pane label="成就配置" name="ACHIEVEMENTS" />
        <el-tab-pane label="每日任务" name="DAILY_TASKS" />
        <el-tab-pane label="种植作物" name="CROPS" />
        <el-tab-pane label="秘境事件" name="SECRET_EVENTS" />
        <el-tab-pane label="基础参数" name="GAME_SETTINGS" />
      </el-tabs>
    </div>

    <!-- ========== 职业属性 JOB_BASE ========== -->
    <div v-if="activeTab==='JOB_BASE'" class="game-card">
      <h3>职业基础属性 [生命, 攻击, 防御, 速度]</h3>
      <div class="table-wrap">
        <el-table :data="jobBaseData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column prop="job" label="职业" min-width="100"><template #default="{row}">{{ jobNames[row.job]||row.job }}</template></el-table-column>
          <el-table-column label="生命" min-width="140"><template #default="{row}"><el-input-number v-model="row.hp" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="攻击" min-width="140"><template #default="{row}"><el-input-number v-model="row.atk" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="防御" min-width="140"><template #default="{row}"><el-input-number v-model="row.def" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="速度" min-width="140"><template #default="{row}"><el-input-number v-model="row.spd" :min="1" size="small" style="width:120px" /></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveJobBase">保存</el-button></div>
    </div>

    <!-- ========== 职业成长 JOB_GROWTH ========== -->
    <div v-if="activeTab==='JOB_GROWTH'" class="game-card">
      <h3>职业每级成长 [生命, 攻击, 防御, 速度]</h3>
      <div class="table-wrap">
        <el-table :data="jobGrowthData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column prop="job" label="职业" min-width="100"><template #default="{row}">{{ jobNames[row.job]||row.job }}</template></el-table-column>
          <el-table-column label="生命" min-width="140"><template #default="{row}"><el-input-number v-model="row.hp" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="攻击" min-width="140"><template #default="{row}"><el-input-number v-model="row.atk" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="防御" min-width="140"><template #default="{row}"><el-input-number v-model="row.def" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="速度" min-width="140"><template #default="{row}"><el-input-number v-model="row.spd" :min="1" size="small" style="width:120px" /></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveJobGrowth">保存</el-button></div>
    </div>

    <!-- ========== 物品管理 ITEMS ========== -->
    <div v-if="activeTab==='ITEMS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>物品管理（共 {{ itemsData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="addItem">+ 新增物品</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="itemsData" size="small" max-height="500" show-overflow-tooltip>
          <el-table-column prop="key" label="Key" min-width="140"><template #default="{row}"><el-input v-model="row.key" size="small" /></template></el-table-column>
          <el-table-column prop="name" label="名称" min-width="120"><template #default="{row}"><el-input v-model="row.name" size="small" /></template></el-table-column>
          <el-table-column prop="type" label="类型" min-width="130"><template #default="{row}">
            <el-select v-model="row.type" size="small" style="width:100%"><el-option v-for="t in itemTypes" :key="t" :label="t" :value="t" /></el-select>
          </template></el-table-column>
          <el-table-column prop="subType" label="子类型" min-width="110"><template #default="{row}"><el-input v-model="row.subType" size="small" /></template></el-table-column>
          <el-table-column prop="sellPrice" label="售价" min-width="120"><template #default="{row}"><el-input-number v-model="row.sellPrice" :min="0" size="small" style="width:100px" /></template></el-table-column>
          <el-table-column prop="desc" label="描述" min-width="280"><template #default="{row}"><el-input v-model="row.desc" size="small" /></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{row,$index}"><el-button size="small" type="danger" @click="itemsData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveItems">保存</el-button></div>
    </div>

    <!-- ========== 矿洞配置 MINES ========== -->
    <div v-if="activeTab==='MINES'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>矿洞配置（共 {{ minesData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="addMine">+ 新增矿洞</el-button>
      </div>
      <div v-for="(m,idx) in minesData" :key="idx" class="config-card">
        <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:center">
          <el-input v-model="m.name" placeholder="矿洞名称" style="width:160px" size="small" />
          <el-input-number v-model="m.energyCost" :min="1" size="small" style="width:120px" /><span style="font-size:12px;color:var(--muted)">体力</span>
          <el-input-number v-model="m.cooldown" :min="1" size="small" style="width:120px" /><span style="font-size:12px;color:var(--muted)">秒冷却</span>
          <el-input v-model="m.desc" placeholder="描述" style="flex:1;min-width:200px" size="small" />
          <el-button size="small" type="danger" @click="minesData.splice(idx,1)">删除矿洞</el-button>
        </div>
        <div style="margin-top:8px">
          <p style="font-size:12px;color:var(--muted);margin-bottom:4px">矿石掉落：</p>
          <div v-for="(dk,dIdx) in m.dropKeys" :key="dIdx" style="display:flex;gap:6px;align-items:center;margin-bottom:4px">
            <el-select v-model="m.dropKeys[dIdx]" size="small" style="width:160px" filterable><el-option v-for="k in oreKeys" :key="k" :label="itemNames[k]||k" :value="k" /></el-select>
            <el-input-number v-model="m.weights[dIdx]" :min="1" size="small" style="width:120px" /><span style="font-size:11px;color:var(--muted)">权重</span>
            <el-button size="small" type="danger" link @click="m.dropKeys.splice(dIdx,1);m.weights.splice(dIdx,1)">删</el-button>
          </div>
          <el-button size="small" @click="m.dropKeys.push('iron_ore');m.weights.push(10)">+ 添加矿石</el-button>
        </div>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveMines">保存</el-button></div>
    </div>

    <!-- ========== 冒险章节 CHAPTERS ========== -->
    <div v-if="activeTab==='CHAPTERS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>冒险章节（共 {{ chaptersData.length }} 章）</h3>
        <el-button type="primary" size="small" @click="addChapter">+ 新增章节</el-button>
      </div>
      <div v-for="(ch,cIdx) in chaptersData" :key="cIdx" class="config-card">
        <div style="display:flex;gap:12px;align-items:center;margin-bottom:8px">
          <span style="font-weight:700">第{{ ch.chapter }}章</span>
          <el-input v-model="ch.name" style="width:160px" size="small" placeholder="章节名" />
          <span style="font-size:12px;color:var(--muted)">等级要求:</span>
          <el-input-number v-model="ch.levelReq" :min="1" size="small" style="width:100px" />
          <el-button size="small" type="danger" @click="chaptersData.splice(cIdx,1)">删除章节</el-button>
        </div>
        <div v-for="(s,sIdx) in ch.stages" :key="sIdx" style="background:var(--surface2);border-radius:6px;padding:10px;margin-bottom:6px">
          <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center">
            <el-input v-model="s.key" style="width:120px" size="small" placeholder="key" />
            <el-input v-model="s.name" style="width:120px" size="small" placeholder="怪物名" />
            <span style="font-size:11px">Lv</span><el-input-number v-model="s.level" :min="1" size="small" style="width:80px" />
            <span style="font-size:11px">HP</span><el-input-number v-model="s.hp" :min="1" size="small" style="width:100px" />
            <span style="font-size:11px">ATK</span><el-input-number v-model="s.atk" :min="1" size="small" style="width:90px" />
            <span style="font-size:11px">DEF</span><el-input-number v-model="s.def" :min="0" size="small" style="width:90px" />
            <span style="font-size:11px">SPD</span><el-input-number v-model="s.spd" :min="1" size="small" style="width:80px" />
            <span style="font-size:11px">EXP</span><el-input-number v-model="s.expReward" :min="0" size="small" style="width:90px" />
            <span style="font-size:11px">Gold</span><el-input-number v-model="s.goldReward" :min="0" size="small" style="width:90px" />
            <el-button size="small" type="danger" link @click="ch.stages.splice(sIdx,1)">删除怪物</el-button>
          </div>
          <div v-if="s.dropKeys" style="margin-top:6px;display:flex;gap:6px;flex-wrap:wrap;align-items:center">
            <span style="font-size:11px;color:var(--muted)">掉落:</span>
            <div v-for="(dk,dIdx) in s.dropKeys" :key="dIdx" style="display:flex;gap:4px;align-items:center">
              <el-select v-model="s.dropKeys[dIdx]" size="small" style="width:140px" filterable><el-option v-for="k in itemKeys" :key="k" :label="itemNames[k]||k" :value="k" /></el-select>
              <el-input-number v-model="s.dropRates[dIdx]" :min="0" size="small" style="width:100px" />
              <el-button size="small" type="danger" link @click="s.dropKeys.splice(dIdx,1);s.dropRates.splice(dIdx,1)">删</el-button>
            </div>
            <el-button size="small" link @click="if(!s.dropKeys)s.dropKeys=[];if(!s.dropRates)s.dropRates=[];s.dropKeys.push('iron_ore');s.dropRates.push(1000)">+掉落</el-button>
          </div>
        </div>
        <el-button size="small" @click="ch.stages.push({key:'new_monster',name:'新怪物',level:1,hp:100,atk:10,def:5,spd:5,expReward:10,goldReward:5,dropKeys:[],dropRates:[]})">+ 添加怪物</el-button>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveChapters">保存</el-button></div>
    </div>

    <!-- ========== 副本配置 DUNGEONS ========== -->
    <div v-if="activeTab==='DUNGEONS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>副本配置（共 {{ dungeonsData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="addDungeon">+ 新增副本</el-button>
      </div>
      <div v-for="(d,idx) in dungeonsData" :key="idx" class="config-card">
        <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:center;margin-bottom:8px">
          <el-input v-model="d.key" style="width:140px" size="small" placeholder="key" />
          <el-input v-model="d.name" style="width:140px" size="small" placeholder="副本名" />
          <span style="font-size:11px">等级</span><el-input-number v-model="d.levelReq" :min="1" size="small" style="width:80px" />
          <span style="font-size:11px">体力</span><el-input-number v-model="d.energyCost" :min="1" size="small" style="width:80px" />
          <span style="font-size:11px">次数</span><el-input-number v-model="d.dailyLimit" :min="1" size="small" style="width:80px" />
          <el-input v-model="d.desc" style="flex:1;min-width:150px" size="small" placeholder="描述" />
          <el-button size="small" type="danger" @click="dungeonsData.splice(idx,1)">删除副本</el-button>
        </div>
        <div style="background:var(--surface2);border-radius:6px;padding:10px;margin-bottom:4px">
          <p style="font-size:12px;font-weight:700;margin-bottom:6px">Boss：</p>
          <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center">
            <el-input v-model="d.boss.key" style="width:120px" size="small" placeholder="key" />
            <el-input v-model="d.boss.name" style="width:120px" size="small" placeholder="Boss名" />
            <span style="font-size:11px">Lv</span><el-input-number v-model="d.boss.level" :min="1" size="small" style="width:80px" />
            <span style="font-size:11px">HP</span><el-input-number v-model="d.boss.hp" :min="1" size="small" style="width:100px" />
            <span style="font-size:11px">ATK</span><el-input-number v-model="d.boss.atk" :min="1" size="small" style="width:90px" />
            <span style="font-size:11px">DEF</span><el-input-number v-model="d.boss.def" :min="0" size="small" style="width:90px" />
            <span style="font-size:11px">SPD</span><el-input-number v-model="d.boss.spd" :min="1" size="small" style="width:80px" />
            <span style="font-size:11px">EXP</span><el-input-number v-model="d.boss.expReward" :min="0" size="small" style="width:90px" />
            <span style="font-size:11px">Gold</span><el-input-number v-model="d.boss.goldReward" :min="0" size="small" style="width:90px" />
          </div>
          <div style="margin-top:6px;display:flex;gap:6px;flex-wrap:wrap;align-items:center">
            <span style="font-size:11px;color:var(--muted)">掉落:</span>
            <div v-for="(dk,dIdx) in d.boss.dropKeys" :key="dIdx" style="display:flex;gap:4px;align-items:center">
              <el-select v-model="d.boss.dropKeys[dIdx]" size="small" style="width:140px" filterable><el-option v-for="k in itemKeys" :key="k" :label="itemNames[k]||k" :value="k" /></el-select>
              <el-input-number v-model="d.boss.dropRates[dIdx]" :min="0" size="small" style="width:100px" />
              <el-button size="small" type="danger" link @click="d.boss.dropKeys.splice(dIdx,1);d.boss.dropRates.splice(dIdx,1)">删</el-button>
            </div>
            <el-button size="small" link @click="if(!d.boss.dropKeys)d.boss.dropKeys=[];if(!d.boss.dropRates)d.boss.dropRates=[];d.boss.dropKeys.push('iron_ore');d.boss.dropRates.push(1000)">+掉落</el-button>
          </div>
        </div>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveDungeons">保存</el-button></div>
    </div>

    <!-- ========== 锻造配方 FORGE_RECIPES ========== -->
    <div v-if="activeTab==='FORGE_RECIPES'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>锻造配方（共 {{ forgeData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="addForgeRecipe">+ 新增配方</el-button>
      </div>
      <div v-for="(r,idx) in forgeData" :key="idx" class="config-card">
        <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:center;margin-bottom:8px">
          <el-input v-model="r.resultKey" style="width:120px" size="small" placeholder="key" />
          <el-input v-model="r.resultName" style="width:120px" size="small" placeholder="装备名" />
          <el-select v-model="r.part" size="small" style="width:120px"><el-option v-for="p in parts" :key="p" :label="partNames[p]||p" :value="p" /></el-select>
          <span style="font-size:11px">金币</span><el-input-number v-model="r.goldCost" :min="0" size="small" style="width:110px" />
          <el-button size="small" type="danger" @click="forgeData.splice(idx,1)">删除配方</el-button>
        </div>
        <div style="display:flex;gap:24px;flex-wrap:wrap">
          <div>
            <p style="font-size:11px;color:var(--muted);margin-bottom:4px">材料：</p>
            <div v-for="(m,mIdx) in r._materials" :key="mIdx" style="display:flex;gap:4px;align-items:center;margin-bottom:4px">
              <el-select v-model="m.key" size="small" style="width:140px" filterable><el-option v-for="k in oreKeys" :key="k" :label="itemNames[k]||k" :value="k" /></el-select>
              <el-input-number v-model="m.count" :min="1" size="small" style="width:100px" />
              <el-button size="small" type="danger" link @click="r._materials.splice(mIdx,1)">删</el-button>
            </div>
            <el-button size="small" @click="r._materials.push({key:'iron_ore',count:1})">+ 添加材料</el-button>
          </div>
          <div>
            <p style="font-size:11px;color:var(--muted);margin-bottom:4px">品质概率：</p>
            <div style="display:flex;gap:8px;flex-wrap:wrap">
              <div v-for="q in qualities" :key="q.key" style="display:flex;gap:4px;align-items:center">
                <span :style="{color:q.color,fontSize:'12px',width:'30px',fontWeight:600}">{{ q.label }}</span>
                <el-input-number v-model="r.qualityWeights[q.key]" :min="0" size="small" style="width:90px" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveForge">保存</el-button></div>
    </div>

    <!-- ========== 品质倍率 QUALITY_MULT ========== -->
    <div v-if="activeTab==='QUALITY_MULT'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>品质属性倍率</h3>
        <el-button type="primary" size="small" @click="qualityMultData.push({key:'NEW',value:1.0})">+ 新增品质</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="qualityMultData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column label="品质Key" min-width="140"><template #default="{row}"><el-input v-model="row.key" size="small" /></template></el-table-column>
          <el-table-column label="倍率" min-width="200"><template #default="{row}"><el-input-number v-model="row.value" :min="0.1" :step="0.05" :precision="2" size="small" style="width:160px" /></template></el-table-column>
          <el-table-column label="说明" min-width="250"><template #default="{row}"><span style="color:var(--muted);font-size:12px">装备属性 × {{ row.value }}</span></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{$index}"><el-button size="small" type="danger" @click="qualityMultData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveQualityMult">保存</el-button></div>
    </div>

    <!-- ========== 装备属性 QUALITY_ATTR_BASE ========== -->
    <div v-if="activeTab==='QUALITY_ATTR_BASE'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>装备部位基础属性 [攻击, 防御, 生命]</h3>
        <el-button type="primary" size="small" @click="qualityAttrData.push({part:'NEW',atk:5,def:3,hp:15})">+ 新增部位</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="qualityAttrData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column label="部位Key" min-width="140"><template #default="{row}"><el-input v-model="row.part" size="small" /></template></el-table-column>
          <el-table-column label="攻击" min-width="140"><template #default="{row}"><el-input-number v-model="row.atk" :min="0" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="防御" min-width="140"><template #default="{row}"><el-input-number v-model="row.def" :min="0" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="生命" min-width="140"><template #default="{row}"><el-input-number v-model="row.hp" :min="0" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{$index}"><el-button size="small" type="danger" @click="qualityAttrData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveQualityAttr">保存</el-button></div>
    </div>

    <!-- ========== 商店物品 SHOP_ITEMS ========== -->
    <div v-if="activeTab==='SHOP_ITEMS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>商店物品（共 {{ shopData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="shopData.push({itemKey:'mana_potion',price:10,currency:'GOLD',stock:10})">+ 新增</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="shopData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column label="物品" min-width="180"><template #default="{row}"><el-select v-model="row.itemKey" size="small" style="width:100%" filterable><el-option v-for="k in itemKeys" :key="k" :label="itemNames[k]||k" :value="k" /></el-select></template></el-table-column>
          <el-table-column label="价格" min-width="140"><template #default="{row}"><el-input-number v-model="row.price" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="货币" min-width="130"><template #default="{row}"><el-select v-model="row.currency" size="small"><el-option label="金币" value="GOLD" /><el-option label="钻石" value="DIAMOND" /></el-select></template></el-table-column>
          <el-table-column label="库存" min-width="140"><template #default="{row}"><el-input-number v-model="row.stock" :min="1" size="small" style="width:120px" /></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{$index}"><el-button size="small" type="danger" @click="shopData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveShop">保存</el-button></div>
    </div>

    <!-- ========== 技能配置 SKILLS ========== -->
    <div v-if="activeTab==='SKILLS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>技能配置（共 {{ skillsData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="addSkill">+ 新增技能</el-button>
      </div>
      <div v-for="(s,idx) in skillsData" :key="idx" class="config-card">
        <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:8px">
          <el-input v-model="s.key" style="width:140px" size="small" placeholder="key" />
          <el-input v-model="s.name" style="width:100px" size="small" placeholder="名称" />
          <el-select v-model="s.job" size="small" style="width:80px"><el-option v-for="j in jobKeys" :key="j" :label="jobNames[j]" :value="j" /></el-select>
          <el-select v-model="s.type" size="small" style="width:80px"><el-option label="主动" value="ACTIVE" /><el-option label="被动" value="PASSIVE" /></el-select>
          <span style="font-size:11px">解锁Lv</span><el-input-number v-model="s.unlockLevel" :min="1" size="small" style="width:70px" />
          <span style="font-size:11px">最高Lv</span><el-input-number v-model="s.maxLevel" :min="1" size="small" style="width:70px" />
          <span style="font-size:11px">金币</span><el-input-number v-model="s.goldCost" :min="0" size="small" style="width:80px" />
          <span style="font-size:11px">技能书</span><el-input-number v-model="s.bookCost" :min="0" size="small" style="width:70px" />
          <el-button size="small" type="danger" @click="skillsData.splice(idx,1)">删除技能</el-button>
        </div>
        <el-input v-model="s.desc" size="small" placeholder="技能描述" style="margin-bottom:8px" />
        <div style="background:var(--surface2);border-radius:6px;padding:10px">
          <p style="font-size:12px;font-weight:700;margin-bottom:6px">效果参数：</p>
          <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:center">
            <div>
              <span style="font-size:11px;color:var(--muted)">效果类型</span>
              <el-select v-model="s.effectType" size="small" style="width:140px;display:block;margin-top:4px">
                <el-option label="伤害(DAMAGE)" value="DAMAGE" />
                <el-option label="治疗(HEAL)" value="HEAL" />
                <el-option label="伤害+治疗" value="DAMAGE_HEAL" />
                <el-option label="被动-防御" value="PASSIVE_DEF" />
                <el-option label="被动-生命" value="PASSIVE_HP" />
                <el-option label="被动-速度" value="PASSIVE_SPD" />
                <el-option label="被动-攻击" value="PASSIVE_ATK" />
                <el-option label="挖矿加成" value="MINE_BONUS" />
                <el-option label="稀有矿概率" value="MINE_RARE" />
                <el-option label="体力上限" value="ENERGY_MAX" />
              </el-select>
            </div>
            <div>
              <span style="font-size:11px;color:var(--muted)">基础值</span>
              <el-input-number v-model="s.baseValue" :step="0.01" size="small" style="width:110px;display:block;margin-top:4px" />
            </div>
            <div>
              <span style="font-size:11px;color:var(--muted)">每级增量</span>
              <el-input-number v-model="s.valuePerLevel" :step="0.01" size="small" style="width:110px;display:block;margin-top:4px" />
            </div>
            <div v-if="s.effectType==='DAMAGE_HEAL'">
              <span style="font-size:11px;color:var(--muted)">回血比例</span>
              <el-input-number v-model="s.secondaryValue" :step="0.01" :min="0" :max="1" size="small" style="width:110px;display:block;margin-top:4px" />
            </div>
            <div style="flex:1;min-width:200px">
              <span style="font-size:11px;color:var(--muted)">效果预览</span>
              <p style="color:var(--green);font-size:12px;margin-top:4px">{{ skillEffectPreview(s) }}</p>
            </div>
          </div>
        </div>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveSkills">保存</el-button></div>
    </div>

    <!-- ========== 成就配置 ACHIEVEMENTS ========== -->
    <div v-if="activeTab==='ACHIEVEMENTS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>成就配置（共 {{ achieveData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="achieveData.push({key:'new_achieve',name:'新成就',desc:'描述',target:1,rewardType:'gold',rewardAmount:100})">+ 新增</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="achieveData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column label="Key" min-width="130"><template #default="{row}"><el-input v-model="row.key" size="small" /></template></el-table-column>
          <el-table-column label="名称" min-width="120"><template #default="{row}"><el-input v-model="row.name" size="small" /></template></el-table-column>
          <el-table-column label="描述" min-width="200"><template #default="{row}"><el-input v-model="row.desc" size="small" /></template></el-table-column>
          <el-table-column label="目标值" min-width="120"><template #default="{row}"><el-input-number v-model="row.target" :min="1" size="small" style="width:100px" /></template></el-table-column>
          <el-table-column label="奖励类型" min-width="120"><template #default="{row}"><el-select v-model="row.rewardType" size="small"><el-option label="金币" value="gold" /><el-option label="钻石" value="diamond" /></el-select></template></el-table-column>
          <el-table-column label="奖励数量" min-width="120"><template #default="{row}"><el-input-number v-model="row.rewardAmount" :min="1" size="small" style="width:100px" /></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{$index}"><el-button size="small" type="danger" @click="achieveData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveAchievements">保存</el-button></div>
    </div>

    <!-- ========== 每日任务 DAILY_TASKS ========== -->
    <div v-if="activeTab==='DAILY_TASKS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>每日任务（共 {{ taskData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="taskData.push({key:'daily_new',name:'新任务',desc:'描述',target:1,rewardType:'gold',rewardAmount:50})">+ 新增</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="taskData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column label="Key" min-width="130"><template #default="{row}"><el-input v-model="row.key" size="small" /></template></el-table-column>
          <el-table-column label="名称" min-width="120"><template #default="{row}"><el-input v-model="row.name" size="small" /></template></el-table-column>
          <el-table-column label="描述" min-width="200"><template #default="{row}"><el-input v-model="row.desc" size="small" /></template></el-table-column>
          <el-table-column label="目标次数" min-width="120"><template #default="{row}"><el-input-number v-model="row.target" :min="1" size="small" style="width:100px" /></template></el-table-column>
          <el-table-column label="奖励类型" min-width="120"><template #default="{row}"><el-select v-model="row.rewardType" size="small"><el-option label="金币" value="gold" /><el-option label="钻石" value="diamond" /></el-select></template></el-table-column>
          <el-table-column label="奖励数量" min-width="120"><template #default="{row}"><el-input-number v-model="row.rewardAmount" :min="1" size="small" style="width:100px" /></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{$index}"><el-button size="small" type="danger" @click="taskData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveTasks">保存</el-button></div>
    </div>

    <!-- ========== 种植作物 CROPS ========== -->
    <div v-if="activeTab==='CROPS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>种植作物（共 {{ cropData.length }} 种）</h3>
        <el-button type="primary" size="small" @click="cropData.push({cropKey:'new_crop',name:'新作物',growMinutes:5,baseYield:3,desc:'描述'})">+ 新增</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="cropData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column label="Key" min-width="130"><template #default="{row}"><el-input v-model="row.cropKey" size="small" /></template></el-table-column>
          <el-table-column label="名称" min-width="120"><template #default="{row}"><el-input v-model="row.name" size="small" /></template></el-table-column>
          <el-table-column label="成熟时间(分)" min-width="130"><template #default="{row}"><el-input-number v-model="row.growMinutes" :min="1" size="small" style="width:110px" /></template></el-table-column>
          <el-table-column label="基础产量" min-width="120"><template #default="{row}"><el-input-number v-model="row.baseYield" :min="1" size="small" style="width:100px" /></template></el-table-column>
          <el-table-column label="描述" min-width="250"><template #default="{row}"><el-input v-model="row.desc" size="small" /></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{$index}"><el-button size="small" type="danger" @click="cropData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveCrops">保存</el-button></div>
    </div>

    <!-- ========== 秘境事件 SECRET_EVENTS ========== -->
    <div v-if="activeTab==='SECRET_EVENTS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>秘境事件（共 {{ secretData.length }} 个）</h3>
        <el-button type="primary" size="small" @click="addSecretEvent">+ 新增事件</el-button>
      </div>
      <div v-for="(e,idx) in secretData" :key="idx" class="config-card">
        <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:center">
          <el-select v-model="e.type" size="small" style="width:110px">
            <el-option label="战斗" value="BATTLE" /><el-option label="宝箱" value="CHEST" />
            <el-option label="陷阱" value="TRAP" /><el-option label="奇遇" value="WONDER" />
          </el-select>
          <el-input v-model="e.desc" style="width:220px" size="small" placeholder="事件描述" />
          <template v-if="e.type!=='BATTLE'">
            <el-select v-model="e.rewardType" size="small" style="width:110px"><el-option label="金币" value="GOLD" /><el-option label="钻石" value="DIAMOND" /><el-option label="物品" value="ITEM" /></el-select>
            <el-input-number v-model="e.rewardAmount" size="small" style="width:110px" />
            <template v-if="e.rewardType==='ITEM'">
              <el-select v-model="e.rewardItemKey" size="small" style="width:140px" filterable><el-option v-for="k in itemKeys" :key="k" :label="itemNames[k]||k" :value="k" /></el-select>
            </template>
          </template>
          <el-button size="small" type="danger" @click="secretData.splice(idx,1)">删除事件</el-button>
        </div>
        <template v-if="e.type==='BATTLE'">
          <div style="background:var(--surface2);border-radius:6px;padding:10px;margin-top:8px">
            <p style="font-size:11px;color:var(--muted);margin-bottom:6px">怪物属性：</p>
            <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center">
              <el-input v-model="e.monster.key" style="width:110px" size="small" placeholder="key" />
              <el-input v-model="e.monster.name" style="width:100px" size="small" placeholder="名称" />
              <span style="font-size:11px">Lv</span><el-input-number v-model="e.monster.level" :min="1" size="small" style="width:70px" />
              <span style="font-size:11px">HP</span><el-input-number v-model="e.monster.hp" :min="1" size="small" style="width:90px" />
              <span style="font-size:11px">ATK</span><el-input-number v-model="e.monster.atk" :min="1" size="small" style="width:80px" />
              <span style="font-size:11px">DEF</span><el-input-number v-model="e.monster.def" :min="0" size="small" style="width:80px" />
              <span style="font-size:11px">SPD</span><el-input-number v-model="e.monster.spd" :min="1" size="small" style="width:70px" />
              <span style="font-size:11px">EXP</span><el-input-number v-model="e.monster.expReward" :min="0" size="small" style="width:80px" />
              <span style="font-size:11px">Gold</span><el-input-number v-model="e.monster.goldReward" :min="0" size="small" style="width:80px" />
            </div>
          </div>
        </template>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveSecret">保存</el-button></div>
    </div>

    <!-- ========== 基础参数 GAME_SETTINGS ========== -->
    <div v-if="activeTab==='GAME_SETTINGS'" class="game-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3>游戏基础参数</h3>
        <el-button type="primary" size="small" @click="settingsData.push({key:'new_param',value:0,desc:'新参数说明'})">+ 新增参数</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="settingsData" size="small" max-height="400" show-overflow-tooltip>
          <el-table-column label="参数名" min-width="200"><template #default="{row}"><el-input v-model="row.key" size="small" /></template></el-table-column>
          <el-table-column label="值" min-width="200"><template #default="{row}"><el-input-number v-model="row.value" :min="0" size="small" style="width:160px" /></template></el-table-column>
          <el-table-column label="说明" min-width="300"><template #default="{row}"><el-input v-model="row.desc" size="small" /></template></el-table-column>
          <el-table-column label="操作" width="80" fixed="right"><template #default="{$index}"><el-button size="small" type="danger" @click="settingsData.splice($index,1)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
      <div style="text-align:right;margin-top:12px"><el-button type="primary" @click="saveSettings">保存</el-button></div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage } from 'element-plus'
const adminHeaders = { headers: { Authorization: 'Bearer ' + localStorage.getItem('adminToken') } }
const activeTab = ref('JOB_BASE')

const jobNames = {WARRIOR:'战士',MAGE:'法师',ARCHER:'射手',PRIEST:'牧师',MINER:'矿工'}
const jobKeys = ['WARRIOR','MAGE','ARCHER','PRIEST','MINER']
const partNames = {WEAPON:'武器',HELMET:'头盔',ARMOR:'衣服',PANTS:'裤子',SHOES:'鞋子',NECKLACE:'项链',RING:'戒指'}
const parts = ['WEAPON','HELMET','ARMOR','PANTS','SHOES','NECKLACE','RING']
const itemTypes = ['MATERIAL','CONSUMABLE','EQUIPMENT']
const oreKeys = ['iron_ore','copper_ore','silver_ore','gold_ore','mithril_ore','adamantite_ore','star_ore']
const qualities = [{key:'WHITE',label:'白',color:'#9ca3af'},{key:'GREEN',label:'绿',color:'#4ade80'},{key:'BLUE',label:'蓝',color:'#60a5fa'},{key:'PURPLE',label:'紫',color:'#a855f7'},{key:'ORANGE',label:'橙',color:'#f59e0b'},{key:'RED',label:'红',color:'#ef4444'}]

const itemNames = ref({})
const itemKeys = ref([])

const jobBaseData = ref([])
const jobGrowthData = ref([])
const itemsData = ref([])
const minesData = ref([])
const chaptersData = ref([])
const dungeonsData = ref([])
const forgeData = ref([])
const qualityMultData = ref([])
const qualityAttrData = ref([])
const shopData = ref([])
const skillsData = ref([])
const achieveData = ref([])
const taskData = ref([])
const cropData = ref([])
const secretData = ref([])
const settingsData = ref([])

async function loadConfig(key) {
  const r = await api.get('/api/admin/config/get', { params: { key }, ...adminHeaders })
  return r.data ? JSON.parse(r.data.json) : null
}

async function saveConfig(key, data) {
  await api.post('/api/admin/config/update', { key, json: JSON.stringify(data) }, adminHeaders)
  ElMessage.success('保存成功')
}

function addItem() { itemsData.value.push({key:'new_item',name:'新物品',type:'MATERIAL',subType:'',sellPrice:1,desc:'物品描述'}) }
function addMine() { minesData.value.push({name:'新矿洞',energyCost:10,cooldown:60,dropKeys:['iron_ore'],weights:[50],desc:'矿洞描述'}) }
function addChapter() { chaptersData.value.push({chapter:chaptersData.value.length+1,name:'新章节',levelReq:1,stages:[{key:'new_monster',name:'新怪物',level:1,hp:100,atk:10,def:5,spd:5,expReward:10,goldReward:5,dropKeys:[],dropRates:[]}]}) }
function addDungeon() { dungeonsData.value.push({key:'new_dungeon',name:'新副本',levelReq:1,energyCost:10,dailyLimit:1,boss:{key:'new_boss',name:'新Boss',level:1,hp:500,atk:30,def:15,spd:8,expReward:100,goldReward:50,dropKeys:[],dropRates:[]},desc:'副本描述'}) }
function addForgeRecipe() { forgeData.value.push({resultKey:'new_equip',resultName:'新装备',part:'WEAPON',goldCost:100,_materials:[{key:'iron_ore',count:5}],qualityWeights:{WHITE:50,GREEN:30,BLUE:15,PURPLE:5}}) }
function addSkill() { skillsData.value.push({key:'new_skill',name:'新技能',job:'WARRIOR',type:'ACTIVE',unlockLevel:1,maxLevel:10,goldCost:80,bookCost:0,desc:'技能描述',effectType:'DAMAGE',baseValue:1.5,valuePerLevel:0.05,secondaryValue:0}) }
function skillEffectPreview(s) {
  if (!s.effectType) return s.desc || ''
  const val = s.baseValue + s.valuePerLevel * (s.maxLevel || 10)
  const maxVal = s.baseValue + s.valuePerLevel * (s.maxLevel || 10)
  switch(s.effectType) {
    case 'DAMAGE': return `Lv1: 造成${(s.baseValue*100).toFixed(0)}%伤害 → Lv${s.maxLevel}: 造成${(maxVal*100).toFixed(0)}%伤害`
    case 'HEAL': return `Lv1: 恢复${(s.baseValue*100).toFixed(0)}%生命 → Lv${s.maxLevel}: 恢复${(maxVal*100).toFixed(0)}%生命`
    case 'DAMAGE_HEAL': return `Lv1: ${(s.baseValue*100).toFixed(0)}%伤害+${(s.secondaryValue*100).toFixed(0)}%回血 → Lv${s.maxLevel}: ${(maxVal*100).toFixed(0)}%伤害`
    case 'PASSIVE_DEF': return `Lv1: 防御+${s.baseValue.toFixed(0)}% → Lv${s.maxLevel}: 防御+${maxVal.toFixed(0)}%`
    case 'PASSIVE_HP': return `Lv1: 生命+${s.baseValue.toFixed(0)}% → Lv${s.maxLevel}: 生命+${maxVal.toFixed(0)}%`
    case 'PASSIVE_SPD': return `Lv1: 速度+${s.baseValue.toFixed(0)}% → Lv${s.maxLevel}: 速度+${maxVal.toFixed(0)}%`
    case 'PASSIVE_ATK': return `Lv1: 攻击+${s.baseValue.toFixed(0)}% → Lv${s.maxLevel}: 攻击+${maxVal.toFixed(0)}%`
    case 'MINE_BONUS': return `Lv1: 矿石+${s.baseValue.toFixed(0)} → Lv${s.maxLevel}: 矿石+${maxVal.toFixed(0)}`
    case 'MINE_RARE': return `Lv1: 稀有矿+${s.baseValue.toFixed(0)}% → Lv${s.maxLevel}: 稀有矿+${maxVal.toFixed(0)}%`
    case 'ENERGY_MAX': return `Lv1: 体力+${s.baseValue.toFixed(0)} → Lv${s.maxLevel}: 体力+${maxVal.toFixed(0)}`
    default: return s.desc || ''
  }
}
function addSecretEvent() { secretData.value.push({type:'CHEST',desc:'发现宝箱！',rewardType:'GOLD',rewardAmount:100,monster:{key:'monster',name:'怪物',level:1,hp:100,atk:10,def:5,spd:5,expReward:10,goldReward:5}}) }

async function onTabChange(tab) {
  const data = await loadConfig(tab)
  if (!data) return
  switch(tab) {
    case 'JOB_BASE': jobBaseData.value = Object.entries(data).map(([job,v])=>({job,hp:v[0],atk:v[1],def:v[2],spd:v[3]})); break
    case 'JOB_GROWTH': jobGrowthData.value = Object.entries(data).map(([job,v])=>({job,hp:v[0],atk:v[1],def:v[2],spd:v[3]})); break
    case 'ITEMS':
      itemsData.value = Object.entries(data).map(([key,v])=>({key,...v}))
      itemKeys.value = itemsData.value.map(i=>i.key)
      itemNames.value = {}; itemsData.value.forEach(i=>itemNames.value[i.key]=i.name)
      break
    case 'MINES': minesData.value = JSON.parse(JSON.stringify(data)); break
    case 'CHAPTERS':
      chaptersData.value = JSON.parse(JSON.stringify(data))
      chaptersData.value.forEach(ch=>ch.stages.forEach(s=>{if(!s.dropKeys)s.dropKeys=[];if(!s.dropRates)s.dropRates=[]}))
      break
    case 'DUNGEONS':
      dungeonsData.value = JSON.parse(JSON.stringify(data))
      dungeonsData.value.forEach(d=>{if(!d.boss.dropKeys)d.boss.dropKeys=[];if(!d.boss.dropRates)d.boss.dropRates=[]})
      break
    case 'FORGE_RECIPES':
      forgeData.value = data.map(r=>({...r,_materials:Object.entries(r.materials).map(([key,count])=>({key,count}))}))
      break
    case 'QUALITY_MULT': qualityMultData.value = Object.entries(data).map(([key,value])=>({key,value})); break
    case 'QUALITY_ATTR_BASE': qualityAttrData.value = Object.entries(data).map(([part,v])=>({part,atk:v[0],def:v[1],hp:v[2]})); break
    case 'SHOP_ITEMS': shopData.value = JSON.parse(JSON.stringify(data)); break
    case 'SKILLS': skillsData.value = JSON.parse(JSON.stringify(data)); break
    case 'ACHIEVEMENTS': achieveData.value = JSON.parse(JSON.stringify(data)); break
    case 'DAILY_TASKS': taskData.value = JSON.parse(JSON.stringify(data)); break
    case 'CROPS': cropData.value = JSON.parse(JSON.stringify(data)); break
    case 'SECRET_EVENTS':
      secretData.value = JSON.parse(JSON.stringify(data))
      secretData.value.forEach(e=>{if(!e.monster)e.monster={key:'',name:'',level:1,hp:100,atk:10,def:5,spd:5,expReward:10,goldReward:5}})
      break
    case 'GAME_SETTINGS':
      const sd = {diamondToGoldRate:'钻石兑金币比例',energyRecoverSec:'体力恢复间隔(秒)',spiritRecoverSec:'精力恢复间隔(秒)',pveEnergyCost:'冒险体力消耗',pvpSpiritCost:'PVP精力消耗',secretSpiritCost:'秘境精力消耗',signNormalMin:'签到普通日最小',signNormalMax:'签到普通日最大',signDay7Min:'签到7天最小',signDay7Max:'签到7天最大',signDay30Min:'签到30天最小',signDay30Max:'签到30天最大',levelExpCoeffA:'经验系数A',levelExpCoeffB:'经验系数B'}
      settingsData.value = Object.entries(data).map(([key,value])=>({key,value,desc:sd[key]||''}))
      break
  }
}

async function saveJobBase() { const o={}; jobBaseData.value.forEach(r=>o[r.job]=[r.hp,r.atk,r.def,r.spd]); await saveConfig('JOB_BASE',o) }
async function saveJobGrowth() { const o={}; jobGrowthData.value.forEach(r=>o[r.job]=[r.hp,r.atk,r.def,r.spd]); await saveConfig('JOB_GROWTH',o) }
async function saveItems() { const o={}; itemsData.value.forEach(i=>{o[i.key]={name:i.name,type:i.type,subType:i.subType,sellPrice:i.sellPrice,desc:i.desc}}); await saveConfig('ITEMS',o) }
async function saveMines() { await saveConfig('MINES',minesData.value) }
async function saveChapters() { await saveConfig('CHAPTERS',chaptersData.value) }
async function saveDungeons() { await saveConfig('DUNGEONS',dungeonsData.value) }
async function saveForge() {
  const recipes = forgeData.value.map(r => {
    const materials = {}; r._materials.forEach(m => { materials[m.key] = m.count })
    return { resultKey:r.resultKey, resultName:r.resultName, part:r.part, goldCost:r.goldCost, materials, qualityWeights:r.qualityWeights }
  })
  await saveConfig('FORGE_RECIPES', recipes)
}
async function saveQualityMult() { const o={}; qualityMultData.value.forEach(r=>o[r.key]=r.value); await saveConfig('QUALITY_MULT',o) }
async function saveQualityAttr() { const o={}; qualityAttrData.value.forEach(r=>o[r.part]=[r.atk,r.def,r.hp]); await saveConfig('QUALITY_ATTR_BASE',o) }
async function saveShop() { await saveConfig('SHOP_ITEMS',shopData.value) }
async function saveSkills() { await saveConfig('SKILLS',skillsData.value) }
async function saveAchievements() { await saveConfig('ACHIEVEMENTS',achieveData.value) }
async function saveTasks() { await saveConfig('DAILY_TASKS',taskData.value) }
async function saveCrops() { await saveConfig('CROPS',cropData.value) }
async function saveSecret() { await saveConfig('SECRET_EVENTS',secretData.value) }
async function saveSettings() { const o={}; settingsData.value.forEach(r=>o[r.key]=r.value); await saveConfig('GAME_SETTINGS',o) }

async function reloadAll() { await api.post('/api/admin/config/reload', {}, adminHeaders); ElMessage.success('已重新加载'); onTabChange(activeTab.value) }

onMounted(() => onTabChange('JOB_BASE'))
</script>
<style scoped>
.table-wrap { overflow-x: auto; }
.config-card { border:1px solid var(--border); border-radius:8px; padding:14px; margin-bottom:12px; }
</style>
