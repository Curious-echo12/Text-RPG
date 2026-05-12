package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.config.security.JwtUtil;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminUserMapper adminMapper;
    private final SysUserMapper userMapper;
    private final GameRoleMapper roleMapper;
    private final MailMapper mailMapper;
    private final UserEquipMapper equipMapper;
    private final UserBagMapper bagMapper;
    private final UserSkillMapper skillMapper;
    private final FarmPlotMapper farmPlotMapper;
    private final PvpRecordMapper pvpRecordMapper;
    private final UserSignMapper signMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_PASSWORD = "qaz123";

    /** 管理员登录 */
    public Map<String, Object> login(String username, String password) {
        AdminUser admin = adminMapper.selectOne(
            new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username));
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException("管理员账号或密码错误");
        }
        String token = jwtUtil.generateToken(admin.getId(), "ADMIN:" + admin.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", admin.getUsername());
        return result;
    }

    /** 获取所有玩家列表（支持筛选） */
    public Map<String, Object> getPlayers(int page, int size, String keyword,
                                           String job, Integer minLevel, Integer maxLevel) {
        // 先查满足角色条件的userId
        List<Long> roleFilterIds = null;
        if (job != null || minLevel != null || maxLevel != null) {
            LambdaQueryWrapper<GameRole> rw = new LambdaQueryWrapper<>();
            if (job != null && !job.isEmpty()) rw.eq(GameRole::getJob, job);
            if (minLevel != null) rw.ge(GameRole::getLevel, minLevel);
            if (maxLevel != null) rw.le(GameRole::getLevel, maxLevel);
            List<GameRole> filteredRoles = roleMapper.selectList(rw);
            roleFilterIds = filteredRoles.stream().map(GameRole::getUserId).collect(Collectors.toList());
            if (roleFilterIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("list", Collections.emptyList()); result.put("total", 0);
                result.put("page", page); result.put("size", size);
                return result;
            }
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(SysUser::getUsername, keyword)
                .or().like(SysUser::getPlayerId, keyword)
                .or().like(SysUser::getEmail, keyword));
        }
        if (roleFilterIds != null) wrapper.in(SysUser::getId, roleFilterIds);
        wrapper.orderByDesc(SysUser::getCreateTime);

        Long total = userMapper.selectCount(wrapper);
        List<SysUser> users = userMapper.selectList(wrapper.last("LIMIT " + size + " OFFSET " + (page * size)));

        List<Map<String, Object>> list = new ArrayList<>();
        for (SysUser u : users) {
            Map<String, Object> info = buildPlayerInfo(u);
            list.add(info);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    private Map<String, Object> buildPlayerInfo(SysUser u) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", u.getId());
        info.put("playerId", u.getPlayerId());
        info.put("username", u.getUsername());
        info.put("email", u.getEmail());
        info.put("emailVerified", u.getEmailVerified());
        info.put("createTime", u.getCreateTime() != null ? u.getCreateTime().format(FMT) : "");

        GameRole role = roleMapper.selectOne(
            new LambdaQueryWrapper<GameRole>().eq(GameRole::getUserId, u.getId()));
        if (role != null) {
            info.put("roleName", role.getName());
            info.put("roleJob", role.getJob());
            info.put("roleJobName", jobName(role.getJob()));
            info.put("roleLevel", role.getLevel());
            info.put("fightPower", role.getFightPower());
            info.put("gold", role.getGold());
            info.put("diamond", role.getDiamond());
            info.put("energy", role.getEnergy());
            info.put("maxEnergy", role.getMaxEnergy());
            info.put("spirit", role.getSpirit());
            info.put("maxSpirit", role.getMaxSpirit());
            info.put("hp", role.getHp());
            info.put("maxHp", role.getMaxHp());
            info.put("attack", role.getAttack());
            info.put("defense", role.getDefense());
            info.put("speed", role.getSpeed());
            info.put("critRate", role.getCritRate());
            info.put("critDmg", role.getCritDmg());
            info.put("exp", role.getExp());
            info.put("skillPoint", role.getSkillPoint());
            info.put("pvpScore", role.getPvpScore());
            info.put("pvpStar", role.getPvpStar());
            info.put("rebornCount", role.getRebornCount());
            info.put("mineLevel", role.getMineLevel());
            info.put("mineExp", role.getMineExp());
            info.put("farmLevel", role.getFarmLevel());
            info.put("farmExp", role.getFarmExp());
            info.put("forgeLevel", role.getForgeLevel());
            info.put("forgeExp", role.getForgeExp());
        }
        return info;
    }

    /** 修改玩家角色信息 */
    public void modifyPlayer(Long userId, Map<String, Object> params) {
        GameRole role = roleMapper.selectOne(
            new LambdaQueryWrapper<GameRole>().eq(GameRole::getUserId, userId));
        if (role == null) throw new BusinessException("该玩家没有角色");

        if (params.containsKey("gold")) role.setGold(Long.parseLong(params.get("gold").toString()));
        if (params.containsKey("diamond")) role.setDiamond(Integer.parseInt(params.get("diamond").toString()));
        if (params.containsKey("level")) role.setLevel(Integer.parseInt(params.get("level").toString()));
        if (params.containsKey("exp")) role.setExp(Long.parseLong(params.get("exp").toString()));
        if (params.containsKey("energy")) role.setEnergy(Integer.parseInt(params.get("energy").toString()));
        if (params.containsKey("spirit")) role.setSpirit(Integer.parseInt(params.get("spirit").toString()));
        if (params.containsKey("skillPoint")) role.setSkillPoint(Integer.parseInt(params.get("skillPoint").toString()));
        if (params.containsKey("pvpScore")) role.setPvpScore(Integer.parseInt(params.get("pvpScore").toString()));
        if (params.containsKey("hp")) role.setHp(Integer.parseInt(params.get("hp").toString()));
        if (params.containsKey("maxHp")) role.setMaxHp(Integer.parseInt(params.get("maxHp").toString()));
        if (params.containsKey("attack")) role.setAttack(Integer.parseInt(params.get("attack").toString()));
        if (params.containsKey("defense")) role.setDefense(Integer.parseInt(params.get("defense").toString()));
        if (params.containsKey("speed")) role.setSpeed(Integer.parseInt(params.get("speed").toString()));
        if (params.containsKey("mineLevel")) role.setMineLevel(Integer.parseInt(params.get("mineLevel").toString()));
        if (params.containsKey("farmLevel")) role.setFarmLevel(Integer.parseInt(params.get("farmLevel").toString()));
        if (params.containsKey("forgeLevel")) role.setForgeLevel(Integer.parseInt(params.get("forgeLevel").toString()));
        if (params.containsKey("rebornCount")) role.setRebornCount(Integer.parseInt(params.get("rebornCount").toString()));

        roleMapper.updateById(role);
    }

    /** 重置玩家密码为默认密码并发送邮件通知 */
    public Map<String, Object> resetPlayerPassword(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("玩家不存在");

        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userMapper.updateById(user);

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(mailFrom);
                message.setTo(user.getEmail());
                message.setSubject("【文字江湖】密码重置通知");
                message.setText("尊敬的玩家 " + user.getUsername() + " 您好：\n\n"
                    + "您的密码已被管理员重置为默认密码：" + DEFAULT_PASSWORD + "\n\n"
                    + "请尽快登录游戏并修改密码。\n\n"
                    + "—— 文字江湖");
                mailSender.send(message);
            } catch (Exception e) {}
        }

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "密码已重置为 " + DEFAULT_PASSWORD);
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            result.put("emailSent", true); result.put("email", user.getEmail());
        } else {
            result.put("emailSent", false); result.put("note", "该玩家未绑定邮箱");
        }
        return result;
    }

    /** 发送系统邮件（支持物品+装备+品质，支持条件筛选） */
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendMail(Map<String, Object> params) {
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        if (title == null || title.trim().isEmpty()) throw new BusinessException("邮件标题不能为空");
        if (content == null || content.trim().isEmpty()) throw new BusinessException("邮件内容不能为空");

        // 构建附件JSON（普通物品）
        Map<String, Object> items = (Map<String, Object>) params.get("items");

        // 装备附件
        String equipItemKey = (String) params.get("equipItem");
        String equipQuality = (String) params.getOrDefault("equipQuality", "WHITE");

        // 合并为最终itemJson
        Map<String, Object> finalItems = new LinkedHashMap<>();
        if (items != null) finalItems.putAll(items);

        // 如果有装备，计算属性并加入 equipment 数组
        List<Map<String, Object>> equipmentList = new ArrayList<>();
        if (equipItemKey != null && !equipItemKey.isEmpty()) {
            GameConfig.ItemDef itemDef = GameConfig.ITEMS.get(equipItemKey);
            if (itemDef != null && "EQUIPMENT".equals(itemDef.type)) {
                String part = itemDef.subType;
                double mult = GameConfig.QUALITY_MULT.getOrDefault(equipQuality, 1.0);
                int[] baseAttr = GameConfig.QUALITY_ATTR_BASE.getOrDefault(part.toUpperCase(), new int[]{5, 3, 15});
                JSONObject attr = new JSONObject();
                attr.put("atk", (int)(baseAttr[0] * mult));
                attr.put("def", (int)(baseAttr[1] * mult));
                attr.put("hp", (int)(baseAttr[2] * mult));
                JSONObject extra = new JSONObject();
                extra.put("part", part.toUpperCase());
                extra.put("quality", equipQuality);
                extra.put("strengthenLevel", 0);
                extra.put("baseAttr", attr.toJSONString());
                Map<String, Object> equipEntry = new LinkedHashMap<>();
                equipEntry.put("itemKey", equipItemKey);
                equipEntry.put("extraJson", extra.toJSONString());
                equipmentList.add(equipEntry);
            }
        }

        // 构建最终 JSON：普通物品 + 装备数组
        Map<String, Object> mailPayload = new LinkedHashMap<>();
        if (!finalItems.isEmpty()) mailPayload.put("items", finalItems);
        if (!equipmentList.isEmpty()) mailPayload.put("equipment", equipmentList);
        String itemJson = mailPayload.isEmpty() ? null : JSON.toJSONString(mailPayload);

        // 确定发送对象
        boolean sendToAll = Boolean.TRUE.equals(params.get("sendToAll"));
        List<Long> targetUserIds = new ArrayList<>();
        if (params.get("userIds") instanceof List) {
            for (Object id : (List<?>) params.get("userIds")) {
                targetUserIds.add(Long.parseLong(id.toString()));
            }
        }

        // 条件筛选
        if (!sendToAll && targetUserIds.isEmpty()) {
            String filterJob = (String) params.get("filterJob");
            Integer filterMinLevel = params.containsKey("filterMinLevel") ?
                Integer.parseInt(params.get("filterMinLevel").toString()) : null;
            Integer filterMaxLevel = params.containsKey("filterMaxLevel") ?
                Integer.parseInt(params.get("filterMaxLevel").toString()) : null;

            if (filterJob != null || filterMinLevel != null || filterMaxLevel != null) {
                LambdaQueryWrapper<GameRole> rw = new LambdaQueryWrapper<>();
                if (filterJob != null && !filterJob.isEmpty()) rw.eq(GameRole::getJob, filterJob);
                if (filterMinLevel != null) rw.ge(GameRole::getLevel, filterMinLevel);
                if (filterMaxLevel != null) rw.le(GameRole::getLevel, filterMaxLevel);
                List<GameRole> filtered = roleMapper.selectList(rw);
                targetUserIds = filtered.stream().map(GameRole::getUserId).collect(Collectors.toList());
            }
        }

        if (sendToAll) {
            List<SysUser> allUsers = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().select(SysUser::getId));
            targetUserIds = allUsers.stream().map(SysUser::getId).collect(Collectors.toList());
        }

        if (targetUserIds.isEmpty()) throw new BusinessException("请选择发送对象");

        int count = 0;
        for (Long uid : targetUserIds) {
            Mail mail = new Mail();
            mail.setUserId(uid);
            mail.setTitle(title);
            mail.setContent(content);
            mail.setItemJson(itemJson);
            mail.setCreateTime(LocalDateTime.now());
            mailMapper.insert(mail);
            count++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "邮件发送成功，共发送 " + count + " 封");
        result.put("count", count);
        return result;
    }

    /** 获取所有玩家选项 */
    public List<Map<String, Object>> getAllPlayerOptions() {
        List<SysUser> users = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().select(SysUser::getId, SysUser::getPlayerId, SysUser::getUsername));
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser u : users) {
            GameRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<GameRole>().eq(GameRole::getUserId, u.getId()));
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("userId", u.getId());
            info.put("playerId", u.getPlayerId());
            info.put("username", u.getUsername());
            if (role != null) {
                info.put("roleName", role.getName());
                info.put("level", role.getLevel());
                info.put("job", role.getJob());
            }
            result.add(info);
        }
        return result;
    }

    /** 获取玩家完整详情（所有信息） */
    public Map<String, Object> getPlayerDetail(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("玩家不存在");

        GameRole role = roleMapper.selectOne(
            new LambdaQueryWrapper<GameRole>().eq(GameRole::getUserId, userId));

        Map<String, Object> result = new LinkedHashMap<>();
        // 账号信息
        result.put("id", user.getId());
        result.put("playerId", user.getPlayerId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("emailVerified", user.getEmailVerified());
        result.put("createTime", user.getCreateTime() != null ? user.getCreateTime().format(FMT) : "");

        if (role == null) return result;

        // 角色全部属性
        result.put("roleName", role.getName());
        result.put("roleJob", role.getJob());
        result.put("roleJobName", jobName(role.getJob()));
        result.put("roleLevel", role.getLevel());
        result.put("exp", role.getExp());
        result.put("hp", role.getHp());
        result.put("maxHp", role.getMaxHp());
        result.put("attack", role.getAttack());
        result.put("defense", role.getDefense());
        result.put("speed", role.getSpeed());
        result.put("critRate", role.getCritRate());
        result.put("critDmg", role.getCritDmg());
        result.put("fightPower", role.getFightPower());
        result.put("rebornCount", role.getRebornCount());
        result.put("gold", role.getGold());
        result.put("diamond", role.getDiamond());
        result.put("energy", role.getEnergy());
        result.put("maxEnergy", role.getMaxEnergy());
        result.put("spirit", role.getSpirit());
        result.put("maxSpirit", role.getMaxSpirit());
        result.put("skillPoint", role.getSkillPoint());
        result.put("pvpScore", role.getPvpScore());
        result.put("pvpStar", role.getPvpStar());
        result.put("mineLevel", role.getMineLevel());
        result.put("mineExp", role.getMineExp());
        result.put("farmLevel", role.getFarmLevel());
        result.put("farmExp", role.getFarmExp());
        result.put("forgeLevel", role.getForgeLevel());
        result.put("forgeExp", role.getForgeExp());
        result.put("lastEnergyTime", role.getLastEnergyTime());
        result.put("lastSpiritTime", role.getLastSpiritTime());

        // 已穿戴装备
        List<UserEquip> equips = equipMapper.selectList(
            new LambdaQueryWrapper<UserEquip>().eq(UserEquip::getUserId, userId));
        List<Map<String, Object>> equipList = new ArrayList<>();
        for (UserEquip e : equips) {
            Map<String, Object> ei = new LinkedHashMap<>();
            GameConfig.ItemDef itemDef = GameConfig.ITEMS.get(e.getItemKey());
            ei.put("name", itemDef != null ? itemDef.name : e.getItemKey());
            ei.put("itemKey", e.getItemKey());
            ei.put("part", e.getPart());
            ei.put("quality", e.getQuality());
            ei.put("strengthenLevel", e.getStrengthenLevel());
            ei.put("baseAttrJson", e.getBaseAttrJson());
            equipList.add(ei);
        }
        result.put("equips", equipList);

        // 背包物品
        List<UserBag> bagItems = bagMapper.selectList(
            new LambdaQueryWrapper<UserBag>().eq(UserBag::getUserId, userId));
        List<Map<String, Object>> bagList = new ArrayList<>();
        for (UserBag b : bagItems) {
            Map<String, Object> bi = new LinkedHashMap<>();
            GameConfig.ItemDef itemDef = GameConfig.ITEMS.get(b.getItemKey());
            bi.put("id", b.getId());
            bi.put("itemKey", b.getItemKey());
            bi.put("name", itemDef != null ? itemDef.name : b.getItemKey());
            bi.put("type", itemDef != null ? itemDef.type : "");
            bi.put("count", b.getCount());
            bi.put("extraJson", b.getExtraJson());
            bagList.add(bi);
        }
        result.put("bagItems", bagList);

        // 已学技能
        List<UserSkill> skills = skillMapper.selectList(
            new LambdaQueryWrapper<UserSkill>().eq(UserSkill::getUserId, userId));
        List<Map<String, Object>> skillList = new ArrayList<>();
        for (UserSkill s : skills) {
            Map<String, Object> si = new LinkedHashMap<>();
            GameConfig.SkillDef sd = GameConfig.SKILLS.stream()
                .filter(sk -> sk.key.equals(s.getSkillKey())).findFirst().orElse(null);
            si.put("skillKey", s.getSkillKey());
            si.put("name", sd != null ? sd.name : s.getSkillKey());
            si.put("level", s.getLevel());
            si.put("type", sd != null ? sd.type : "");
            si.put("desc", sd != null ? sd.desc : "");
            skillList.add(si);
        }
        result.put("skills", skillList);

        // 农场地块
        List<FarmPlot> plots = farmPlotMapper.selectList(
            new LambdaQueryWrapper<FarmPlot>().eq(FarmPlot::getUserId, userId));
        result.put("farmPlots", plots);

        // PVP记录（最近20条）
        List<PvpRecord> pvpRecords = pvpRecordMapper.selectList(
            new LambdaQueryWrapper<PvpRecord>()
                .eq(PvpRecord::getAttackerId, userId)
                .orderByDesc(PvpRecord::getCreateTime).last("LIMIT 20"));
        result.put("pvpRecords", pvpRecords);

        // 签到记录
        UserSign lastSign = signMapper.selectOne(
            new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getUserId, userId)
                .orderByDesc(UserSign::getSignDate).last("LIMIT 1"));
        result.put("lastSignDate", lastSign != null ? lastSign.getSignDate() : null);
        result.put("consecutiveDays", lastSign != null ? lastSign.getConsecutiveDays() : 0);

        return result;
    }

    /** 删除玩家 */
    public void deletePlayer(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("玩家不存在");
        roleMapper.delete(new LambdaQueryWrapper<GameRole>().eq(GameRole::getUserId, userId));
        bagMapper.delete(new LambdaQueryWrapper<UserBag>().eq(UserBag::getUserId, userId));
        equipMapper.delete(new LambdaQueryWrapper<UserEquip>().eq(UserEquip::getUserId, userId));
        skillMapper.delete(new LambdaQueryWrapper<UserSkill>().eq(UserSkill::getUserId, userId));
        farmPlotMapper.delete(new LambdaQueryWrapper<FarmPlot>().eq(FarmPlot::getUserId, userId));
        mailMapper.delete(new LambdaQueryWrapper<Mail>().eq(Mail::getUserId, userId));
        pvpRecordMapper.delete(new LambdaQueryWrapper<PvpRecord>().eq(PvpRecord::getAttackerId, userId));
        userMapper.deleteById(userId);
    }

    private String jobName(String job) {
        switch (job) {
            case "WARRIOR": return "战士";
            case "MAGE": return "法师";
            case "ARCHER": return "射手";
            case "PRIEST": return "牧师";
            case "MINER": return "矿工";
            default: return job;
        }
    }
}
