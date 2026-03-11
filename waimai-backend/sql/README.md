# 数据库 SQL 脚本说明

## 文件说明

- `schema.sql` - 数据库建表脚本，包含所有表结构和索引
- `init_data.sql` - 初始数据脚本，包含默认分类、默认管理员等

## 使用步骤

### 1. 执行建表脚本

```bash
mysql -u root -p < schema.sql
```

或者在 MySQL 客户端中执行：

```sql
source schema.sql;
```

### 2. 执行初始数据脚本

**重要**：在执行 `init_data.sql` 之前，需要先设置默认管理员的密码。

#### 方法一：使用 Java 代码生成密码

1. 运行以下 Java 代码生成密码 hash 和 salt：

```java
import com.school.waimai.common.util.PasswordUtil;

public class GeneratePassword {
    public static void main(String[] args) {
        String password = "admin123";  // 默认密码
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        System.out.println("Salt: " + salt);
        System.out.println("Password Hash: " + hash);
    }
}
```

2. 将生成的 `salt` 和 `hash` 替换到 `init_data.sql` 中的对应位置：

```sql
INSERT INTO `admin_user` (`username`, `password_hash`, `salt`, `status`) VALUES
('admin', '生成的hash值', '生成的salt值', 1);
```

#### 方法二：手动执行 SQL

```sql
-- 1. 先插入管理员（使用临时密码）
INSERT INTO `admin_user` (`username`, `password_hash`, `salt`, `status`) VALUES
('admin', '临时hash', '临时salt', 1);

-- 2. 启动应用后，通过注册接口创建管理员账号
-- 或者使用应用提供的密码重置功能
```

### 3. 执行初始数据

```bash
mysql -u root -p waimai < init_data.sql
```

## 数据库配置

在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/waimai?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

## 表结构说明

### 核心表

1. **user** - 小程序用户表
2. **admin_user** - 管理员表
3. **shop_category** - 商家分类表
4. **shop** - 商家表
5. **shop_platform_link** - 商家平台链接表
6. **shop_rating** - 商家评分表
7. **shop_favorite** - 商家收藏表
8. **daily_recommend_config** - 每日推荐配置表
9. **operation_log** - 操作日志表

### 索引说明

- 所有表都有主键索引
- 外键关联字段建立了索引
- 查询频繁的字段组合建立了联合索引
- 唯一性约束通过唯一索引实现

## 注意事项

1. **字符集**：所有表使用 `utf8mb4` 字符集，支持 emoji 等特殊字符
2. **逻辑删除**：所有业务表都有 `deleted` 字段，使用逻辑删除而非物理删除
3. **时间字段**：`create_time` 和 `update_time` 使用 MySQL 的 `CURRENT_TIMESTAMP` 自动维护
4. **默认值**：所有字段都有合理的默认值
5. **索引优化**：根据查询场景建立了合适的索引，避免过度索引

## 平台类型枚举

在 `shop_platform_link` 表中：

- `platform_type`: 1=美团，2=饿了么，3=京东，4=小程序自营，9=其他
- `link_type`: 1=H5，2=外部小程序，3=内部小程序path

## 推荐模式枚举

在 `daily_recommend_config` 表中：

- `mode`: 1=手动，2=随机

## 数据备份建议

定期备份数据库：

```bash
mysqldump -u root -p waimai > waimai_backup_$(date +%Y%m%d).sql
```

## 常见问题

### Q: 如何重置管理员密码？

A: 可以通过以下方式：

1. 使用密码重置接口（需要其他管理员权限）
2. 直接更新数据库（需要知道 salt 和 hash 生成方式）
3. 删除旧账号，通过注册接口创建新账号

### Q: 如何清空测试数据？

A: 执行以下 SQL（谨慎操作）：

```sql
-- 清空业务数据（保留分类和管理员）
DELETE FROM shop_rating;
DELETE FROM shop_favorite;
DELETE FROM shop_platform_link;
DELETE FROM shop;
DELETE FROM daily_recommend_config;
DELETE FROM operation_log;
```

### Q: 如何查看表结构？

A: 使用以下命令：

```sql
DESC table_name;
-- 或
SHOW CREATE TABLE table_name;
```
