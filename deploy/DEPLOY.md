# 文字江湖 — 部署指南

本文档包含本地开发和云端部署的完整步骤。

---

## 一、本地开发环境

### 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8+ | Java 运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0+ | 数据库 |
| Node.js | 16+ | 前端运行环境 |
| npm | 9+ | 前端包管理 |

### 1. 克隆项目

```bash
git clone https://github.com/Curious-echo12/Text-RPG.git
cd Text-RPG
```

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE text_rpg DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 导入表结构
mysql -u root -p text_rpg < backend/src/main/resources/schema.sql

# 导入游戏配置数据
mysql -u root -p text_rpg < backend/src/main/resources/migration_config.sql
```

### 3. 配置后端

```bash
# 复制配置模板
cp backend/src/main/resources/application-example.yml backend/src/main/resources/application.yml
```

编辑 `application.yml`，填入：
- 数据库用户名和密码
- QQ 邮箱和 SMTP 授权码（可选，不配置则邮件功能不可用）
- JWT 密钥（任意 32+ 字符字符串）

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:3000`，自动代理 API 到 8080 端口

### 6. 访问

- 游戏：`http://localhost:3000`
- 管理后台：`http://localhost:3000/admin/login`（admin / admin123456）

---

## 二、云服务器部署（宝塔面板）

### 环境要求

| 软件 | 安装方式 |
|------|---------|
| JDK 8 | 宝塔面板 → 软件商店 → Java 项目管理器 |
| MySQL 8.0 | 宝塔面板 → 软件商店 → MySQL |
| Nginx | 宝塔面板 → 软件商店 → Nginx |
| Node.js 16+ | `curl -fsSL https://rpm.nodesource.com/setup_18.x \| bash - && yum install -y nodejs` |
| Maven | `yum install -y maven` |

### 1. 上传代码

```bash
# 方式一：Git 克隆
cd /opt
git clone https://github.com/Curious-echo12/Text-RPG.git text-rpg

# 方式二：SCP 上传
scp -r ./Text-RPG/* root@your-server-ip:/opt/text-rpg/
```

### 2. 初始化数据库

```bash
mysql -u root -p < /opt/text-rpg/backend/src/main/resources/schema.sql
mysql -u root -p text_rpg < /opt/text-rpg/backend/src/main/resources/migration_config.sql
```

### 3. 配置应用

```bash
cp /opt/text-rpg/backend/src/main/resources/application-example.yml /opt/text-rpg/application.yml
vi /opt/text-rpg/application.yml
```

填入服务器数据库密码、邮箱配置等。

### 4. 构建后端

```bash
cd /opt/text-rpg/backend
mvn clean package -DskipTests
```

### 5. 构建前端

```bash
cd /opt/text-rpg/frontend
npm install
npm run build
```

### 6. 配置 systemd 服务

创建 `/etc/systemd/system/textrpg.service`：

```ini
[Unit]
Description=TextRPG Game Server
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/text-rpg
ExecStart=/usr/bin/java -jar /opt/text-rpg/backend/target/text-rpg-1.0.0.jar --spring.config.location=/opt/text-rpg/application.yml
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
systemctl daemon-reload
systemctl enable textrpg
systemctl start textrpg
```

### 7. 配置 Nginx

在宝塔面板中添加站点，配置反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    root /opt/text-rpg/frontend/dist;
    index index.html;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|ico|svg|woff2?)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 8. 验证

```bash
# 检查服务状态
systemctl status textrpg

# 测试 API
curl http://localhost:8080/api/announcement/list

# 测试前端
curl -I http://your-domain.com
```

---

## 三、日常更新

```bash
cd /opt/text-rpg
git pull

# 重新构建后端
cd backend && mvn clean package -DskipTests && cd ..

# 重新构建前端
cd frontend && npm run build && cd ..

# 重启服务
systemctl restart textrpg
nginx -s reload
```

---

## 四、常用命令

```bash
# 服务管理
systemctl start textrpg      # 启动
systemctl stop textrpg       # 停止
systemctl restart textrpg    # 重启
systemctl status textrpg     # 状态

# 查看日志
journalctl -u textrpg -f                    # 实时日志
journalctl -u textrpg -n 100 --no-pager     # 最近 100 行

# 数据库操作
mysql -u root -p text_rpg                   # 连接数据库

# Nginx
nginx -t                     # 测试配置
nginx -s reload              # 重载配置
```

---

## 五、故障排查

| 问题 | 排查方式 |
|------|---------|
| 后端启动失败 | `journalctl -u textrpg -n 50` 查看错误日志 |
| 数据库连接失败 | 检查 MySQL 服务状态和密码配置 |
| 前端白屏 | 检查 Nginx 配置和前端构建产物 |
| API 404 | 检查 Nginx 反向代理配置 |
| 邮件发送失败 | 检查 SMTP 授权码配置 |
| 端口被占用 | `lsof -i:8080` 查看占用进程 |

---

## 六、安全建议

1. **修改默认密码**：首次部署后立即修改管理员密码
2. **配置 HTTPS**：使用宝塔面板申请 Let's Encrypt 免费证书
3. **限制数据库访问**：不要使用 root 用户，创建专用数据库用户
4. **定期备份**：备份数据库和上传目录
5. **防火墙**：只开放 80、443 端口
