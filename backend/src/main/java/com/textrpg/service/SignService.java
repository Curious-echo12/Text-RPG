package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SignService {
    private final UserSignMapper signMapper;
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;

    public Map<String, Object> getSignInfo(Long userId) {
        LocalDate today = LocalDate.now();
        UserSign todaySign = signMapper.selectOne(new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getUserId, userId).eq(UserSign::getSignDate, today));
        UserSign yesterdaySign = signMapper.selectOne(new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getUserId, userId).eq(UserSign::getSignDate, today.minusDays(1)));
        int yesterdayConsecutive = yesterdaySign != null ? yesterdaySign.getConsecutiveDays() : 0;
        // 如果今天已签到，显示今天的连续天数；否则显示昨天的
        int currentConsecutive = todaySign != null ? todaySign.getConsecutiveDays() : yesterdayConsecutive;
        int nextDay = yesterdayConsecutive + 1;

        Map<String, Object> info = new HashMap<>();
        info.put("signedToday", todaySign != null);
        info.put("consecutiveDays", currentConsecutive);
        info.put("nextDay", nextDay);

        int[] range = GameConfig.getSignRewardRange(nextDay);
        boolean isDiamond = GameConfig.isSignDiamond(nextDay);
        info.put("rewardMin", range[0]);
        info.put("rewardMax", range[1]);
        info.put("isDiamond", isDiamond);
        return info;
    }

    public Map<String, Object> sign(Long userId) {
        LocalDate today = LocalDate.now();
        if (signMapper.selectOne(new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getUserId, userId).eq(UserSign::getSignDate, today)) != null)
            throw new BusinessException("今日已签到");

        UserSign yesterdaySign = signMapper.selectOne(new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getUserId, userId).eq(UserSign::getSignDate, today.minusDays(1)));
        int consecutive = yesterdaySign != null ? yesterdaySign.getConsecutiveDays() + 1 : 1;

        UserSign sign = new UserSign();
        sign.setUserId(userId);
        sign.setSignDate(today);
        sign.setConsecutiveDays(consecutive);
        signMapper.insert(sign);

        boolean isDiamond = GameConfig.isSignDiamond(consecutive);
        int reward = GameConfig.randomSignReward(consecutive);
        int diamondBonus = GameConfig.getSetting("signDiamondBonusMin", 5) +
                new Random().nextInt(GameConfig.getSetting("signDiamondBonusMax", 10) - GameConfig.getSetting("signDiamondBonusMin", 5) + 1);

        GameRole role = roleService.getByUserId(userId);
        if (isDiamond) {
            role.setDiamond(role.getDiamond() + reward);
        } else {
            role.setGold(role.getGold() + reward);
        }
        // 每日签到额外赠送钻石
        role.setDiamond(role.getDiamond() + diamondBonus);
        roleMapper.updateById(role);

        Map<String, Object> result = new HashMap<>();
        if (isDiamond) {
            result.put("msg", "签到成功！连续" + consecutive + "天，获得" + reward + "钻石 + " + diamondBonus + "钻石");
        } else {
            result.put("msg", "签到成功！连续" + consecutive + "天，获得" + reward + "金币 + " + diamondBonus + "钻石");
        }
        result.put("reward", reward);
        result.put("diamondBonus", diamondBonus);
        result.put("isDiamond", isDiamond);
        result.put("role", role);
        return result;
    }
}
