// backend/src/main/java/com/textrpg/service/PveService.java
package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PveService {
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;
    private final BagService bagService;
    private final BattleService battleService;
    private final UserSkillMapper skillMapper;

    public List<Map<String, Object>> getChapters(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameConfig.ChapterDef ch : GameConfig.CHAPTERS) {
            Map<String, Object> info = new HashMap<>();
            info.put("chapter", ch.chapter);
            info.put("name", ch.name);
            info.put("levelReq", ch.levelReq);
            info.put("unlocked", role.getLevel() >= ch.levelReq);
            List<Map<String, Object>> stages = new ArrayList<>();
            for (int i = 0; i < ch.stages.size(); i++) {
                GameConfig.MonsterDef m = ch.stages.get(i);
                Map<String, Object> si = new HashMap<>();
                si.put("index", i); si.put("name", m.name); si.put("level", m.level);
                si.put("hp", m.hp); si.put("atk", m.atk);
                si.put("exp", m.expReward); si.put("gold", m.goldReward);
                stages.add(si);
            }
            info.put("stages", stages);
            result.add(info);
        }
        return result;
    }

    public Map<String, Object> fight(Long userId, int chapter, int stage) {
        if (chapter < 1 || chapter > GameConfig.CHAPTERS.size())
            throw new BusinessException("章节不存在");
        GameConfig.ChapterDef ch = GameConfig.CHAPTERS.get(chapter - 1);
        GameRole role = roleService.getByUserId(userId);
        roleService.recoverEnergy(role);
        if (role.getLevel() < ch.levelReq)
            throw new BusinessException("等级不足，需要Lv." + ch.levelReq);
        if (stage < 0 || stage >= ch.stages.size())
            throw new BusinessException("关卡不存在");
        if (role.getEnergy() < 5)
            throw new BusinessException("体力不足，需要5点");

        role.setEnergy(role.getEnergy() - 5);
        role.setLastEnergyTime(LocalDateTime.now());
        roleMapper.updateById(role);

        GameConfig.MonsterDef monster = ch.stages.get(stage);
        List<UserSkill> skills = skillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>().eq(UserSkill::getUserId, userId));

        Map<String, Object> battleResult = battleService.doBattle(role, monster, skills);

        if ((Boolean) battleResult.get("win")) {
            // 安全转换：兼容 Integer 和 Long
            long exp = toLong(battleResult.get("expReward"));
            long gold = toLong(battleResult.get("goldReward"));
            role.setExp(role.getExp() + exp);
            role.setGold(role.getGold() + gold);
            roleService.checkLevelUp(role);

            List<String> drops = (List<String>) battleResult.get("drops");
            if (drops != null) {
                for (String dk : drops) bagService.addItem(userId, dk, 1);
            }
        }

        // 无论胜负，PVE战斗后生命恢复满
        role.setHp(role.getMaxHp());
        roleMapper.updateById(role);

        battleResult.put("role", role);
        return battleResult;
    }

    /** 安全地将 Object 转为 Long，兼容 Integer/Long/Number */
    private long toLong(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }
}
