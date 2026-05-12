package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MailService {
    private final MailMapper mailMapper;
    private final RoleService roleService;
    private final GameRoleMapper roleMapper;
    private final BagService bagService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Map<String, Object>> getMails(Long userId) {
        List<Mail> mails = mailMapper.selectList(new LambdaQueryWrapper<Mail>()
            .eq(Mail::getUserId, userId).orderByDesc(Mail::getCreateTime).last("LIMIT 50"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Mail m : mails) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", m.getId());
            info.put("title", m.getTitle());
            info.put("content", m.getContent());
            info.put("isRead", m.getIsRead());
            info.put("isClaimed", m.getIsClaimed());
            info.put("createTime", m.getCreateTime() != null ? m.getCreateTime().format(FMT) : "");
            info.put("items", parseMailItems(m.getItemJson()));
            info.put("hasAttachment", m.getItemJson() != null && !m.getItemJson().isEmpty());
            result.add(info);
        }
        return result;
    }

    public boolean hasUnreadMail(Long userId) {
        Long count = mailMapper.selectCount(new LambdaQueryWrapper<Mail>()
            .eq(Mail::getUserId, userId).eq(Mail::getIsRead, 0));
        return count != null && count > 0;
    }

    /** 解析邮件附件为可读信息（兼容新旧格式） */
    private List<Map<String, Object>> parseMailItems(String itemJson) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (itemJson == null || itemJson.isEmpty()) return items;
        try {
            // 新格式：{"items":{...}, "equipment":[...]}
            if (itemJson.trim().startsWith("{")) {
                JSONObject obj = JSON.parseObject(itemJson);
                // 普通物品
                if (obj.containsKey("items")) {
                    JSONObject normalItems = obj.getJSONObject("items");
                    for (String key : normalItems.keySet()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        if ("gold".equals(key)) {
                            item.put("key", "gold"); item.put("name", "金币");
                            item.put("count", normalItems.getIntValue(key)); item.put("isEquip", false);
                        } else if ("diamond".equals(key)) {
                            item.put("key", "diamond"); item.put("name", "钻石");
                            item.put("count", normalItems.getIntValue(key)); item.put("isEquip", false);
                        } else {
                            GameConfig.ItemDef def = GameConfig.ITEMS.get(key);
                            item.put("key", key); item.put("name", def != null ? def.name : key);
                            item.put("count", normalItems.getIntValue(key)); item.put("isEquip", false);
                        }
                        items.add(item);
                    }
                }
                // 装备数组（新格式）
                if (obj.containsKey("equipment")) {
                    JSONArray eqArr = obj.getJSONArray("equipment");
                    for (int i = 0; i < eqArr.size(); i++) {
                        JSONObject eq = eqArr.getJSONObject(i);
                        String itemKey = eq.getString("itemKey");
                        String extraJson = eq.getString("extraJson");
                        GameConfig.ItemDef def = GameConfig.ITEMS.get(itemKey);
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("key", itemKey);
                        item.put("name", def != null ? def.name : itemKey);
                        item.put("isEquip", true);
                        item.put("count", 1);
                        parseEquipExtra(item, extraJson);
                        items.add(item);
                    }
                }
                // 兼容旧格式 _equip 后缀
                for (String key : obj.keySet()) {
                    if (key.endsWith("_equip") && !key.equals("equipment")) {
                        String itemKey = key.substring(0, key.length() - 5);
                        String extraJson = obj.getString(key);
                        GameConfig.ItemDef def = GameConfig.ITEMS.get(itemKey);
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("key", itemKey);
                        item.put("name", def != null ? def.name : itemKey);
                        item.put("isEquip", true);
                        item.put("count", 1);
                        parseEquipExtra(item, extraJson);
                        items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败返回空
        }
        return items;
    }

    private void parseEquipExtra(Map<String, Object> item, String extraJson) {
        try {
            JSONObject extra = JSON.parseObject(extraJson);
            String quality = extra.getString("quality");
            item.put("quality", quality);
            item.put("qualityName", qualityName(quality));
            String baseAttrJson = extra.getString("baseAttr");
            if (baseAttrJson != null) {
                JSONObject attr = JSON.parseObject(baseAttrJson);
                item.put("atk", attr.getIntValue("atk"));
                item.put("def", attr.getIntValue("def"));
                item.put("hp", attr.getIntValue("hp"));
            }
        } catch (Exception ignored) {}
    }

    private String qualityName(String q) {
        if (q == null) return "";
        switch (q) {
            case "WHITE": return "白色普通";
            case "GREEN": return "绿色优秀";
            case "BLUE": return "蓝色稀有";
            case "PURPLE": return "紫色史诗";
            case "ORANGE": return "橙色传说";
            case "RED": return "红色神话";
            default: return q;
        }
    }

    public Map<String, Object> readMail(Long userId, Long mailId) {
        Mail mail = mailMapper.selectById(mailId);
        if (mail == null || !mail.getUserId().equals(userId)) throw new BusinessException("邮件不存在");
        mail.setIsRead(1); mailMapper.updateById(mail);
        Map<String, Object> result = new HashMap<>();
        result.put("mail", mail);
        return result;
    }

    public Map<String, Object> claimMail(Long userId, Long mailId) {
        Mail mail = mailMapper.selectById(mailId);
        if (mail == null || !mail.getUserId().equals(userId)) throw new BusinessException("邮件不存在");
        if (mail.getIsClaimed() != null && mail.getIsClaimed() == 1) throw new BusinessException("已领取");
        if (mail.getItemJson() == null || mail.getItemJson().isEmpty()) throw new BusinessException("没有附件");

        mail.setIsClaimed(1); mail.setIsRead(1); mailMapper.updateById(mail);
        GameRole role = roleService.getByUserId(userId);
        List<String> claimedList = new ArrayList<>();

        JSONObject payload = JSON.parseObject(mail.getItemJson());

        // 处理普通物品（新格式 items 对象，或旧格式直接是 key-value）
        JSONObject normalItems = null;
        if (payload.containsKey("items")) {
            normalItems = payload.getJSONObject("items");
        } else {
            // 旧格式：顶层就是 key-value（排除 _equip 后缀的）
            normalItems = new JSONObject();
            for (String key : payload.keySet()) {
                if (!key.endsWith("_equip") && !key.equals("equipment")) {
                    normalItems.put(key, payload.get(key));
                }
            }
        }
        if (normalItems != null) {
            for (String key : normalItems.keySet()) {
                if ("gold".equals(key)) {
                    int count = normalItems.getIntValue(key);
                    role.setGold(role.getGold() + count);
                    claimedList.add("金币 x" + count);
                } else if ("diamond".equals(key)) {
                    int count = normalItems.getIntValue(key);
                    role.setDiamond(role.getDiamond() + count);
                    claimedList.add("钻石 x" + count);
                } else {
                    int count = normalItems.getIntValue(key);
                    bagService.addItem(userId, key, count);
                    GameConfig.ItemDef def = GameConfig.ITEMS.get(key);
                    claimedList.add((def != null ? def.name : key) + " x" + count);
                }
            }
        }

        // 处理装备（新格式 equipment 数组）
        if (payload.containsKey("equipment")) {
            JSONArray eqArr = payload.getJSONArray("equipment");
            for (int i = 0; i < eqArr.size(); i++) {
                JSONObject eq = eqArr.getJSONObject(i);
                String itemKey = eq.getString("itemKey");
                String extraJson = eq.getString("extraJson");
                GameConfig.ItemDef def = GameConfig.ITEMS.get(itemKey);
                bagService.addEquipmentItem(userId, itemKey, extraJson);
                String quality = "";
                try { quality = qualityName(JSON.parseObject(extraJson).getString("quality")); } catch (Exception ignored) {}
                claimedList.add((def != null ? def.name : itemKey) + "（" + quality + "装备）");
            }
        }

        // 兼容旧格式 _equip
        for (String key : payload.keySet()) {
            if (key.endsWith("_equip")) {
                String itemKey = key.substring(0, key.length() - 5);
                String extraJson = payload.getString(key);
                GameConfig.ItemDef def = GameConfig.ITEMS.get(itemKey);
                bagService.addEquipmentItem(userId, itemKey, extraJson);
                String quality = "";
                try { quality = qualityName(JSON.parseObject(extraJson).getString("quality")); } catch (Exception ignored) {}
                claimedList.add((def != null ? def.name : itemKey) + "（" + quality + "装备）");
            }
        }

        roleMapper.updateById(role);

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "领取成功！获得：" + String.join("、", claimedList));
        result.put("claimedItems", claimedList);
        result.put("role", role);
        return result;
    }

    public Map<String, Object> deleteMail(Long userId, Long mailId) {
        Mail mail = mailMapper.selectById(mailId);
        if (mail == null || !mail.getUserId().equals(userId)) throw new BusinessException("邮件不存在");
        mailMapper.deleteById(mailId);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "邮件已删除");
        return result;
    }
}
