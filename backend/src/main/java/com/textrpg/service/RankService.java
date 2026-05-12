package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.entity.GameRole;
import com.textrpg.mapper.GameRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RankService {
    private final GameRoleMapper roleMapper;

    public List<Map<String, Object>> getRankByJob(String type, String job, int page, int size) {
        LambdaQueryWrapper<GameRole> wrapper = new LambdaQueryWrapper<GameRole>()
                .eq(GameRole::getJob, job);
        switch (type) {
            case "level": wrapper.orderByDesc(GameRole::getLevel); break;
            case "fight": wrapper.orderByDesc(GameRole::getFightPower); break;
            case "mine":  wrapper.orderByDesc(GameRole::getMineLevel); break;
            case "farm":  wrapper.orderByDesc(GameRole::getFarmLevel); break;
            case "forge": wrapper.orderByDesc(GameRole::getForgeLevel); break;
            default: wrapper.orderByDesc(GameRole::getFightPower);
        }
        wrapper.last("LIMIT " + size + " OFFSET " + (page * size));
        List<GameRole> roles = roleMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = page * size + 1;
        for (GameRole r : roles) {
            Map<String, Object> info = new HashMap<>();
            info.put("rank", rank++);
            info.put("name", r.getName());
            info.put("level", r.getLevel());
            info.put("job", r.getJob());
            info.put("jobName", jobName(r.getJob()));
            info.put("fightPower", r.getFightPower());
            switch (type) {
                case "level": info.put("value", r.getLevel()); break;
                case "fight": info.put("value", r.getFightPower()); break;
                case "mine":  info.put("value", r.getMineLevel()); break;
                case "farm":  info.put("value", r.getFarmLevel()); break;
                case "forge": info.put("value", r.getForgeLevel()); break;
                default: info.put("value", r.getFightPower());
            }
            result.add(info);
        }
        return result;
    }

    public Map<String, Object> getMyRank(Long userId, String type) {
        GameRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<GameRole>().eq(GameRole::getUserId, userId));
        if (role == null) return Collections.emptyMap();

        LambdaQueryWrapper<GameRole> wrapper = new LambdaQueryWrapper<GameRole>()
                .eq(GameRole::getJob, role.getJob());
        switch (type) {
            case "level": wrapper.gt(GameRole::getLevel, role.getLevel()); break;
            case "fight": wrapper.gt(GameRole::getFightPower, role.getFightPower()); break;
            case "mine":  wrapper.gt(GameRole::getMineLevel, role.getMineLevel()); break;
            case "farm":  wrapper.gt(GameRole::getFarmLevel, role.getFarmLevel()); break;
            case "forge": wrapper.gt(GameRole::getForgeLevel, role.getForgeLevel()); break;
            default: wrapper.gt(GameRole::getFightPower, role.getFightPower());
        }

        Long count = roleMapper.selectCount(wrapper);
        int higherCount = count != null ? count.intValue() : 0;

        Map<String, Object> info = new HashMap<>();
        info.put("rank", higherCount + 1);
        info.put("name", role.getName());
        info.put("job", role.getJob());
        info.put("jobName", jobName(role.getJob()));
        switch (type) {
            case "level": info.put("value", role.getLevel()); break;
            case "fight": info.put("value", role.getFightPower()); break;
            case "mine":  info.put("value", role.getMineLevel()); break;
            case "farm":  info.put("value", role.getFarmLevel()); break;
            case "forge": info.put("value", role.getForgeLevel()); break;
            default: info.put("value", role.getFightPower());
        }
        return info;
    }

    public Map<String, Integer> getJobCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String job : Arrays.asList("WARRIOR", "MAGE", "ARCHER", "PRIEST", "MINER")) {
            Long count = roleMapper.selectCount(
                    new LambdaQueryWrapper<GameRole>().eq(GameRole::getJob, job));
            counts.put(job, count != null ? count.intValue() : 0);
        }
        return counts;
    }

    private String jobName(String job) {
        switch (job) {
            case "WARRIOR": return "战士";
            case "MAGE":    return "法师";
            case "ARCHER":  return "射手";
            case "PRIEST":  return "牧师";
            case "MINER":   return "矿工";
            default:        return job;
        }
    }
}
