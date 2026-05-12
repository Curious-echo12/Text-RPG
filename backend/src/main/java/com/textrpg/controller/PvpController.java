package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.service.PvpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pvp")
@RequiredArgsConstructor
public class PvpController {
    private final PvpService pvpService;

    @PostMapping("/match")
    public Result<?> match(Authentication auth) {
        return Result.ok(pvpService.match((Long) auth.getPrincipal()));
    }

    @PostMapping("/fight")
    public Result<?> fight(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(pvpService.fight((Long) auth.getPrincipal(), body.get("opponentUserId")));
    }

    @GetMapping("/records")
    public Result<?> records(Authentication auth,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(pvpService.getRecords((Long) auth.getPrincipal(), page, size));
    }

    @GetMapping("/rank")
    public Result<?> pvpRank(Authentication auth,
                             @RequestParam(required = false) String job,
                             @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        Map<String, Object> myRank = pvpService.getMyPvpRank(userId);
        String viewJob = (job != null && !job.isEmpty()) ? job : (String) myRank.get("job");
        Map<String, Object> data = new HashMap<>();
        data.put("myRank", myRank);
        data.put("list", pvpService.getPvpRankByJob(viewJob, size));
        return Result.ok(data);
    }
}
