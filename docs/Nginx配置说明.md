# Nginx 配置说明

## Nginx 在本项目中的作用

Nginx 在这个校园外卖聚合平台项目中扮演着**关键的基础设施角色**，主要有以下几个重要用途：

### 1. **反向代理后端服务** ⭐ 核心功能

**作用**：将外部请求转发到 Spring Boot 后端服务（运行在 8080 端口）

**为什么需要**：

- 隐藏后端服务的真实端口和地址
- 统一入口，便于管理和维护
- 可以配置负载均衡（如果有多实例）
- 方便后续扩展和迁移

**配置示例**：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

### 2. **托管后台管理前端静态资源** ⭐ 核心功能

**作用**：提供后台管理系统的前端页面（Vue3 构建后的静态文件）

**为什么需要**：

- Vue3 项目构建后是纯静态文件（HTML、CSS、JS）
- 需要 Web 服务器来提供这些静态资源
- Nginx 性能优秀，适合静态资源服务

**配置示例**：

```nginx
location /admin/ {
    alias /var/www/waimai-admin/dist/;
    try_files $uri $uri/ /admin/index.html;
    index index.html;
}
```

### 3. **提供 HTTPS 支持** ⭐ 必需功能

**作用**：为小程序提供 HTTPS 访问

**为什么需要**：

- **微信小程序强制要求**：服务器域名必须使用 HTTPS
- 保护数据传输安全
- 提升用户信任度

**配置示例**：

```nginx
server {
    listen 443 ssl;
    server_name api.yourdomain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # ... 其他配置
}
```

### 4. **性能优化**

**作用**：提升系统性能和用户体验

**具体功能**：

- **静态资源缓存**：缓存 CSS、JS、图片等，减少服务器压力
- **Gzip 压缩**：压缩响应内容，减少传输时间
- **连接复用**：提高并发处理能力

**配置示例**：

```nginx
# Gzip 压缩
gzip on;
gzip_types text/plain text/css application/json application/javascript;

# 静态资源缓存
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 30d;
    add_header Cache-Control "public, immutable";
}
```

### 5. **安全防护**

**作用**：增强系统安全性

**具体功能**：

- **隐藏后端信息**：不暴露 Spring Boot 版本等敏感信息
- **请求限制**：防止恶意请求和 DDoS 攻击
- **访问控制**：可以配置 IP 白名单等

**配置示例**：

```nginx
# 限制请求频率
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;

location /api/ {
    limit_req zone=api_limit burst=20;
    # ...
}
```

### 6. **统一域名管理**

**作用**：通过不同路径或子域名访问不同服务

**典型配置**：

- `https://api.yourdomain.com/api/*` → 后端 API
- `https://admin.yourdomain.com/*` → 后台管理前端
- 或者 `https://yourdomain.com/api/*` 和 `https://yourdomain.com/admin/*`

---

## 典型部署架构

```
                    ┌─────────────┐
                    │   Nginx      │  ← 80/443 端口，对外提供服务
                    │  (反向代理)  │
                    └──────┬───────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
    ┌───────▼──────┐ ┌─────▼─────┐ ┌─────▼─────┐
    │ Spring Boot  │ │ 静态资源   │ │  其他服务  │
    │   :8080      │ │ (admin)    │ │           │
    └──────────────┘ └────────────┘ └───────────┘
```

---

## 完整 Nginx 配置示例

创建一个完整的 Nginx 配置文件供参考：

```nginx
# 上游服务器配置（后端服务）
upstream waimai_backend {
    server 127.0.0.1:8080;
    # 如果有多个实例，可以配置负载均衡
    # server 127.0.0.1:8081;
}

# HTTP 服务器（重定向到 HTTPS）
server {
    listen 80;
    server_name api.yourdomain.com admin.yourdomain.com;

    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

# HTTPS 服务器 - API 服务
server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    # SSL 证书配置
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 日志
    access_log /var/log/nginx/api_access.log;
    error_log /var/log/nginx/api_error.log;

    # API 接口代理
    location /api/ {
        proxy_pass http://waimai_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 健康检查（可选）
    location /health {
        proxy_pass http://waimai_backend/actuator/health;
        access_log off;
    }
}

# HTTPS 服务器 - 后台管理前端
server {
    listen 443 ssl http2;
    server_name admin.yourdomain.com;

    # SSL 证书配置（可以使用同一个证书）
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    # 静态资源根目录
    root /var/www/waimai-admin/dist;
    index index.html;

    # 日志
    access_log /var/log/nginx/admin_access.log;
    error_log /var/log/nginx/admin_error.log;

    # 前端路由支持（Vue Router history 模式）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|woff|woff2|ttf|svg)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # API 代理（如果前端需要跨域访问）
    location /api/ {
        proxy_pass http://waimai_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}

# 全局配置
http {
    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript
               application/json application/javascript application/xml+rss;

    # 请求限制
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;

    # 其他全局配置...
}
```

---

## 部署步骤

### 1. 安装 Nginx

**Ubuntu/Debian**:

```bash
sudo apt update
sudo apt install nginx
```

**CentOS/RHEL**:

```bash
sudo yum install nginx
```

### 2. 配置 SSL 证书

可以使用 Let's Encrypt 免费证书：

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d api.yourdomain.com -d admin.yourdomain.com
```

### 3. 部署后端服务

```bash
# 构建 Spring Boot 应用
cd waimai-backend
mvn clean package

# 运行（可以使用 systemd 管理）
java -jar target/waimai-backend-1.0.0.jar
```

### 4. 部署前端静态资源

```bash
# 构建前端
cd waimai-admin
npm run build

# 复制到 Nginx 目录
sudo cp -r dist/* /var/www/waimai-admin/dist/
```

### 5. 配置 Nginx

```bash
# 编辑配置文件
sudo vim /etc/nginx/sites-available/waimai

# 创建软链接
sudo ln -s /etc/nginx/sites-available/waimai /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重载配置
sudo systemctl reload nginx
```

---

## 总结

**Nginx 在本项目中的核心价值**：

1. ✅ **反向代理**：统一入口，转发请求到后端
2. ✅ **静态资源服务**：托管后台管理前端
3. ✅ **HTTPS 支持**：满足小程序 HTTPS 要求（必需）
4. ✅ **性能优化**：缓存、压缩、连接复用
5. ✅ **安全防护**：隐藏后端信息、请求限制

**对于小程序项目来说，Nginx 是必需的**，因为：

- 微信小程序强制要求 HTTPS
- 需要统一管理 API 和静态资源
- 提供更好的性能和安全性

---

## 注意事项

1. **域名配置**：需要在微信公众平台配置服务器域名（request 合法域名）
2. **SSL 证书**：生产环境必须使用有效的 SSL 证书
3. **防火墙**：确保 80 和 443 端口开放
4. **日志监控**：定期查看 Nginx 日志，排查问题
5. **性能调优**：根据实际访问量调整 worker_processes、worker_connections 等参数
