# SSH 笔记（精炼版）

## 一、SSH 是什么

**SSH（Secure Shell）**：一种远程登录和传输数据的加密协议，用来安全地访问和管理另一台电脑。

| 目的       | 说明                                            |
| ---------- | ----------------------------------------------- |
| 远程登录   | 在本地通过命令行操作远程服务器（Linux/Mac）     |
| 远程执行   | 在服务器上执行命令，如 `ssh user@host "ls -la"` |
| 安全传文件 | 用 `scp` / `sftp` 加密传输文件                  |

---

## 二、什么时候会用到

| 场景                | 说明                                        |
| ------------------- | ------------------------------------------- |
| 购买云服务器后      | SSH 登录 Linux，部署网站、配置 Nginx        |
| 公司有 Linux 服务器 | 运维/开发远程连接查日志、改配置、重启服务   |
| 部署/上线项目       | `scp` 传 Jar 包到服务器，`ssh` 执行启动命令 |
| Git 私有仓库        | 用 SSH 密钥替代账号密码认证                 |

---

## 三、常用命令

```bash
# 登录服务器（默认 22 端口）
ssh 用户名@IP地址
ssh root@192.168.1.100

# 指定端口
ssh -p 2222 user@host

# 密钥登录（免密）
ssh -i ~/.ssh/id_rsa user@host

# 传文件到服务器
scp ./app.jar root@120.78.xxx.xxx:/home/app/

# 远程执行一条命令（不进入交互）
ssh root@120.78.xxx.xxx "systemctl restart nginx"
```

---

## 四、实战例子：云服务器部署网站

1. 购买云服务器（腾讯云/阿里云），选 Linux 系统，获取公网 IP：`120.78.xxx.xxx`

2. SSH 登录：

   ```bash
   ssh root@120.78.xxx.xxx
   ```

   输入密码后进入服务器命令行。

3. 在服务器上执行操作：

   ```bash
   apt update
   apt install -y docker.io
   docker run -d -p 80:80 nginx
   ```

4. 本地传文件到服务器：

   ```bash
   scp ./waimai-backend.jar root@120.78.xxx.xxx:/home/app/
   ```

5. 远程执行重启：
   ```bash
   ssh root@120.78.xxx.xxx "systemctl restart nginx"
   ```

---

## 五、一句话总结

> SSH = 远程操作另一台机器的加密通道，典型场景是登录云服务器做部署、运维和配置。
