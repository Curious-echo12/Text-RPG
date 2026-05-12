package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.PveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/pve") @RequiredArgsConstructor
public class PveController {
    private final PveService pveService;
    @GetMapping("/chapters")
    public Result<?> chapters(Authentication auth) { return Result.ok(pveService.getChapters((Long) auth.getPrincipal())); }
    @PostMapping("/fight")
    public Result<?> fight(Authentication auth, @RequestBody Map<String, Integer> body) {
        return Result.ok(pveService.fight((Long) auth.getPrincipal(), body.get("chapter"), body.get("stage")));
    }
}
