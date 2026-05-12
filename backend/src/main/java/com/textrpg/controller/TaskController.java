package com.textrpg.controller;
import com.textrpg.common.BusinessException;
import com.textrpg.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/task")
public class TaskController {

    @GetMapping("/daily")
    public Result<?> daily() {
        throw new BusinessException("任务系统维护中，暂不可用");
    }

    @PostMapping("/claim")
    public Result<?> claim() {
        throw new BusinessException("任务系统维护中，暂不可用");
    }

    @GetMapping("/achievement")
    public Result<?> achievement() {
        throw new BusinessException("成就系统维护中，暂不可用");
    }

    @PostMapping("/claimAchievement")
    public Result<?> claimAchievement() {
        throw new BusinessException("成就系统维护中，暂不可用");
    }
}
