package com.textrpg.service;

import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.GameRole;
import com.textrpg.mapper.GameRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;
    private final BagService bagService;

    public List<Map<String, Object>> getShopItems(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameConfig.ShopItem si : GameConfig.SHOP_ITEMS) {
            Map<String, Object> info = new HashMap<>();
            GameConfig.ItemDef item = GameConfig.ITEMS.get(si.itemKey);
            info.put("itemKey", si.itemKey);
            info.put("itemName", item != null ? item.name : si.itemKey);
            info.put("desc", item != null ? item.desc : "");
            info.put("price", si.price);
            info.put("currency", si.currency);
            info.put("stock", si.stock);
            result.add(info);
        }
        return result;
    }

    public Map<String, Object> buy(Long userId, String itemKey, int count) {
        GameConfig.ShopItem shopItem = GameConfig.SHOP_ITEMS.stream()
                .filter(s -> s.itemKey.equals(itemKey)).findFirst()
                .orElseThrow(() -> new BusinessException("商品不存在"));
        GameRole role = roleService.getByUserId(userId);
        long totalPrice = (long) shopItem.price * count;
        if ("GOLD".equals(shopItem.currency)) {
            if (role.getGold() < totalPrice) throw new BusinessException("金币不足");
            role.setGold(role.getGold() - totalPrice);
        } else {
            if (role.getDiamond() < totalPrice) throw new BusinessException("钻石不足");
            role.setDiamond((int)(role.getDiamond() - totalPrice));
        }
        roleMapper.updateById(role);
        bagService.addItem(userId, itemKey, count);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "购买成功！");
        result.put("role", role);
        return result;
    }

    public Map<String, Object> exchangeDiamond(Long userId, int diamondAmount) {
        if (diamondAmount <= 0) throw new BusinessException("兑换数量必须大于0");
        GameRole role = roleService.getByUserId(userId);
        if (role.getDiamond() < diamondAmount)
            throw new BusinessException("钻石不足，当前拥有" + role.getDiamond() + "个");
        long goldGain = (long) diamondAmount * GameConfig.getDiamondToGoldRate();
        role.setDiamond(role.getDiamond() - diamondAmount);
        role.setGold(role.getGold() + goldGain);
        roleMapper.updateById(role);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "兑换成功！" + diamondAmount + "钻石 → " + goldGain + "金币");
        result.put("goldGain", goldGain);
        result.put("role", role);
        return result;
    }
}
