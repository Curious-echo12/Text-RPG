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
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final GameRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final FarmPlotMapper farmPlotMapper;
    private final UserAchievementMapper achieveMapper;
    private final MailMapper mailMapper;
    private final UserEquipMapper equipMapper;

    public GameRole getByUserId(Long userId) {
        return roleMapper.selectOne(new LambdaQueryWrapper<GameRole>().eq(GameRole::getUserId, userId));
    }

    /** 检查角色名是否已被使用 */
    public boolean isNameTaken(String name) {
        return roleMapper.selectCount(new LambdaQueryWrapper<GameRole>().eq(GameRole::getName, name)) > 0;
    }

    @Transactional
    public GameRole createRole(Long userId, String name, String job) {
        if (getByUserId(userId) != null) throw new BusinessException("角色已存在");
        if (name == null || name.trim().isEmpty()) throw new BusinessException("角色名不能为空");
        if (name.length() < 2 || name.length() > 12) throw new BusinessException("角色名长度需为2-12个字符");
        if (isNameTaken(name)) throw new BusinessException("该角色名已被使用，请换一个");
        if (!GameConfig.JOB_BASE.containsKey(job)) throw new BusinessException("无效职业");
        int[] base = GameConfig.JOB_BASE.get(job);
        GameRole role = new GameRole();
        role.setUserId(userId); role.setName(name); role.setJob(job); role.setLevel(1);
        role.setMaxHp(base[0]); role.setHp(base[0]);
        role.setAttack(base[1]); role.setDefense(base[2]); role.setSpeed(base[3]);
        role.setCritRate(0.05); role.setCritDmg(1.5);
        role.setGold(80L); role.setDiamond(10);
        role.setEnergy(100); role.setMaxEnergy(100);
        role.setSpirit(50); role.setMaxSpirit(50);
        role.setLastEnergyTime(LocalDateTime.now());
        role.setLastSpiritTime(LocalDateTime.now());
        roleMapper.insert(role);
        recalcFightPower(role);
        for (int i = 0; i < 3; i++) {
            FarmPlot fp = new FarmPlot();
            fp.setUserId(userId); fp.setPlotIndex(i); fp.setStatus("EMPTY");
            farmPlotMapper.insert(fp);
        }
        for (GameConfig.AchieveDef ad : GameConfig.ACHIEVEMENTS) {
            UserAchievement ua = new UserAchievement();
            ua.setUserId(userId); ua.setAchieveKey(ad.key); ua.setTarget(ad.target);
            achieveMapper.insert(ua);
        }
        // 新手大礼包（根据改动重新规划）
        Mail mail = new Mail();
        mail.setUserId(userId); mail.setTitle("欢迎来到文字江湖！");
        mail.setContent("少侠好！随信附上新手礼包，祝你闯荡江湖顺利！\n\n包含：50金币、10钻石、3个精力药水、5个强化石");
        mail.setItemJson("{\"gold\":50,\"diamond\":10,\"mana_potion\":3,\"strengthen_stone\":5}");
        mail.setCreateTime(LocalDateTime.now());
        mailMapper.insert(mail);
        return role;
    }

    public void recalcFightPower(GameRole role) {
        long fp = (long) (role.getMaxHp() * 0.1 + role.getAttack() * 2.0 + role.getDefense() * 1.5
                + role.getSpeed() * 1.0 + role.getCritRate() * 100 + role.getCritDmg() * 50);
        role.setFightPower(fp);
        roleMapper.updateById(role);
    }

    public void checkLevelUp(GameRole role) {
        boolean changed = false;
        while (role.getExp() >= GameConfig.levelExp(role.getLevel())) {
            role.setExp(role.getExp() - GameConfig.levelExp(role.getLevel()));
            role.setLevel(role.getLevel() + 1);
            int[] growth = GameConfig.JOB_GROWTH.getOrDefault(role.getJob(), new int[]{10, 3, 2, 0});
            role.setMaxHp(role.getMaxHp() + growth[0]);
            role.setHp(role.getMaxHp());
            role.setAttack(role.getAttack() + growth[1]);
            role.setDefense(role.getDefense() + growth[2]);
            role.setSpeed(role.getSpeed() + growth[3]);
            role.setSkillPoint(role.getSkillPoint() + 1);
            changed = true;
        }
        if (changed) recalcFightPower(role);
    }

    public void recoverEnergy(GameRole role) {
        LocalDateTime now = LocalDateTime.now();
        boolean updated = false;
        int energyInterval = GameConfig.getSetting("energyRecoverSec", 60);
        int spiritInterval = GameConfig.getSetting("spiritRecoverSec", 60);
        if (role.getLastEnergyTime() != null && role.getEnergy() < role.getMaxEnergy()) {
            long seconds = java.time.Duration.between(role.getLastEnergyTime(), now).getSeconds();
            int recover = (int) Math.min(seconds / energyInterval, role.getMaxEnergy() - role.getEnergy());
            if (recover > 0) {
                role.setEnergy(role.getEnergy() + recover);
                role.setLastEnergyTime(role.getLastEnergyTime().plusSeconds((long) recover * energyInterval));
                updated = true;
            }
        }
        if (role.getLastSpiritTime() != null && role.getSpirit() < role.getMaxSpirit()) {
            long seconds = java.time.Duration.between(role.getLastSpiritTime(), now).getSeconds();
            int recover = (int) Math.min(seconds / spiritInterval, role.getMaxSpirit() - role.getSpirit());
            if (recover > 0) {
                role.setSpirit(role.getSpirit() + recover);
                role.setLastSpiritTime(role.getLastSpiritTime().plusSeconds((long) recover * spiritInterval));
                updated = true;
            }
        }
        if (updated) roleMapper.updateById(role);
    }

    public Map<String, Object> getRecoverInfo(GameRole role) {
        Map<String, Object> info = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        int energyInterval = GameConfig.getSetting("energyRecoverSec", 60);
        int spiritInterval = GameConfig.getSetting("spiritRecoverSec", 60);
        if (role.getEnergy() < role.getMaxEnergy() && role.getLastEnergyTime() != null) {
            long elapsed = java.time.Duration.between(role.getLastEnergyTime(), now).getSeconds();
            info.put("energyRecoverSec", energyInterval - (elapsed % energyInterval));
        } else {
            info.put("energyRecoverSec", -1);
        }
        if (role.getSpirit() < role.getMaxSpirit() && role.getLastSpiritTime() != null) {
            long elapsed = java.time.Duration.between(role.getLastSpiritTime(), now).getSeconds();
            info.put("spiritRecoverSec", spiritInterval - (elapsed % spiritInterval));
        } else {
            info.put("spiritRecoverSec", -1);
        }
        return info;
    }

    /** 获取角色属性拆分详情 */
    public Map<String, Object> getRoleDetail(Long userId) {
        GameRole role = getByUserId(userId);
        if (role == null) return Collections.emptyMap();
        recoverEnergy(role);

        int[] base = GameConfig.JOB_BASE.getOrDefault(role.getJob(), new int[]{100, 10, 5, 5});
        int[] growth = GameConfig.JOB_GROWTH.getOrDefault(role.getJob(), new int[]{10, 3, 2, 0});
        int lvl = role.getLevel();
        double rebornMult = 1.0 + role.getRebornCount() * 0.2;

        List<UserEquip> equips = equipMapper.selectList(
                new LambdaQueryWrapper<UserEquip>().eq(UserEquip::getUserId, userId));

        int equipHp = 0, equipAtk = 0, equipDef = 0;
        List<Map<String, Object>> equipList = new ArrayList<>();
        for (UserEquip e : equips) {
            Map<String, Object> ei = new HashMap<>();
            ei.put("name", GameConfig.ITEMS.get(e.getItemKey()) != null ?
                    GameConfig.ITEMS.get(e.getItemKey()).name : e.getItemKey());
            ei.put("part", e.getPart());
            ei.put("quality", e.getQuality());
            ei.put("strengthenLevel", e.getStrengthenLevel());
            if (e.getBaseAttrJson() != null) {
                try {
                    JSONObject attr = JSON.parseObject(e.getBaseAttrJson());
                    double slMult = 1.0 + e.getStrengthenLevel() * 0.05;
                    int aHp = (int)(attr.getIntValue("hp") * slMult);
                    int aAtk = (int)(attr.getIntValue("atk") * slMult);
                    int aDef = (int)(attr.getIntValue("def") * slMult);
                    equipHp += aHp; equipAtk += aAtk; equipDef += aDef;
                    ei.put("hp", aHp); ei.put("atk", aAtk); ei.put("def", aDef);
                } catch (Exception ignored) {}
            }
            equipList.add(ei);
        }

        String jobName;
        switch (role.getJob()) {
            case "WARRIOR": jobName = "战士"; break;
            case "MAGE":    jobName = "法师"; break;
            case "ARCHER":  jobName = "射手"; break;
            case "PRIEST":  jobName = "牧师"; break;
            case "MINER":   jobName = "矿工"; break;
            default: jobName = role.getJob();
        }

        Map<String, Object> detail = new HashMap<>();

        Map<String, Object> hpD = new LinkedHashMap<>();
        hpD.put("base", base[0]); hpD.put("levelGrowth", growth[0] * (lvl - 1));
        hpD.put("equip", equipHp); hpD.put("rebornMult", rebornMult); hpD.put("total", role.getMaxHp());
        detail.put("maxHp", hpD);

        Map<String, Object> atkD = new LinkedHashMap<>();
        atkD.put("base", base[1]); atkD.put("levelGrowth", growth[1] * (lvl - 1));
        atkD.put("equip", equipAtk); atkD.put("rebornMult", rebornMult); atkD.put("total", role.getAttack());
        detail.put("attack", atkD);

        Map<String, Object> defD = new LinkedHashMap<>();
        defD.put("base", base[2]); defD.put("levelGrowth", growth[2] * (lvl - 1));
        defD.put("equip", equipDef); defD.put("rebornMult", rebornMult); defD.put("total", role.getDefense());
        detail.put("defense", defD);

        Map<String, Object> spdD = new LinkedHashMap<>();
        spdD.put("base", base[3]); spdD.put("levelGrowth", growth[3] * (lvl - 1));
        spdD.put("total", role.getSpeed());
        detail.put("speed", spdD);

        detail.put("critRate", role.getCritRate());
        detail.put("critDmg", role.getCritDmg());
        detail.put("jobName", jobName);
        detail.put("rebornCount", role.getRebornCount());
        detail.put("equips", equipList);

        // 挖矿等级加成说明
        int mineBonus = role.getMineLevel() / 5;
        detail.put("mineLevel", role.getMineLevel());
        detail.put("mineBonus", mineBonus);
        detail.put("mineBonusDesc", "挖矿等级" + role.getMineLevel() + "级，每次挖矿额外产出" + mineBonus + "个矿石");

        // 种植等级加成说明
        int farmBonus = role.getFarmLevel() / 3;
        detail.put("farmLevel", role.getFarmLevel());
        detail.put("farmBonus", farmBonus);
        detail.put("farmBonusDesc", "种植等级" + role.getFarmLevel() + "级，每次收获额外产出" + farmBonus + "个作物");

        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        result.put("detail", detail);

        // 返回玩家ID和邮箱
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            result.put("playerId", user.getPlayerId());
            result.put("email", user.getEmail());
        }
        return result;
    }

    public Map<String, Object> reborn(Long userId) {
        GameRole role = getByUserId(userId);
        if (role.getLevel() < 100) throw new BusinessException("需要100级才能转生");
        if (role.getGold() < 100000) throw new BusinessException("需要100000金币");
        role.setGold(role.getGold() - 100000);
        role.setRebornCount(role.getRebornCount() + 1);
        role.setLevel(1); role.setExp(0L);
        int[] base = GameConfig.JOB_BASE.get(role.getJob());
        double mult = 1.0 + role.getRebornCount() * 0.2;
        role.setMaxHp((int) (base[0] * mult)); role.setHp(role.getMaxHp());
        role.setAttack((int) (base[1] * mult));
        role.setDefense((int) (base[2] * mult));
        role.setSpeed((int) (base[3] * mult));
        recalcFightPower(role);
        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        result.put("msg", "转生成功！属性大幅提升！");
        return result;
    }
}
