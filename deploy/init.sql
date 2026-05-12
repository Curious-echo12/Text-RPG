-- =============================================
-- 文字江湖 - 完整数据库初始化脚本
-- 适用于全新部署
-- =============================================

CREATE DATABASE IF NOT EXISTS text_rpg DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE text_rpg;

-- ========== 核心表 ==========

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  player_id VARCHAR(6) NOT NULL UNIQUE COMMENT '6位唯一玩家ID',
  username VARCHAR(32) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  email VARCHAR(128) DEFAULT NULL COMMENT '绑定邮箱',
  email_verified TINYINT DEFAULT 0 COMMENT '邮箱是否已验证',
  is_admin TINYINT DEFAULT 0 COMMENT '是否为管理员',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  name VARCHAR(32) NOT NULL,
  job VARCHAR(16) NOT NULL,
  level INT DEFAULT 1,
  exp BIGINT DEFAULT 0,
  hp INT DEFAULT 100,
  max_hp INT DEFAULT 100,
  attack INT DEFAULT 10,
  defense INT DEFAULT 5,
  speed INT DEFAULT 5,
  crit_rate DOUBLE DEFAULT 0.05,
  crit_dmg DOUBLE DEFAULT 1.5,
  fight_power BIGINT DEFAULT 0,
  reborn_count INT DEFAULT 0,
  gold BIGINT DEFAULT 100,
  diamond INT DEFAULT 10,
  energy INT DEFAULT 100,
  max_energy INT DEFAULT 100,
  spirit INT DEFAULT 50,
  max_spirit INT DEFAULT 50,
  mine_level INT DEFAULT 1,
  mine_exp INT DEFAULT 0,
  farm_level INT DEFAULT 1,
  farm_exp INT DEFAULT 0,
  forge_level INT DEFAULT 1,
  forge_exp INT DEFAULT 0,
  pvp_score INT DEFAULT 0,
  pvp_star INT DEFAULT 0,
  skill_point INT DEFAULT 0,
  last_energy_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_spirit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_bag (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  item_key VARCHAR(64) NOT NULL,
  count INT DEFAULT 1,
  is_bind TINYINT DEFAULT 0,
  extra_json TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
);

CREATE TABLE IF NOT EXISTS user_equip (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  item_key VARCHAR(64) NOT NULL,
  part VARCHAR(16) NOT NULL,
  quality VARCHAR(16) DEFAULT 'WHITE',
  strengthen_level INT DEFAULT 0,
  base_attr_json TEXT,
  extra_attr_json TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
);

CREATE TABLE IF NOT EXISTS user_skill (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  skill_key VARCHAR(64) NOT NULL,
  level INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_skill (user_id, skill_key)
);

CREATE TABLE IF NOT EXISTS farm_plot (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  plot_index INT NOT NULL,
  seed_key VARCHAR(64),
  plant_time DATETIME,
  harvest_time DATETIME,
  status VARCHAR(16) DEFAULT 'EMPTY',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
);

CREATE TABLE IF NOT EXISTS pvp_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  attacker_id BIGINT NOT NULL,
  defender_id BIGINT NOT NULL,
  attacker_name VARCHAR(32),
  defender_name VARCHAR(32),
  result VARCHAR(8) NOT NULL,
  score_change INT DEFAULT 0,
  battle_log TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_attacker (attacker_id)
);

CREATE TABLE IF NOT EXISTS mail (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content TEXT,
  item_json TEXT,
  is_read TINYINT DEFAULT 0,
  is_claimed TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
);

CREATE TABLE IF NOT EXISTS user_task (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  task_key VARCHAR(64) NOT NULL,
  progress INT DEFAULT 0,
  target INT DEFAULT 1,
  is_complete TINYINT DEFAULT 0,
  is_claimed TINYINT DEFAULT 0,
  task_date DATE NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_task_date (user_id, task_key, task_date)
);

CREATE TABLE IF NOT EXISTS user_achievement (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  achieve_key VARCHAR(64) NOT NULL,
  progress INT DEFAULT 0,
  target INT DEFAULT 1,
  is_complete TINYINT DEFAULT 0,
  is_claimed TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_achieve (user_id, achieve_key)
);

CREATE TABLE IF NOT EXISTS user_sign (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  sign_date DATE NOT NULL,
  consecutive_days INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_date (user_id, sign_date)
);

-- ========== 管理系统表 ==========

CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(32) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(128) NOT NULL,
  content TEXT NOT NULL,
  is_active TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS email_verification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  email VARCHAR(128) NOT NULL,
  code VARCHAR(6) NOT NULL,
  purpose VARCHAR(32) NOT NULL DEFAULT 'BIND',
  used TINYINT DEFAULT 0,
  expire_time DATETIME NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_purpose (user_id, purpose),
  INDEX idx_email_code (email, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS game_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  config_key VARCHAR(64) NOT NULL UNIQUE,
  config_json LONGTEXT NOT NULL,
  description VARCHAR(256) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 初始公告数据 ==========

INSERT INTO announcement (title, content) VALUES
('欢迎来到文字江湖！', '这是一款纯文字RPG游戏，包含冒险、挖矿、种菜、锻造、竞技等丰富玩法。每日签到可获得随机奖励，祝你游戏愉快！'),
('竞技场分职业对战', '竞技场已支持分职业对战和排行，仅与同职业玩家匹配，战力相差不超过15%的对手才会匹配。公平竞技！'),
('矿洞系统说明', '各矿洞冷却时间和矿石掉落概率已优化：越高等级矿洞冷却时间越长，稀有矿石获取难度相应提升。挖矿等级越高，每次产出矿石数量越多！'),
('种植系统说明', '种植无需种子，直接在空地上种植即可。种植等级越高，收获数量越多。随等级提升可解锁更多菜地（最多9块）。'),
('锻造系统说明', '锻造装备消耗矿石和金币，品质随机（受锻造等级影响）。同一件装备可重复锻造，也可以出售已有装备返还50%矿石。');

-- ========== 游戏配置数据（由 migration_config.sql 提供） ==========
-- 请在执行此脚本后再执行 migration_config.sql
