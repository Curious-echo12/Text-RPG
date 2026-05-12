package com.textrpg.service;

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
public class MineService {
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;
    private final BagService bagService;
    private final MemoryCache cache;

    public Map<String, Object> getMineInfo(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        roleService.recoverEnergy(role);
        Map<String, Object> info = new HashMap<>();
        info.put("mineLevel", role.getMineLevel());
        info.put("mineExp", role.getMineExp());
        info.put("mineNextExp", role.getMineLevel() * 100);
        info.put("energy", role.getEnergy());

        // 挖矿等级加成说明
        int bonusCount = role.getMineLevel() / 5;
        info.put("mineBonusCount", bonusCount);
        info.put("mineBonusDesc", "挖矿等级" + role.getMineLevel() + "级，每次挖矿额外产出" + bonusCount + "个矿石（每5级+1）");

        List<Map<String, Object>> mineList = new ArrayList<>();
        for (int i = 0; i < GameConfig.MINES.size(); i++) {
            GameConfig.MineDef mine = GameConfig.MINES.get(i);
            Map<String, Object> mineInfo = new HashMap<>();
            mineInfo.put("name", mine.name);
            mineInfo.put("desc", mine.desc);
            mineInfo.put("energyCost", mine.energyCost);
            mineInfo.put("cooldown", mine.cooldown);

            int totalWeight = mine.weights.stream().mapToInt(Integer::intValue).sum();
            List<Map<String, Object>> drops = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (int j = 0; j < mine.dropKeys.size(); j++) {
                String key = mine.dropKeys.get(j);
                if (seen.contains(key)) continue;
                seen.add(key);
                int mergedWeight = 0;
                for (int k = 0; k < mine.dropKeys.size(); k++) {
                    if (mine.dropKeys.get(k).equals(key)) mergedWeight += mine.weights.get(k);
                }
                Map<String, Object> drop = new HashMap<>();
                GameConfig.ItemDef item = GameConfig.ITEMS.get(key);
                drop.put("key", key);
                drop.put("name", item != null ? item.name : key);
                drop.put("rate", Math.round(mergedWeight * 100.0 / totalWeight));
                drops.add(drop);
            }
            mineInfo.put("drops", drops);

            String cdKey = "mine_cd:" + userId + ":" + i;
            long cdRemain = cache.getTtlRemaining(cdKey);
            mineInfo.put("cooldownRemain", cdRemain);

            mineList.add(mineInfo);
        }
        info.put("mines", mineList);
        return info;
    }

    public Map<String, Object> dig(Long userId, int mineIndex) {
        if (mineIndex < 0 || mineIndex >= GameConfig.MINES.size())
            throw new BusinessException("无效矿洞");
        GameConfig.MineDef mine = GameConfig.MINES.get(mineIndex);
        GameRole role = roleService.getByUserId(userId);
        roleService.recoverEnergy(role);

        String cdKey = "mine_cd:" + userId + ":" + mineIndex;
        long cdRemain = cache.getTtlRemaining(cdKey);
        if (cdRemain > 0)
            throw new BusinessException("冷却中，还需" + cdRemain + "秒");
        if (role.getEnergy() < mine.energyCost)
            throw new BusinessException("体力不足，需要" + mine.energyCost + "点");

        role.setEnergy(role.getEnergy() - mine.energyCost);
        role.setLastEnergyTime(LocalDateTime.now());

        String dropKey = weightedRandom(mine.dropKeys, mine.weights);
        int dropCount = 1 + role.getMineLevel() / 5;
        if ("MINER".equals(role.getJob())) dropCount += 1;

        bagService.addItem(userId, dropKey, dropCount);

        role.setMineExp(role.getMineExp() + 10);
        while (role.getMineExp() >= role.getMineLevel() * 100) {
            role.setMineExp(role.getMineExp() - role.getMineLevel() * 100);
            role.setMineLevel(role.getMineLevel() + 1);
        }
        roleMapper.updateById(role);

        cache.set(cdKey, String.valueOf(mine.cooldown), mine.cooldown);

        GameConfig.ItemDef item = GameConfig.ITEMS.get(dropKey);
        int levelBonus = role.getMineLevel() / 5;
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "挖矿成功！获得 " + (item != null ? item.name : dropKey) + " x" + dropCount
                + (levelBonus > 0 ? "（等级加成: +" + levelBonus + "）" : ""));
        result.put("role", role);
        return result;
    }

    private String weightedRandom(List<String> keys, List<Integer> weights) {
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int rand = new Random().nextInt(total);
        int sum = 0;
        for (int i = 0; i < keys.size(); i++) {
            sum += weights.get(i);
            if (rand < sum) return keys.get(i);
        }
        return keys.get(keys.size() - 1);
    }
}
