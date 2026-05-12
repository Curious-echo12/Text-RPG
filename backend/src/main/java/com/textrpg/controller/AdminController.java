package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.service.AdminService;
import com.textrpg.service.GameConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final GameConfigService configService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        return Result.ok(adminService.login(body.get("username"), body.get("password")));
    }

    @GetMapping("/players")
    public Result<?> players(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String job,
                             @RequestParam(required = false) Integer minLevel,
                             @RequestParam(required = false) Integer maxLevel) {
        return Result.ok(adminService.getPlayers(page, size, keyword, job, minLevel, maxLevel));
    }

    @GetMapping("/players/detail")
    public Result<?> playerDetail(@RequestParam Long userId) {
        return Result.ok(adminService.getPlayerDetail(userId));
    }

    @PostMapping("/players/modify")
    public Result<?> modifyPlayer(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(body.get("userId").toString());
        adminService.modifyPlayer(userId, body);
        return Result.ok("修改成功");
    }

    @PostMapping("/players/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(body.get("userId").toString());
        return Result.ok(adminService.resetPlayerPassword(userId));
    }

    @PostMapping("/players/delete")
    public Result<?> deletePlayer(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(body.get("userId").toString());
        adminService.deletePlayer(userId);
        return Result.ok("玩家已删除");
    }

    @PostMapping("/mail/send")
    public Result<?> sendMail(@RequestBody Map<String, Object> body) {
        return Result.ok(adminService.sendMail(body));
    }

    @GetMapping("/players/options")
    public Result<?> playerOptions() {
        return Result.ok(adminService.getAllPlayerOptions());
    }

    // ========== 游戏配置管理 ==========
    @GetMapping("/config/list")
    public Result<?> configList() {
        return Result.ok(configService.getConfigKeys());
    }

    @GetMapping("/config/get")
    public Result<?> configGet(@RequestParam String key) {
        String json = configService.getConfigJson(key);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("key", key);
        result.put("json", json);
        return Result.ok(result);
    }

    @PostMapping("/config/update")
    public Result<?> configUpdate(@RequestBody java.util.Map<String, String> body) {
        configService.updateConfig(body.get("key"), body.get("json"));
        return Result.ok("配置已保存并生效");
    }

    @PostMapping("/config/reload")
    public Result<?> configReload() {
        configService.reload();
        return Result.ok("配置已重新加载");
    }
}
