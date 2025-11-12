# 数据库版本秒杀系统测试指南

## 概述

这是不使用 Redis 的数据库版本秒杀实现，用于与 Redis 版本进行性能对比测试。

## 实现方案

- **乐观锁**：使用版本号（version）字段实现乐观锁，避免悲观锁的性能问题
- **数据库事务**：使用 `@Transactional` 保证操作的原子性
- **多次数据库交互**：需要多次查询和更新操作

## 数据库表结构

执行 `src/main/resources/db/schema.sql` 创建以下表：

1. `sku_stock` - SKU库存表（带版本号）
2. `user_sku_order` - 用户SKU购买记录表
3. `user_act_order` - 用户活动购买记录表
4. `seckill_order` - 秒杀订单表

## 配置步骤

### 1. 创建数据库

```sql
CREATE DATABASE seckill CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行建表SQL

```bash
mysql -u root -p seckill < src/main/resources/db/schema.sql
```

### 3. 配置数据库连接

编辑 `src/main/resources/application.yml`，取消注释数据源配置：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root

  # MyBatis Plus 配置
  mybatis-plus:
    mapper-locations: classpath*:/mapper/**/*.xml
    type-aliases-package: com.seckill.entity
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    global-config:
      db-config:
        id-type: ASSIGN_ID
        logic-delete-field: deleted
        logic-delete-value: 1
        logic-not-delete-value: 0
        insert-strategy: NOT_NULL
        update-strategy: NOT_NULL
```

### 4. 修改 Application.java

取消排除数据源自动配置：

```java
@SpringBootApplication
// 移除 DataSourceAutoConfiguration.class 的排除
public class Application {
    // ...
}
```

## API 接口

### 添加库存（数据库版本）

```
GET /seckill-db/skuadd?actid=2025&skuid=sku001&amount=1000
```

### 秒杀接口（数据库版本）

```
GET /seckill-db/skusecond?actid=2025&userid=user001&buynum=1&skuid=sku001&perskulim=5&peractlim=10
```

## 性能对比测试

### Redis 版本接口
- 添加库存：`/seckill/skuadd`
- 秒杀：`/seckill/skusecond`

### 数据库版本接口
- 添加库存：`/seckill-db/skuadd`
- 秒杀：`/seckill-db/skusecond`

### 使用 JMeter 或 Apache Bench 进行压测

```bash
# 使用 ab 工具测试（需要先安装）
ab -n 10000 -c 100 http://localhost:8080/seckill-db/skusecond?actid=2025&userid=user001&buynum=1&skuid=sku001&perskulim=5&peractlim=10
```

## 预期性能差异

| 指标 | Redis 版本 | 数据库版本 | 差距 |
|------|-----------|-----------|------|
| QPS | 10万+ | 1000-3000 | 30-100倍 |
| 延迟 | < 1ms | 10-50ms | 10-50倍 |
| 原子性 | ✅ 完美 | ⚠️ 需重试 | - |

## 注意事项

1. **版本冲突**：高并发下乐观锁可能出现大量版本冲突，导致秒杀失败率较高
2. **数据库连接池**：需要配置足够的数据库连接池大小
3. **事务超时**：高并发下可能出现事务超时，需要调整事务超时时间
4. **数据库性能**：确保数据库有足够的性能（索引、硬件等）

## 测试建议

1. 先测试 Redis 版本的性能作为基准
2. 再测试数据库版本的性能
3. 对比两者的 QPS、延迟、成功率等指标
4. 观察数据库的 CPU、IO、连接数等资源使用情况

