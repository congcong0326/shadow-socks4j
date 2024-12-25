
## 特性
这是一个基于 Netty 开发的 Shadow Socks 服务端，具备以下特性：

1. 支持 AES_128_GCM、AES_256_GCM 和 CHACHA20_POLY1305 加密算法。
2. 内置仪表盘，可以查看服务的基本状态及流量使用情况。
3. 能防止重放攻击并具有检测功能。

## 如何使用

我的服务器配置为 1 核 CPU 和 500MB 内存，由于内存较小，选择了 CentOS 7。无论选择哪个操作系统，建议将 TCP 拥塞控制算法改为 BBR，这能在弱网环境下提供更好的网络传输速率。

### 安装 JDK 环境
推荐安装 JDK 11，因为它原生支持 CHACHA20 解密算法。
```bash
sudo yum update
sudo yum install java-11-openjdk-devel
export PATH=$PATH:$JAVA_HOME/bin
source ~/.bash_profile
```

### 下载 IP 库
下载一个简单的 IP 数据库，用于分析访问 IP 的地理位置。如果系统未安装 `wget`，需要先安装。
```bash
sudo yum install wget
wget https://github.com/P3TERX/GeoLite.mmdb/raw/download/GeoLite2-City.mmdb
```

### 设置服务器时间
正确设置服务器时间有助于仪表盘的显示。CentOS 默认使用 `chronyd` 作为 NTP 服务。
```bash
# 启动 NTP 服务
sudo systemctl start chronyd

# 设置时区为上海
sudo timedatectl set-timezone Asia/Shanghai
```

### 编写配置文件
程序使用 YAML 文件作为配置。以下是一个示例配置：
```yaml
services:
  - name: shadowSocksChaCha  # 服务名称
    port: 36198              # 监听端口
    type: SHADOW_SOCKS      # 代理服务器类型，可选值：HTTP、SOCKS5、SHADOW_SOCKS
    enable: true            # 是否启用服务
    supportDecryption: true # 是否启用解密
    decryptionAlgorithm: CHACHA20_POLY1305 # 加密算法（支持 AES_256_GCM、AES_128_GCM、CHACHA20_POLY1305）
    decryptionKey: xxxxxx     # 解密密钥
    credentials:
      - username: user_1  # 用户名，仪表盘流量统计将使用此用户名作为唯一标识

  - name: shadowSocksChaCha
    port: 36170
    type: SHADOW_SOCKS
    enable: true
    supportDecryption: true
    decryptionAlgorithm: CHACHA20_POLY1305
    decryptionKey: xxxxx
    credentials:
      - username: user_2

  - name: admin-ui  # 仪表盘，默认监控 127.0.0.1:36199
    port: 36199
    type: ADMIN
    enable: true
```

### 启动程序
启动程序时，按照需要分配内存，或者按照以下示例分配。以下是启动命令：
```bash
nohup java -Xms64m -Xmx128m -XX:MaxDirectMemorySize=128m  --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true -jar proxy-1.0-SNAPSHOT.jar > /dev/null 2>&1 &
```

## 防止检测原理

针对重放攻击，我们可以通过一些特征表现出与普通代理服务器的不同之处，从而避免被检测。研究表明，最理想的应对方式是采用 “read forever” 策略，与互联网上多数服务器一样，即不断读取数据直到连接超时，这可以有效防止重放攻击的检测。

--- 
