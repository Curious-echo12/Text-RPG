package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.GameConfigEntity;
import com.textrpg.entity.GameRole;
import com.textrpg.mapper.GameConfigMapper;
import com.textrpg.mapper.GameRoleMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameConfigService implements CommandLineRunner {
    private final GameConfigMapper configMapper;
    private final GameRoleMapper roleMapper;

    private Map<String, String> configCache = new HashMap<>();

    @Override
    public void run(String... args) {
        loadAll();
    }

    /** 从数据库加载所有配置到缓存，并同步到GameConfig静态字段 */
    public void loadAll() {
        List<GameConfigEntity> all = configMapper.selectList(null);
        configCache.clear();
        for (GameConfigEntity e : all) {
            configCache.put(e.getConfigKey(), e.getConfigJson());
        }
        syncToGameConfig();
        log.info("游戏配置已从数据库加载，共 {} 项", configCache.size());
    }

    /** 重新加载配置 */
    public void reload() {
        loadAll();
    }

    /** 获取配置JSON */
    public String getConfigJson(String key) {
        return configCache.get(key);
    }

    /** 获取所有配置键列表 */
    public List<Map<String, Object>> getConfigKeys() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<GameConfigEntity> all = configMapper.selectList(null);
        for (GameConfigEntity e : all) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("key", e.getConfigKey());
            info.put("description", e.getDescription());
            info.put("updateTime", e.getUpdateTime() != null ? e.getUpdateTime().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            result.add(info);
        }
        return result;
    }

    /** 更新配置 */
    public void updateConfig(String key, String json) {
        // 验证JSON格式
        try {
            JSON.parse(json);
        } catch (Exception e) {
            throw new RuntimeException("JSON格式错误: " + e.getMessage());
        }

        GameConfigEntity entity = configMapper.selectOne(
            new LambdaQueryWrapper<GameConfigEntity>().eq(GameConfigEntity::getConfigKey, key));
        if (entity == null) {
            entity = new GameConfigEntity();
            entity.setConfigKey(key);
            entity.setConfigJson(json);
            entity.setDescription("自定义配置");
            configMapper.insert(entity);
        } else {
            entity.setConfigJson(json);
            configMapper.updateById(entity);
        }
        configCache.put(key, json);
        syncToGameConfig();
    }

    /** 将缓存中的JSON同步到GameConfig静态字段 */
    private void syncToGameConfig() {
        boolean jobChanged = false;
        try { syncJobBase(); jobChanged = true; } catch (Exception e) { log.warn("同步JOB_BASE失败: {}", e.getMessage()); }
        try { syncJobGrowth(); jobChanged = true; } catch (Exception e) { log.warn("同步JOB_GROWTH失败: {}", e.getMessage()); }
        if (jobChanged) {
            try { recalcAllPlayers(); } catch (Exception e) { log.warn("重算玩家属性失败: {}", e.getMessage()); }
        }
        try { syncItems(); } catch (Exception e) { log.warn("同步ITEMS失败: {}", e.getMessage()); }
        try { syncMines(); } catch (Exception e) { log.warn("同步MINES失败: {}", e.getMessage()); }
        try { syncChapters(); } catch (Exception e) { log.warn("同步CHAPTERS失败: {}", e.getMessage()); }
        try { syncDungeons(); } catch (Exception e) { log.warn("同步DUNGEONS失败: {}", e.getMessage()); }
        try { syncForgeRecipes(); } catch (Exception e) { log.warn("同步FORGE_RECIPES失败: {}", e.getMessage()); }
        try { syncQualityMult(); } catch (Exception e) { log.warn("同步QUALITY_MULT失败: {}", e.getMessage()); }
        try { syncQualityAttrBase(); } catch (Exception e) { log.warn("同步QUALITY_ATTR_BASE失败: {}", e.getMessage()); }
        try { syncShopItems(); } catch (Exception e) { log.warn("同步SHOP_ITEMS失败: {}", e.getMessage()); }
        try { syncSkills(); } catch (Exception e) { log.warn("同步SKILLS失败: {}", e.getMessage()); }
        try { syncAchievements(); } catch (Exception e) { log.warn("同步ACHIEVEMENTS失败: {}", e.getMessage()); }
        try { syncDailyTasks(); } catch (Exception e) { log.warn("同步DAILY_TASKS失败: {}", e.getMessage()); }
        try { syncCrops(); } catch (Exception e) { log.warn("同步CROPS失败: {}", e.getMessage()); }
        try { syncSecretEvents(); } catch (Exception e) { log.warn("同步SECRET_EVENTS失败: {}", e.getMessage()); }
        try { syncGameSettings(); } catch (Exception e) { log.warn("同步GAME_SETTINGS失败: {}", e.getMessage()); }
    }

    /** 职业属性/成长变更后，重新计算所有玩家的基础属性 */
    private void recalcAllPlayers() {
        List<GameRole> allRoles = roleMapper.selectList(null);
        int count = 0;
        for (GameRole role : allRoles) {
            String job = role.getJob();
            int[] base = GameConfig.JOB_BASE.get(job);
            int[] growth = GameConfig.JOB_GROWTH.get(job);
            if (base == null || growth == null) continue;
            int lvl = role.getLevel();
            double rebornMult = 1.0 + role.getRebornCount() * 0.2;

            int newMaxHp = (int)((base[0] + growth[0] * (lvl - 1)) * rebornMult);
            int newAtk = (int)((base[1] + growth[1] * (lvl - 1)) * rebornMult);
            int newDef = (int)((base[2] + growth[2] * (lvl - 1)) * rebornMult);
            int newSpd = (int)((base[3] + growth[3] * (lvl - 1)) * rebornMult);

            role.setMaxHp(newMaxHp);
            role.setHp(Math.min(role.getHp(), newMaxHp));
            role.setAttack(newAtk);
            role.setDefense(newDef);
            role.setSpeed(newSpd);

            // 重算战力
            long fp = (long) (newMaxHp * 0.1 + newAtk * 2.0 + newDef * 1.5
                    + newSpd * 1.0 + role.getCritRate() * 100 + role.getCritDmg() * 50);
            role.setFightPower(fp);

            roleMapper.updateById(role);
            count++;
        }
        log.info("职业属性变更，已重算 {} 个玩家属性", count);
    }

    private void syncJobBase() {
        String json = configCache.get("JOB_BASE");
        if (json == null) return;
        JSONObject obj = JSON.parseObject(json);
        GameConfig.JOB_BASE.clear();
        for (String key : obj.keySet()) {
            JSONArray arr = obj.getJSONArray(key);
            int[] vals = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) vals[i] = arr.getIntValue(i);
            GameConfig.JOB_BASE.put(key, vals);
        }
    }

    private void syncJobGrowth() {
        String json = configCache.get("JOB_GROWTH");
        if (json == null) return;
        JSONObject obj = JSON.parseObject(json);
        GameConfig.JOB_GROWTH.clear();
        for (String key : obj.keySet()) {
            JSONArray arr = obj.getJSONArray(key);
            int[] vals = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) vals[i] = arr.getIntValue(i);
            GameConfig.JOB_GROWTH.put(key, vals);
        }
    }

    private void syncItems() {
        String json = configCache.get("ITEMS");
        if (json == null) return;
        JSONObject obj = JSON.parseObject(json);
        GameConfig.ITEMS.clear();
        for (String key : obj.keySet()) {
            JSONObject item = obj.getJSONObject(key);
            GameConfig.ITEMS.put(key, new GameConfig.ItemDef(
                key, item.getString("name"), item.getString("type"),
                item.getString("subType"), item.getIntValue("sellPrice"),
                item.getString("desc")));
        }
    }

    private void syncMines() {
        String json = configCache.get("MINES");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.MineDef> mines = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject m = arr.getJSONObject(i);
            List<String> dropKeys = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            JSONArray dkArr = m.getJSONArray("dropKeys");
            JSONArray wArr = m.getJSONArray("weights");
            for (int j = 0; j < dkArr.size(); j++) dropKeys.add(dkArr.getString(j));
            for (int j = 0; j < wArr.size(); j++) weights.add(wArr.getIntValue(j));
            mines.add(new GameConfig.MineDef(m.getString("name"), m.getIntValue("energyCost"),
                m.getIntValue("cooldown"), dropKeys, weights, m.getString("desc")));
        }
        GameConfig.MINES = mines;
    }

    private void syncChapters() {
        String json = configCache.get("CHAPTERS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.ChapterDef> chapters = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject ch = arr.getJSONObject(i);
            List<GameConfig.MonsterDef> stages = new ArrayList<>();
            JSONArray stArr = ch.getJSONArray("stages");
            for (int j = 0; j < stArr.size(); j++) {
                JSONObject s = stArr.getJSONObject(j);
                List<String> dk = s.containsKey("dropKeys") ? s.getJSONArray("dropKeys").toJavaList(String.class) : null;
                List<Integer> dr = s.containsKey("dropRates") ? s.getJSONArray("dropRates").toJavaList(Integer.class) : null;
                stages.add(new GameConfig.MonsterDef(s.getString("key"), s.getString("name"),
                    s.getIntValue("level"), s.getIntValue("hp"), s.getIntValue("atk"),
                    s.getIntValue("def"), s.getIntValue("spd"), s.getIntValue("expReward"),
                    s.getIntValue("goldReward"), dk, dr));
            }
            chapters.add(new GameConfig.ChapterDef(ch.getIntValue("chapter"), ch.getString("name"),
                ch.getIntValue("levelReq"), stages));
        }
        GameConfig.CHAPTERS = chapters;
    }

    private void syncDungeons() {
        String json = configCache.get("DUNGEONS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.DungeonDef> dungeons = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject d = arr.getJSONObject(i);
            JSONObject b = d.getJSONObject("boss");
            List<String> bdk = b.containsKey("dropKeys") ? b.getJSONArray("dropKeys").toJavaList(String.class) : null;
            List<Integer> bdr = b.containsKey("dropRates") ? b.getJSONArray("dropRates").toJavaList(Integer.class) : null;
            GameConfig.MonsterDef boss = new GameConfig.MonsterDef(b.getString("key"), b.getString("name"),
                b.getIntValue("level"), b.getIntValue("hp"), b.getIntValue("atk"),
                b.getIntValue("def"), b.getIntValue("spd"), b.getIntValue("expReward"),
                b.getIntValue("goldReward"), bdk, bdr);
            dungeons.add(new GameConfig.DungeonDef(d.getString("key"), d.getString("name"),
                d.getIntValue("levelReq"), d.getIntValue("energyCost"), d.getIntValue("dailyLimit"),
                boss, d.getString("desc")));
        }
        GameConfig.DUNGEONS = dungeons;
    }

    private void syncForgeRecipes() {
        String json = configCache.get("FORGE_RECIPES");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.ForgeRecipe> recipes = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject r = arr.getJSONObject(i);
            Map<String, Integer> materials = new LinkedHashMap<>();
            JSONObject matObj = r.getJSONObject("materials");
            for (String k : matObj.keySet()) materials.put(k, matObj.getIntValue(k));
            Map<String, Integer> qw = new LinkedHashMap<>();
            JSONObject qwObj = r.getJSONObject("qualityWeights");
            for (String k : qwObj.keySet()) qw.put(k, qwObj.getIntValue(k));
            recipes.add(new GameConfig.ForgeRecipe(r.getString("resultKey"), r.getString("resultName"),
                r.getString("part"), r.getIntValue("goldCost"), materials, qw));
        }
        GameConfig.FORGE_RECIPES = recipes;
    }

    private void syncQualityMult() {
        String json = configCache.get("QUALITY_MULT");
        if (json == null) return;
        JSONObject obj = JSON.parseObject(json);
        GameConfig.QUALITY_MULT.clear();
        for (String k : obj.keySet()) GameConfig.QUALITY_MULT.put(k, obj.getDoubleValue(k));
    }

    private void syncQualityAttrBase() {
        String json = configCache.get("QUALITY_ATTR_BASE");
        if (json == null) return;
        JSONObject obj = JSON.parseObject(json);
        GameConfig.QUALITY_ATTR_BASE.clear();
        for (String k : obj.keySet()) {
            JSONArray arr = obj.getJSONArray(k);
            GameConfig.QUALITY_ATTR_BASE.put(k, new int[]{arr.getIntValue(0), arr.getIntValue(1), arr.getIntValue(2)});
        }
    }

    private void syncShopItems() {
        String json = configCache.get("SHOP_ITEMS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.ShopItem> items = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject s = arr.getJSONObject(i);
            items.add(new GameConfig.ShopItem(s.getString("itemKey"), s.getIntValue("price"),
                s.getString("currency"), s.getIntValue("stock")));
        }
        GameConfig.SHOP_ITEMS = items;
    }

    private void syncSkills() {
        String json = configCache.get("SKILLS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.SkillDef> skills = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject s = arr.getJSONObject(i);
            skills.add(new GameConfig.SkillDef(s.getString("key"), s.getString("name"),
                s.getString("job"), s.getString("type"), s.getIntValue("unlockLevel"),
                s.getIntValue("maxLevel"), s.getIntValue("goldCost"), s.getIntValue("bookCost"),
                s.getString("desc"),
                s.getString("effectType"), s.getDoubleValue("baseValue"),
                s.getDoubleValue("valuePerLevel"), s.getDoubleValue("secondaryValue")));
        }
        GameConfig.SKILLS = skills;
    }

    private void syncAchievements() {
        String json = configCache.get("ACHIEVEMENTS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.AchieveDef> achs = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject a = arr.getJSONObject(i);
            achs.add(new GameConfig.AchieveDef(a.getString("key"), a.getString("name"),
                a.getString("desc"), a.getIntValue("target"), a.getString("rewardType"),
                a.getIntValue("rewardAmount")));
        }
        GameConfig.ACHIEVEMENTS = achs;
    }

    private void syncDailyTasks() {
        String json = configCache.get("DAILY_TASKS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.TaskDef> tasks = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject t = arr.getJSONObject(i);
            tasks.add(new GameConfig.TaskDef(t.getString("key"), t.getString("name"),
                t.getString("desc"), t.getIntValue("target"), t.getString("rewardType"),
                t.getIntValue("rewardAmount")));
        }
        GameConfig.DAILY_TASKS = tasks;
    }

    private void syncCrops() {
        String json = configCache.get("CROPS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.CropDef> crops = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject c = arr.getJSONObject(i);
            crops.add(new GameConfig.CropDef(c.getString("cropKey"), c.getString("name"),
                c.getIntValue("growMinutes"), c.getIntValue("baseYield"), c.getString("desc")));
        }
        GameConfig.CROPS = crops;
    }

    private void syncSecretEvents() {
        String json = configCache.get("SECRET_EVENTS");
        if (json == null) return;
        JSONArray arr = JSON.parseArray(json);
        List<GameConfig.SecretEvent> events = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject e = arr.getJSONObject(i);
            GameConfig.MonsterDef monster = null;
            if (e.containsKey("monster")) {
                JSONObject m = e.getJSONObject("monster");
                List<String> dk = m.containsKey("dropKeys") ? m.getJSONArray("dropKeys").toJavaList(String.class) : null;
                List<Integer> dr = m.containsKey("dropRates") ? m.getJSONArray("dropRates").toJavaList(Integer.class) : null;
                monster = new GameConfig.MonsterDef(m.getString("key"), m.getString("name"),
                    m.getIntValue("level"), m.getIntValue("hp"), m.getIntValue("atk"),
                    m.getIntValue("def"), m.getIntValue("spd"), m.getIntValue("expReward"),
                    m.getIntValue("goldReward"), dk, dr);
            }
            events.add(new GameConfig.SecretEvent(e.getString("type"), e.getString("desc"),
                monster, e.getString("rewardType"), e.getString("rewardItemKey"),
                e.getIntValue("rewardAmount")));
        }
        GameConfig.SECRET_EVENTS = events;
    }

    private void syncGameSettings() {
        String json = configCache.get("GAME_SETTINGS");
        if (json == null) return;
        JSONObject obj = JSON.parseObject(json);
        GameConfig.gameSettings.clear();
        for (String k : obj.keySet()) GameConfig.gameSettings.put(k, obj.get(k));
    }
}
