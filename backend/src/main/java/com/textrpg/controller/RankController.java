package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {
    private final RankService rankService;

    @GetMapping("/list")
    public Result<?> list(Authentication auth,
                          @RequestParam(defaultValue = "fight") String type,
                          @RequestParam String job,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        Map<String, Object> data = new HashMap<>();
        data.put("list", rankService.getRankByJob(type, job, page, size));
        data.put("myRank", rankService.getMyRank(userId, type));
        data.put("jobCounts", rankService.getJobCounts());
        return Result.ok(data);
    }

    @GetMapping("/jobCounts")
    public Result<?> jobCounts() {
        return Result.ok(rankService.getJobCounts());
    }
}
