package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ForgeService {
    private final UserBagMapper bagMapper;
    private final UserEquipMapper equipMapper;
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;
    private final BagService bagService;

    public List<Map<String, Object>> getRecipes(Long userId) {
        List<UserBag> bag = bagService.getBag(userId);
        Map<String, Integer> bagMap = new HashMap<>();
        for (UserBag b : bag) bagMap.merge(b.getItemKey(), b.getCount(), Integer::sum);

        GameRole role = roleService.getByUserId(userId);
        int forgeLevel = role != null ? role.getForgeLevel() : 1;

        List<Map<String, Object>> result = new ArrayList<>();
        for (GameConfig.ForgeRecipe r : GameConfig.FORGE_RECIPES) {
            Map<String, Object> info = new HashMap<>();
            info.put("resultKey", r.resultKey);
            info.put("resultName", r.resultName);
            info.put("part", r.part);
            info.put("goldCost", r.goldCost);
            info.put("qualityWeights", r.qualityWeights);

            // 计算锻造等级加成后的实际概率（服务端计算，确保与配置同步）
            Map<String, Integer> adjusted = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : r.qualityWeights.entrySet()) {
                adjusted.put(e.getKey(), "WHITE".equals(e.getKey()) ? e.getValue() : e.getValue() + forgeLevel);
            }
            int total = adjusted.values().stream().mapToInt(Integer::intValue).sum();
            Map<String, Integer> adjustedPercent = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : adjusted.entrySet()) {
                adjustedPercent.put(e.getKey(), Math.round(e.getValue() * 100f / total));
            }
            info.put("adjustedPercent", adjustedPercent);

            List<Map<String, Object>> materialDetails = new ArrayList<>();
            boolean canForge = true;
            for (Map.Entry<String, Integer> req : r.materials.entrySet()) {
                Map<String, Object> mat = new HashMap<>();
                int have = bagMap.getOrDefault(req.getKey(), 0);
                mat.put("key", req.getKey());
                GameConfig.ItemDef itemDef = GameConfig.ITEMS.get(req.getKey());
                mat.put("name", itemDef != null ? itemDef.name : req.getKey());
                mat.put("need", req.getValue());
                mat.put("have", have);
                mat.put("enough", have >= req.getValue());
                if (have < req.getValue()) canForge = false;
                materialDetails.add(mat);
            }
            info.put("materials", materialDetails);

            // 属性预览：每个品质对应的属性
            Map<String, Map<String, Integer>> preview = new LinkedHashMap<>();
            int[] baseAttr = GameConfig.QUALITY_ATTR_BASE.getOrDefault(r.part, new int[]{5, 3, 15});
            for (Map.Entry<String, Double> q : GameConfig.QUALITY_MULT.entrySet()) {
                Map<String, Integer> attr = new LinkedHashMap<>();
                attr.put("atk", (int)(baseAttr[0] * q.getValue()));
                attr.put("def", (int)(baseAttr[1] * q.getValue()));
                attr.put("hp", (int)(baseAttr[2] * q.getValue()));
                preview.put(q.getKey(), attr);
            }
            info.put("preview", preview);

            info.put("canForge", canForge);

            // 出售返还描述
            info.put("sellRefundDesc", buildSellRefundDesc(r.materials));
            info.put("redDesc", "红色品质为最高品质，属性为白色的2.3倍。锻造等级越高，出现概率越大。");

            result.add(info);
        }
        return result;
    }

    private String buildSellRefundDesc(Map<String, Integer> materials) {
        StringBuilder sb = new StringBuilder("出售可返还：");
        boolean first = true;
        for (Map.Entry<String, Integer> req : materials.entrySet()) {
            int refund = req.getValue() / 2;
            if (refund > 0) {
                if (!first) sb.append("、");
                GameConfig.ItemDef def = GameConfig.ITEMS.get(req.getKey());
                sb.append((def != null ? def.name : req.getKey())).append(" x").append(refund);
                first = false;
            }
        }
        return sb.toString();
    }

    @Transactional
    public Map<String, Object> doForge(Long userId, String recipeKey) {
        GameConfig.ForgeRecipe recipe = GameConfig.FORGE_RECIPES.stream()
                .filter(r -> r.resultKey.equals(recipeKey)).findFirst()
                .orElseThrow(() -> new BusinessException("配方不存在"));
        GameRole role = roleService.getByUserId(userId);

        List<UserBag> bag = bagService.getBag(userId);
        Map<String, Integer> bagMap = new HashMap<>();
        for (UserBag b : bag) bagMap.merge(b.getItemKey(), b.getCount(), Integer::sum);

        for (Map.Entry<String, Integer> req : recipe.materials.entrySet()) {
            int have = bagMap.getOrDefault(req.getKey(), 0);
            if (have < req.getValue()) {
                GameConfig.ItemDef item = GameConfig.ITEMS.get(req.getKey());
                throw new BusinessException("材料不足：" + (item != null ? item.name : req.getKey())
                        + " 需要" + req.getValue() + "个，当前" + have + "个");
            }
        }
        if (role.getGold() < recipe.goldCost)
            throw new BusinessException("金币不足，需要" + recipe.goldCost);

        for (Map.Entry<String, Integer> req : recipe.materials.entrySet()) {
            deductItem(userId, req.getKey(), req.getValue());
        }
        role.setGold(role.getGold() - recipe.goldCost);

        role.setForgeExp(role.getForgeExp() + 15);
        while (role.getForgeExp() >= role.getForgeLevel() * 100) {
            role.setForgeExp(role.getForgeExp() - role.getForgeLevel() * 100);
            role.setForgeLevel(role.getForgeLevel() + 1);
        }
        roleMapper.updateById(role);

        String quality = weightedRandomQuality(recipe.qualityWeights, role.getForgeLevel());
        double mult = GameConfig.QUALITY_MULT.getOrDefault(quality, 1.0);
        int[] baseAttr = GameConfig.QUALITY_ATTR_BASE.getOrDefault(recipe.part, new int[]{5, 3, 15});

        JSONObject attr = new JSONObject();
        attr.put("atk", (int) (baseAttr[0] * mult));
        attr.put("def", (int) (baseAttr[1] * mult));
        attr.put("hp", (int) (baseAttr[2] * mult));

        JSONObject extra = new JSONObject();
        extra.put("part", recipe.part);
        extra.put("quality", quality);
        extra.put("strengthenLevel", 0);
        extra.put("baseAttr", attr.toJSONString());

        UserBag equipItem = new UserBag();
        equipItem.setUserId(userId);
        equipItem.setItemKey(recipe.resultKey);
        equipItem.setCount(1);
        equipItem.setExtraJson(extra.toJSONString());
        bagMapper.insert(equipItem);

        GameConfig.ItemDef itemDef = GameConfig.ITEMS.get(recipe.resultKey);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "锻造成功！获得 " + (itemDef != null ? itemDef.name : recipe.resultKey)
                + " 品质：" + qualityName(quality)
                + " | 攻击+" + attr.getIntValue("atk")
                + " 防御+" + attr.getIntValue("def")
                + " 生命+" + attr.getIntValue("hp"));
        result.put("quality", quality);
        result.put("role", role);
        return result;
    }

    /** 出售装备，返还50%矿石材料 */
    @Transactional
    public Map<String, Object> sellForgedEquipment(Long userId, Long bagId) {
        UserBag bag = bagMapper.selectById(bagId);
        if (bag == null || !bag.getUserId().equals(userId))
            throw new BusinessException("物品不存在");
        if (bag.getExtraJson() == null)
            throw new BusinessException("该物品不是装备，无法出售返还材料");

        String itemKey = bag.getItemKey();
        GameConfig.ForgeRecipe recipe = GameConfig.FORGE_RECIPES.stream()
                .filter(r -> r.resultKey.equals(itemKey)).findFirst().orElse(null);
        if (recipe == null) throw new BusinessException("该装备不是锻造产物");

        // 删除装备
        bag.setCount(bag.getCount() - 1);
        if (bag.getCount() <= 0) bagMapper.deleteById(bagId);
        else bagMapper.updateById(bag);

        // 返还50%矿石
        List<String> refundList = new ArrayList<>();
        for (Map.Entry<String, Integer> req : recipe.materials.entrySet()) {
            int refund = req.getValue() / 2;
            if (refund > 0) {
                bagService.addItem(userId, req.getKey(), refund);
                GameConfig.ItemDef def = GameConfig.ITEMS.get(req.getKey());
                refundList.add((def != null ? def.name : req.getKey()) + " x" + refund);
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (refundList.isEmpty()) {
            result.put("msg", "装备已出售，无材料返还");
        } else {
            result.put("msg", "装备出售成功！返还材料：" + String.join("、", refundList));
        }
        result.put("refunds", refundList);
        result.put("role", roleService.getByUserId(userId));
        return result;
    }

    public Map<String, Object> getStrengthenInfo(Long userId, Long equipId) {
        UserBag equipBag = bagMapper.selectById(equipId);
        if (equipBag == null || !equipBag.getUserId().equals(userId) || equipBag.getExtraJson() == null)
            throw new BusinessException("装备不存在");
        JSONObject extra = JSONObject.parseObject(equipBag.getExtraJson());
        int currentLevel = extra.getIntValue("strengthenLevel");
        if (currentLevel >= 20) throw new BusinessException("已达最高强化等级 +20");
        int newLevel = currentLevel + 1;
        GameConfig.ItemDef oreDef = GameConfig.ITEMS.get(GameConfig.getStrengthenOre(newLevel));
        Map<String, Object> info = new HashMap<>();
        info.put("currentLevel", currentLevel);
        info.put("newLevel", newLevel);
        info.put("goldCost", GameConfig.strengthenCostGold(newLevel));
        info.put("stoneCost", GameConfig.strengthenCostStone(newLevel));
        info.put("oreKey", GameConfig.getStrengthenOre(newLevel));
        info.put("oreName", oreDef != null ? oreDef.name : GameConfig.getStrengthenOre(newLevel));
        info.put("oreCount", GameConfig.getStrengthenOreCount(newLevel));
        info.put("rate", GameConfig.strengthenRate(newLevel));
        return info;
    }

    @Transactional
    public Map<String, Object> strengthen(Long userId, Long equipId) {
        UserBag equipBag = bagMapper.selectById(equipId);
        if (equipBag == null || !equipBag.getUserId().equals(userId) || equipBag.getExtraJson() == null)
            throw new BusinessException("装备不存在");

        JSONObject extra = JSONObject.parseObject(equipBag.getExtraJson());
        int currentLevel = extra.getIntValue("strengthenLevel");
        if (currentLevel >= 20) throw new BusinessException("已达最高强化等级 +20");

        int newLevel = currentLevel + 1;
        int goldCost = GameConfig.strengthenCostGold(newLevel);
        int stoneCost = GameConfig.strengthenCostStone(newLevel);
        String oreKey = GameConfig.getStrengthenOre(newLevel);
        int oreCount = GameConfig.getStrengthenOreCount(newLevel);

        GameRole role = roleService.getByUserId(userId);
        if (role.getGold() < goldCost) throw new BusinessException("金币不足，需要" + goldCost);

        List<UserBag> bag = bagService.getBag(userId);

        int stoneHave = bag.stream().filter(b -> "strengthen_stone".equals(b.getItemKey()))
                .mapToInt(UserBag::getCount).sum();
        if (stoneHave < stoneCost)
            throw new BusinessException("强化石不足，需要" + stoneCost + "个，当前" + stoneHave + "个");

        int oreHave = bag.stream().filter(b -> oreKey.equals(b.getItemKey()))
                .mapToInt(UserBag::getCount).sum();
        GameConfig.ItemDef oreDef = GameConfig.ITEMS.get(oreKey);
        String oreName = oreDef != null ? oreDef.name : oreKey;
        if (oreHave < oreCount)
            throw new BusinessException(oreName + "不足，需要" + oreCount + "个，当前" + oreHave + "个");

        role.setGold(role.getGold() - goldCost);
        roleMapper.updateById(role);
        deductItem(userId, "strengthen_stone", stoneCost);
        deductItem(userId, oreKey, oreCount);

        int rate = GameConfig.strengthenRate(newLevel);
        Map<String, Object> result = new HashMap<>();

        if (new Random().nextInt(100) < rate) {
            extra.put("strengthenLevel", newLevel);
            String part = extra.getString("part");
            String quality = extra.getString("quality");
            double mult = GameConfig.QUALITY_MULT.getOrDefault(quality, 1.0);
            int[] baseAttr = GameConfig.QUALITY_ATTR_BASE.getOrDefault(part, new int[]{5, 3, 15});
            JSONObject attr = new JSONObject();
            attr.put("atk", (int)(baseAttr[0] * mult * (1 + newLevel * 0.05)));
            attr.put("def", (int)(baseAttr[1] * mult * (1 + newLevel * 0.05)));
            attr.put("hp", (int)(baseAttr[2] * mult * (1 + newLevel * 0.05)));
            extra.put("baseAttr", attr.toJSONString());
            equipBag.setExtraJson(extra.toJSONString());
            bagMapper.updateById(equipBag);
            result.put("msg", "强化成功！+" + newLevel + "（成功率" + rate + "%）| 攻击+" + attr.getIntValue("atk") + " 防御+" + attr.getIntValue("def") + " 生命+" + attr.getIntValue("hp"));
            result.put("success", true);
        } else {
            if (currentLevel > 10) {
                extra.put("strengthenLevel", currentLevel - 1);
                equipBag.setExtraJson(extra.toJSONString());
                bagMapper.updateById(equipBag);
                result.put("msg", "强化失败！等级降至 +" + (currentLevel - 1));
            } else {
                result.put("msg", "强化失败！等级不变（+10以下不降级）");
            }
            result.put("success", false);
        }
        result.put("role", role);
        return result;
    }

    private void deductItem(Long userId, String itemKey, int count) {
        List<UserBag> items = bagMapper.selectList(new LambdaQueryWrapper<UserBag>()
                .eq(UserBag::getUserId, userId).eq(UserBag::getItemKey, itemKey).orderByAsc(UserBag::getCount));
        int remaining = count;
        for (UserBag item : items) {
            if (remaining <= 0) break;
            if (item.getCount() <= remaining) {
                remaining -= item.getCount();
                bagMapper.deleteById(item.getId());
            } else {
                item.setCount(item.getCount() - remaining);
                bagMapper.updateById(item);
                remaining = 0;
            }
        }
    }

    private String weightedRandomQuality(Map<String, Integer> weights, int forgeLevel) {
        Map<String, Integer> adjusted = new LinkedHashMap<>(weights);
        // 只对配方中已有的品质加成锻造等级，不存在的品质不添加
        for (String q : new ArrayList<>(adjusted.keySet())) {
            if (!"WHITE".equals(q)) {
                adjusted.merge(q, forgeLevel, Integer::sum);
            }
        }
        int total = adjusted.values().stream().mapToInt(Integer::intValue).sum();
        int rand = new Random().nextInt(total);
        int sum = 0;
        for (Map.Entry<String, Integer> e : adjusted.entrySet()) {
            sum += e.getValue();
            if (rand < sum) return e.getKey();
        }
        return "WHITE";
    }

    private String qualityName(String q) {
        switch (q) {
            case "WHITE": return "白色·普通";
            case "GREEN": return "绿色·优秀";
            case "BLUE": return "蓝色·稀有";
            case "PURPLE": return "紫色·史诗";
            case "ORANGE": return "橙色·传说";
            case "RED": return "红色·神话";
            default: return q;
        }
    }
}
