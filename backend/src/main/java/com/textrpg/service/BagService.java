package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BagService {
    private final UserBagMapper bagMapper;
    private final UserEquipMapper equipMapper;
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;

    public List<UserBag> getBag(Long userId) {
        return bagMapper.selectList(new LambdaQueryWrapper<UserBag>().eq(UserBag::getUserId, userId));
    }

    public List<UserEquip> getEquips(Long userId) {
        return equipMapper.selectList(new LambdaQueryWrapper<UserEquip>().eq(UserEquip::getUserId, userId));
    }

    @Transactional
    public void addItem(Long userId, String itemKey, int count) {
        if (count <= 0) return;
        GameConfig.ItemDef def = GameConfig.ITEMS.get(itemKey);
        if (def == null) return;
        UserBag existing = bagMapper.selectOne(new LambdaQueryWrapper<UserBag>()
                .eq(UserBag::getUserId, userId).eq(UserBag::getItemKey, itemKey));
        if (existing != null) {
            existing.setCount(existing.getCount() + count);
            bagMapper.updateById(existing);
        } else {
            UserBag bag = new UserBag();
            bag.setUserId(userId);
            bag.setItemKey(itemKey);
            bag.setCount(count);
            bagMapper.insert(bag);
        }
    }

    /** 添加装备物品（带extraJson属性） */
    @Transactional
    public void addEquipmentItem(Long userId, String itemKey, String extraJson) {
        UserBag bag = new UserBag();
        bag.setUserId(userId);
        bag.setItemKey(itemKey);
        bag.setCount(1);
        bag.setExtraJson(extraJson);
        bagMapper.insert(bag);
    }

    /** 使用消耗品，支持指定数量 */
    @Transactional
    public Map<String, Object> useItem(Long userId, Long bagId, int useCount) {
        if (useCount <= 0) throw new BusinessException("使用数量必须大于0");
        UserBag bag = bagMapper.selectById(bagId);
        if (bag == null || !bag.getUserId().equals(userId))
            throw new BusinessException("物品不存在");
        if (bag.getCount() < useCount)
            throw new BusinessException("数量不足，当前拥有" + bag.getCount() + "个");

        GameRole role = roleService.getByUserId(userId);
        Map<String, Object> result = new HashMap<>();

        if ("mana_potion".equals(bag.getItemKey())) {
            int maxUse = Math.min(useCount, (role.getMaxSpirit() - role.getSpirit() + 19) / 20);
            if (maxUse <= 0) throw new BusinessException("精力已满，无需使用精力药水");
            int actualUse = Math.min(useCount, maxUse);
            int recover = Math.min(20 * actualUse, role.getMaxSpirit() - role.getSpirit());
            role.setSpirit(role.getSpirit() + recover);
            result.put("msg", "使用了" + actualUse + "个精力药水，恢复了" + recover + "点精力");
            useCount = actualUse;
        } else if ("energy_potion".equals(bag.getItemKey())) {
            if (role.getEnergy() >= role.getMaxEnergy()) throw new BusinessException("体力已满，无需使用体力药水");
            role.setEnergy(role.getMaxEnergy());
            role.setLastEnergyTime(java.time.LocalDateTime.now());
            result.put("msg", "使用了体力药水，体力已回满！");
            useCount = 1;
        } else {
            throw new BusinessException("该物品无法直接使用");
        }

        bag.setCount(bag.getCount() - useCount);
        if (bag.getCount() <= 0) bagMapper.deleteById(bagId);
        else bagMapper.updateById(bag);
        roleMapper.updateById(role);
        result.put("role", role);
        return result;
    }

    @Transactional
    public Map<String, Object> sellItem(Long userId, Long bagId, int count) {
        UserBag bag = bagMapper.selectById(bagId);
        if (bag == null || !bag.getUserId().equals(userId))
            throw new BusinessException("物品不存在");
        if (count <= 0) throw new BusinessException("出售数量必须大于0");
        if (bag.getCount() < count)
            throw new BusinessException("数量不足，当前拥有" + bag.getCount() + "个");

        GameConfig.ItemDef def = GameConfig.ITEMS.get(bag.getItemKey());
        int unitPrice = def != null ? def.sellPrice : 1;

        // 如果是装备（有extraJson），检查是否为锻造产物，返还50%矿石
        if (bag.getExtraJson() != null) {
            GameConfig.ForgeRecipe recipe = GameConfig.FORGE_RECIPES.stream()
                .filter(r -> r.resultKey.equals(bag.getItemKey())).findFirst().orElse(null);
            if (recipe != null) {
                // 锻造装备：返还50%矿石材料
                for (Map.Entry<String, Integer> req : recipe.materials.entrySet()) {
                    int refund = req.getValue() / 2;
                    if (refund > 0) addItem(userId, req.getKey(), refund);
                }
            }
            // 无论是否锻造产物，都按基础售价出售
        }

        long earn = (long) unitPrice * count;
        GameRole role = roleService.getByUserId(userId);
        role.setGold(role.getGold() + earn);
        roleMapper.updateById(role);
        bag.setCount(bag.getCount() - count);
        if (bag.getCount() <= 0) bagMapper.deleteById(bagId);
        else bagMapper.updateById(bag);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "出售成功！获得 " + earn + " 金币");
        result.put("earn", earn);
        result.put("role", role);
        return result;
    }

    public int getItemSellPrice(String itemKey) {
        GameConfig.ItemDef def = GameConfig.ITEMS.get(itemKey);
        return def != null ? def.sellPrice : 1;
    }

    @Transactional
    public Map<String, Object> equip(Long userId, Long bagId) {
        UserBag bag = bagMapper.selectById(bagId);
        if (bag == null || !bag.getUserId().equals(userId))
            throw new BusinessException("物品不存在");
        if (bag.getExtraJson() == null)
            throw new BusinessException("该物品不是装备");
        JSONObject extra = JSON.parseObject(bag.getExtraJson());
        String part = extra.getString("part");
        if (part == null) throw new BusinessException("装备部位异常");

        UserEquip old = equipMapper.selectOne(new LambdaQueryWrapper<UserEquip>()
                .eq(UserEquip::getUserId, userId).eq(UserEquip::getPart, part));
        if (old != null) unequipToBag(userId, old);

        UserEquip equip = new UserEquip();
        equip.setUserId(userId);
        equip.setItemKey(bag.getItemKey());
        equip.setPart(part);
        equip.setQuality(extra.getString("quality"));
        equip.setStrengthenLevel(extra.getIntValue("strengthenLevel"));
        equip.setBaseAttrJson(extra.getString("baseAttr"));
        equipMapper.insert(equip);

        bag.setCount(bag.getCount() - 1);
        if (bag.getCount() <= 0) bagMapper.deleteById(bagId);
        else bagMapper.updateById(bag);

        recalcEquipAttr(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "穿戴成功");
        result.put("role", roleService.getByUserId(userId));
        return result;
    }

    @Transactional
    public Map<String, Object> unequip(Long userId, Long equipId) {
        UserEquip equip = equipMapper.selectById(equipId);
        if (equip == null || !equip.getUserId().equals(userId))
            throw new BusinessException("装备不存在");
        unequipToBag(userId, equip);
        recalcEquipAttr(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "卸下成功");
        result.put("role", roleService.getByUserId(userId));
        return result;
    }

    private void unequipToBag(Long userId, UserEquip equip) {
        JSONObject extra = new JSONObject();
        extra.put("part", equip.getPart());
        extra.put("quality", equip.getQuality());
        extra.put("strengthenLevel", equip.getStrengthenLevel());
        extra.put("baseAttr", equip.getBaseAttrJson());
        UserBag bagItem = new UserBag();
        bagItem.setUserId(userId);
        bagItem.setItemKey(equip.getItemKey());
        bagItem.setCount(1);
        bagItem.setExtraJson(extra.toJSONString());
        bagMapper.insert(bagItem);
        equipMapper.deleteById(equip.getId());
    }

    public void recalcEquipAttr(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        int[] base = GameConfig.JOB_BASE.get(role.getJob());
        int[] growth = GameConfig.JOB_GROWTH.get(role.getJob());
        int lvl = role.getLevel();
        double rebornMult = 1.0 + role.getRebornCount() * 0.2;

        int baseHp = (int)((base[0] + growth[0] * (lvl - 1)) * rebornMult);
        int baseAtk = (int)((base[1] + growth[1] * (lvl - 1)) * rebornMult);
        int baseDef = (int)((base[2] + growth[2] * (lvl - 1)) * rebornMult);
        int baseSpd = (int)((base[3] + growth[3] * (lvl - 1)) * rebornMult);

        int equipHp = 0, equipAtk = 0, equipDef = 0;
        List<UserEquip> equips = getEquips(userId);
        for (UserEquip e : equips) {
            if (e.getBaseAttrJson() != null) {
                try {
                    JSONObject attr = JSON.parseObject(e.getBaseAttrJson());
                    double slMult = 1.0 + e.getStrengthenLevel() * 0.05;
                    equipHp += (int)(attr.getIntValue("hp") * slMult);
                    equipAtk += (int)(attr.getIntValue("atk") * slMult);
                    equipDef += (int)(attr.getIntValue("def") * slMult);
                } catch (Exception ignored) {}
            }
        }

        int oldMaxHp = role.getMaxHp();
        int newMaxHp = baseHp + equipHp;
        role.setMaxHp(newMaxHp);
        // 同步当前生命值：如果最大生命增加，当前生命也增加相同差值
        if (newMaxHp > oldMaxHp) {
            role.setHp(Math.min(role.getHp() + (newMaxHp - oldMaxHp), newMaxHp));
        } else {
            role.setHp(Math.min(role.getHp(), newMaxHp));
        }
        role.setAttack(baseAtk + equipAtk);
        role.setDefense(baseDef + equipDef);
        role.setSpeed(baseSpd);
        roleService.recalcFightPower(role);
    }
}
