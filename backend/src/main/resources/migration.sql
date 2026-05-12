-- =============================================
-- 数据库迁移脚本
-- 功能：管理员系统、公告系统、邮箱绑定、玩家ID
-- =============================================

USE text_rpg;

-- 1. sys_user 表新增字段（分步执行，兼容已有数据）
-- 第一步：先添加为可空字段，带默认值
ALTER TABLE sys_user ADD COLUMN player_id VARCHAR(6) DEFAULT NULL COMMENT '6位唯一玩家ID' AFTER id;
ALTER TABLE sys_user ADD COLUMN email VARCHAR(128) DEFAULT NULL COMMENT '绑定邮箱' AFTER password;
ALTER TABLE sys_user ADD COLUMN email_verified TINYINT DEFAULT 0 COMMENT '邮箱是否已验证' AFTER email;
ALTER TABLE sys_user ADD COLUMN is_admin TINYINT DEFAULT 0 COMMENT '是否为管理员' AFTER email_verified;

-- 第二步：为已有行生成唯一6位玩家ID
-- 使用 FLOOR(100000 + RAND() * 900000) 生成随机6位数，循环更新避免重复
UPDATE sys_user SET player_id = LPAD(FLOOR(100000 + RAND() * 900000), 6, '0') WHERE player_id IS NULL;
-- 如果存在重复（极小概率），再更新一次
UPDATE sys_user s1 INNER JOIN sys_user s2 ON s1.player_id = s2.player_id AND s1.id > s2.id
SET s1.player_id = LPAD(FLOOR(100000 + RAND() * 900000), 6, '0');

-- 第三步：改为 NOT NULL 并添加唯一索引
ALTER TABLE sys_user MODIFY COLUMN player_id VARCHAR(6) NOT NULL COMMENT '6位唯一玩家ID';
ALTER TABLE sys_user ADD UNIQUE INDEX uk_player_id (player_id);

-- 2. 管理员表（独立表，更安全）
CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(32) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员账号表';

-- 管理员账号由应用启动时自动创建（DataInitializer）
-- 默认账号: admin / admin123456

-- 3. 公告表（替代硬编码公告）
CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(128) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  is_active TINYINT DEFAULT 1 COMMENT '是否显示',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 4. 邮箱验证码表
CREATE TABLE IF NOT EXISTS email_verification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  email VARCHAR(128) NOT NULL COMMENT '邮箱地址',
  code VARCHAR(6) NOT NULL COMMENT '6位验证码',
  purpose VARCHAR(32) NOT NULL DEFAULT 'BIND' COMMENT '用途: BIND=绑定邮箱, RESET=重置密码',
  used TINYINT DEFAULT 0 COMMENT '是否已使用',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_purpose (user_id, purpose),
  INDEX idx_email_code (email, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- 5. 插入初始公告
INSERT INTO announcement (title, content) VALUES
('欢迎来到文字江湖！', '这是一款纯文字RPG游戏，包含冒险、挖矿、种菜、锻造、竞技等丰富玩法。每日签到可获得随机奖励，祝你游戏愉快！'),
('竞技场分职业对战', '竞技场已支持分职业对战和排行，仅与同职业玩家匹配，战力相差不超过15%的对手才会匹配。公平竞技！'),
('矿洞系统说明', '各矿洞冷却时间和矿石掉落概率已优化：越高等级矿洞冷却时间越长，稀有矿石获取难度相应提升。挖矿等级越高，每次产出矿石数量越多！'),
('种植系统说明', '种植无需种子，直接在空地上种植即可。种植等级越高，收获数量越多。随等级提升可解锁更多菜地（最多9块）。'),
('锻造系统说明', '锻造装备消耗矿石和金币，品质随机（受锻造等级影响）。同一件装备可重复锻造，也可以出售已有装备返还50%矿石。');
