package com.textrpg.controller;

import com.textrpg.common.GameConfig;
import com.textrpg.common.Result;
import com.textrpg.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @GetMapping("/list")
    public Result<?> list(Authentication auth) {
        return Result.ok(shopService.getShopItems((Long) auth.getPrincipal()));
    }

    @PostMapping("/buy")
    public Result<?> buy(Authentication auth, @RequestBody Map<String, Object> body) {
        return Result.ok(shopService.buy((Long) auth.getPrincipal(),
                (String) body.get("itemKey"), (Integer) body.get("count")));
    }

    @PostMapping("/exchange")
    public Result<?> exchange(Authentication auth, @RequestBody Map<String, Object> body) {
        return Result.ok(shopService.exchangeDiamond((Long) auth.getPrincipal(),
                (Integer) body.get("amount")));
    }

    @GetMapping("/exchangeRate")
    public Result<?> exchangeRate() {
        Map<String, Object> data = new HashMap<>();
        data.put("rate", GameConfig.getDiamondToGoldRate());
        return Result.ok(data);
    }
}
