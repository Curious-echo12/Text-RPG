package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        return Result.ok(authService.register(body.get("username"), body.get("password")));
    }
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        return Result.ok(authService.login(body.get("username"), body.get("password")));
    }
}
