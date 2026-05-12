package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.MineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/mine") @RequiredArgsConstructor
public class MineController {
    private final MineService mineService;
    @GetMapping("/info")
    public Result<?> info(Authentication auth) { return Result.ok(mineService.getMineInfo((Long) auth.getPrincipal())); }
    @PostMapping("/dig")
    public Result<?> dig(Authentication auth, @RequestBody Map<String, Integer> body) {
        return Result.ok(mineService.dig((Long) auth.getPrincipal(), body.get("mineIndex")));
    }
}
