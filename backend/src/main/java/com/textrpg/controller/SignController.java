package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/sign") @RequiredArgsConstructor
public class SignController {
    private final SignService signService;
    @GetMapping("/info")
    public Result<?> info(Authentication auth) { return Result.ok(signService.getSignInfo((Long) auth.getPrincipal())); }
    @PostMapping("/do")
    public Result<?> doSign(Authentication auth) { return Result.ok(signService.sign((Long) auth.getPrincipal())); }
}
