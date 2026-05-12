package com.textrpg.common;

import java.util.*;

public class GameConfig {

    // 动态游戏参数（从数据库加载）
    public static Map<String, Object> gameSettings = new HashMap<>();

    public static int getSetting(String key, int defaultVal) {
        Object v = gameSettings.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return defaultVal;
    }

    public static long levelExp(int level) {
        int a = getSetting("levelExpCoeffA", 120);
        int b = getSetting("levelExpCoeffB", 8);
        return (long) level * a + (long) level * level * b;
    }

    public static Map<String, int[]> JOB_BASE = new HashMap<>();
    static {
        JOB_BASE.put("WARRIOR", new int[]{200, 25, 15, 10});
        JOB_BASE.put("MAGE",    new int[]{150, 35, 8,  12});
        JOB_BASE.put("ARCHER",  new int[]{160, 30, 10, 15});
        JOB_BASE.put("PRIEST",  new int[]{180, 20, 12, 11});
        JOB_BASE.put("MINER",   new int[]{170, 18, 18, 9});
    }

    public static Map<String, int[]> JOB_GROWTH = new HashMap<>();
    static {
        JOB_GROWTH.put("WARRIOR", new int[]{20, 5, 4, 1});
        JOB_GROWTH.put("MAGE",    new int[]{12, 8, 2, 2});
        JOB_GROWTH.put("ARCHER",  new int[]{14, 7, 3, 2});
        JOB_GROWTH.put("PRIEST",  new int[]{16, 4, 3, 1});
        JOB_GROWTH.put("MINER",   new int[]{15, 3, 5, 1});
    }

    public static Map<String, ItemDef> ITEMS = new LinkedHashMap<>();
    static {
        // 矿石
        ITEMS.put("iron_ore",      new ItemDef("iron_ore","铁矿石","MATERIAL","矿石",2,"最基础的矿石，用于锻造白色品质装备和强化+1~+5"));
        ITEMS.put("copper_ore",    new ItemDef("copper_ore","铜矿石","MATERIAL","矿石",5,"常见的矿石，用于锻造绿色品质装备和强化+6~+10"));
        ITEMS.put("silver_ore",    new ItemDef("silver_ore","银矿石","MATERIAL","矿石",12,"稀有矿石，用于锻造蓝色品质装备和强化+11~+15"));
        ITEMS.put("gold_ore",      new ItemDef("gold_ore","金矿石","MATERIAL","矿石",30,"珍贵矿石，用于锻造紫色品质装备和强化+16~+20"));
        ITEMS.put("mithril_ore",   new ItemDef("mithril_ore","秘银矿石","MATERIAL","矿石",80,"稀有矿石，用于锻造橙色品质装备"));
        ITEMS.put("adamantite_ore",new ItemDef("adamantite_ore","精金矿石","MATERIAL","矿石",200,"传说矿石，用于锻造高级装备"));
        ITEMS.put("star_ore",      new ItemDef("star_ore","星辰矿石","MATERIAL","矿石",500,"神话矿石，用于锻造顶级装备"));
        // 农产品（保留作为种植产出）
        ITEMS.put("wheat",     new ItemDef("wheat","小麦","MATERIAL","农产品",1,"基础农作物，可出售换取金币"));
        ITEMS.put("carrot",    new ItemDef("carrot","胡萝卜","MATERIAL","农产品",3,"常见农作物，可出售换取金币"));
        ITEMS.put("herb",      new ItemDef("herb","药草","MATERIAL","农产品",8,"珍贵药材，可出售换取金币"));
        ITEMS.put("star_flower",new ItemDef("star_flower","星辰花","MATERIAL","农产品",35,"稀有花卉，可出售换取金币"));
        // 消耗品
        ITEMS.put("mana_potion",   new ItemDef("mana_potion","精力药水","CONSUMABLE","药水",10,"恢复20点精力值，可在背包中选择数量使用"));
        ITEMS.put("energy_potion", new ItemDef("energy_potion","体力药水","CONSUMABLE","药水",30,"使用后直接回满体力值"));
        // 工具
        ITEMS.put("strengthen_stone",new ItemDef("strengthen_stone","强化石","MATERIAL","强化",30,"装备强化必需材料，每次强化消耗数量随等级增加"));
        ITEMS.put("skill_book",    new ItemDef("skill_book","技能书","MATERIAL","技能",100,"学习和升级技能的必需品，通过副本掉落获取"));
        // 装备
        ITEMS.put("sword",    new ItemDef("sword","铁剑","EQUIPMENT","weapon",25,"基础武器 | 攻击+8 | 可通过强化提升属性"));
        ITEMS.put("helmet",   new ItemDef("helmet","银盔","EQUIPMENT","helmet",50,"头部防具 | 防御+4 生命+20 | 可通过强化提升属性"));
        ITEMS.put("armor",    new ItemDef("armor","金甲","EQUIPMENT","armor",100,"身体防具 | 防御+6 生命+30 | 可通过强化提升属性"));
        ITEMS.put("pants",    new ItemDef("pants","秘银护腿","EQUIPMENT","pants",75,"腿部防具 | 防御+5 生命+20 | 可通过强化提升属性"));
        ITEMS.put("boots",    new ItemDef("boots","精金战靴","EQUIPMENT","boots",80,"足部防具 | 攻击+2 防御+3 生命+10 | 可通过强化提升属性"));
        ITEMS.put("necklace", new ItemDef("necklace","星辰项链","EQUIPMENT","necklace",90,"饰品 | 攻击+5 防御+2 生命+15 | 可通过强化提升属性"));
        ITEMS.put("ring",     new ItemDef("ring","星辰戒指","EQUIPMENT","ring",95,"饰品 | 攻击+6 生命+10 | 可通过强化提升属性"));
    }

    // 种子已移除 - 种植系统改为直接种植，不需要种子

    // ==================== 矿洞（冷却递增，稀有矿概率递减，冷却x3） ====================
    public static List<MineDef> MINES = Arrays.asList(
            new MineDef("铜矿洞", 5, 30,
                    Arrays.asList("iron_ore","copper_ore","silver_ore","gold_ore","mithril_ore"),
                    Arrays.asList(55, 28, 12, 4, 1),
                    "新手矿洞，主要产出铁矿石和铜矿石。银矿石12%概率，金矿石4%概率，秘银矿石1%概率"),
            new MineDef("银矿洞", 10, 60,
                    Arrays.asList("copper_ore","silver_ore","gold_ore","mithril_ore","adamantite_ore"),
                    Arrays.asList(35, 30, 18, 12, 5),
                    "中级矿洞，银矿石主要产出地。金矿石18%概率，秘银矿石12%概率，精金矿石5%概率"),
            new MineDef("金矿洞", 20, 105,
                    Arrays.asList("silver_ore","gold_ore","mithril_ore","adamantite_ore","star_ore"),
                    Arrays.asList(28, 28, 20, 18, 6),
                    "高级矿洞，金矿石和秘银矿石富集区。精金矿石18%概率，星辰矿石6%概率"),
            new MineDef("秘银矿洞", 35, 150,
                    Arrays.asList("gold_ore","mithril_ore","adamantite_ore","star_ore","gold_ore"),
                    Arrays.asList(18, 30, 25, 10, 17),
                    "稀有矿洞，秘银矿石和精金矿石主要产出地。星辰矿石10%概率"),
            new MineDef("星辰矿洞", 50, 210,
                    Arrays.asList("mithril_ore","adamantite_ore","star_ore","adamantite_ore","mithril_ore"),
                    Arrays.asList(20, 30, 15, 22, 13),
                    "最高级矿洞，星辰矿石高概率产出地（15%概率），精金矿石概率最高（52%合计）")
    );

    public static List<ChapterDef> CHAPTERS = new ArrayList<>();
    static {
        // 第1章 新手村 Lv.1+：入门难度，无装备可过
        CHAPTERS.add(new ChapterDef(1,"新手村",1,
                Arrays.asList(
                        new MonsterDef("slime","史莱姆",1,50,8,3,5,15,8,null,null),
                        new MonsterDef("rat","巨鼠",2,130,16,8,6,30,15,null,null),
                        new MonsterDef("goblin","哥布林",3,240,24,13,7,50,25,
                                Arrays.asList("iron_ore"),Arrays.asList(2500)))));
        // 第2章 幽暗森林 Lv.5+：白色装备可过，需要消耗约60%HP
        CHAPTERS.add(new ChapterDef(2,"幽暗森林",5,
                Arrays.asList(
                        new MonsterDef("wolf","灰狼",5,420,32,16,10,80,40,null,null),
                        new MonsterDef("bear","黑熊",7,600,42,22,8,130,60,null,null),
                        new MonsterDef("treant","树人",10,900,55,30,6,200,90,
                                Arrays.asList("copper_ore","herb"),Arrays.asList(2500,1500)))));
        // 第3章 荒芜之地 Lv.15+：绿色装备可过，需要消耗约50-70%HP
        CHAPTERS.add(new ChapterDef(3,"荒芜之地",15,
                Arrays.asList(
                        new MonsterDef("skeleton","骷髅兵",15,1500,75,40,12,300,140,null,null),
                        new MonsterDef("zombie","僵尸",18,2200,95,50,8,420,200,null,null),
                        new MonsterDef("death_knight","死亡骑士",20,3000,120,62,14,600,300,
                                Arrays.asList("silver_ore","strengthen_stone"),Arrays.asList(2500,1500)))));
        // 第4章 熔岩山脉 Lv.25+：蓝色装备可过，需要消耗约50-80%HP
        CHAPTERS.add(new ChapterDef(4,"熔岩山脉",25,
                Arrays.asList(
                        new MonsterDef("fire_slime","火焰史莱姆",25,4200,145,60,15,800,380,null,null),
                        new MonsterDef("lava_golem","熔岩巨人",30,6000,185,78,8,1200,550,null,null),
                        new MonsterDef("fire_dragon","火龙幼崽",35,9000,230,98,16,2000,900,
                                Arrays.asList("gold_ore","skill_book"),Arrays.asList(2500,1200)))));
        // 第5章 天空之城 Lv.40+：紫色装备为门槛，橙色舒适
        CHAPTERS.add(new ChapterDef(5,"天空之城",40,
                Arrays.asList(
                        new MonsterDef("angel","堕天使",40,13000,300,120,18,3000,1400,null,null),
                        new MonsterDef("thunder_eagle","雷鹰",45,18000,380,150,22,4000,2000,null,null),
                        new MonsterDef("storm_lord","风暴领主",50,25000,480,185,20,5500,2800,
                                Arrays.asList("mithril_ore","star_ore"),Arrays.asList(2500,800)))));
    }

    public static List<DungeonDef> DUNGEONS = Arrays.asList(
            new DungeonDef("goblin_cave","哥布林洞穴",3,15,3,
                    new MonsterDef("goblin_king","哥布林王",5,500,30,16,8,120,80,
                            Arrays.asList("iron_ore","copper_ore"),Arrays.asList(4000,2500)),
                    "低级副本，通关后有概率获得铁矿石和铜矿石"),
            new DungeonDef("dark_tower","黑暗之塔",10,20,2,
                    new MonsterDef("dark_mage","暗黑法师",15,1400,70,40,12,350,200,
                            Arrays.asList("silver_ore","skill_book"),Arrays.asList(3500,1500)),
                    "中级副本，通关后有概率获得银矿石和技能书"),
            new DungeonDef("dragon_nest","龙巢",25,30,2,
                    new MonsterDef("young_dragon","幼龙",30,5000,180,80,15,1200,500,
                            Arrays.asList("gold_ore","mithril_ore","skill_book"),Arrays.asList(2500,1500,800)),
                    "高级副本，通关后有概率获得金矿石、秘银矿石和技能书"),
            new DungeonDef("abyss","深渊",40,40,1,
                    new MonsterDef("demon_lord","魔王",50,16000,420,190,20,3500,1800,
                            Arrays.asList("adamantite_ore","star_ore","skill_book"),Arrays.asList(2500,1500,1200)),
                    "最高级副本，通关后有概率获得精金矿石、星辰矿石和技能书")
    );

    public static List<ForgeRecipe> FORGE_RECIPES = Arrays.asList(
            new ForgeRecipe("sword","铁剑","WEAPON",150,
                    new LinkedHashMap<String,Integer>(){{put("iron_ore",6);put("copper_ore",4);}},
                    new LinkedHashMap<String,Integer>(){{put("WHITE",45);put("GREEN",28);put("BLUE",15);put("PURPLE",8);put("ORANGE",3);put("RED",1);}}),
            new ForgeRecipe("helmet","银盔","HELMET",200,
                    new LinkedHashMap<String,Integer>(){{put("copper_ore",6);put("silver_ore",4);}},
                    new LinkedHashMap<String,Integer>(){{put("WHITE",45);put("GREEN",28);put("BLUE",15);put("PURPLE",8);put("ORANGE",3);put("RED",1);}}),
            new ForgeRecipe("armor","金甲","ARMOR",300,
                    new LinkedHashMap<String,Integer>(){{put("silver_ore",6);put("gold_ore",4);}},
                    new LinkedHashMap<String,Integer>(){{put("WHITE",38);put("GREEN",28);put("BLUE",18);put("PURPLE",10);put("ORANGE",4);put("RED",2);}}),
            new ForgeRecipe("pants","秘银护腿","PANTS",250,
                    new LinkedHashMap<String,Integer>(){{put("silver_ore",5);put("mithril_ore",3);}},
                    new LinkedHashMap<String,Integer>(){{put("WHITE",38);put("GREEN",28);put("BLUE",18);put("PURPLE",10);put("ORANGE",4);put("RED",2);}}),
            new ForgeRecipe("boots","精金战靴","SHOES",300,
                    new LinkedHashMap<String,Integer>(){{put("gold_ore",5);put("mithril_ore",3);}},
                    new LinkedHashMap<String,Integer>(){{put("WHITE",32);put("GREEN",27);put("BLUE",18);put("PURPLE",13);put("ORANGE",7);put("RED",3);}}),
            new ForgeRecipe("necklace","星辰项链","NECKLACE",500,
                    new LinkedHashMap<String,Integer>(){{put("mithril_ore",4);put("adamantite_ore",3);}},
                    new LinkedHashMap<String,Integer>(){{put("WHITE",28);put("GREEN",23);put("BLUE",20);put("PURPLE",15);put("ORANGE",9);put("RED",5);}}),
            new ForgeRecipe("ring","星辰戒指","RING",500,
                    new LinkedHashMap<String,Integer>(){{put("mithril_ore",4);put("star_ore",2);}},
                    new LinkedHashMap<String,Integer>(){{put("WHITE",28);put("GREEN",23);put("BLUE",20);put("PURPLE",15);put("ORANGE",9);put("RED",5);}})
    );

    public static Map<String, Double> QUALITY_MULT = new LinkedHashMap<>();
    static {
        QUALITY_MULT.put("WHITE",1.0); QUALITY_MULT.put("GREEN",1.15);
        QUALITY_MULT.put("BLUE",1.35); QUALITY_MULT.put("PURPLE",1.6);
        QUALITY_MULT.put("ORANGE",1.9); QUALITY_MULT.put("RED",2.3);
    }

    public static Map<String, int[]> QUALITY_ATTR_BASE = new LinkedHashMap<>();
    static {
        QUALITY_ATTR_BASE.put("WEAPON",  new int[]{12, 3, 18});
        QUALITY_ATTR_BASE.put("HELMET",  new int[]{3, 8, 35});
        QUALITY_ATTR_BASE.put("ARMOR",   new int[]{5, 12, 55});
        QUALITY_ATTR_BASE.put("PANTS",   new int[]{3, 10, 40});
        QUALITY_ATTR_BASE.put("SHOES",   new int[]{4, 5, 18});
        QUALITY_ATTR_BASE.put("NECKLACE",new int[]{10, 3, 30});
        QUALITY_ATTR_BASE.put("RING",    new int[]{12, 2, 18});
    }

    public static int strengthenCostGold(int newLevel) { return newLevel * getSetting("strengthenGoldPerLevel", 80); }
    public static int strengthenCostStone(int newLevel) { return newLevel; }
    public static int strengthenRate(int newLevel) {
        if (newLevel <= 5) return getSetting("strengthenRate1to5", 90);
        if (newLevel <= 10) return getSetting("strengthenRate6to10", 70);
        if (newLevel <= 15) return getSetting("strengthenRate11to15", 40);
        return getSetting("strengthenRate16to20", 20);
    }
    public static String getStrengthenOre(int newLevel) {
        if (newLevel <= 5) return "iron_ore";
        if (newLevel <= 10) return "copper_ore";
        if (newLevel <= 15) return "silver_ore";
        return "gold_ore";
    }
    public static int getStrengthenOreCount(int newLevel) { return newLevel; }

    public static List<ShopItem> SHOP_ITEMS = Arrays.asList(
            new ShopItem("mana_potion",15,"GOLD",20),
            new ShopItem("energy_potion",20,"DIAMOND",10),
            new ShopItem("strengthen_stone",80,"GOLD",10),
            new ShopItem("skill_book",500,"GOLD",3),
            new ShopItem("iron_ore",500,"GOLD",50),
            new ShopItem("copper_ore",800,"GOLD",40),
            new ShopItem("silver_ore",1200,"GOLD",30),
            new ShopItem("gold_ore",2000,"GOLD",20),
            new ShopItem("mithril_ore",50,"DIAMOND",10),
            new ShopItem("adamantite_ore",100,"DIAMOND",5),
            new ShopItem("star_ore",150,"DIAMOND",3)
    );

    public static int getDiamondToGoldRate() { return getSetting("diamondToGoldRate", 100); }

    public static int[] getSignRewardRange(int day) {
        if (day % 30 == 0) return new int[]{getSetting("signDay30Min", 30), getSetting("signDay30Max", 60)};
        if (day % 7 == 0) return new int[]{getSetting("signDay7Min", 5), getSetting("signDay7Max", 15)};
        return new int[]{getSetting("signNormalMin", 5), getSetting("signNormalMax", 12)};
    }
    public static boolean isSignDiamond(int day) { return day % 7 == 0; }
    public static int randomSignReward(int day) {
        int[] range = getSignRewardRange(day);
        return range[0] + new Random().nextInt(range[1] - range[0] + 1);
    }

    public static String getPvpRank(int score) {
        if (score >= 6000) return "巅峰王者";
        if (score >= 5000) return "王者";
        if (score >= 4000) return "钻石";
        if (score >= 3000) return "铂金";
        if (score >= 2000) return "黄金";
        if (score >= 1000) return "白银";
        return "青铜";
    }

    public static List<SecretEvent> SECRET_EVENTS = Arrays.asList(
            new SecretEvent("BATTLE","遭遇野兽！",
                    new MonsterDef("wild_beast","野兽",5,200,20,10,8,80,40,null,null),null,null,0),
            new SecretEvent("CHEST","发现宝箱！",null,"GOLD",null,100),
            new SecretEvent("TRAP","踩中陷阱！",null,"GOLD",null,-50),
            new SecretEvent("WONDER","遇到神秘老人！",null,"ITEM","mana_potion",3),
            new SecretEvent("CHEST","发现钻石宝箱！",null,"DIAMOND",null,5),
            new SecretEvent("BATTLE","遭遇精英怪物！",
                    new MonsterDef("elite_monster","精英怪物",10,500,40,20,10,200,100,
                            Arrays.asList("silver_ore"),Arrays.asList(5000)),null,null,0),
            new SecretEvent("WONDER","发现矿脉！",null,"ITEM","copper_ore",5),
            new SecretEvent("CHEST","发现大量金币！",null,"GOLD",null,200)
    );

    public static List<AchieveDef> ACHIEVEMENTS = Arrays.asList(
            new AchieveDef("first_kill","初出茅庐","击杀第一个怪物",1,"gold",100),
            new AchieveDef("level_10","小有成就","达到10级",10,"diamond",20),
            new AchieveDef("level_50","一方霸主","达到50级",50,"diamond",100),
            new AchieveDef("mine_10","初级矿工","挖矿等级达到10",10,"gold",500),
            new AchieveDef("farm_10","初级农夫","种植等级达到10",10,"gold",500),
            new AchieveDef("forge_10","初级锻造师","锻造等级达到10",10,"gold",500),
            new AchieveDef("pvp_1000","竞技新星","PVP积分达到1000",1000,"diamond",50)
    );

    public static class TaskDef {
        public String key, name, desc, rewardType;
        public int target, rewardAmount;
        public TaskDef(String key, String name, String desc, int target, String rewardType, int rewardAmount) {
            this.key = key; this.name = name; this.desc = desc;
            this.target = target; this.rewardType = rewardType; this.rewardAmount = rewardAmount;
        }
    }

    public static List<TaskDef> DAILY_TASKS = Arrays.asList(
            new TaskDef("daily_mine","每日挖矿","完成3次挖矿",3,"gold",50),
            new TaskDef("daily_farm","每日种植","收获3次作物",3,"gold",50),
            new TaskDef("daily_fight","每日战斗","完成5次冒险战斗",5,"gold",80),
            new TaskDef("daily_forge","每日锻造","锻造1件装备",1,"gold",100),
            new TaskDef("daily_pvp","每日竞技","完成2次竞技场战斗",2,"diamond",10),
            new TaskDef("daily_sign","每日签到","签到1次",1,"gold",30)
    );

    public static List<SkillDef> SKILLS = Arrays.asList(
            new SkillDef("warrior_slash","猛击","WARRIOR","ACTIVE",1,10,80,0,
                    "造成伤害，基础150%，每级+5%，满级200%",
                    "DAMAGE",1.5,0.05,0),
            new SkillDef("warrior_defend","铁壁","WARRIOR","PASSIVE",5,10,150,1,
                    "被动提升防御力，基础+10%，每级+2%，满级+28%",
                    "PASSIVE_DEF",10,2,0),
            new SkillDef("warrior_berserk","狂暴","WARRIOR","ACTIVE",10,10,300,1,
                    "造成高额伤害，基础200%，每级+10%，满级290%",
                    "DAMAGE",2.0,0.1,0),
            new SkillDef("mage_fireball","火球术","MAGE","ACTIVE",1,10,80,0,
                    "造成法术伤害，基础180%，每级+8%，满级252%",
                    "DAMAGE",1.8,0.08,0),
            new SkillDef("mage_meteor","陨石术","MAGE","ACTIVE",10,10,300,1,
                    "造成超高法术伤害，基础250%，每级+10%，满级340%",
                    "DAMAGE",2.5,0.1,0),
            new SkillDef("mage_mana","法力护盾","MAGE","PASSIVE",5,10,150,1,
                    "被动提升生命值，基础+10%，每级+2%，满级+28%",
                    "PASSIVE_HP",10,2,0),
            new SkillDef("archer_double","双重射击","ARCHER","ACTIVE",1,10,80,0,
                    "快速射击两次，基础150%，每级+5%，满级200%",
                    "DAMAGE",1.5,0.05,0),
            new SkillDef("archer_rain","箭雨","ARCHER","ACTIVE",10,10,300,1,
                    "箭雨覆盖攻击，基础200%，每级+8%，满级272%",
                    "DAMAGE",2.0,0.08,0),
            new SkillDef("archer_dodge","闪避","ARCHER","PASSIVE",5,10,150,1,
                    "被动提升速度，基础+10%，每级+2%，满级+28%",
                    "PASSIVE_SPD",10,2,0),
            new SkillDef("priest_heal","治愈术","PRIEST","ACTIVE",1,10,80,0,
                    "恢复生命值，基础30%，每级+3%，满级57%",
                    "HEAL",0.3,0.03,0),
            new SkillDef("priest_holy","神圣打击","PRIEST","ACTIVE",10,10,300,1,
                    "神圣攻击并回血，伤害基础120%每级+5%，附带15%回血",
                    "DAMAGE_HEAL",1.2,0.05,0.15),
            new SkillDef("priest_bless","祝福","PRIEST","PASSIVE",5,10,150,1,
                    "被动提升攻击力，基础+10%，每级+2%，满级+28%",
                    "PASSIVE_ATK",10,2,0),
            new SkillDef("miner_efficient","高效采矿","MINER","PASSIVE",1,10,80,0,
                    "被动增加矿石产出数量，基础+1，每2级额外+1，满级+5",
                    "MINE_BONUS",1,0.5,0),
            new SkillDef("miner_lucky","幸运矿工","MINER","PASSIVE",5,10,150,1,
                    "被动提升稀有矿石掉落概率，基础+5%，每级+2%，满级+23%",
                    "MINE_RARE",5,2,0),
            new SkillDef("miner_endurance","耐力","MINER","PASSIVE",10,10,300,1,
                    "被动提升体力上限，基础+20，每级+5，满级+65",
                    "ENERGY_MAX",20,5,0)
    );

    // ==================== 公告 ====================
    public static class Announcement {
        public String title, content, time;
        public Announcement(String title, String content, String time) {
            this.title = title; this.content = content; this.time = time;
        }
    }

    public static List<Announcement> ANNOUNCEMENTS = Arrays.asList(
            new Announcement("欢迎来到文字江湖！",
                    "这是一款纯文字RPG游戏，包含冒险、挖矿、种菜、锻造、竞技等丰富玩法。每日签到可获得随机奖励，祝你游戏愉快！",
                    "2026-05-01"),
            new Announcement("竞技场分职业对战",
                    "竞技场已支持分职业对战和排行，仅与同职业玩家匹配，战力相差不超过15%的对手才会匹配。公平竞技！",
                    "2026-05-05"),
            new Announcement("矿洞系统说明",
                    "各矿洞冷却时间和矿石掉落概率已优化：越高等级矿洞冷却时间越长，稀有矿石获取难度相应提升。挖矿等级越高，每次产出矿石数量越多！合理规划体力使用是关键！",
                    "2026-05-08"),
            new Announcement("种植系统说明",
                    "种植无需种子，直接在空地上种植即可。种植等级越高，收获数量越多。每块地种植后需等待一定时间才能收获。",
                    "2026-05-08"),
            new Announcement("锻造系统说明",
                    "锻造装备消耗矿石和金币，品质随机（受锻造等级影响）。同一件装备可重复锻造，也可以出售已有装备返还50%矿石。强化需要消耗矿石和强化石，+10以下失败不降级，+10以上失败降1级。",
                    "2026-05-08")
    );

    // ==================== 种植配置（无需种子） ====================
    public static List<CropDef> CROPS = Arrays.asList(
            new CropDef("wheat","小麦",1,3,"基础农作物，1分钟成熟"),
            new CropDef("carrot","胡萝卜",6,5,"常见农作物，6分钟成熟"),
            new CropDef("herb","药草",10,8,"珍贵药材，10分钟成熟"),
            new CropDef("star_flower","星辰花",20,12,"稀有花卉，20分钟成熟")
    );

    // ==================== 内部类 ====================
    public static class ItemDef {
        public String key, name, type, subType, desc;
        public int sellPrice;
        public ItemDef(String key, String name, String type, String subType, int sellPrice, String desc) {
            this.key = key; this.name = name; this.type = type; this.subType = subType;
            this.sellPrice = sellPrice; this.desc = desc;
        }
    }
    public static class CropDef {
        public String cropKey, name, desc;
        public int growMinutes, baseYield;
        public CropDef(String cropKey, String name, int growMinutes, int baseYield, String desc) {
            this.cropKey = cropKey; this.name = name;
            this.growMinutes = growMinutes; this.baseYield = baseYield; this.desc = desc;
        }
    }
    public static class MineDef {
        public String name, desc;
        public int energyCost, cooldown;
        public List<String> dropKeys;
        public List<Integer> weights;
        public MineDef(String name, int energyCost, int cooldown, List<String> dropKeys, List<Integer> weights, String desc) {
            this.name = name; this.energyCost = energyCost; this.cooldown = cooldown;
            this.dropKeys = dropKeys; this.weights = weights; this.desc = desc;
        }
    }
    public static class ChapterDef {
        public int chapter, levelReq;
        public String name;
        public List<MonsterDef> stages;
        public ChapterDef(int chapter, String name, int levelReq, List<MonsterDef> stages) {
            this.chapter = chapter; this.name = name; this.levelReq = levelReq; this.stages = stages;
        }
    }
    public static class MonsterDef {
        public String key, name;
        public int level, hp, atk, def, spd, expReward, goldReward;
        public List<String> dropKeys;
        public List<Integer> dropRates;
        public MonsterDef(String key, String name, int level, int hp, int atk, int def, int spd,
                          int expReward, int goldReward, List<String> dropKeys, List<Integer> dropRates) {
            this.key = key; this.name = name; this.level = level; this.hp = hp;
            this.atk = atk; this.def = def; this.spd = spd;
            this.expReward = expReward; this.goldReward = goldReward;
            this.dropKeys = dropKeys; this.dropRates = dropRates;
        }
    }
    public static class DungeonDef {
        public String key, name, desc;
        public int levelReq, energyCost, dailyLimit;
        public MonsterDef boss;
        public DungeonDef(String key, String name, int levelReq, int energyCost, int dailyLimit, MonsterDef boss, String desc) {
            this.key = key; this.name = name; this.levelReq = levelReq;
            this.energyCost = energyCost; this.dailyLimit = dailyLimit; this.boss = boss; this.desc = desc;
        }
    }
    public static class ForgeRecipe {
        public String resultKey, resultName, part;
        public int goldCost;
        public Map<String, Integer> materials, qualityWeights;
        public ForgeRecipe(String resultKey, String resultName, String part, int goldCost,
                           Map<String, Integer> materials, Map<String, Integer> qualityWeights) {
            this.resultKey = resultKey; this.resultName = resultName; this.part = part;
            this.goldCost = goldCost; this.materials = materials; this.qualityWeights = qualityWeights;
        }
    }
    public static class ShopItem {
        public String itemKey, currency;
        public int price, stock;
        public ShopItem(String itemKey, int price, String currency, int stock) {
            this.itemKey = itemKey; this.price = price; this.currency = currency; this.stock = stock;
        }
    }
    public static class SecretEvent {
        public String type, desc, rewardType, rewardItemKey;
        public int rewardAmount;
        public MonsterDef monster;
        public SecretEvent(String type, String desc, MonsterDef monster,
                           String rewardType, String rewardItemKey, int rewardAmount) {
            this.type = type; this.desc = desc; this.monster = monster;
            this.rewardType = rewardType; this.rewardItemKey = rewardItemKey; this.rewardAmount = rewardAmount;
        }
    }
    public static class AchieveDef {
        public String key, name, desc, rewardType;
        public int target, rewardAmount;
        public AchieveDef(String key, String name, String desc, int target, String rewardType, int rewardAmount) {
            this.key = key; this.name = name; this.desc = desc; this.target = target;
            this.rewardType = rewardType; this.rewardAmount = rewardAmount;
        }
    }
    public static class SkillDef {
        public String key, name, job, type, desc, effectType;
        public int unlockLevel, maxLevel, goldCost, bookCost;
        public double baseValue, valuePerLevel, secondaryValue;
        public SkillDef(String key, String name, String job, String type, int unlockLevel,
                        int maxLevel, int goldCost, int bookCost, String desc,
                        String effectType, double baseValue, double valuePerLevel, double secondaryValue) {
            this.key = key; this.name = name; this.job = job; this.type = type;
            this.unlockLevel = unlockLevel; this.maxLevel = maxLevel;
            this.goldCost = goldCost; this.bookCost = bookCost; this.desc = desc;
            this.effectType = effectType; this.baseValue = baseValue;
            this.valuePerLevel = valuePerLevel; this.secondaryValue = secondaryValue;
        }
    }
}
