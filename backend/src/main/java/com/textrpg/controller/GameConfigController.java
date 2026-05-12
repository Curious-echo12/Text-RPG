package com.textrpg.controller;

import com.textrpg.common.GameConfig;
import com.textrpg.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/game")
public class GameConfigController {

    /** 获取所有公开游戏配置（前端展示用） */
    @GetMapping("/config")
    public Result<?> getPublicConfig() {
        Map<String, Object> config = new LinkedHashMap<>();

        // 物品定义
        Map<String, Object> items = new LinkedHashMap<>();
        for (Map.Entry<String, GameConfig.ItemDef> e : GameConfig.ITEMS.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", e.getValue().name);
            item.put("type", e.getValue().type);
            item.put("subType", e.getValue().subType);
            item.put("sellPrice", e.getValue().sellPrice);
            item.put("desc", e.getValue().desc);
            items.put(e.getKey(), item);
        }
        config.put("items", items);

        // 品质倍率
        config.put("qualityMult", GameConfig.QUALITY_MULT);

        // 装备基础属性
        config.put("qualityAttrBase", GameConfig.QUALITY_ATTR_BASE);

        // 矿洞信息
        List<Map<String, Object>> mines = new ArrayList<>();
        for (GameConfig.MineDef m : GameConfig.MINES) {
            Map<String, Object> mine = new LinkedHashMap<>();
            mine.put("name", m.name);
            mine.put("energyCost", m.energyCost);
            mine.put("cooldown", m.cooldown);
            mine.put("dropKeys", m.dropKeys);
            mine.put("weights", m.weights);
            mine.put("desc", m.desc);
            mines.add(mine);
        }
        config.put("mines", mines);

        // 作物
        List<Map<String, Object>> crops = new ArrayList<>();
        for (GameConfig.CropDef c : GameConfig.CROPS) {
            Map<String, Object> crop = new LinkedHashMap<>();
            crop.put("cropKey", c.cropKey);
            crop.put("name", c.name);
            crop.put("growMinutes", c.growMinutes);
            crop.put("baseYield", c.baseYield);
            crop.put("desc", c.desc);
            crops.add(crop);
        }
        config.put("crops", crops);

        // 锻造配方
        List<Map<String, Object>> recipes = new ArrayList<>();
        for (GameConfig.ForgeRecipe r : GameConfig.FORGE_RECIPES) {
            Map<String, Object> recipe = new LinkedHashMap<>();
            recipe.put("resultKey", r.resultKey);
            recipe.put("resultName", r.resultName);
            recipe.put("part", r.part);
            recipe.put("goldCost", r.goldCost);
            recipe.put("materials", r.materials);
            recipe.put("qualityWeights", r.qualityWeights);
            recipes.add(recipe);
        }
        config.put("forgeRecipes", recipes);

        // 商店
        List<Map<String, Object>> shop = new ArrayList<>();
        for (GameConfig.ShopItem s : GameConfig.SHOP_ITEMS) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemKey", s.itemKey);
            item.put("price", s.price);
            item.put("currency", s.currency);
            item.put("stock", s.stock);
            shop.add(item);
        }
        config.put("shopItems", shop);

        // 技能
        List<Map<String, Object>> skills = new ArrayList<>();
        for (GameConfig.SkillDef s : GameConfig.SKILLS) {
            Map<String, Object> skill = new LinkedHashMap<>();
            skill.put("key", s.key);
            skill.put("name", s.name);
            skill.put("job", s.job);
            skill.put("type", s.type);
            skill.put("unlockLevel", s.unlockLevel);
            skill.put("maxLevel", s.maxLevel);
            skill.put("goldCost", s.goldCost);
            skill.put("bookCost", s.bookCost);
            skill.put("desc", s.desc);
            skill.put("effectType", s.effectType);
            skill.put("baseValue", s.baseValue);
            skill.put("valuePerLevel", s.valuePerLevel);
            skill.put("secondaryValue", s.secondaryValue);
            skills.add(skill);
        }
        config.put("skills", skills);

        // 副本
        List<Map<String, Object>> dungeons = new ArrayList<>();
        for (GameConfig.DungeonDef d : GameConfig.DUNGEONS) {
            Map<String, Object> dungeon = new LinkedHashMap<>();
            dungeon.put("key", d.key);
            dungeon.put("name", d.name);
            dungeon.put("levelReq", d.levelReq);
            dungeon.put("energyCost", d.energyCost);
            dungeon.put("dailyLimit", d.dailyLimit);
            dungeon.put("desc", d.desc);
            if (d.boss != null) {
                Map<String, Object> boss = new LinkedHashMap<>();
                boss.put("name", d.boss.name);
                boss.put("level", d.boss.level);
                boss.put("hp", d.boss.hp);
                boss.put("atk", d.boss.atk);
                boss.put("def", d.boss.def);
                boss.put("dropKeys", d.boss.dropKeys);
                boss.put("dropRates", d.boss.dropRates);
                dungeon.put("boss", boss);
            }
            dungeons.add(dungeon);
        }
        config.put("dungeons", dungeons);

        // PVE章节
        List<Map<String, Object>> chapters = new ArrayList<>();
        for (GameConfig.ChapterDef ch : GameConfig.CHAPTERS) {
            Map<String, Object> chapter = new LinkedHashMap<>();
            chapter.put("chapter", ch.chapter);
            chapter.put("name", ch.name);
            chapter.put("levelReq", ch.levelReq);
            List<Map<String, Object>> stages = new ArrayList<>();
            for (GameConfig.MonsterDef m : ch.stages) {
                Map<String, Object> monster = new LinkedHashMap<>();
                monster.put("key", m.key);
                monster.put("name", m.name);
                monster.put("level", m.level);
                monster.put("hp", m.hp);
                monster.put("atk", m.atk);
                monster.put("def", m.def);
                monster.put("expReward", m.expReward);
                monster.put("goldReward", m.goldReward);
                monster.put("dropKeys", m.dropKeys);
                monster.put("dropRates", m.dropRates);
                stages.add(monster);
            }
            chapter.put("stages", stages);
            chapters.add(chapter);
        }
        config.put("chapters", chapters);

        // 游戏参数
        config.put("gameSettings", GameConfig.gameSettings);

        return Result.ok(config);
    }
}
