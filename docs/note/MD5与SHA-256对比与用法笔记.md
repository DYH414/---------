# MD5 与 SHA-256 对比与用法笔记

## 1. 一句话结论

- **MD5**：快、短（128-bit），但**已不安全**（可实用碰撞）。只适合**非安全场景**的快速校验/分桶/去重等。
- **SHA-256**：更长（256-bit）、更抗碰撞，仍属于通用密码学哈希函数；适合**完整性校验、签名/摘要、HMAC**等安全场景的“基础积木”。
- **重要**：**不要直接用 MD5 或 SHA-256 存密码**。密码应使用 **bcrypt / scrypt / Argon2 / PBKDF2** 这类“慢哈希/口令派生函数（KDF）”。

## 2. 它们是什么（哈希 ≠ 加密）

**哈希函数（Hash）**的核心特征：

- **输入任意长度**，输出**固定长度摘要**（digest）
- **单向性**：从摘要反推原文在计算上应极难（原像抗性）
- **抗碰撞**：找到两个不同输入产生相同摘要应极难
- **雪崩效应**：输入改 1 bit，输出应大幅变化

哈希不是加密：**哈希没有“解密”**；加密是可逆的（有密钥、可解密）。

## 3. 输出长度与安全性对比（常用维度）

| 维度                           |                                     MD5 |                                 SHA-256 |
| ------------------------------ | --------------------------------------: | --------------------------------------: |
| 输出长度                       | 128 bit（16 字节，通常 32 个 hex 字符） | 256 bit（32 字节，通常 64 个 hex 字符） |
| 结构                           |                 Merkle–Damgård 迭代结构 |   Merkle–Damgård 迭代结构（SHA-2 家族） |
| 速度                           |                                    很快 |                    快（通常略慢于 MD5） |
| 碰撞安全性                     |        **已被攻破**（存在实用碰撞构造） |    目前未出现实用碰撞（理论生日界更高） |
| 典型用途                       |              非安全校验、去重、快速索引 |    安全摘要、完整性校验、签名摘要、HMAC |
| 是否适合做“安全校验（防篡改）” |            **不适合**（碰撞攻击可绕过） |   更适合（仍需配合密钥/HMAC或签名体系） |

## 4. 原理概要（抓重点）

### 4.1 共通流程（MD5 / SHA-256 都是这一类）

它们都属于“分块迭代式哈希”，整体流程非常相似：

1. **填充（padding）**：在消息末尾追加 `1`、再补 `0`，并附上**原消息长度**（使总长度满足分组要求）
2. **分组**：把消息切成固定大小的块（MD5：512-bit/块；SHA-256：512-bit/块）
3. **初始化向量（IV）**：固定的初始状态（若干个 32-bit word）
4. **压缩函数（compression function）**：对每个分组做多轮混合（非线性函数、按位运算、模加、移位等）
5. **迭代**：把每一块处理后的状态继续喂给下一块
6. **输出摘要**：最终内部状态拼接形成 digest

直觉理解：把长消息“搅拌”成固定长度的“浓缩摘要”。

### 4.2 为什么“碰撞”跟输出长度有关（生日悖论）

若哈希输出是 \(n\) bit，理想情况下：

- 找到碰撞的工作量大约是 \(2^{n/2}\)（生日攻击）
- 因此：
  - MD5：\(n=128\)，生日界约 \(2^{64}\)，再叠加其结构弱点，导致**实用碰撞**出现
  - SHA-256：\(n=256\)，生日界约 \(2^{128}\)，现实可行性远低

## 5. 常见用法与选型建议

### 5.1 文件/数据完整性校验（“传输是否损坏”）

- **优先 SHA-256**（更稳妥）
- MD5 也能“发现随机误码”，但**不能抵抗恶意篡改者**（可构造碰撞让你误判“没变”）

### 5.2 防篡改校验（“有人会故意改数据”）

- 单纯 `SHA-256(data)` 仍然**不够**：攻击者可以改 data 后再重新算 hash
- 正确姿势：
  - **HMAC-SHA256**（有共享密钥，常用于 API 签名）
  - 或用**公私钥签名**（如 RSA/ECDSA/Ed25519），对 `SHA-256` 摘要签名

### 5.3 密码存储（“用户口令怎么存”）

**不要**：

- `MD5(password)` / `SHA-256(password)` / 甚至 `SHA-256(password + salt)` 直接存库（太快，易被 GPU 暴力破解）

应该：

- 使用 **bcrypt / Argon2 / scrypt / PBKDF2**（慢、可调参数、抗并行）
- 每个用户使用**随机盐**（salt），必要时加 **pepper**（服务端私密常量）

如果你出于兼容原因只能用 SHA-256，至少做到：

- 每个用户一个 salt
- 大量迭代（如 PBKDF2-HMAC-SHA256，而不是裸 SHA-256）
- 仍建议尽快迁移到 bcrypt/Argon2

## 6. Java 用法示例（MessageDigest / HMAC）

### 6.1 计算字符串的 MD5 / SHA-256（hex 输出）

```java
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashDemo {
    public static String hexDigest(String algorithm, String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return toHex(digest);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("MD5:      " + hexDigest("MD5", "hello"));
        System.out.println("SHA-256:  " + hexDigest("SHA-256", "hello"));
    }
}
```

### 6.2 HMAC-SHA256（常用于接口签名）

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class HmacDemo {
    public static String hmacSha256Hex(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] out = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return toHex(out);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
```

### 6.3 大文件流式计算（避免一次性读入内存）

```java
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public final class FileHashDemo {
    public static String sha256Hex(Path path) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
        }
        return toHex(md.digest());
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
```

## 7. Windows 常用命令（你当前环境可直接用）

### 7.1 `certutil` 计算文件哈希

```powershell
certutil -hashfile .\yourfile.zip MD5
certutil -hashfile .\yourfile.zip SHA256
```

### 7.2 OpenSSL（如已安装）

```powershell
openssl dgst -md5 .\yourfile.zip
openssl dgst -sha256 .\yourfile.zip
```

## 8. 常见误区速查

- **“我用 SHA-256 做 API 防篡改”**：如果没有密钥，攻击者改了数据也能重算 hash。用 **HMAC-SHA256** 或签名。
- **“MD5 也能校验文件是否被改”**：对随机错误可以；对恶意攻击不行（可利用碰撞绕过）。
- **“加盐后就能用 SHA-256 存密码”**：盐只能防彩虹表复用；**核心问题是太快**，需要慢哈希/KDF。
