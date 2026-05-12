package com.textrpg.service;

import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BattleService {

    public Map<String, Object> doBattle(GameRole role, GameConfig.MonsterDef monster, List<UserSkill> skills) {
        List<String> log = new ArrayList<>();
        Random rand = new Random();

        int playerHp = role.getHp();
        int playerMaxHp = role.getMaxHp();
        int playerAtk = role.getAttack();
        int playerDef = role.getDefense();
        int playerSpd = role.getSpeed();
        double playerCritRate = role.getCritRate();
        double playerCritDmg = role.getCritDmg();

        // 应用被动技能效果
        if (skills != null) {
            for (UserSkill s : skills) {
                GameConfig.SkillDef sd = GameConfig.SKILLS.stream()
                        .filter(sk -> sk.key.equals(s.getSkillKey()) && "PASSIVE".equals(sk.type))
                        .findFirst().orElse(null);
                if (sd == null) continue;
                double val = sd.baseValue + s.getLevel() * sd.valuePerLevel;
                switch (sd.effectType) {
                    case "PASSIVE_DEF":
                        playerDef += (int)(playerDef * val / 100.0);
                        break;
                    case "PASSIVE_HP":
                        playerMaxHp += (int)(playerMaxHp * val / 100.0);
                        playerHp = playerMaxHp;
                        break;
                    case "PASSIVE_SPD":
                        playerSpd += (int)(playerSpd * val / 100.0);
                        break;
                    case "PASSIVE_ATK":
                        playerAtk += (int)(playerAtk * val / 100.0);
                        break;
                }
            }
        }

        int monsterHp = monster.hp;
        int monsterAtk = monster.atk;
        int monsterDef = monster.def;
        int monsterSpd = monster.spd;

        boolean playerFirst = playerSpd >= monsterSpd;

        String activeSkill = null;
        int skillLevel = 0;
        GameConfig.SkillDef activeSkillDef = null;
        if (skills != null) {
            for (UserSkill s : skills) {
                GameConfig.SkillDef sd = GameConfig.SKILLS.stream()
                        .filter(sk -> sk.key.equals(s.getSkillKey()) && "ACTIVE".equals(sk.type))
                        .findFirst().orElse(null);
                if (sd != null) {
                    activeSkill = s.getSkillKey();
                    skillLevel = s.getLevel();
                    activeSkillDef = sd;
                    break;
                }
            }
        }

        int round = 0;
        while (playerHp > 0 && monsterHp > 0 && round < 50) {
            round++;
            log.add("=== 第" + round + "回合 ===");

            if (playerFirst) {
                int[] r = playerTurn(activeSkill, activeSkillDef, skillLevel, playerAtk, playerHp, playerMaxHp,
                        monsterHp, monsterDef, playerCritRate, playerCritDmg, rand, log);
                playerHp = r[0]; monsterHp = r[1];
                if (monsterHp <= 0) break;

                int mDmg = Math.max(1, monsterAtk - playerDef / 2 + rand.nextInt(Math.max(1, monsterAtk / 5)));
                playerHp -= mDmg;
                log.add("【" + monster.name + "】发起攻击，造成 " + mDmg + " 伤害");
            } else {
                int mDmg = Math.max(1, monsterAtk - playerDef / 2 + rand.nextInt(Math.max(1, monsterAtk / 5)));
                playerHp -= mDmg;
                log.add("【" + monster.name + "】发起攻击，造成 " + mDmg + " 伤害");
                if (playerHp <= 0) break;

                int[] r = playerTurn(activeSkill, activeSkillDef, skillLevel, playerAtk, playerHp, playerMaxHp,
                        monsterHp, monsterDef, playerCritRate, playerCritDmg, rand, log);
                playerHp = r[0]; monsterHp = r[1];
            }
        }

        boolean win = monsterHp <= 0;
        Map<String, Object> result = new HashMap<>();
        result.put("log", log);
        result.put("win", win);
        result.put("playerHp", Math.max(0, playerHp));
        result.put("monsterHp", Math.max(0, monsterHp));

        if (win) {
            result.put("expReward", (long) monster.expReward);
            result.put("goldReward", (long) monster.goldReward);
            List<String> drops = new ArrayList<>();
            if (monster.dropKeys != null) {
                for (int i = 0; i < monster.dropKeys.size(); i++) {
                    if (rand.nextInt(10000) < monster.dropRates.get(i)) {
                        drops.add(monster.dropKeys.get(i));
                    }
                }
            }
            result.put("drops", drops);
        }
        return result;
    }

    private int[] playerTurn(String activeSkill, GameConfig.SkillDef skillDef, int skillLevel, int playerAtk,
                             int playerHp, int playerMaxHp, int monsterHp, int monsterDef,
                             double critRate, double critDmg, Random rand, List<String> log) {
        boolean useSkill = activeSkill != null && rand.nextInt(100) < 50;

        if (useSkill && skillDef != null && "HEAL".equals(skillDef.effectType)) {
            double healPct = skillDef.baseValue + skillLevel * skillDef.valuePerLevel;
            int heal = (int) (playerMaxHp * healPct);
            playerHp = Math.min(playerMaxHp, playerHp + heal);
            log.add("【你】使用 " + skillDef.name + "，恢复 " + heal + " HP");
        } else if (useSkill && skillDef != null && "DAMAGE_HEAL".equals(skillDef.effectType)) {
            double multiplier = skillDef.baseValue + skillLevel * skillDef.valuePerLevel;
            int dmg = calcDamageWithMultiplier(playerAtk, monsterDef, critRate, critDmg, rand, multiplier);
            double healPct = skillDef.secondaryValue;
            int heal = (int) (playerMaxHp * healPct);
            playerHp = Math.min(playerMaxHp, playerHp + heal);
            log.add("【你】使用 " + skillDef.name + "，造成 " + dmg + " 伤害，恢复 " + heal + " HP");
            monsterHp -= dmg;
        } else if (useSkill && skillDef != null && "DAMAGE".equals(skillDef.effectType)) {
            double multiplier = skillDef.baseValue + skillLevel * skillDef.valuePerLevel;
            int dmg = calcDamageWithMultiplier(playerAtk, monsterDef, critRate, critDmg, rand, multiplier);
            log.add("【你】使用 " + skillDef.name + "，造成 " + dmg + " 伤害");
            monsterHp -= dmg;
        } else if (useSkill) {
            int dmg = calcDamageWithMultiplier(playerAtk, monsterDef, critRate, critDmg, rand, 1.3 + skillLevel * 0.05);
            log.add("【你】使用 " + getSkillName(activeSkill) + "，造成 " + dmg + " 伤害");
            monsterHp -= dmg;
        } else {
            int dmg = calcDamageWithMultiplier(playerAtk, monsterDef, critRate, critDmg, rand, 1.0);
            log.add("【你】发起攻击，造成 " + dmg + " 伤害");
            monsterHp -= dmg;
        }
        return new int[]{playerHp, monsterHp};
    }

    private int calcDamageWithMultiplier(int atk, int def, double critRate, double critDmg,
                                          Random rand, double multiplier) {
        int baseDmg = Math.max(1, (int) (atk * multiplier - def * 0.5));
        int dmg = (int) (baseDmg * (0.9 + rand.nextDouble() * 0.2));
        if (rand.nextDouble() < critRate) {
            dmg = (int) (dmg * critDmg);
        }
        return Math.max(1, dmg);
    }

    private String getSkillName(String key) {
        return GameConfig.SKILLS.stream().filter(s -> s.key.equals(key))
                .map(s -> s.name).findFirst().orElse(key);
    }
}
