package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.DungeonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/dungeon") @RequiredArgsConstructor
public class DungeonController {
    private final DungeonService dungeonService;
    @GetMapping("/list")
    public Result<?> list(Authentication auth) { return Result.ok(dungeonService.getDungeons((Long) auth.getPrincipal())); }
    @PostMapping("/enter")
    public Result<?> enter(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(dungeonService.enterDungeon((Long) auth.getPrincipal(), body.get("dungeonKey")));
    }
    @PostMapping("/secret")
    public Result<?> secret(Authentication auth) { return Result.ok(dungeonService.exploreSecret((Long) auth.getPrincipal())); }
}
