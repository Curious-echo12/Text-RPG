package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PvpService {
    private final GameRoleMapper roleMapper;
    private final PvpRecordMapper recordMapper;
    private final RoleService roleService;
    private final BattleService battleService;
    private final UserSkillMapper skillMapper;

    /** 公平匹配：同职业PVP积分相差不超过15%，无AI对手 */
    public Map<String, Object> match(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        String myJob = role.getJob();
        int myScore = role.getPvpScore();

        List<GameRole> candidates = roleMapper.selectList(
                new LambdaQueryWrapper<GameRole>()
                        .ne(GameRole::getUserId, userId)
                        .eq(GameRole::getJob, myJob)
                        .between(GameRole::getPvpScore,
                                Math.max(0, (int)(myScore * 0.85)),
                                (int)(myScore * 1.15 + 100))
                        .last("LIMIT 10"));

        if (candidates.isEmpty()) {
            throw new BusinessException("当前无合适的同职业对手，请稍后再试");
        }

        GameRole opponent = candidates.get(new Random().nextInt(candidates.size()));

        Map<String, Object> result = new HashMap<>();
        result.put("opponentName", opponent.getName());
        result.put("opponentLevel", opponent.getLevel());
        result.put("opponentPower", opponent.getFightPower());
        result.put("opponentScore", opponent.getPvpScore());
        result.put("opponentJob", opponent.getJob());
        result.put("opponentJobName", jobName(opponent.getJob()));
        result.put("opponentUserId", opponent.getUserId());
        return result;
    }

    public Map<String, Object> fight(Long userId, Long opponentUserId) {
        GameRole attacker = roleService.getByUserId(userId);
        roleService.recoverEnergy(attacker);
        if (attacker.getSpirit() < 5)
            throw new BusinessException("精力不足，需要5点");
        attacker.setSpirit(attacker.getSpirit() - 5);
        attacker.setLastSpiritTime(LocalDateTime.now());
        roleMapper.updateById(attacker);

        if (opponentUserId == null || opponentUserId <= 0)
            throw new BusinessException("无效的对手");

        GameRole defender = roleService.getByUserId(opponentUserId);
        if (defender == null) throw new BusinessException("对手不存在");
        if (!defender.getJob().equals(attacker.getJob()))
            throw new BusinessException("只能与同职业对手对战");

        List<UserSkill> skills = skillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>().eq(UserSkill::getUserId, userId));

        GameConfig.MonsterDef monsterDef = new GameConfig.MonsterDef(
                "pvp", defender.getName(), defender.getLevel(),
                defender.getMaxHp(), defender.getAttack(),
                defender.getDefense(), defender.getSpeed(), 0, 0, null, null);

        Map<String, Object> battleResult = battleService.doBattle(attacker, monsterDef, skills);
        boolean win = (Boolean) battleResult.get("win");
        int resultHp = (Integer) battleResult.get("playerHp");

        int scoreChange;
        if (win) {
            scoreChange = 30 + Math.max(0, (defender.getPvpScore() - attacker.getPvpScore()) / 10);
            attacker.setPvpScore(attacker.getPvpScore() + scoreChange);
            attacker.setPvpStar(attacker.getPvpStar() + 1);
        } else {
            scoreChange = -20;
            attacker.setPvpScore(Math.max(0, attacker.getPvpScore() + scoreChange));
        }

        // PVP战斗后回满血
        attacker.setHp(attacker.getMaxHp());
        roleMapper.updateById(attacker);

        PvpRecord record = new PvpRecord();
        record.setAttackerId(userId);
        record.setDefenderId(opponentUserId);
        record.setAttackerName(attacker.getName());
        record.setDefenderName(defender.getName());
        record.setResult(win ? "WIN" : "LOSE");
        record.setScoreChange(scoreChange);
        @SuppressWarnings("unchecked")
        List<String> logList = (List<String>) battleResult.get("log");
        record.setBattleLog(String.join("\n", logList != null ? logList : Collections.emptyList()));
        recordMapper.insert(record);

        battleResult.put("scoreChange", scoreChange);
        battleResult.put("pvpScore", attacker.getPvpScore());
        battleResult.put("pvpRank", GameConfig.getPvpRank(attacker.getPvpScore()));
        battleResult.put("role", attacker);
        return battleResult;
    }

    public List<Map<String, Object>> getPvpRankByJob(String job, int size) {
        List<GameRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<GameRole>()
                        .eq(GameRole::getJob, job)
                        .orderByDesc(GameRole::getPvpScore)
                        .last("LIMIT " + size));
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (GameRole r : roles) {
            Map<String, Object> info = new HashMap<>();
            info.put("rank", rank++);
            info.put("name", r.getName());
            info.put("level", r.getLevel());
            info.put("job", r.getJob());
            info.put("jobName", jobName(r.getJob()));
            info.put("pvpScore", r.getPvpScore());
            info.put("pvpRank", GameConfig.getPvpRank(r.getPvpScore()));
            info.put("fightPower", r.getFightPower());
            result.add(info);
        }
        return result;
    }

    public Map<String, Object> getMyPvpRank(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        List<GameRole> higher = roleMapper.selectList(
                new LambdaQueryWrapper<GameRole>()
                        .eq(GameRole::getJob, role.getJob())
                        .gt(GameRole::getPvpScore, role.getPvpScore()));
        Map<String, Object> info = new HashMap<>();
        info.put("rank", higher.size() + 1);
        info.put("score", role.getPvpScore());
        info.put("rankName", GameConfig.getPvpRank(role.getPvpScore()));
        info.put("job", role.getJob());
        info.put("jobName", jobName(role.getJob()));
        return info;
    }

    /** 分页查询PVP记录 */
    public Map<String, Object> getRecords(Long userId, int page, int size) {
        int offset = page * size;
        List<PvpRecord> records = recordMapper.selectList(new LambdaQueryWrapper<PvpRecord>()
                .eq(PvpRecord::getAttackerId, userId)
                .orderByDesc(PvpRecord::getCreateTime)
                .last("LIMIT " + size + " OFFSET " + offset));

        Long total = recordMapper.selectCount(new LambdaQueryWrapper<PvpRecord>()
                .eq(PvpRecord::getAttackerId, userId));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> list = new ArrayList<>();
        for (PvpRecord r : records) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", r.getId());
            info.put("attackerName", r.getAttackerName());
            info.put("defenderName", r.getDefenderName());
            info.put("result", r.getResult());
            info.put("scoreChange", r.getScoreChange());
            info.put("createTime", r.getCreateTime() != null ? r.getCreateTime().format(fmt) : "");
            list.add(info);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    private String jobName(String job) {
        switch (job) {
            case "WARRIOR": return "战士";
            case "MAGE": return "法师";
            case "ARCHER": return "射手";
            case "PRIEST": return "牧师";
            case "MINER": return "矿工";
            default: return job;
        }
    }
}
