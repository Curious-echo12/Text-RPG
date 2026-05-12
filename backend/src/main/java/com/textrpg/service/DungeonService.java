package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.common.MemoryCache;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DungeonService {
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;
    private final BagService bagService;
    private final BattleService battleService;
    private final UserSkillMapper skillMapper;
    private final MemoryCache cache;

    public List<Map<String, Object>> getDungeons(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameConfig.DungeonDef d : GameConfig.DUNGEONS) {
            Map<String, Object> info = new HashMap<>();
            info.put("key", d.key); info.put("name", d.name);
            info.put("desc", d.desc);
            info.put("levelReq", d.levelReq);
            info.put("energyCost", d.energyCost);
            info.put("dailyLimit", d.dailyLimit);

            // Boss信息
            Map<String, Object> bossInfo = new HashMap<>();
            bossInfo.put("name", d.boss.name);
            bossInfo.put("level", d.boss.level);
            bossInfo.put("hp", d.boss.hp);
            bossInfo.put("atk", d.boss.atk);
            info.put("boss", bossInfo);

            // 奖励掉落概率展示
            if (d.boss.dropKeys != null) {
                List<Map<String, Object>> dropInfo = new ArrayList<>();
                for (int i = 0; i < d.boss.dropKeys.size(); i++) {
                    Map<String, Object> di = new HashMap<>();
                    String key = d.boss.dropKeys.get(i);
                    GameConfig.ItemDef item = GameConfig.ITEMS.get(key);
                    di.put("key", key);
                    di.put("name", item != null ? item.name : key);
                    di.put("rate", d.boss.dropRates.get(i) / 100.0); // 转为百分比
                    dropInfo.add(di);
                }
                info.put("drops", dropInfo);
            }

            String countKey = "dungeon:" + userId + ":" + d.key + ":" + java.time.LocalDate.now();
            String used = cache.get(countKey);
            int usedCount = used != null ? Integer.parseInt(used) : 0;
            info.put("usedCount", usedCount);
            info.put("remainCount", d.dailyLimit - usedCount);
            result.add(info);
        }
        return result;
    }

    public Map<String, Object> enterDungeon(Long userId, String dungeonKey) {
        GameConfig.DungeonDef dungeon = GameConfig.DUNGEONS.stream()
                .filter(d -> d.key.equals(dungeonKey)).findFirst()
                .orElseThrow(() -> new BusinessException("副本不存在"));
        GameRole role = roleService.getByUserId(userId);
        roleService.recoverEnergy(role);
        if (role.getLevel() < dungeon.levelReq)
            throw new BusinessException("等级不足，需要Lv." + dungeon.levelReq);
        if (role.getEnergy() < dungeon.energyCost)
            throw new BusinessException("体力不足，需要" + dungeon.energyCost + "点");

        String countKey = "dungeon:" + userId + ":" + dungeonKey + ":" + java.time.LocalDate.now();
        String used = cache.get(countKey);
        int usedCount = used != null ? Integer.parseInt(used) : 0;
        if (usedCount >= dungeon.dailyLimit)
            throw new BusinessException("今日次数已用完（" + dungeon.dailyLimit + "次）");

        role.setEnergy(role.getEnergy() - dungeon.energyCost);
        role.setLastEnergyTime(LocalDateTime.now());
        roleMapper.updateById(role);

        List<UserSkill> skills = skillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>().eq(UserSkill::getUserId, userId));
        Map<String, Object> battleResult = battleService.doBattle(role, dungeon.boss, skills);

        cache.set(countKey, String.valueOf(usedCount + 1), 86400);

        if ((Boolean) battleResult.get("win")) {
            long exp = toLong(battleResult.get("expReward"));
            long gold = toLong(battleResult.get("goldReward"));
            role.setExp(role.getExp() + exp);
            role.setGold(role.getGold() + gold);
            roleService.checkLevelUp(role);
            @SuppressWarnings("unchecked")
            List<String> drops = (List<String>) battleResult.get("drops");
            List<String> dropNames = new ArrayList<>();
            if (drops != null) {
                for (String dk : drops) {
                    bagService.addItem(userId, dk, 1);
                    GameConfig.ItemDef item = GameConfig.ITEMS.get(dk);
                    dropNames.add(item != null ? item.name : dk);
                }
            }
            if (!dropNames.isEmpty()) {
                battleResult.put("dropMsg", "额外获得：" + String.join("、", dropNames));
            }
        }

        // 副本战斗后回满血
        role.setHp(role.getMaxHp());
        roleMapper.updateById(role);
        battleResult.put("role", role);
        return battleResult;
    }

    public Map<String, Object> exploreSecret(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        roleService.recoverEnergy(role);
        if (role.getSpirit() < 10)
            throw new BusinessException("精力不足，需要10点");

        role.setSpirit(role.getSpirit() - 10);
        role.setLastSpiritTime(LocalDateTime.now());
        roleMapper.updateById(role);

        GameConfig.SecretEvent event = GameConfig.SECRET_EVENTS.get(
                new Random().nextInt(GameConfig.SECRET_EVENTS.size()));
        Map<String, Object> result = new HashMap<>();
        result.put("eventType", event.type);
        result.put("desc", event.desc);

        switch (event.type) {
            case "BATTLE":
                List<UserSkill> skills = skillMapper.selectList(
                        new LambdaQueryWrapper<UserSkill>().eq(UserSkill::getUserId, userId));
                Map<String, Object> br = battleService.doBattle(role, event.monster, skills);
                if ((Boolean) br.get("win")) {
                    long exp = toLong(br.get("expReward"));
                    long gold = toLong(br.get("goldReward"));
                    role.setExp(role.getExp() + exp);
                    role.setGold(role.getGold() + gold);
                    roleService.checkLevelUp(role);
                    @SuppressWarnings("unchecked")
                    List<String> drops = (List<String>) br.get("drops");
                    if (drops != null) for (String dk : drops) bagService.addItem(userId, dk, 1);
                }
                // 秘境战斗后回满血
                role.setHp(role.getMaxHp());
                roleMapper.updateById(role);
                result.put("battle", br);
                break;
            case "CHEST":
                if ("DIAMOND".equals(event.rewardType)) {
                    role.setDiamond(role.getDiamond() + event.rewardAmount);
                    result.put("msg", "获得 " + event.rewardAmount + " 钻石！");
                } else {
                    role.setGold(role.getGold() + Math.abs(event.rewardAmount));
                    result.put("msg", "获得 " + Math.abs(event.rewardAmount) + " 金币！");
                }
                roleMapper.updateById(role);
                break;
            case "TRAP":
                long loss = Math.min(role.getGold(), Math.abs(event.rewardAmount));
                role.setGold(role.getGold() - loss);
                roleMapper.updateById(role);
                result.put("msg", "损失了 " + loss + " 金币...");
                break;
            case "WONDER":
                if ("ITEM".equals(event.rewardType) && event.rewardItemKey != null) {
                    bagService.addItem(userId, event.rewardItemKey, event.rewardAmount);
                    GameConfig.ItemDef item = GameConfig.ITEMS.get(event.rewardItemKey);
                    result.put("msg", "获得 " + (item != null ? item.name : event.rewardItemKey) + " x" + event.rewardAmount);
                } else {
                    role.setDiamond(role.getDiamond() + event.rewardAmount);
                    roleMapper.updateById(role);
                    result.put("msg", "获得 " + event.rewardAmount + " 钻石！");
                }
                break;
        }
        result.put("role", role);
        return result;
    }

    private long toLong(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }
}
