package com.textrpg.controller;

import com.textrpg.common.GameConfig;
import com.textrpg.common.Result;
import com.textrpg.entity.UserBag;
import com.textrpg.entity.UserEquip;
import com.textrpg.service.BagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/bag")
@RequiredArgsConstructor
public class BagController {
    private final BagService bagService;

    @GetMapping("/list")
    public Result<?> list(Authentication auth) {
        Long uid = (Long) auth.getPrincipal();

        // 背包物品（附带解析后的名称和装备属性）
        List<UserBag> rawItems = bagService.getBag(uid);
        List<Map<String, Object>> items = new ArrayList<>();
        for (UserBag b : rawItems) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", b.getId());
            item.put("itemKey", b.getItemKey());
            item.put("count", b.getCount());
            item.put("extraJson", b.getExtraJson());
            GameConfig.ItemDef def = GameConfig.ITEMS.get(b.getItemKey());
            item.put("itemName", def != null ? def.name : b.getItemKey());
            item.put("itemDesc", def != null ? def.desc : "");
            item.put("itemType", def != null ? def.type : "");
            items.add(item);
        }

        // 已穿戴装备（附带解析后的名称）
        List<UserEquip> rawEquips = bagService.getEquips(uid);
        List<Map<String, Object>> equips = new ArrayList<>();
        for (UserEquip e : rawEquips) {
            Map<String, Object> eq = new LinkedHashMap<>();
            eq.put("id", e.getId());
            eq.put("itemKey", e.getItemKey());
            eq.put("part", e.getPart());
            eq.put("quality", e.getQuality());
            eq.put("strengthenLevel", e.getStrengthenLevel());
            eq.put("baseAttrJson", e.getBaseAttrJson());
            GameConfig.ItemDef def = GameConfig.ITEMS.get(e.getItemKey());
            eq.put("itemName", def != null ? def.name : e.getItemKey());
            equips.add(eq);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("equips", equips);
        return Result.ok(data);
    }

    /** 使用消耗品，支持指定数量 */
    @PostMapping("/use")
    public Result<?> use(Authentication auth, @RequestBody Map<String, Object> body) {
        Long bagId = Long.parseLong(body.get("bagId").toString());
        int useCount = body.containsKey("useCount") ? Integer.parseInt(body.get("useCount").toString()) : 1;
        return Result.ok(bagService.useItem((Long) auth.getPrincipal(), bagId, useCount));
    }

    @PostMapping("/sell")
    public Result<?> sell(Authentication auth, @RequestBody Map<String, Object> body) {
        return Result.ok(bagService.sellItem((Long) auth.getPrincipal(),
                Long.parseLong(body.get("bagId").toString()),
                Integer.parseInt(body.get("count").toString())));
    }

    @PostMapping("/equip")
    public Result<?> equip(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(bagService.equip((Long) auth.getPrincipal(), body.get("bagId")));
    }

    @PostMapping("/unequip")
    public Result<?> unequip(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(bagService.unequip((Long) auth.getPrincipal(), body.get("equipId")));
    }

    @GetMapping("/sellPrice")
    public Result<?> sellPrice(Authentication auth, @RequestParam String itemKey) {
        Map<String, Object> data = new HashMap<>();
        data.put("price", bagService.getItemSellPrice(itemKey));
        return Result.ok(data);
    }
}
