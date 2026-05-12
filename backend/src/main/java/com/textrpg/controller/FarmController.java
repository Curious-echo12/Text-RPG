package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/farm") @RequiredArgsConstructor
public class FarmController {
    private final FarmService farmService;

    @GetMapping("/plots")
    public Result<?> plots(Authentication auth) { return Result.ok(farmService.getPlots((Long) auth.getPrincipal())); }

    /** 获取可种植作物列表和菜地信息 */
    @GetMapping("/crops")
    public Result<?> crops(Authentication auth) { return Result.ok(farmService.getCropList((Long) auth.getPrincipal())); }

    /** 种植（无需种子，直接选择作物） */
    @PostMapping("/plant")
    public Result<?> plant(Authentication auth, @RequestBody Map<String, Object> body) {
        return Result.ok(farmService.plant((Long) auth.getPrincipal(),
                (Integer) body.get("plotIndex"), (String) body.get("cropKey")));
    }

    @PostMapping("/harvest")
    public Result<?> harvest(Authentication auth, @RequestBody Map<String, Integer> body) {
        return Result.ok(farmService.harvest((Long) auth.getPrincipal(), body.get("plotIndex")));
    }
}
