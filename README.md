# ⚔️ 文字江湖 — 纯文字网页 RPG 游戏

一款基于 Spring Boot + Vue 3 的全栈文字 RPG 网页游戏，包含冒险战斗、挖矿采集、农田种植、装备锻造、竞技对战、副本挑战等丰富玩法。

---

## 🎮 游戏特色

- **5 大职业**：战士、法师、射手、牧师、矿工，各有独特属性成长和技能
- **回合制战斗**：PVE 冒险 + PVP 竞技场 + 副本 Boss
- **生活技能**：挖矿、种植、锻造，完整的生产制造链
- **装备系统**：6 个品质等级（白→绿→蓝→紫→橙→红），强化 +20
- **管理后台**：动态配置所有游戏参数，发布公告，发送邮件，管理玩家
- **邮箱系统**：绑定邮箱找回密码，系统邮件通知

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 8, Spring Boot 2.7, Spring Security (JWT), MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3 (Composition API), Vite 5, Element Plus, Pinia, Axios |
| 部署 | Nginx 反向代理, systemd 服务管理 |

---

## 📁 项目结构

```
text-rpg/
├── backend/                    # 后端 Spring Boot 项目
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/textrpg/
│       │   ├── common/         # 配置类、工具类
│       │   ├── config/         # Spring 配置、安全配置
│       │   ├── controller/     # REST 控制器（20个）
│       │   ├── entity/         # 数据库实体（16个）
│       │   ├── mapper/         # MyBatis-Plus Mapper
│       │   └── service/        # 业务逻辑层（16个）
│       └── resources/
│           ├── application-example.yml  # 配置模板
│           ├── schema.sql               # 数据库表结构
│           ├── migration.sql            # 增量迁移脚本
│           └── migration_config.sql     # 游戏配置数据
├── frontend/                   # 前端 Vue 3 项目
│   ├── src/
│   │   ├── views/              # 页面组件（20+个）
│   │   ├── views/admin/        # 管理后台页面（7个）
│   │   ├── router/             # 路由配置
│   │   ├── store/              # Pinia 状态管理
│   │   └── utils/              # HTTP 请求工具
│   └── package.json
├── deploy/                     # 部署相关文件
│   ├── init.sql                # 完整初始化 SQL
│   ├── nginx.conf              # Nginx 配置参考
│   └── DEPLOY.md               # 详细部署文档
└── README.md
```

---

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ / npm 9+

### 1. 克隆项目

```bash
git clone https://github.com/Curious-echo12/Text-RPG.git
cd Text-RPG
```

### 2. 配置数据库

```bash
# 创建数据库并导入表结构
mysql -u root -p < backend/src/main/resources/schema.sql

# 导入游戏初始配置数据
mysql -u root -p text_rpg < backend/src/main/resources/migration_config.sql
```

### 3. 配置应用

```bash
# 复制配置模板
cp backend/src/main/resources/application-example.yml backend/src/main/resources/application.yml

# 编辑配置文件，填入你的数据库密码、邮箱等信息
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`

### 6. 访问游戏

- 游戏地址：`http://localhost:3000`
- 管理后台：`http://localhost:3000/admin/login`
- 默认管理员：`admin` / `admin123456`

---

## 🎯 游戏玩法概览

### 职业系统

| 职业 | 特点 | 基础属性 (HP/ATK/DEF/SPD) |
|------|------|---------------------------|
| ⚔️ 战士 | 高生命高防御 | 200/25/15/10 |
| 🔮 法师 | 高攻击低防御 | 150/35/8/12 |
| 🏹 射手 | 高速度先手 | 160/30/10/15 |
| ✝️ 牧师 | 均衡有治疗 | 180/20/12/11 |
| ⛏️ 矿工 | 高防御挖矿加成 | 170/18/18/9 |

### 核心玩法

- **冒险**：5 个章节，每章 3 个怪物关卡，等级递增
- **副本**：4 个 Boss 副本，每日限定次数，掉落稀有材料
- **竞技场**：同职业 PVP 匹配，积分段位制
- **挖矿**：5 个矿洞，冷却递增，稀有矿概率递减
- **种植**：无需种子，4 种作物，随等级解锁更多菜地
- **锻造**：7 种装备配方，6 个品质等级，可强化至 +20
- **签到**：每日签到获得金币/钻石，连续签到有额外奖励

### 经济系统

- **金币**：战斗掉落、出售物品、签到、钻石兑换
- **钻石**：签到（每 7 天）、秘境探索、新手礼包、每日签到赠送
- **体力**：冒险/挖矿消耗，60 秒恢复 1 点
- **精力**：PVP/秘境消耗，60 秒恢复 1 点

---

## ⚙️ 管理后台

访问 `/admin/login` 进入管理后台，可进行：

| 功能 | 说明 |
|------|------|
| 👥 玩家管理 | 查看/编辑/删除玩家，重置密码，查看完整角色信息 |
| 📢 公告管理 | 发布/编辑/删除/启停公告 |
| 📧 发送邮件 | 发送物品/装备，按职业/等级筛选，一键全选 |
| ⚙️ 游戏配置 | 修改所有游戏参数，即时生效 |

### 游戏配置管理

管理员可在后台图形化修改以下所有配置：

- 职业属性与成长值
- 物品定义（名称、类型、售价、描述）
- 矿洞配置（体力消耗、冷却时间、掉落概率）
- 冒险章节与怪物属性
- 副本配置与 Boss 属性
- 锻造配方与品质概率
- 商店物品与价格
- 技能配置与效果参数
- 成就、任务、签到奖励
- 种植作物配置
- 秘境事件配置
- 基础游戏参数（恢复间隔、经验公式等）

---

## 📧 邮箱配置

游戏支持邮箱绑定和找回密码功能，需要配置 SMTP：

1. 申请 QQ 邮箱授权码（设置 → 账户 → POP3/SMTP 服务）
2. 在 `application.yml` 中配置邮箱信息
3. 玩家可在角色页面绑定邮箱，用于找回密码

---

## 📖 API 文档

### 公开接口（无需认证）

| 接口 | 说明 |
|------|------|
| `POST /api/auth/register` | 注册 |
| `POST /api/auth/login` | 登录 |
| `GET /api/announcement/list` | 获取公告列表 |
| `GET /api/game/config` | 获取游戏配置 |
| `POST /api/email/sendResetCode` | 发送重置密码验证码 |
| `POST /api/email/resetPassword` | 重置密码 |

### 需要认证的接口（Bearer Token）

| 模块 | 接口数 | 说明 |
|------|--------|------|
| 角色 | 4 | 创建、信息、详情、转生 |
| 背包 | 5 | 列表、使用、出售、穿戴、卸下 |
| 技能 | 3 | 列表、学习、升级 |
| 挖矿 | 2 | 信息、挖掘 |
| 种植 | 3 | 地块、种植、收获 |
| 锻造 | 4 | 配方、锻造、强化信息、强化 |
| 商店 | 4 | 列表、购买、兑换、汇率 |
| 冒险 | 2 | 章节、战斗 |
| 副本 | 3 | 列表、进入、秘境 |
| 竞技 | 4 | 匹配、战斗、记录、排行 |
| 排行 | 2 | 列表、职业统计 |
| 签到 | 2 | 信息、签到 |
| 邮件 | 5 | 列表、已读、领取、删除、未读检查 |
| 邮箱 | 4 | 发送绑定码、绑定、发送更换码、更换邮箱 |

---

## 📄 License

MIT License

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)
