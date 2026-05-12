package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final UserSkillMapper skillMapper;
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;

    public List<Map<String, Object>> getSkills(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        List<UserSkill> learned = skillMapper.selectList(
            new LambdaQueryWrapper<UserSkill>().eq(UserSkill::getUserId, userId));
        Map<String, UserSkill> learnedMap = learned.stream()
            .collect(Collectors.toMap(UserSkill::getSkillKey, s -> s));
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameConfig.SkillDef sd : GameConfig.SKILLS) {
            if (!sd.job.equals(role.getJob())) continue;
            Map<String, Object> info = new HashMap<>();
            info.put("key", sd.key); info.put("name", sd.name); info.put("type", sd.type);
            info.put("unlockLevel", sd.unlockLevel); info.put("maxLevel", sd.maxLevel);
            info.put("desc", sd.desc); info.put("goldCost", sd.goldCost); info.put("bookCost", sd.bookCost);
            UserSkill us = learnedMap.get(sd.key);
            info.put("learned", us != null);
            info.put("level", us != null ? us.getLevel() : 0);
            info.put("canLearn", role.getLevel() >= sd.unlockLevel);

            // 当前等级效果描述
            if (us != null) {
                info.put("currentEffect", getCurrentEffect(sd, us.getLevel()));
                info.put("nextEffect", us.getLevel() < sd.maxLevel ? getCurrentEffect(sd, us.getLevel() + 1) : "已满级");
            }
            result.add(info);
        }
        return result;
    }

    /** 获取技能在指定等级的效果描述（基于配置参数） */
    private String getCurrentEffect(GameConfig.SkillDef sd, int level) {
        if (sd.effectType == null) return sd.desc;
        double val = sd.baseValue + level * sd.valuePerLevel;
        switch (sd.effectType) {
            case "DAMAGE":
                return "造成" + String.format("%.0f", val * 100) + "%伤害";
            case "HEAL":
                return "恢复" + String.format("%.0f", val * 100) + "%生命值";
            case "DAMAGE_HEAL":
                return "造成" + String.format("%.0f", val * 100) + "%伤害，恢复" + String.format("%.0f", sd.secondaryValue * 100) + "%生命";
            case "PASSIVE_DEF":
                return "防御提升" + String.format("%.0f", val) + "%";
            case "PASSIVE_HP":
                return "生命提升" + String.format("%.0f", val) + "%";
            case "PASSIVE_SPD":
                return "速度提升" + String.format("%.0f", val) + "%";
            case "PASSIVE_ATK":
                return "攻击提升" + String.format("%.0f", val) + "%";
            case "MINE_BONUS":
                return "矿石产出+" + String.format("%.0f", val);
            case "MINE_RARE":
                return "稀有矿概率提升" + String.format("%.0f", val) + "%";
            case "ENERGY_MAX":
                return "体力上限+" + String.format("%.0f", val);
            default:
                return sd.desc;
        }
    }

    public Map<String, Object> learnSkill(Long userId, String skillKey) {
        GameRole role = roleService.getByUserId(userId);
        GameConfig.SkillDef sd = GameConfig.SKILLS.stream().filter(s -> s.key.equals(skillKey)).findFirst()
            .orElseThrow(() -> new BusinessException("技能不存在"));
        if (!sd.job.equals(role.getJob())) throw new BusinessException("职业不符");
        if (role.getLevel() < sd.unlockLevel) throw new BusinessException("等级不足");
        UserSkill existing = skillMapper.selectOne(new LambdaQueryWrapper<UserSkill>()
            .eq(UserSkill::getUserId, userId).eq(UserSkill::getSkillKey, skillKey));
        if (existing != null) throw new BusinessException("已学习该技能");
        if (role.getSkillPoint() < 1) throw new BusinessException("技能点不足");
        if (role.getGold() < sd.goldCost) throw new BusinessException("金币不足");
        role.setSkillPoint(role.getSkillPoint() - 1);
        role.setGold(role.getGold() - sd.goldCost);
        roleMapper.updateById(role);
        UserSkill us = new UserSkill();
        us.setUserId(userId); us.setSkillKey(skillKey); us.setLevel(1);
        skillMapper.insert(us);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "学习成功！当前效果：" + getCurrentEffect(sd, 1));
        result.put("role", role);
        return result;
    }

    public Map<String, Object> upgradeSkill(Long userId, String skillKey) {
        UserSkill us = skillMapper.selectOne(new LambdaQueryWrapper<UserSkill>()
            .eq(UserSkill::getUserId, userId).eq(UserSkill::getSkillKey, skillKey));
        if (us == null) throw new BusinessException("未学习该技能");
        GameConfig.SkillDef sd = GameConfig.SKILLS.stream().filter(s -> s.key.equals(skillKey)).findFirst().orElse(null);
        if (sd != null && us.getLevel() >= sd.maxLevel) throw new BusinessException("已达最大等级");
        GameRole role = roleService.getByUserId(userId);
        if (role.getSkillPoint() < 1) throw new BusinessException("技能点不足");
        int cost = sd != null ? sd.goldCost * us.getLevel() : 100;
        if (role.getGold() < cost) throw new BusinessException("金币不足");
        role.setSkillPoint(role.getSkillPoint() - 1);
        role.setGold(role.getGold() - cost);
        roleMapper.updateById(role);
        us.setLevel(us.getLevel() + 1);
        skillMapper.updateById(us);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "升级成功！当前等级：" + us.getLevel()
                + "，效果：" + (sd != null ? getCurrentEffect(sd, us.getLevel()) : ""));
        result.put("role", role);
        return result;
    }
}
