package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/skill") @RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;
    @GetMapping("/list")
    public Result<?> list(Authentication auth) { return Result.ok(skillService.getSkills((Long) auth.getPrincipal())); }
    @PostMapping("/learn")
    public Result<?> learn(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(skillService.learnSkill((Long) auth.getPrincipal(), body.get("skillKey")));
    }
    @PostMapping("/upgrade")
    public Result<?> upgrade(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(skillService.upgradeSkill((Long) auth.getPrincipal(), body.get("skillKey")));
    }
}
