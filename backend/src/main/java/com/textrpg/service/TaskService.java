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
public class TaskService {
    private final UserTaskMapper taskMapper;
    private final UserAchievementMapper achieveMapper;
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;

    public List<Map<String, Object>> getDailyTasks(Long userId) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameConfig.TaskDef td : GameConfig.DAILY_TASKS) {
            UserTask ut = getOrCreateTask(userId, td.key, td.target, today);
            Map<String, Object> info = new HashMap<>();
            info.put("key", td.key); info.put("name", td.name); info.put("desc", td.desc);
            info.put("progress", ut.getProgress() != null ? ut.getProgress() : 0);
            info.put("target", ut.getTarget());
            info.put("isComplete", ut.getIsComplete() != null && ut.getIsComplete() == 1);
            info.put("isClaimed", ut.getIsClaimed() != null && ut.getIsClaimed() == 1);
            info.put("rewardType", td.rewardType); info.put("rewardAmount", td.rewardAmount);
            result.add(info);
        }
        return result;
    }

    /** 获取或创建任务记录 */
    private UserTask getOrCreateTask(Long userId, String taskKey, int target, LocalDate date) {
        UserTask ut = taskMapper.selectOne(new LambdaQueryWrapper<UserTask>()
            .eq(UserTask::getUserId, userId).eq(UserTask::getTaskKey, taskKey).eq(UserTask::getTaskDate, date));
        if (ut == null) {
            ut = new UserTask(); ut.setUserId(userId); ut.setTaskKey(taskKey);
            ut.setTarget(target); ut.setProgress(0); ut.setIsComplete(0); ut.setIsClaimed(0);
            ut.setTaskDate(date); taskMapper.insert(ut);
        }
        return ut;
    }

    public void addProgress(Long userId, String taskKey, int amount) {
        LocalDate today = LocalDate.now();
        GameConfig.TaskDef td = GameConfig.DAILY_TASKS.stream().filter(t -> t.key.equals(taskKey)).findFirst().orElse(null);
        if (td == null) return;
        UserTask ut = getOrCreateTask(userId, taskKey, td.target, today);
        if (ut.getIsComplete() != null && ut.getIsComplete() == 1) return;
        int newProgress = Math.min((ut.getProgress() != null ? ut.getProgress() : 0) + amount, ut.getTarget());
        ut.setProgress(newProgress);
        if (newProgress >= ut.getTarget()) ut.setIsComplete(1);
        taskMapper.updateById(ut);
    }

    public Map<String, Object> claimTask(Long userId, String taskKey) {
        LocalDate today = LocalDate.now();
        UserTask ut = taskMapper.selectOne(new LambdaQueryWrapper<UserTask>()
            .eq(UserTask::getUserId, userId).eq(UserTask::getTaskKey, taskKey).eq(UserTask::getTaskDate, today));
        if (ut == null) throw new BusinessException("任务不存在");
        if (ut.getIsComplete() == null || ut.getIsComplete() != 1) throw new BusinessException("任务未完成");
        if (ut.getIsClaimed() != null && ut.getIsClaimed() == 1) throw new BusinessException("已领取");
        ut.setIsClaimed(1); taskMapper.updateById(ut);
        GameConfig.TaskDef td = GameConfig.DAILY_TASKS.stream().filter(t -> t.key.equals(taskKey)).findFirst().orElse(null);
        GameRole role = roleService.getByUserId(userId);
        if (td != null && "gold".equals(td.rewardType)) role.setGold(role.getGold() + td.rewardAmount);
        else if (td != null) role.setDiamond(role.getDiamond() + td.rewardAmount);
        roleMapper.updateById(role);
        Map<String, Object> result = new HashMap<>();
        String rewardMsg = td != null ? ("领取成功！获得" + td.rewardAmount + ("gold".equals(td.rewardType) ? "金币" : "钻石")) : "领取成功！";
        result.put("msg", rewardMsg);
        result.put("role", role);
        return result;
    }

    public List<Map<String, Object>> getAchievements(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameConfig.AchieveDef ad : GameConfig.ACHIEVEMENTS) {
            UserAchievement ua = achieveMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId).eq(UserAchievement::getAchieveKey, ad.key));
            Map<String, Object> info = new HashMap<>();
            info.put("key", ad.key); info.put("name", ad.name); info.put("desc", ad.desc);
            info.put("progress", ua != null && ua.getProgress() != null ? ua.getProgress() : 0); info.put("target", ad.target);
            info.put("isComplete", ua != null && ua.getIsComplete() != null && ua.getIsComplete() == 1);
            info.put("isClaimed", ua != null && ua.getIsClaimed() != null && ua.getIsClaimed() == 1);
            info.put("rewardType", ad.rewardType); info.put("rewardAmount", ad.rewardAmount);
            result.add(info);
        }
        return result;
    }

    public Map<String, Object> claimAchievement(Long userId, String achieveKey) {
        UserAchievement ua = achieveMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
            .eq(UserAchievement::getUserId, userId).eq(UserAchievement::getAchieveKey, achieveKey));
        if (ua == null) throw new BusinessException("成就不存在");
        if (ua.getIsComplete() == null || ua.getIsComplete() != 1) throw new BusinessException("成就未完成");
        if (ua.getIsClaimed() != null && ua.getIsClaimed() == 1) throw new BusinessException("已领取");
        ua.setIsClaimed(1); achieveMapper.updateById(ua);
        GameConfig.AchieveDef ad = GameConfig.ACHIEVEMENTS.stream().filter(a -> a.key.equals(achieveKey)).findFirst().orElse(null);
        GameRole role = roleService.getByUserId(userId);
        if (ad != null && "gold".equals(ad.rewardType)) role.setGold(role.getGold() + ad.rewardAmount);
        else if (ad != null) role.setDiamond(role.getDiamond() + ad.rewardAmount);
        roleMapper.updateById(role);
        Map<String, Object> result = new HashMap<>();
        String achieveMsg = ad != null ? ("成就奖励领取成功！获得" + ad.rewardAmount + ("gold".equals(ad.rewardType) ? "金币" : "钻石")) : "成就奖励领取成功！";
        result.put("msg", achieveMsg);
        result.put("role", role);
        return result;
    }
}
