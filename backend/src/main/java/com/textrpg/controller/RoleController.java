package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.entity.GameRole;
import com.textrpg.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping("/info")
    public Result<?> info(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        GameRole role = roleService.getByUserId(userId);
        if (role == null) return Result.ok(null);
        roleService.recoverEnergy(role);
        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        data.put("recover", roleService.getRecoverInfo(role));
        return Result.ok(data);
    }

    @GetMapping("/detail")
    public Result<?> detail(Authentication auth) {
        return Result.ok(roleService.getRoleDetail((Long) auth.getPrincipal()));
    }

    @PostMapping("/create")
    public Result<?> create(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(roleService.createRole((Long) auth.getPrincipal(), body.get("name"), body.get("job")));
    }

    @PostMapping("/reborn")
    public Result<?> reborn(Authentication auth) {
        return Result.ok(roleService.reborn((Long) auth.getPrincipal()));
    }
}
