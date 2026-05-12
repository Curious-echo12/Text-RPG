package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.service.ForgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/forge")
@RequiredArgsConstructor
public class ForgeController {
    private final ForgeService forgeService;

    @GetMapping("/recipes")
    public Result<?> recipes(Authentication auth) {
        return Result.ok(forgeService.getRecipes((Long) auth.getPrincipal()));
    }

    @PostMapping("/do")
    public Result<?> doForge(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(forgeService.doForge((Long) auth.getPrincipal(), body.get("recipeKey")));
    }

    /** 出售锻造装备，返还50%矿石 */
    @PostMapping("/sell")
    public Result<?> sellEquipment(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(forgeService.sellForgedEquipment((Long) auth.getPrincipal(), body.get("bagId")));
    }

    @GetMapping("/strengthenInfo")
    public Result<?> strengthenInfo(Authentication auth, @RequestParam Long equipId) {
        return Result.ok(forgeService.getStrengthenInfo((Long) auth.getPrincipal(), equipId));
    }

    @PostMapping("/strengthen")
    public Result<?> strengthen(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(forgeService.strengthen((Long) auth.getPrincipal(), body.get("equipId")));
    }
}
